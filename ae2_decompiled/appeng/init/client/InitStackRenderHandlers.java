/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
 *  net.neoforged.neoforge.fluids.FluidStack
 *  org.joml.Matrix4f
 */
package appeng.init.client;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class InitStackRenderHandlers {
    private InitStackRenderHandlers() {
    }

    public static void init() {
        AEKeyRendering.register(AEKeyType.items(), AEItemKey.class, new ItemKeyRenderHandler());
        AEKeyRendering.register(AEKeyType.fluids(), AEFluidKey.class, new FluidKeyRenderHandler());
    }

    private static class ItemKeyRenderHandler
    implements AEKeyRenderHandler<AEItemKey> {
        private ItemKeyRenderHandler() {
        }

        @Override
        public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AEItemKey stack) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            ItemStack displayStack = stack.getReadOnlyStack();
            guiGraphics.renderItem(displayStack, x, y);
            guiGraphics.renderItemDecorations(minecraft.font, displayStack, x, y, "");
            poseStack.popPose();
        }

        @Override
        public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEItemKey what, float scale, int combinedLight, Level level) {
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.01f);
            poseStack.mulPose(new Matrix4f().scale(scale, scale, 0.001f));
            poseStack.last().normal().rotateX(-0.7853982f);
            Minecraft.getInstance().getItemRenderer().renderStatic(what.getReadOnlyStack(), ItemDisplayContext.GUI, combinedLight, OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
            poseStack.popPose();
        }

        @Override
        public Component getDisplayName(AEItemKey stack) {
            return stack.getDisplayName();
        }

        @Override
        public List<Component> getTooltip(AEItemKey stack) {
            return stack.getReadOnlyStack().getTooltipLines(Item.TooltipContext.of((Level)Minecraft.getInstance().level), (Player)Minecraft.getInstance().player, (TooltipFlag)(Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL));
        }
    }

    private static class FluidKeyRenderHandler
    implements AEKeyRenderHandler<AEFluidKey> {
        private FluidKeyRenderHandler() {
        }

        @Override
        public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AEFluidKey what) {
            FluidBlitter.create(what).dest(x, y, 16, 16).blit(guiGraphics);
        }

        @Override
        public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEFluidKey what, float scale, int combinedLight, Level level) {
            FluidStack fluidStack = what.toStack(1);
            IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of((Fluid)what.getFluid());
            ResourceLocation texture = renderProps.getStillTexture(fluidStack);
            int color = renderProps.getTintColor(fluidStack);
            TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.01f);
            VertexConsumer buffer = buffers.getBuffer(RenderType.solid());
            float x0 = -(scale -= 0.05f) / 2.0f;
            float y0 = scale / 2.0f;
            float x1 = scale / 2.0f;
            float y1 = -scale / 2.0f;
            Matrix4f transform = poseStack.last().pose();
            buffer.addVertex(transform, x0, y1, 0.0f).setColor(color).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0f, 0.0f, 1.0f);
            buffer.addVertex(transform, x1, y1, 0.0f).setColor(color).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0f, 0.0f, 1.0f);
            buffer.addVertex(transform, x1, y0, 0.0f).setColor(color).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0f, 0.0f, 1.0f);
            buffer.addVertex(transform, x0, y0, 0.0f).setColor(color).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0f, 0.0f, 1.0f);
            poseStack.popPose();
        }

        @Override
        public Component getDisplayName(AEFluidKey stack) {
            return stack.getDisplayName();
        }

        @Override
        public List<Component> getTooltip(AEFluidKey stack) {
            ArrayList<Component> tooltip = new ArrayList<Component>();
            tooltip.add(stack.toStack(1).getHoverName());
            String modName = Platform.formatModName(stack.getModId());
            if (!((Component)tooltip.getLast()).getString().equals(modName)) {
                tooltip.add((Component)Component.literal((String)modName));
            }
            return tooltip;
        }
    }
}

