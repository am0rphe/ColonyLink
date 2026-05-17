/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.itemlists;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.integration.modules.itemlists.DropTarget;
import appeng.menu.AEBaseMenu;
import appeng.menu.slot.FakeSlot;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class DropTargets {
    private DropTargets() {
    }

    public static List<DropTarget> getTargets(AEBaseScreen<?> aeScreen) {
        ArrayList<DropTarget> targets = new ArrayList<DropTarget>();
        for (Slot slot : ((AEBaseMenu)aeScreen.getMenu()).slots) {
            if (!slot.isActive() || !(slot instanceof FakeSlot)) continue;
            FakeSlot fakeSlot = (FakeSlot)slot;
            Rect2i area = new Rect2i(aeScreen.getGuiLeft() + slot.x, aeScreen.getGuiTop() + slot.y, 16, 16);
            targets.add(new FakeSlotDropTarget(area, fakeSlot));
        }
        return targets;
    }

    private record FakeSlotDropTarget(Rect2i area, FakeSlot slot) implements DropTarget
    {
        @Override
        public boolean canDrop(GenericStack stack) {
            return this.slot.canSetFilterTo(FakeSlotDropTarget.wrapFilterAsItem(stack));
        }

        @Override
        public boolean drop(GenericStack stack) {
            ItemStack itemStack = FakeSlotDropTarget.wrapFilterAsItem(stack);
            if (this.slot.canSetFilterTo(itemStack)) {
                this.slot.setFilterTo(itemStack);
                return true;
            }
            return false;
        }

        private static ItemStack wrapFilterAsItem(GenericStack genericStack) {
            AEKey aEKey = genericStack.what();
            if (aEKey instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)aEKey;
                return itemKey.toStack(Ints.saturatedCast((long)Math.max(1L, genericStack.amount())));
            }
            return GenericStack.wrapInItemStack(genericStack.what(), Math.max(1L, genericStack.amount()));
        }
    }
}

