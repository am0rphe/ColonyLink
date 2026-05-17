/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package appeng.mixins;

import appeng.hooks.GuiGraphicsHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiGraphics.class})
public abstract class GuiGraphicsMixin {
    @Inject(method={"Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    protected void renderGuiItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        GuiGraphics self = (GuiGraphics)this;
        if (GuiGraphicsHooks.onRenderGuiItem(self, livingEntity, level, stack, x, y, seed, z)) {
            ci.cancel();
        }
    }
}

