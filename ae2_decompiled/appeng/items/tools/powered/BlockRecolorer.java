/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.EnumHashBiMap
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 */
package appeng.items.tools.powered;

import appeng.api.util.AEColor;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class BlockRecolorer {
    private static final BiMap<AEColor, Block> STAINED_GLASS_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_STAINED_GLASS).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_STAINED_GLASS).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_STAINED_GLASS).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_STAINED_GLASS).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_STAINED_GLASS).put((Object)AEColor.LIME, (Object)Blocks.LIME_STAINED_GLASS).put((Object)AEColor.PINK, (Object)Blocks.PINK_STAINED_GLASS).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_STAINED_GLASS).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_STAINED_GLASS).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_STAINED_GLASS).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_STAINED_GLASS).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_STAINED_GLASS).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_STAINED_GLASS).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_STAINED_GLASS).put((Object)AEColor.RED, (Object)Blocks.RED_STAINED_GLASS).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_STAINED_GLASS).build());
    private static final BiMap<AEColor, Block> STAINED_GLASS_PANE_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_STAINED_GLASS_PANE).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_STAINED_GLASS_PANE).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_STAINED_GLASS_PANE).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_STAINED_GLASS_PANE).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_STAINED_GLASS_PANE).put((Object)AEColor.LIME, (Object)Blocks.LIME_STAINED_GLASS_PANE).put((Object)AEColor.PINK, (Object)Blocks.PINK_STAINED_GLASS_PANE).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_STAINED_GLASS_PANE).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_STAINED_GLASS_PANE).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_STAINED_GLASS_PANE).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_STAINED_GLASS_PANE).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_STAINED_GLASS_PANE).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_STAINED_GLASS_PANE).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_STAINED_GLASS_PANE).put((Object)AEColor.RED, (Object)Blocks.RED_STAINED_GLASS_PANE).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_STAINED_GLASS_PANE).build());
    private static final BiMap<AEColor, Block> WOOL_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_WOOL).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_WOOL).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_WOOL).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_WOOL).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_WOOL).put((Object)AEColor.LIME, (Object)Blocks.LIME_WOOL).put((Object)AEColor.PINK, (Object)Blocks.PINK_WOOL).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_WOOL).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_WOOL).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_WOOL).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_WOOL).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_WOOL).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_WOOL).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_WOOL).put((Object)AEColor.RED, (Object)Blocks.RED_WOOL).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_WOOL).build());
    private static final BiMap<AEColor, Block> BANNER_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_BANNER).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_BANNER).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_BANNER).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_BANNER).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_BANNER).put((Object)AEColor.LIME, (Object)Blocks.LIME_BANNER).put((Object)AEColor.PINK, (Object)Blocks.PINK_BANNER).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_BANNER).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_BANNER).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_BANNER).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_BANNER).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_BANNER).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_BANNER).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_BANNER).put((Object)AEColor.RED, (Object)Blocks.RED_BANNER).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_BANNER).build());
    private static final BiMap<AEColor, Block> WALL_BANNER_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_WALL_BANNER).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_WALL_BANNER).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_WALL_BANNER).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_WALL_BANNER).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_WALL_BANNER).put((Object)AEColor.LIME, (Object)Blocks.LIME_WALL_BANNER).put((Object)AEColor.PINK, (Object)Blocks.PINK_WALL_BANNER).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_WALL_BANNER).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_WALL_BANNER).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_WALL_BANNER).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_WALL_BANNER).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_WALL_BANNER).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_WALL_BANNER).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_WALL_BANNER).put((Object)AEColor.RED, (Object)Blocks.RED_WALL_BANNER).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_WALL_BANNER).build());
    private static final BiMap<AEColor, Block> CARPET_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_CARPET).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_CARPET).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_CARPET).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_CARPET).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_CARPET).put((Object)AEColor.LIME, (Object)Blocks.LIME_CARPET).put((Object)AEColor.PINK, (Object)Blocks.PINK_CARPET).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_CARPET).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_CARPET).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_CARPET).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_CARPET).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_CARPET).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_CARPET).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_CARPET).put((Object)AEColor.RED, (Object)Blocks.RED_CARPET).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_CARPET).build());
    private static final BiMap<AEColor, Block> TERRACOTTA_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_TERRACOTTA).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_TERRACOTTA).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_TERRACOTTA).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_TERRACOTTA).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_TERRACOTTA).put((Object)AEColor.LIME, (Object)Blocks.LIME_TERRACOTTA).put((Object)AEColor.PINK, (Object)Blocks.PINK_TERRACOTTA).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_TERRACOTTA).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_TERRACOTTA).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_TERRACOTTA).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_TERRACOTTA).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_TERRACOTTA).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_TERRACOTTA).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_TERRACOTTA).put((Object)AEColor.RED, (Object)Blocks.RED_TERRACOTTA).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_TERRACOTTA).build());
    private static final BiMap<AEColor, Block> GLAZED_TERRACOTTA_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_GLAZED_TERRACOTTA).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_GLAZED_TERRACOTTA).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_GLAZED_TERRACOTTA).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_GLAZED_TERRACOTTA).put((Object)AEColor.LIME, (Object)Blocks.LIME_GLAZED_TERRACOTTA).put((Object)AEColor.PINK, (Object)Blocks.PINK_GLAZED_TERRACOTTA).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_GLAZED_TERRACOTTA).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_GLAZED_TERRACOTTA).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_GLAZED_TERRACOTTA).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_GLAZED_TERRACOTTA).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_GLAZED_TERRACOTTA).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_GLAZED_TERRACOTTA).put((Object)AEColor.RED, (Object)Blocks.RED_GLAZED_TERRACOTTA).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_GLAZED_TERRACOTTA).build());
    private static final BiMap<AEColor, Block> CONCRETE_BY_COLOR = EnumHashBiMap.create((Map)ImmutableMap.builder().put((Object)AEColor.WHITE, (Object)Blocks.WHITE_CONCRETE).put((Object)AEColor.ORANGE, (Object)Blocks.ORANGE_CONCRETE).put((Object)AEColor.MAGENTA, (Object)Blocks.MAGENTA_CONCRETE).put((Object)AEColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_CONCRETE).put((Object)AEColor.YELLOW, (Object)Blocks.YELLOW_CONCRETE).put((Object)AEColor.LIME, (Object)Blocks.LIME_CONCRETE).put((Object)AEColor.PINK, (Object)Blocks.PINK_CONCRETE).put((Object)AEColor.GRAY, (Object)Blocks.GRAY_CONCRETE).put((Object)AEColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_CONCRETE).put((Object)AEColor.CYAN, (Object)Blocks.CYAN_CONCRETE).put((Object)AEColor.PURPLE, (Object)Blocks.PURPLE_CONCRETE).put((Object)AEColor.BLUE, (Object)Blocks.BLUE_CONCRETE).put((Object)AEColor.BROWN, (Object)Blocks.BROWN_CONCRETE).put((Object)AEColor.GREEN, (Object)Blocks.GREEN_CONCRETE).put((Object)AEColor.RED, (Object)Blocks.RED_CONCRETE).put((Object)AEColor.BLACK, (Object)Blocks.BLACK_CONCRETE).build());
    private static final List<RecolorableBlockGroup> BLOCK_GROUPS = ImmutableList.of((Object)new RecolorableBlockGroup(Blocks.GLASS, STAINED_GLASS_BY_COLOR), (Object)new RecolorableBlockGroup(Blocks.GLASS_PANE, STAINED_GLASS_PANE_BY_COLOR), (Object)new RecolorableBlockGroup(Blocks.WHITE_WOOL, WOOL_BY_COLOR), (Object)new RecolorableBlockGroup(Blocks.WHITE_BANNER, BANNER_BY_COLOR), (Object)new RecolorableBlockGroup(Blocks.WHITE_WALL_BANNER, WALL_BANNER_BY_COLOR), (Object)new RecolorableBlockGroup(Blocks.WHITE_CARPET, CARPET_BY_COLOR), (Object)new RecolorableBlockGroup(Blocks.TERRACOTTA, TERRACOTTA_BY_COLOR), (Object)new RecolorableBlockGroup(null, GLAZED_TERRACOTTA_BY_COLOR), (Object)new RecolorableBlockGroup(null, CONCRETE_BY_COLOR));

    private BlockRecolorer() {
    }

    public static Block recolor(Block block, AEColor newColor) {
        Objects.requireNonNull(block);
        for (RecolorableBlockGroup group : BLOCK_GROUPS) {
            if (group.uncoloredVariant != block && !group.coloredVariants.containsValue((Object)block)) continue;
            Block newBlock = (Block)group.coloredVariants.get((Object)newColor);
            if (newBlock == null) {
                newBlock = group.uncoloredVariant != null ? group.uncoloredVariant : block;
            }
            return newBlock;
        }
        return block;
    }

    private static class RecolorableBlockGroup {
        final Block uncoloredVariant;
        final BiMap<AEColor, Block> coloredVariants;

        public RecolorableBlockGroup(Block uncoloredVariant, BiMap<AEColor, Block> coloredVariants) {
            this.uncoloredVariant = uncoloredVariant;
            this.coloredVariants = coloredVariants;
        }
    }
}

