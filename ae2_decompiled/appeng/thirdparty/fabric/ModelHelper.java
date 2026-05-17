/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemTransform
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.core.Direction
 *  net.minecraft.util.Mth
 *  org.jetbrains.annotations.Contract
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.Mesh;
import appeng.thirdparty.fabric.QuadView;
import appeng.thirdparty.fabric.SpriteFinder;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3f;

public abstract class ModelHelper {
    public static final int NULL_FACE_ID = 6;
    private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);
    public static final ItemTransform TRANSFORM_BLOCK_GUI = ModelHelper.makeTransform(30.0f, 225.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.625f, 0.625f, 0.625f);
    public static final ItemTransform TRANSFORM_BLOCK_GROUND = ModelHelper.makeTransform(0.0f, 0.0f, 0.0f, 0.0f, 3.0f, 0.0f, 0.25f, 0.25f, 0.25f);
    public static final ItemTransform TRANSFORM_BLOCK_FIXED = ModelHelper.makeTransform(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f);
    public static final ItemTransform TRANSFORM_BLOCK_3RD_PERSON_RIGHT = ModelHelper.makeTransform(75.0f, 45.0f, 0.0f, 0.0f, 2.5f, 0.0f, 0.375f, 0.375f, 0.375f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_RIGHT = ModelHelper.makeTransform(0.0f, 45.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.4f, 0.4f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_LEFT = ModelHelper.makeTransform(0.0f, 225.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.4f, 0.4f);
    public static final ItemTransforms MODEL_TRANSFORM_BLOCK = new ItemTransforms(TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_1ST_PERSON_LEFT, TRANSFORM_BLOCK_1ST_PERSON_RIGHT, ItemTransform.NO_TRANSFORM, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);

    private ModelHelper() {
    }

    public static int toFaceIndex(Direction face) {
        return face == null ? 6 : face.get3DDataValue();
    }

    @Contract(value="null -> null")
    public static Direction faceFromIndex(int faceIndex) {
        return FACES[faceIndex];
    }

    public static List<BakedQuad>[] toQuadLists(Mesh mesh) {
        SpriteFinder finder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        ImmutableList.Builder[] builders = new ImmutableList.Builder[7];
        for (int i = 0; i < 7; ++i) {
            builders[i] = ImmutableList.builder();
        }
        if (mesh != null) {
            mesh.forEach(q -> {
                Direction cullFace = q.cullFace();
                builders[cullFace == null ? 6 : cullFace.get3DDataValue()].add((Object)q.toBakedQuad(finder.find((QuadView)q)));
            });
        }
        List[] result = new List[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = builders[i].build();
        }
        return result;
    }

    private static ItemTransform makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
        Vector3f translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation = new Vector3f(Mth.clamp((float)translation.x, (float)-5.0f, (float)5.0f), Mth.clamp((float)translation.y, (float)-5.0f, (float)5.0f), Mth.clamp((float)translation.z, (float)-5.0f, (float)5.0f));
        return new ItemTransform(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }
}

