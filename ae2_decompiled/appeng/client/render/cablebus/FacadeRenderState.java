/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.client.render.cablebus;

import net.minecraft.world.level.block.state.BlockState;

public class FacadeRenderState {
    private final BlockState sourceBlock;
    private final boolean transparent;

    public FacadeRenderState(BlockState sourceBlock, boolean transparent) {
        this.sourceBlock = sourceBlock;
        this.transparent = transparent;
    }

    public BlockState getSourceBlock() {
        return this.sourceBlock;
    }

    public boolean isTransparent() {
        return this.transparent;
    }
}

