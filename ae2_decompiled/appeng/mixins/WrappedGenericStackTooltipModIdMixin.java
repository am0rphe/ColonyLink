/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.GenericStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Screen.class}, priority=1001)
public class WrappedGenericStackTooltipModIdMixin {
    @Inject(method={"getTooltipFromItem"}, at={@At(value="RETURN")}, cancellable=true)
    private static void getTooltipFromItem(Minecraft client, ItemStack itemStack, CallbackInfoReturnable<List<Component>> cri) {
        GenericStack unwrapped = GenericStack.unwrapItemStack(itemStack);
        if (unwrapped != null) {
            cri.setReturnValue(AEKeyRendering.getTooltip(unwrapped.what()));
        }
    }
}

