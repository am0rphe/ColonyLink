/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 */
package appeng.worldgen.meteorite;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum CraterType {
    NONE(null),
    NORMAL(Blocks.AIR),
    LAVA(Blocks.LAVA),
    OBSIDIAN(Blocks.OBSIDIAN),
    WATER(Blocks.WATER),
    SNOW(Blocks.SNOW_BLOCK),
    ICE(Blocks.ICE);

    private final Block filler;

    private CraterType(Block filler) {
        this.filler = filler;
    }

    public Block getFiller() {
        return this.filler;
    }
}

