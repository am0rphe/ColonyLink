/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 */
package appeng.debug;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class EraserItem
extends AEBaseItem {
    private static final int BOX_SIZE = 48;
    private static final int BLOCK_ERASE_LIMIT = 110592;
    static final Set<Block> COMMON_BLOCKS = new HashSet<Block>();

    public EraserItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Block state = level.getBlockState(pos).getBlock();
        boolean bulk = InteractionUtil.isInAlternateUseMode(player);
        ArrayDeque<BlockPos> next = new ArrayDeque<BlockPos>();
        HashSet<BlockPos> closed = new HashSet<BlockPos>();
        Set<Block> commonBlocks = this.getCommonBlocks();
        next.add(pos);
        int blocks = 0;
        while (blocks < 110592 && next.peek() != null) {
            BlockPos wc = (BlockPos)next.poll();
            Block c_state = level.getBlockState(wc).getBlock();
            boolean contains = state == c_state || bulk && commonBlocks.contains(c_state);
            closed.add(wc);
            if (!contains) continue;
            ++blocks;
            level.setBlock(wc, Blocks.AIR.defaultBlockState(), 2);
            level.destroyBlock(wc, false);
            if (!this.isInsideBox(wc, pos)) continue;
            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    for (int z = -1; z <= 1; ++z) {
                        BlockPos nextPos;
                        if (0 == x && 0 == y && 0 == z || closed.contains(nextPos = wc.offset(x, y, z))) continue;
                        next.add(nextPos);
                    }
                }
            }
        }
        AELog.info("Delete " + blocks + " blocks", new Object[0]);
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    private boolean isInsideBox(BlockPos pos, BlockPos origin) {
        boolean ret = true;
        if (pos.getX() > origin.getX() + 48 || pos.getX() < origin.getX() - 48) {
            ret = false;
        }
        if (pos.getY() > origin.getY() + 48 || pos.getY() < origin.getY() - 48) {
            ret = false;
        }
        if (pos.getZ() > origin.getZ() + 48 || pos.getZ() < origin.getZ() - 48) {
            ret = false;
        }
        return ret;
    }

    private Set<Block> getCommonBlocks() {
        if (COMMON_BLOCKS.isEmpty()) {
            COMMON_BLOCKS.add(Blocks.STONE);
            COMMON_BLOCKS.add(Blocks.DIRT);
            COMMON_BLOCKS.add(Blocks.GRASS_BLOCK);
            COMMON_BLOCKS.add(Blocks.COBBLESTONE);
            COMMON_BLOCKS.add(Blocks.ANDESITE);
            COMMON_BLOCKS.add(Blocks.GRANITE);
            COMMON_BLOCKS.add(Blocks.DIORITE);
            COMMON_BLOCKS.add(Blocks.GRAVEL);
            COMMON_BLOCKS.add(Blocks.SANDSTONE);
            COMMON_BLOCKS.add(Blocks.NETHERRACK);
            COMMON_BLOCKS.add(Blocks.WATER);
            COMMON_BLOCKS.add(Blocks.LAVA);
            COMMON_BLOCKS.addAll(BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.LEAVES).stream().map(Holder::value).toList());
            COMMON_BLOCKS.addAll(BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.SAND).stream().map(Holder::value).toList());
            COMMON_BLOCKS.addAll(BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.LOGS).stream().map(Holder::value).toList());
        }
        return COMMON_BLOCKS;
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (AEConfig.instance().isDebugToolsEnabled()) {
            output.accept((ItemLike)this);
        }
    }
}

