/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package appeng.client.render.overlay;

import appeng.api.util.DimensionalBlockPos;
import appeng.client.render.overlay.IOverlayDataSource;
import appeng.client.render.overlay.OverlayRenderType;
import appeng.client.render.overlay.OverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class OverlayManager {
    private static final OverlayManager INSTANCE = new OverlayManager();
    private final Map<DimensionalBlockPos, OverlayRenderer> overlayHandlers = new HashMap<DimensionalBlockPos, OverlayRenderer>();

    public static OverlayManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        if (this.overlayHandlers.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Vec3 projectedView = minecraft.gameRenderer.getMainCamera().getPosition();
        Quaternionf rotation = new Quaternionf((Quaternionfc)minecraft.gameRenderer.getMainCamera().rotation());
        rotation.invert();
        poseStack.mulPose(rotation);
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        for (OverlayRenderer handler : this.overlayHandlers.entrySet().stream().filter(e -> ((DimensionalBlockPos)e.getKey()).getLevel() == minecraft.level).map(Map.Entry::getValue).toList()) {
            handler.render(poseStack, (MultiBufferSource)buffer);
        }
        poseStack.popPose();
        buffer.endBatch(OverlayRenderType.getBlockHilightLineOccluded());
        buffer.endBatch(OverlayRenderType.getBlockHilightFace());
        buffer.endBatch(OverlayRenderType.getBlockHilightLine());
    }

    public OverlayRenderer showArea(IOverlayDataSource source) {
        Objects.requireNonNull(source);
        OverlayRenderer handler = new OverlayRenderer(source);
        this.overlayHandlers.put(source.getOverlaySourceLocation(), handler);
        return handler;
    }

    public boolean isShowing(IOverlayDataSource source) {
        return this.overlayHandlers.containsKey(source.getOverlaySourceLocation());
    }

    public void removeHandlers(IOverlayDataSource source) {
        this.overlayHandlers.remove(source.getOverlaySourceLocation());
    }
}

