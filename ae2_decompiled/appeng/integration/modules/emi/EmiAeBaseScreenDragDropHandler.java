/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.EmiDragDropHandler
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.integration.modules.emi;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.itemlists.DropTarget;
import appeng.integration.modules.itemlists.DropTargets;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

class EmiAeBaseScreenDragDropHandler
implements EmiDragDropHandler<Screen> {
    EmiAeBaseScreenDragDropHandler() {
    }

    public boolean dropStack(Screen screen, EmiIngredient emiIngredient, int x, int y) {
        if (!(screen instanceof AEBaseScreen)) {
            return false;
        }
        AEBaseScreen aeScreen = (AEBaseScreen)screen;
        List<DropTarget> targets = DropTargets.getTargets(aeScreen);
        for (DropTarget target : targets) {
            if (!target.area().contains(x, y)) continue;
            for (EmiStack emiStack : emiIngredient.getEmiStacks()) {
                GenericStack filter = EmiStackHelper.toGenericStack(emiStack);
                if (filter == null || !target.drop(filter)) continue;
                return true;
            }
        }
        return false;
    }

    public void render(Screen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
        if (!(screen instanceof AEBaseScreen)) {
            return;
        }
        AEBaseScreen aeScreen = (AEBaseScreen)screen;
        Set potentialStacks = dragged.getEmiStacks().stream().map(EmiStackHelper::toGenericStack).filter(Objects::nonNull).collect(Collectors.toSet());
        List<DropTarget> targets = DropTargets.getTargets(aeScreen);
        for (DropTarget target : targets) {
            if (potentialStacks.stream().noneMatch(target::canDrop)) continue;
            Rect2i area = target.area();
            draw.fill(area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), -2010989773);
        }
    }
}

