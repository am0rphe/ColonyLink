/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.debug;

import appeng.core.AEConfig;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoritePlacer;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.debug.MeteoriteSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MeteoritePlacerItem
extends AEBaseItem {
    private static final String MODE_TAG = "mode";

    public MeteoritePlacerItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResultHolder.pass((Object)player.getItemInHand(hand));
        }
        if (InteractionUtil.isInAlternateUseMode(player)) {
            ItemStack itemStack = player.getItemInHand(hand);
            CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)itemStack, tag -> {
                if (tag.contains(MODE_TAG)) {
                    byte mode = tag.getByte(MODE_TAG);
                    tag.putByte(MODE_TAG, (byte)((mode + 1) % CraterType.values().length));
                } else {
                    tag.putByte(MODE_TAG, (byte)CraterType.NORMAL.ordinal());
                }
            });
            CraterType craterType = this.getCraterType(itemStack);
            player.sendSystemMessage((Component)Component.literal((String)craterType.name()));
            return InteractionResultHolder.success((Object)itemStack);
        }
        return super.use(level, player, hand);
    }

    private CraterType getCraterType(ItemStack stack) {
        CustomData customData = (CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return CraterType.values()[tag.getByte(MODE_TAG)];
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }
        ServerPlayer player = (ServerPlayer)context.getPlayer();
        ServerLevel level = (ServerLevel)context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        float coreRadius = level.getRandom().nextFloat() * 6.0f + 2.0f;
        boolean pureCrater = level.getRandom().nextFloat() > 0.5f;
        MeteoriteSpawner spawner = new MeteoriteSpawner();
        CraterType craterType = this.getCraterType(stack);
        PlacedMeteoriteSettings spawned = spawner.trySpawnMeteoriteAtSuitableHeight((LevelReader)level, pos, coreRadius, craterType, pureCrater);
        if (spawned == null) {
            player.sendSystemMessage((Component)Component.literal((String)"Un-suitable Location."));
            return InteractionResult.FAIL;
        }
        int range = (int)Math.ceil((coreRadius * 2.0f + 5.0f) * 5.0f);
        BoundingBox boundingBox = new BoundingBox(pos.getX() - range, pos.getY() - 10, pos.getZ() - range, pos.getX() + range, pos.getY() + 10, pos.getZ() + range);
        MeteoritePlacer.place((LevelAccessor)level, spawned, boundingBox, level.random);
        player.sendSystemMessage((Component)Component.literal((String)("Spawned at y=" + spawned.getPos().getY() + " range=" + range)));
        ChunkPos.rangeClosed((ChunkPos)new ChunkPos(spawned.getPos()), (int)1).forEach(cp -> {
            LevelChunk c = level.getChunk(cp.x, cp.z);
            player.connection.send(Platform.getFullChunkPacket(c));
        });
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (AEConfig.instance().isDebugToolsEnabled()) {
            output.accept((ItemLike)this);
        }
    }
}

