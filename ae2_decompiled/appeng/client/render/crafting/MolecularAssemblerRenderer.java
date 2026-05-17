/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.client.render.crafting;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AppEng;
import appeng.core.AppEngClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(value=Dist.CLIENT)
public class MolecularAssemblerRenderer
implements BlockEntityRenderer<MolecularAssemblerBlockEntity> {
    public static final ModelResourceLocation LIGHTS_MODEL = ModelResourceLocation.standalone((ResourceLocation)AppEng.makeId("block/molecular_assembler_lights"));
    private final RandomSource particleRandom = RandomSource.create();

    public MolecularAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(MolecularAssemblerBlockEntity molecularAssembler, float partialTicks, PoseStack ms, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        AssemblerAnimationStatus status = molecularAssembler.getAnimationStatus();
        if (status != null) {
            if (!Minecraft.getInstance().isPaused()) {
                if (status.isExpired()) {
                    molecularAssembler.setAnimationStatus(null);
                }
                status.setAccumulatedTicks(status.getAccumulatedTicks() + partialTicks);
                status.setTicksUntilParticles(status.getTicksUntilParticles() - partialTicks);
            }
            this.renderStatus(molecularAssembler, ms, bufferIn, combinedLightIn, status);
        }
        if (molecularAssembler.isPowered()) {
            this.renderPowerLight(ms, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }

    private void renderPowerLight(PoseStack ms, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel lightsModel = minecraft.getModelManager().getModel(LIGHTS_MODEL);
        VertexConsumer buffer = bufferIn.getBuffer(RenderType.tripwire());
        minecraft.getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer, null, lightsModel, 1.0f, 1.0f, 1.0f, combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
    }

    private void renderStatus(MolecularAssemblerBlockEntity molecularAssembler, PoseStack ms, MultiBufferSource bufferIn, int combinedLightIn, AssemblerAnimationStatus status) {
        double centerX = (float)molecularAssembler.getBlockPos().getX() + 0.5f;
        double centerY = (float)molecularAssembler.getBlockPos().getY() + 0.5f;
        double centerZ = (float)molecularAssembler.getBlockPos().getZ() + 0.5f;
        Minecraft minecraft = Minecraft.getInstance();
        if (status.getTicksUntilParticles() <= 0.0f) {
            status.setTicksUntilParticles(4.0f);
            if (AppEngClient.instance().shouldAddParticles(this.particleRandom)) {
                for (int x = 0; x < (int)Math.ceil((double)status.getSpeed() / 5.0); ++x) {
                    minecraft.particleEngine.createParticle((ParticleOptions)ParticleTypes.CRAFTING, centerX, centerY, centerZ, 0.0, 0.0, 0.0);
                }
            }
        }
        ItemStack is = status.getIs();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        if (!(is.getItem() instanceof BlockItem)) {
            ms.translate(0.0f, -0.3f, 0.0f);
        } else {
            ms.translate(0.0f, -0.2f, 0.0f);
        }
        itemRenderer.renderStatic(is, ItemDisplayContext.GROUND, combinedLightIn, OverlayTexture.NO_OVERLAY, ms, bufferIn, molecularAssembler.getLevel(), 0);
        ms.popPose();
    }
}

