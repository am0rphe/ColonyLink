/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 */
package appeng.client.gui.widgets;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.gui.Font;

public class ConfirmableTextField
extends AETextField {
    private Runnable onConfirm;

    public ConfirmableTextField(ScreenStyle style, Font fontRenderer, int x, int y, int width, int height) {
        super(style, fontRenderer, x, y, width, height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.canConsumeInput() && (keyCode == 257 || keyCode == 335)) {
            if (this.onConfirm != null) {
                this.onConfirm.run();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }
}

