/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.client;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(value=Dist.CLIENT)
public class AEKeyRendering {
    private static volatile Map<AEKeyType, AEKeyRenderHandler<?>> renderers = new IdentityHashMap();

    public static synchronized <T extends AEKey> void register(AEKeyType channel, Class<T> keyClass, AEKeyRenderHandler<T> handler) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(handler, "handler");
        Objects.requireNonNull(keyClass, "keyClass");
        Preconditions.checkArgument((channel.getKeyClass() == keyClass ? 1 : 0) != 0, (String)"%s != %s", channel.getKeyClass(), keyClass);
        IdentityHashMap renderersCopy = new IdentityHashMap(renderers);
        if (renderersCopy.put(channel, handler) != null) {
            throw new IllegalArgumentException("Duplicate registration of render handler for channel " + String.valueOf(channel));
        }
        renderers = renderersCopy;
    }

    @Nullable
    public static AEKeyRenderHandler<?> get(AEKeyType channel) {
        return renderers.get(channel);
    }

    public static AEKeyRenderHandler<?> getOrThrow(AEKeyType channel) {
        AEKeyRenderHandler<?> renderHandler = AEKeyRendering.get(channel);
        if (renderHandler == null) {
            throw new IllegalArgumentException("Missing render handler for channel " + String.valueOf(channel));
        }
        return renderHandler;
    }

    private static AEKeyRenderHandler getUnchecked(AEKey stack) {
        return AEKeyRendering.getOrThrow(stack.getType());
    }

    public static void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, AEKey what) {
        AEKeyRendering.getUnchecked(what).drawInGui(minecraft, guiGraphics, x, y, what);
    }

    public static void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEKey what, float scale, int combinedLightIn, Level level) {
        AEKeyRendering.getUnchecked(what).drawOnBlockFace(poseStack, buffers, what, scale, combinedLightIn, level);
    }

    public static Component getDisplayName(AEKey stack) {
        return AEKeyRendering.getUnchecked(stack).getDisplayName(stack);
    }

    public static List<Component> getTooltip(AEKey stack) {
        return new ArrayList<Component>(AEKeyRendering.getUnchecked(stack).getTooltip(stack));
    }
}

