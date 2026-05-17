/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package appeng.client.render.tesr;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.storage.cells.CellState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.EnumMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class CellLedRenderer {
    private static final EnumMap<CellState, Vector3f> STATE_COLORS;
    private static final Vector3f UNPOWERED_COLOR;
    private static final Vector3f BLINK_COLOR;
    private static final float L = 0.3125f;
    private static final float R = 0.25f;
    private static final float T = 0.0625f;
    private static final float B = -6.25E-5f;
    private static final float FR = -6.25E-5f;
    private static final float BA = 0.0624375f;
    private static final float[] LED_QUADS;
    public static final RenderType RENDER_LAYER;

    public static void renderLed(IChestOrDrive drive, int slot, VertexConsumer buffer, PoseStack ms, float partialTicks) {
        Vector3f color = CellLedRenderer.getColorForSlot(drive, slot, partialTicks);
        if (color == null) {
            return;
        }
        for (int i = 0; i < LED_QUADS.length; i += 3) {
            float x = LED_QUADS[i];
            float y = LED_QUADS[i + 1];
            float z = LED_QUADS[i + 2];
            buffer.addVertex(ms.last().pose(), x, y, z).setColor(color.x(), color.y(), color.z(), 1.0f);
        }
    }

    private static Vector3f getColorForSlot(IChestOrDrive drive, int slot, float partialTicks) {
        CellState state = drive.getCellStatus(slot);
        if (state == CellState.ABSENT) {
            return null;
        }
        if (!drive.isPowered()) {
            return UNPOWERED_COLOR;
        }
        Vector3f col = STATE_COLORS.get((Object)state);
        if (drive.isCellBlinking(slot)) {
            long t = System.currentTimeMillis() % 200L;
            float f = (float)(t - 100L) / 200.0f + 0.5f;
            f = CellLedRenderer.easeInOutCubic(f);
            col = new Vector3f((Vector3fc)col);
            col.lerp((Vector3fc)BLINK_COLOR, f);
        }
        return col;
    }

    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4.0f * x * x * x : 1.0f - (float)Math.pow(-2.0f * x + 2.0f, 3.0) / 2.0f;
    }

    private CellLedRenderer() {
    }

    static {
        UNPOWERED_COLOR = new Vector3f(0.0f, 0.0f, 0.0f);
        BLINK_COLOR = new Vector3f(1.0f, 0.5f, 0.5f);
        STATE_COLORS = new EnumMap(CellState.class);
        for (CellState cellState : CellState.values()) {
            int color = cellState.getStateColor();
            Vector3f colorVector = new Vector3f((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f);
            STATE_COLORS.put(cellState, colorVector);
        }
        LED_QUADS = new float[]{0.25f, 0.0625f, -6.25E-5f, 0.3125f, 0.0625f, -6.25E-5f, 0.3125f, -6.25E-5f, -6.25E-5f, 0.25f, -6.25E-5f, -6.25E-5f, 0.3125f, 0.0625f, -6.25E-5f, 0.3125f, 0.0625f, 0.0624375f, 0.3125f, -6.25E-5f, 0.0624375f, 0.3125f, -6.25E-5f, -6.25E-5f, 0.25f, 0.0625f, 0.0624375f, 0.25f, 0.0625f, -6.25E-5f, 0.25f, -6.25E-5f, -6.25E-5f, 0.25f, -6.25E-5f, 0.0624375f, 0.25f, 0.0625f, 0.0624375f, 0.3125f, 0.0625f, 0.0624375f, 0.3125f, 0.0625f, -6.25E-5f, 0.25f, 0.0625f, -6.25E-5f, 0.25f, -6.25E-5f, -6.25E-5f, 0.3125f, -6.25E-5f, -6.25E-5f, 0.3125f, -6.25E-5f, 0.0624375f, 0.25f, -6.25E-5f, 0.0624375f};
        RENDER_LAYER = RenderType.create((String)"ae_drive_leds", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)32565, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader)).createCompositeState(false));
    }
}

