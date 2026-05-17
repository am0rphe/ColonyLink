/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.client.gui.components.toasts.Toast$Visibility
 *  net.minecraft.client.gui.components.toasts.ToastComponent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 */
package appeng.client.gui.me.common;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.core.localization.GuiText;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class FinishedJobToast
implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.parse((String)"toast/recipe");
    private static final long TIME_VISIBLE = 2500L;
    private static final int TITLE_COLOR = -11534256;
    private static final int TEXT_COLOR = -16777216;
    private final AEKey what;
    private final List<FormattedCharSequence> lines;
    private final int height;

    public FinishedJobToast(AEKey what, long amount) {
        this.what = what;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String formattedAmount = what.formatAmount(amount, AmountFormat.SLOT);
        MutableComponent text = GuiText.ToastCraftingJobFinishedText.text(formattedAmount, AEKeyRendering.getDisplayName(what));
        this.lines = font.split((FormattedText)text, this.width() - 30 - 5);
        int n = super.height();
        int n2 = this.lines.size() - 1;
        Objects.requireNonNull(font);
        this.height = n + n2 * 9;
    }

    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 160, 32, 0, 0, 0, 0, this.width(), 8);
        int middleHeight = this.height - 16;
        for (int middleY = 0; middleY < middleHeight; middleY += 16) {
            int tileHeight = Math.min(middleHeight - middleY, 16);
            guiGraphics.blitSprite(BACKGROUND_SPRITE, 160, 32, 0, 8, 0, 8 + middleY, this.width(), tileHeight);
        }
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 160, 32, 0, 24, 0, this.height - 8, this.width(), 8);
        guiGraphics.drawString(toastComponent.getMinecraft().font, (Component)GuiText.ToastCraftingJobFinishedTitle.text(), 30, 7, -11534256, false);
        int lineY = 18;
        for (FormattedCharSequence line : this.lines) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, line, 30, lineY, -16777216, false);
            Objects.requireNonNull(font);
            lineY += 9;
        }
        AEKeyRendering.drawInGui(minecraft, guiGraphics, 8, 8, this.what);
        return timeSinceLastVisible >= 2500L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public int height() {
        return this.height;
    }
}

