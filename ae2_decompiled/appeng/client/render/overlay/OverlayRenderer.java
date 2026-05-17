/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.level.ChunkPos
 *  org.joml.Matrix4f
 */
package appeng.client.render.overlay;

import appeng.client.render.overlay.IOverlayDataSource;
import appeng.client.render.overlay.OverlayRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

public class OverlayRenderer {
    private IOverlayDataSource source;

    OverlayRenderer(IOverlayDataSource source) {
        this.source = source;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer) {
        RenderType typeLinesOccluded = OverlayRenderType.getBlockHilightLineOccluded();
        this.render(poseStack, buffer.getBuffer(typeLinesOccluded), true, 0x30FFFFFF);
        RenderType typeFaces = OverlayRenderType.getBlockHilightFace();
        this.render(poseStack, buffer.getBuffer(typeFaces), false, this.source.getOverlayColor());
        RenderType typeLines = OverlayRenderType.getBlockHilightLine();
        this.render(poseStack, buffer.getBuffer(typeLines), true, this.source.getOverlayColor());
    }

    private void render(PoseStack poseStack, VertexConsumer builder, boolean renderLines, int color) {
        int[] cols = OverlayRenderType.decomposeColor(color);
        for (ChunkPos pos : this.source.getOverlayChunks()) {
            poseStack.pushPose();
            poseStack.translate((float)pos.getMinBlockX(), 0.0f, (float)pos.getMinBlockZ());
            Matrix4f posMat = poseStack.last().pose();
            this.addVertices(builder, posMat, pos, cols, renderLines);
            poseStack.popPose();
        }
    }

    private void addVertices(VertexConsumer wr, Matrix4f posMat, ChunkPos pos, int[] cols, boolean renderLines) {
        boolean noEast;
        Set<ChunkPos> chunks = this.source.getOverlayChunks();
        float x1 = 0.0f;
        float x2 = 16.0f;
        float y1 = this.source.getOverlaySourceLocation().getLevel().getMinBuildHeight();
        float y2 = this.source.getOverlaySourceLocation().getLevel().getMaxBuildHeight();
        float z1 = 0.0f;
        float z2 = 16.0f;
        boolean noNorth = !chunks.contains(new ChunkPos(pos.x, pos.z - 1));
        boolean noSouth = !chunks.contains(new ChunkPos(pos.x, pos.z + 1));
        boolean noWest = !chunks.contains(new ChunkPos(pos.x - 1, pos.z));
        boolean bl = noEast = !chunks.contains(new ChunkPos(pos.x + 1, pos.z));
        if (noNorth) {
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(-1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(-1.0f, 0.0f, 0.0f);
        }
        if (noSouth) {
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(-1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(-1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(1.0f, 0.0f, 0.0f);
        }
        if (noWest) {
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, 1.0f);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, 1.0f);
            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, -1.0f);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, -1.0f);
        }
        if (noEast) {
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, -1.0f);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, -1.0f);
            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, 1.0f);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 0.0f, 1.0f);
        }
        if (renderLines) {
            if (noNorth || noWest) {
                wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 1.0f, 0.0f);
                wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 1.0f, 0.0f);
            }
            if (noNorth || noEast) {
                wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, -1.0f, 0.0f);
                wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, -1.0f, 0.0f);
            }
            if (noSouth || noEast) {
                wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 1.0f, 0.0f);
                wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, 1.0f, 0.0f);
            }
            if (noSouth || noWest) {
                wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, -1.0f, 0.0f);
                wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(0.0f, -1.0f, 0.0f);
            }
        } else {
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(-1.0f, 0.0f, 0.0f);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setNormal(-1.0f, 0.0f, 0.0f);
        }
    }
}

