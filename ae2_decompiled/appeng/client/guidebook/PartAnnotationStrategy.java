/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.color.ColorValue
 *  guideme.color.SymbolicColor
 *  guideme.scene.ImplicitAnnotationStrategy
 *  guideme.scene.annotation.InWorldBoxAnnotation
 *  guideme.scene.annotation.SceneAnnotation
 *  guideme.scene.level.GuidebookLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3f
 */
package appeng.client.guidebook;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.core.localization.GuiText;
import appeng.parts.BusCollisionHelper;
import guideme.color.ColorValue;
import guideme.color.SymbolicColor;
import guideme.scene.ImplicitAnnotationStrategy;
import guideme.scene.annotation.InWorldBoxAnnotation;
import guideme.scene.annotation.SceneAnnotation;
import guideme.scene.level.GuidebookLevel;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class PartAnnotationStrategy
implements ImplicitAnnotationStrategy {
    @Nullable
    public SceneAnnotation getAnnotation(GuidebookLevel level, BlockState blockState, BlockHitResult blockHitResult) {
        BlockPos pos = blockHitResult.getBlockPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IPartHost)) {
            return null;
        }
        IPartHost partHost = (IPartHost)be;
        SelectedPart partResult = partHost.selectPartWorld(blockHitResult.getLocation());
        if (partResult != null) {
            IPart part = partResult.part;
            IFacadePart facade = partResult.facade;
            AABB aabb = null;
            MutableComponent description = Component.empty();
            if (part != null) {
                aabb = this.getAABB(partResult.side, partResult.part::getBoxes);
                description = partResult.part.getPartItem().asItem().getDescription();
            } else if (facade != null) {
                aabb = this.getAABB(partResult.side, bch -> partResult.facade.getBoxes((IPartCollisionHelper)bch, false));
                description = GuiText.Facade.text(partResult.facade.getItem().getDescription());
            }
            if (aabb != null) {
                InWorldBoxAnnotation annotation = new InWorldBoxAnnotation(new Vector3f((float)pos.getX() + (float)aabb.minX, (float)pos.getY() + (float)aabb.minY, (float)pos.getZ() + (float)aabb.minZ), new Vector3f((float)pos.getX() + (float)aabb.maxX, (float)pos.getY() + (float)aabb.maxY, (float)pos.getZ() + (float)aabb.maxZ), (ColorValue)SymbolicColor.IN_WORLD_BLOCK_HIGHLIGHT);
                annotation.setTooltipContent((Component)description);
                return annotation;
            }
        }
        return null;
    }

    @Nullable
    private AABB getAABB(Direction side, Consumer<IPartCollisionHelper> collisionHelper) {
        ArrayList<AABB> boxes = new ArrayList<AABB>();
        BusCollisionHelper bch = new BusCollisionHelper(boxes, side, true);
        collisionHelper.accept(bch);
        if (boxes.isEmpty()) {
            return null;
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;
        for (AABB box : boxes) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}

