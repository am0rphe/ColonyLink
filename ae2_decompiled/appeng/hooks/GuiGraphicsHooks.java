/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.hooks;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.crafting.pattern.EncodedPatternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class GuiGraphicsHooks {
    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal();

    private GuiGraphicsHooks() {
    }

    public static boolean onRenderGuiItem(GuiGraphics guiGraphics, @Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack stack, int x, int y, int seed, int z) {
        GenericStack unwrapped;
        Minecraft minecraft = Minecraft.getInstance();
        Item item = stack.getItem();
        if (item instanceof EncodedPatternItem) {
            ItemStack output;
            EncodedPatternItem encodedPattern = (EncodedPatternItem)item;
            if (OVERRIDING_FOR.get() == stack) {
                return false;
            }
            boolean shiftHeld = Screen.hasShiftDown();
            if (shiftHeld && level != null && !(output = encodedPattern.getOutput(stack)).isEmpty() && output != stack) {
                GuiGraphicsHooks.renderInstead(guiGraphics, livingEntity, level, output, x, y, seed, z);
                return true;
            }
        }
        if ((unwrapped = GenericStack.unwrapItemStack(stack)) != null) {
            AEKeyRendering.drawInGui(minecraft, guiGraphics, x, y, unwrapped.what());
            if (unwrapped.amount() > 0L) {
                String amtText = unwrapped.what().formatAmount(unwrapped.amount(), AmountFormat.SLOT);
                StackSizeRenderer.renderSizeLabel(guiGraphics, minecraft.font, (float)x, (float)y, amtText, false);
            }
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderInstead(GuiGraphics guiGraphics, @Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack stack, int x, int y, int seed, int z) {
        OVERRIDING_FOR.set(stack);
        try {
            guiGraphics.renderItem(livingEntity, level, stack, x, y, seed, z);
        }
        finally {
            OVERRIDING_FOR.remove();
        }
    }
}

