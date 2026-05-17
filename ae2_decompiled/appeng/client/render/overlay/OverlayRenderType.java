/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$LineStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TransparencyStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeRenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 */
package appeng.client.render.overlay;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class OverlayRenderType
extends RenderType {
    private static RenderType BLOCK_HIGHLIGHT_FACE;
    private static RenderType BLOCK_HIGHLIGHT_LINE;
    private static RenderType BLOCK_HIGHLIGHT_LINE_OCCLUDED;
    private static final RenderStateShard.LineStateShard LINE_3;

    public OverlayRenderType(String nameIn, VertexFormat formatIn, VertexFormat.Mode mode, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, mode, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getBlockHilightFace() {
        if (BLOCK_HIGHLIGHT_FACE == null) {
            BLOCK_HIGHLIGHT_FACE = OverlayRenderType.create((String)"block_hilight", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)65536, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.TransparencyStateShard.CRUMBLING_TRANSPARENCY).setTextureState(NO_TEXTURE).setLightmapState(NO_LIGHTMAP).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setShaderState(RenderStateShard.POSITION_COLOR_SHADER).createCompositeState(false));
        }
        return BLOCK_HIGHLIGHT_FACE;
    }

    public static RenderType getBlockHilightLine() {
        if (BLOCK_HIGHLIGHT_LINE == null) {
            BLOCK_HIGHLIGHT_LINE = OverlayRenderType.makeLineRenderType("block_hilight_line", false);
        }
        return BLOCK_HIGHLIGHT_LINE;
    }

    public static RenderType getBlockHilightLineOccluded() {
        if (BLOCK_HIGHLIGHT_LINE_OCCLUDED == null) {
            BLOCK_HIGHLIGHT_LINE_OCCLUDED = OverlayRenderType.makeLineRenderType("block_hilight_line_occluded", true);
        }
        return BLOCK_HIGHLIGHT_LINE_OCCLUDED;
    }

    private static RenderType.CompositeRenderType makeLineRenderType(String name, boolean occluded) {
        return OverlayRenderType.create((String)name, (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.LINES, (int)65536, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setLineState(LINE_3).setTransparencyState(occluded ? TRANSLUCENT_TRANSPARENCY : ADDITIVE_TRANSPARENCY).setTextureState(NO_TEXTURE).setDepthTestState(occluded ? GREATER_DEPTH_TEST : LEQUAL_DEPTH_TEST).setCullState(NO_CULL).setLightmapState(NO_LIGHTMAP).setWriteMaskState(occluded ? COLOR_WRITE : COLOR_DEPTH_WRITE).setShaderState(RENDERTYPE_LINES_SHADER).createCompositeState(false));
    }

    public static int[] decomposeColor(int color) {
        int[] res = new int[]{color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF};
        return res;
    }

    static {
        LINE_3 = new RenderStateShard.LineStateShard(OptionalDouble.of(3.0));
    }
}

