/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.drag.DraggableStack
 *  me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor
 *  me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor$BoundsProvider
 *  me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult
 *  me.shedaniel.rei.api.client.gui.drag.DraggingContext
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.integration.modules.rei;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.integration.modules.itemlists.DropTarget;
import appeng.integration.modules.itemlists.DropTargets;
import appeng.integration.modules.rei.GenericEntryStackHelper;
import java.util.stream.Stream;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

class GhostIngredientHandler
implements DraggableStackVisitor<AEBaseScreen> {
    GhostIngredientHandler() {
    }

    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof AEBaseScreen;
    }

    public Stream<DraggableStackVisitor.BoundsProvider> getDraggableAcceptingBounds(DraggingContext<AEBaseScreen> context, DraggableStack stack) {
        GenericStack genericStack = GenericEntryStackHelper.ingredientToStack(stack.getStack());
        if (genericStack == null) {
            return Stream.of(new DraggableStackVisitor.BoundsProvider[0]);
        }
        return DropTargets.getTargets((AEBaseScreen)context.getScreen()).stream().filter(dropTarget -> dropTarget.canDrop(genericStack)).map(target -> {
            Rect2i area = target.area();
            return DraggableStackVisitor.BoundsProvider.ofRectangle((Rectangle)new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
        });
    }

    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<AEBaseScreen> context, DraggableStack stack) {
        GenericStack genericStack = GenericEntryStackHelper.ingredientToStack(stack.getStack());
        if (genericStack == null) {
            return DraggedAcceptorResult.PASS;
        }
        Point pos = context.getCurrentPosition();
        if (pos == null) {
            return DraggedAcceptorResult.PASS;
        }
        for (DropTarget target : DropTargets.getTargets((AEBaseScreen)context.getScreen())) {
            if (!target.area().contains(pos.x, pos.y) || !target.canDrop(genericStack)) continue;
            target.drop(genericStack);
            return DraggedAcceptorResult.ACCEPTED;
        }
        return DraggedAcceptorResult.PASS;
    }
}

