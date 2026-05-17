/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.math.Transformation
 *  net.minecraft.client.renderer.block.model.Variant
 *  net.minecraft.client.resources.model.BlockModelRotation
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package appeng.hooks;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.BlockModelRotation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class BlockstateDefinitionHook {
    private static final Transformation[] TRANSFORMS = BlockstateDefinitionHook.createTransformations();

    private static Transformation[] createTransformations() {
        Transformation[] result = new Transformation[64];
        for (int xRot = 0; xRot < 360; xRot += 90) {
            for (int yRot = 0; yRot < 360; yRot += 90) {
                result[BlockstateDefinitionHook.indexFromAngles((int)xRot, (int)yRot, (int)0)] = BlockModelRotation.by((int)xRot, (int)yRot).getRotation();
                for (int zRot = 90; zRot < 360; zRot += 90) {
                    int idx = BlockstateDefinitionHook.indexFromAngles(xRot, yRot, zRot);
                    Quaternionf quaternion = new Quaternionf().rotateYXZ((float)(-yRot) * ((float)Math.PI / 180), (float)(-xRot) * ((float)Math.PI / 180), (float)(-zRot) * ((float)Math.PI / 180));
                    Matrix4f rotationMatrix = new Matrix4f().identity().rotate((Quaternionfc)quaternion);
                    result[idx] = new Transformation(rotationMatrix);
                }
            }
        }
        return result;
    }

    private BlockstateDefinitionHook() {
    }

    public static Variant rotateVariant(Variant variant, int xRot, int yRot, int zRot) {
        int idx = BlockstateDefinitionHook.indexFromAngles(xRot, yRot, zRot);
        return new Variant(variant.getModelLocation(), TRANSFORMS[idx], variant.isUvLocked(), variant.getWeight());
    }

    private static int indexFromAngles(int xRot, int yRot, int zRot) {
        Preconditions.checkArgument((xRot >= 0 && xRot < 360 && xRot % 90 == 0 ? 1 : 0) != 0);
        Preconditions.checkArgument((yRot >= 0 && yRot < 360 && yRot % 90 == 0 ? 1 : 0) != 0);
        Preconditions.checkArgument((zRot >= 0 && zRot < 360 && zRot % 90 == 0 ? 1 : 0) != 0);
        return xRot / 90 * 16 + yRot / 90 * 4 + zRot / 90;
    }
}

