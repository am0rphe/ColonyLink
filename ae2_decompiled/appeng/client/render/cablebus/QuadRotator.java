/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.BlockModel
 *  net.minecraft.core.Direction
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package appeng.client.render.cablebus;

import appeng.api.orientation.BlockOrientation;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import java.util.EnumMap;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class QuadRotator
implements RenderContext.QuadTransform {
    public static final RenderContext.QuadTransform NULL_TRANSFORM = quad -> true;
    private static final EnumMap<BlockOrientation, RenderContext.QuadTransform> TRANSFORMS = new EnumMap(BlockOrientation.class);
    private final BlockOrientation rotation;
    private final Quaternionf quaternion;

    private QuadRotator(BlockOrientation rotation) {
        this.rotation = rotation;
        this.quaternion = rotation.getQuaternion();
    }

    public static RenderContext.QuadTransform get(Direction facing, int spin) {
        return QuadRotator.get(BlockOrientation.get(facing, spin));
    }

    public static RenderContext.QuadTransform get(BlockOrientation rotation) {
        if (rotation.isRedundant()) {
            return NULL_TRANSFORM;
        }
        return TRANSFORMS.get((Object)rotation);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Vector3f tmp = new Vector3f();
        for (int i = 0; i < 4; ++i) {
            quad.copyPos(i, tmp);
            tmp.add(-0.5f, -0.5f, -0.5f);
            tmp.rotate((Quaternionfc)this.quaternion);
            tmp.add(0.5f, 0.5f, 0.5f);
            quad.pos(i, tmp);
            if (!quad.hasNormal(i)) continue;
            quad.copyNormal(i, tmp);
            tmp.rotate((Quaternionfc)this.quaternion);
            quad.normal(i, tmp);
        }
        Direction nominalFace = quad.nominalFace();
        Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(this.rotation.rotate(cullFace));
        }
        Direction rotatedNominalFace = this.rotation.rotate(nominalFace);
        quad.nominalFace(rotatedNominalFace);
        int[] data = new int[32];
        quad.toVanilla(data, 0);
        BlockModel.FACE_BAKERY.recalculateWinding(data, rotatedNominalFace);
        quad.fromVanilla(data, 0);
        return true;
    }

    static {
        for (BlockOrientation rotation : BlockOrientation.values()) {
            if (rotation.isRedundant()) {
                TRANSFORMS.put(rotation, NULL_TRANSFORM);
                continue;
            }
            TRANSFORMS.put(rotation, new QuadRotator(rotation));
        }
    }
}

