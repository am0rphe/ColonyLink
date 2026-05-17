/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package appeng.mixins;

import appeng.menu.slot.AppEngSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={AbstractContainerScreen.class})
public class AbstractContainerScreenMixin {
    @ModifyVariable(method={"renderSlot"}, index=5, at=@At(value="STORE", ordinal=0))
    protected ItemStack ae2_changeStackForDisplay(ItemStack stack, GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof AppEngSlot) {
            AppEngSlot aeSlot = (AppEngSlot)slot;
            return aeSlot.getDisplayStack();
        }
        return stack;
    }
}

