/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record StackWithBounds(GenericStack stack, Rect2i bounds) {
    @Nullable
    public static StackWithBounds fromSlot(AEBaseScreen<?> screen, Slot slot) {
        ItemStack item = slot.getItem();
        GenericStack stack = GenericStack.unwrapItemStack(item);
        if (stack != null) {
            return new StackWithBounds(stack, new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16));
        }
        return null;
    }
}

