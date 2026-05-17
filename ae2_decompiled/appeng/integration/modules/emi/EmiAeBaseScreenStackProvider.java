/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.EmiStackProvider
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.stack.EmiStackInteraction
 *  net.minecraft.client.gui.screens.Screen
 */
package appeng.integration.modules.emi;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.integration.modules.emi.EmiStackHelper;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.client.gui.screens.Screen;

class EmiAeBaseScreenStackProvider
implements EmiStackProvider<Screen> {
    EmiAeBaseScreenStackProvider() {
    }

    public EmiStackInteraction getStackAt(Screen screen, int x, int y) {
        EmiStack emiStack;
        AEBaseScreen aeScreen;
        StackWithBounds stack;
        if (screen instanceof AEBaseScreen && (stack = (aeScreen = (AEBaseScreen)screen).getStackUnderMouse(x, y)) != null && (emiStack = EmiStackHelper.toEmiStack(stack.stack())) != null) {
            return new EmiStackInteraction((EmiIngredient)emiStack, null, false);
        }
        return EmiStackInteraction.EMPTY;
    }
}

