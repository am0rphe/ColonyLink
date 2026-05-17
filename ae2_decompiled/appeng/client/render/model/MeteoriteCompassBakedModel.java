/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.model.BakedModelWrapper
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package appeng.client.render.model;

import appeng.hooks.CompassManager;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class MeteoriteCompassBakedModel
implements IDynamicBakedModel {
    public static final ModelProperty<Float> ROTATION = new ModelProperty();
    private final BakedModel base;
    private final BakedModel pointer;

    public MeteoriteCompassBakedModel(BakedModel base, BakedModel pointer) {
        this.base = base;
        this.pointer = pointer;
    }

    public BakedModel getPointer() {
        return this.pointer;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        Float rotation = Objects.requireNonNullElse((Float)extraData.get(ROTATION), Float.valueOf(0.0f));
        RenderContext.QuadTransform transform = quad -> {
            Quaternionf quaternion = new Quaternionf().rotationY(rotation.floatValue());
            Vector3f pos = new Vector3f();
            for (int i = 0; i < 4; ++i) {
                quad.copyPos(i, pos);
                pos.add(-0.5f, -0.5f, -0.5f);
                pos.rotate((Quaternionfc)quaternion);
                pos.add(0.5f, 0.5f, 0.5f);
                quad.pos(i, pos);
            }
            return true;
        };
        ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>(this.base.getQuads(state, side, rand, extraData, renderType));
        if (side == null && state == null) {
            MutableQuadView quadView = MutableQuadView.getInstance();
            for (BakedQuad bakedQuad : this.pointer.getQuads(state, side, rand, extraData, renderType)) {
                quadView.fromVanilla(bakedQuad, null);
                transform.transform(quadView);
                quads.add(quadView.toBlockBakedQuad());
            }
        }
        return quads;
    }

    public boolean useAmbientOcclusion() {
        return this.base.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return true;
    }

    public boolean usesBlockLight() {
        return false;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.base.getParticleIcon();
    }

    public ItemTransforms getTransforms() {
        return this.base.getTransforms();
    }

    public ItemOverrides getOverrides() {
        return new ItemOverrides(){

            public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                float rotation = level != null && entity != null ? MeteoriteCompassBakedModel.getAnimatedRotation(entity.position(), true, 0.0f) : MeteoriteCompassBakedModel.getAnimatedRotation(null, false, 0.0f);
                return new FixedRotationModel(rotation);
            }
        };
    }

    public static float getAnimatedRotation(@Nullable Vec3 pos, boolean prefetch, float playerRotation) {
        if (pos != null) {
            double dz;
            ChunkPos ourChunkPos = new ChunkPos(BlockPos.containing((Position)pos));
            BlockPos closestMeteorite = CompassManager.INSTANCE.getClosestMeteorite(ourChunkPos, prefetch);
            if (closestMeteorite == null) {
                long timeMillis = System.currentTimeMillis();
                return (float)(timeMillis %= 500L) / 500.0f * (float)Math.PI * 2.0f;
            }
            double dx = pos.x - (double)closestMeteorite.getX();
            double distanceSq = dx * dx + (dz = pos.z - (double)closestMeteorite.getZ()) * dz;
            if (distanceSq > 36.0) {
                int x = closestMeteorite.getX();
                int z = closestMeteorite.getZ();
                return (float)MeteoriteCompassBakedModel.rad(pos.x(), pos.z(), x, z) + playerRotation;
            }
        }
        long timeMillis = System.currentTimeMillis();
        return (float)(timeMillis %= 3000L) / 3000.0f * (float)Math.PI * 2.0f;
    }

    private static double rad(double ax, double az, double bx, double bz) {
        double up = bz - az;
        double side = bx - ax;
        return Math.atan2(-up, side) - 1.5707963267948966;
    }

    class FixedRotationModel
    extends BakedModelWrapper<MeteoriteCompassBakedModel> {
        private final float rotation;

        public FixedRotationModel(float rotation) {
            super((BakedModel)MeteoriteCompassBakedModel.this);
            this.rotation = rotation;
        }

        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            ModelData modelData = ModelData.builder().with(ROTATION, (Object)Float.valueOf(this.rotation)).build();
            return ((MeteoriteCompassBakedModel)this.originalModel).getQuads(state, side, rand, modelData, null);
        }

        public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
            super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
            Vector3f pointerNormal = poseStack.last().transformNormal(0.0f, 0.0f, 1.0f, new Vector3f());
            pointerNormal.y = 0.0f;
            pointerNormal.normalize();
            double d = Mth.atan2((double)pointerNormal.z, (double)pointerNormal.x) - Mth.atan2((double)1.0, (double)0.0);
            if (cameraTransformType == ItemDisplayContext.GUI && Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                LocalPlayer player = Minecraft.getInstance().player;
                float offRads = (float)((double)(player.getYRot() / 180.0f * (float)Math.PI) + Math.PI);
                d += (double)offRads;
            }
            return new FixedRotationModel((float)d + this.rotation);
        }

        public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
            return List.of(this);
        }
    }
}

