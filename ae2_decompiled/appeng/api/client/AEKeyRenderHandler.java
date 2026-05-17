/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.api.client;

import appeng.api.stacks.AEKey;
import appeng.util.Platform;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public interface AEKeyRenderHandler<T extends AEKey> {
    public void drawInGui(Minecraft var1, GuiGraphics var2, int var3, int var4, T var5);

    public void drawOnBlockFace(PoseStack var1, MultiBufferSource var2, T var3, float var4, int var5, Level var6);

    public Component getDisplayName(T var1);

    default public List<Component> getTooltip(T stack) {
        return List.of(this.getDisplayName(stack), Component.literal((String)Platform.formatModName(((AEKey)stack).getModId())));
    }
}

