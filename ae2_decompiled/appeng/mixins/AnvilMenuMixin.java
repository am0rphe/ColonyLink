/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AnvilMenu
 *  net.minecraft.world.inventory.ItemCombinerMenu
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package appeng.mixins;

import appeng.core.definitions.AEParts;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={AnvilMenu.class})
public abstract class AnvilMenuMixin
extends ItemCombinerMenu {
    public AnvilMenuMixin(int containerId, Inventory playerInventory) {
        super(null, 0, null, null);
        throw new AssertionError();
    }

    @ModifyExpressionValue(method={"createResult"}, at={@At(value="INVOKE", target="net/minecraft/world/item/ItemStack.isDamageableItem()Z", ordinal=1)})
    public boolean setAnnihilationPlaneThreadLocal(boolean isDamageable) {
        if (AEParts.ANNIHILATION_PLANE.is(this.inputSlots.getItem(0)) && AEParts.ANNIHILATION_PLANE.is(this.inputSlots.getItem(1))) {
            return true;
        }
        return isDamageable;
    }
}

