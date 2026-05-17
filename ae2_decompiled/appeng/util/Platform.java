/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Containers
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.fml.util.thread.SidedThreadGroups
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
 *  net.neoforged.neoforge.fluids.FluidStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util;

import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnit;
import appeng.api.config.SortOrder;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.util.DimensionalBlockPos;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.hooks.ticking.TickHandler;
import appeng.util.BlockUpdate;
import appeng.util.helpers.P2PHelper;
import com.google.common.annotations.VisibleForTesting;
import com.mojang.authlib.GameProfile;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class Platform {
    @VisibleForTesting
    public static ThreadGroup serverThreadGroup = SidedThreadGroups.SERVER;
    private static final P2PHelper P2P_HELPER = new P2PHelper();
    public static final Direction[] DIRECTIONS_WITH_NULL = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
    @Nullable
    private static final Class<?> ponderLevelClass = Platform.findPonderLevelClass("com.simibubi.create.foundation.ponder.PonderWorld");
    public static RecipeManager fallbackClientRecipeManager;
    public static RegistryAccess fallbackClientRegistryAccess;
    private static final UUID DEFAULT_FAKE_PLAYER_UUID;

    public static RegistryAccess getClientRegistryAccess() {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }
        return Objects.requireNonNull(fallbackClientRegistryAccess);
    }

    public static RecipeManager getClientRecipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager();
        }
        return fallbackClientRecipeManager;
    }

    private static Class<?> findPonderLevelClass(String className) {
        if (!Platform.hasClientClasses()) {
            return null;
        }
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException ignored) {
            AELog.warn("Unable to find class %s. Integration with PonderJS disabled.", className);
            return null;
        }
    }

    public static P2PHelper p2p() {
        return P2P_HELPER;
    }

    public static String formatPowerLong(long n, boolean isRate) {
        return Platform.formatPower((double)n / 100.0, isRate);
    }

    public static String formatPower(double p, boolean isRate) {
        PowerUnit displayUnits = AEConfig.instance().getSelectedEnergyUnit();
        p = PowerUnit.AE.convertTo(displayUnits, p);
        String[] preFixes = new String[]{"k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y"};
        String unitName = displayUnits.getSymbolName();
        String level = "";
        for (int offset = 0; p > 1000.0 && offset < preFixes.length; p /= 1000.0, ++offset) {
            level = preFixes[offset];
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(p) + " " + level + unitName + (isRate ? "/t" : "");
    }

    public static String formatTimeMeasurement(long nanos) {
        if (nanos <= 0L) {
            return "0 ns";
        }
        if (nanos < 1000L) {
            return "<1 \u00b5s";
        }
        if (nanos <= 1000000L) {
            long ms = TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS);
            return ms + "\u00b5s";
        }
        long ms = TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
        return ms + "ms";
    }

    public static Direction crossProduct(Direction forward, Direction up) {
        int west_x = forward.getStepY() * up.getStepZ() - forward.getStepZ() * up.getStepY();
        int west_y = forward.getStepZ() * up.getStepX() - forward.getStepX() * up.getStepZ();
        int west_z = forward.getStepX() * up.getStepY() - forward.getStepY() * up.getStepX();
        return switch (west_x + west_y * 2 + west_z * 3) {
            case 1 -> Direction.EAST;
            case -1 -> Direction.WEST;
            case 2 -> Direction.UP;
            case -2 -> Direction.DOWN;
            case 3 -> Direction.SOUTH;
            case -3 -> Direction.NORTH;
            default -> Direction.NORTH;
        };
    }

    public static boolean hasClientClasses() {
        return FMLEnvironment.dist == null || FMLEnvironment.dist.isClient();
    }

    public static boolean isClient() {
        return Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER;
    }

    public static boolean hasPermissions(DimensionalBlockPos dc, Player player) {
        if (!dc.isInWorld((LevelAccessor)player.level())) {
            return false;
        }
        return player.level().mayInteract(player, dc.getPos());
    }

    public static void spawnDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        if (!level.isClientSide()) {
            for (ItemStack i : drops) {
                Containers.dropItemStack((Level)level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (ItemStack)i);
            }
        }
    }

    public static boolean isServer() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }

    public static void assertServerThread() {
        if (Thread.currentThread().getThreadGroup() != serverThreadGroup) {
            throw new UnsupportedOperationException("This code can only be called server-side and this is most likely a bug.");
        }
    }

    public static String formatModName(String modId) {
        return String.valueOf(ChatFormatting.BLUE) + String.valueOf(ChatFormatting.ITALIC) + Platform.getModName(modId);
    }

    @Nullable
    public static String getModName(String modId) {
        return ModList.get().getModContainerById(modId).map(mc -> mc.getModInfo().getDisplayName()).orElse(modId);
    }

    public static Component getFluidDisplayName(Fluid fluid) {
        FluidStack fluidStack = new FluidStack(fluid, 1);
        return fluidStack.getHoverName();
    }

    public static boolean isChargeable(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        }
        Item item = i.getItem();
        if (item instanceof IAEItemPowerStorage) {
            IAEItemPowerStorage powerStorage = (IAEItemPowerStorage)item;
            return powerStorage.getAEMaxPower(i) > 0.0 && powerStorage.getPowerFlow(i) != AccessRestriction.READ;
        }
        return false;
    }

    public static Player getFakePlayer(ServerLevel level, @Nullable UUID playerUuid) {
        Objects.requireNonNull(level);
        if (playerUuid == null) {
            playerUuid = DEFAULT_FAKE_PLAYER_UUID;
        }
        return FakePlayerFactory.get((ServerLevel)level, (GameProfile)new GameProfile(playerUuid, "[AE2]"));
    }

    public static Direction rotateAround(Direction forward, Direction axis) {
        if (forward.getAxis() == axis.getAxis()) {
            return forward;
        }
        Vec3i newForward = forward.getNormal().cross(axis.getNormal());
        return Objects.requireNonNull(Direction.fromDelta((int)newForward.getX(), (int)newForward.getY(), (int)newForward.getZ()));
    }

    public static void configurePlayer(Player player, Direction side, BlockEntity blockEntity) {
        float pitch = 0.0f;
        float yaw = 0.0f;
        switch (side) {
            case DOWN: 
            case UP: {
                pitch = 90.0f;
                break;
            }
            case EAST: {
                yaw = -90.0f;
                break;
            }
            case NORTH: {
                yaw = 180.0f;
                break;
            }
            case SOUTH: {
                yaw = 0.0f;
                break;
            }
            case WEST: {
                yaw = 90.0f;
                break;
            }
        }
        player.moveTo((double)blockEntity.getBlockPos().getX() + 0.5, (double)blockEntity.getBlockPos().getY() + 0.5, (double)blockEntity.getBlockPos().getZ() + 0.5, yaw, pitch);
    }

    public static void notifyBlocksOfNeighbors(Level level, BlockPos pos) {
        if (level != null && !level.isClientSide) {
            TickHandler.instance().addCallable((LevelAccessor)level, new BlockUpdate(pos));
        }
    }

    public static boolean isSortOrderAvailable(SortOrder order) {
        return true;
    }

    @Nullable
    public static BlockEntity getTickingBlockEntity(@Nullable Level level, BlockPos pos) {
        if (!Platform.areBlockEntitiesTicking(level, pos)) {
            return null;
        }
        return level.getBlockEntity(pos);
    }

    public static boolean areBlockEntitiesTicking(@Nullable Level level, BlockPos pos) {
        return Platform.areBlockEntitiesTicking(level, ChunkPos.asLong((BlockPos)pos));
    }

    public static boolean areBlockEntitiesTicking(@Nullable Level level, long chunkPos) {
        ServerLevel serverLevel;
        return level instanceof ServerLevel && (serverLevel = (ServerLevel)level).getChunkSource().isPositionTicking(chunkPos);
    }

    public static Packet<?> getFullChunkPacket(LevelChunk c) {
        return new ClientboundLevelChunkWithLightPacket(c, c.getLevel().getLightEngine(), null, null);
    }

    public static ItemStack getInsertionRemainder(ItemStack original, long inserted) {
        if (inserted >= (long)original.getCount()) {
            return ItemStack.EMPTY;
        }
        return Platform.copyStackWithSize(original, (int)((long)original.getCount() - inserted));
    }

    public static ItemStack copyStackWithSize(ItemStack itemStack, int size) {
        if (size <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = itemStack.copy();
        copy.setCount(size);
        return copy;
    }

    public static void sendImmediateBlockEntityUpdate(Player player, BlockPos pos) {
        if (player instanceof ServerPlayer) {
            Packet packet;
            ServerPlayer serverPlayer = (ServerPlayer)player;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be != null && (packet = be.getUpdatePacket()) != null) {
                serverPlayer.connection.send(packet);
            }
        }
    }

    public static boolean isPonderLevel(Level level) {
        return ponderLevelClass != null && ponderLevelClass.isInstance(level);
    }

    public static boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    public static Holder<Enchantment> getEnchantment(MinecraftServer server, ResourceKey<Enchantment> enchantment) {
        return server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
    }

    public static Holder<Enchantment> getEnchantment(ServerLevel level, ResourceKey<Enchantment> enchantment) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
    }

    static {
        DEFAULT_FAKE_PLAYER_UUID = UUID.fromString("60C173A5-E1E6-4B87-85B1-272CE424521D");
    }
}

