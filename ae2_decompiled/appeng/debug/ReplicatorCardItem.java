/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.debug;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.spatial.ISpatialService;
import appeng.core.AEConfig;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ReplicatorCardItem
extends AEBaseItem {
    public ReplicatorCardItem(Item.Properties properties) {
        super(properties);
    }

    private CompoundTag getTag(ItemStack stack) {
        return ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY)).copyTag();
    }

    private int getReplications(ItemStack stack) {
        return this.getTag(stack).getInt("r");
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
        if (!level.isClientSide()) {
            ItemStack stack = playerIn.getItemInHand(handIn);
            CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)stack, tag -> {
                int replications = tag.contains("r") ? (tag.getInt("r") + 1) % 4 : 0;
                tag.putInt("r", replications);
            });
            int replications = this.getReplications(stack);
            playerIn.sendSystemMessage((Component)Component.literal((String)(replications + 1 + "\u00b3 Replications")));
        }
        return super.use(level, playerIn, handIn);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (InteractionUtil.isInAlternateUseMode(player)) {
            IInWorldGridNodeHost gridHost = GridHelper.getNodeHost(level, pos);
            if (gridHost != null) {
                CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)player.getItemInHand(hand), tag -> {
                    tag.putInt("x", x);
                    tag.putInt("y", y);
                    tag.putInt("z", z);
                    tag.putInt("side", side.ordinal());
                    tag.putString("w", level.dimension().location().toString());
                    tag.putInt("r", 0);
                });
                this.outputMsg((Entity)player, "Set replicator source");
            } else {
                this.outputMsg((Entity)player, "This does not host a grid node");
            }
        } else {
            CompoundTag ish = this.getTag(player.getItemInHand(hand));
            if (!ish.isEmpty()) {
                int src_x = ish.getInt("x");
                int src_y = ish.getInt("y");
                int src_z = ish.getInt("z");
                int src_side = ish.getInt("side");
                String worldId = ish.getString("w");
                ServerLevel src_w = level.getServer().getLevel(ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)ResourceLocation.parse((String)worldId)));
                int replications = ish.getInt("r") + 1;
                IInWorldGridNodeHost gh = GridHelper.getNodeHost((Level)src_w, new BlockPos(src_x, src_y, src_z));
                if (gh != null) {
                    Direction sideOff = Direction.values()[src_side];
                    Direction currentSideOff = side;
                    IGridNode n = gh.getGridNode(sideOff);
                    if (n != null) {
                        IGrid g = n.getGrid();
                        ISpatialService sc = g.getSpatialService();
                        if (sc.isValidRegion()) {
                            BlockPos min = sc.getMin();
                            BlockPos max = sc.getMax();
                            int sc_size_x = max.getX() - min.getX();
                            int sc_size_y = max.getY() - min.getY();
                            int sc_size_z = max.getZ() - min.getZ();
                            int min_x = min.getX();
                            int min_y = min.getY();
                            int min_z = min.getZ();
                            int x_rot = (int)(-Math.signum(Mth.wrapDegrees((float)player.getYRot())));
                            int z_rot = (int)Math.signum(Mth.wrapDegrees((float)(player.getYRot() + 90.0f)));
                            for (int r_x = 0; r_x < replications; ++r_x) {
                                for (int r_y = 0; r_y < replications; ++r_y) {
                                    for (int r_z = 0; r_z < replications; ++r_z) {
                                        int rel_x = min.getX() - src_x + x + r_x * sc_size_x * x_rot;
                                        int rel_y = min.getY() - src_y + y + r_y * sc_size_y;
                                        int rel_z = min.getZ() - src_z + z + r_z * sc_size_z * z_rot;
                                        for (int i = 1; i < sc_size_x; ++i) {
                                            for (int j = 1; j < sc_size_y; ++j) {
                                                for (int k = 1; k < sc_size_z; ++k) {
                                                    BlockEntity ote;
                                                    CompoundTag data;
                                                    BlockEntity newBe;
                                                    BlockPos p = new BlockPos(min_x + i, min_y + j, min_z + k);
                                                    BlockPos d = new BlockPos(i + rel_x, j + rel_y, k + rel_z);
                                                    BlockState state = src_w.getBlockState(p);
                                                    BlockState prev = level.getBlockState(d);
                                                    level.setBlockAndUpdate(d, state);
                                                    if (state.hasBlockEntity() && (newBe = BlockEntity.loadStatic((BlockPos)d, (BlockState)state, (CompoundTag)(data = (ote = src_w.getBlockEntity(p)).saveWithId((HolderLookup.Provider)level.registryAccess())), (HolderLookup.Provider)level.registryAccess())) != null) {
                                                        level.setBlockEntity(newBe);
                                                    }
                                                    level.sendBlockUpdated(d, prev, state, 3);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            this.outputMsg((Entity)player, "requires valid spatial pylon setup.");
                        }
                    } else {
                        this.outputMsg((Entity)player, "No grid node?");
                    }
                } else {
                    this.outputMsg((Entity)player, "Src is no longer a grid block?");
                }
            } else {
                this.outputMsg((Entity)player, "No Source Defined");
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    private void outputMsg(Entity player, String string) {
        player.sendSystemMessage((Component)Component.literal((String)string));
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (AEConfig.instance().isDebugToolsEnabled()) {
            output.accept((ItemLike)this);
        }
    }
}

