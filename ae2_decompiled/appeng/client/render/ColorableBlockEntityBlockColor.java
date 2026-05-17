/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.color.block.BlockColor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.util.AEColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ColorableBlockEntityBlockColor
implements BlockColor {
    public static final ColorableBlockEntityBlockColor INSTANCE = new ColorableBlockEntityBlockColor();

    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        BlockEntity te;
        AEColor color = AEColor.TRANSPARENT;
        if (level != null && pos != null && (te = level.getBlockEntity(pos)) instanceof IColorableBlockEntity) {
            color = ((IColorableBlockEntity)te).getColor();
        }
        return color.getVariantByTintIndex(tintIndex);
    }
}

