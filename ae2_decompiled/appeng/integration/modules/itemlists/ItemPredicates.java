/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.itemlists;

import appeng.api.ids.AEComponents;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import net.minecraft.world.item.ItemStack;

public final class ItemPredicates {
    private ItemPredicates() {
    }

    public static boolean shouldBeHidden(ItemStack stack) {
        if (ItemPredicates.isInternal(stack)) {
            return true;
        }
        if (!AEConfig.instance().isDebugToolsEnabled() && ItemPredicates.isDeveloperTool(stack)) {
            return true;
        }
        if (!AEConfig.instance().isEnableFacadesInRecipeViewer() && ItemPredicates.isFacade(stack)) {
            return true;
        }
        return AEConfig.instance().isDisableColoredCableRecipesInRecipeViewer() && ItemPredicates.isColoredCable(stack);
    }

    private static boolean isInternal(ItemStack stack) {
        return AEItems.WRAPPED_GENERIC_STACK.is(stack) || AEItems.MISSING_CONTENT.is(stack) || ItemPredicates.isBrokenFacade(stack) || AEBlocks.CABLE_BUS.is(stack) || AEBlocks.MATRIX_FRAME.is(stack) || AEBlocks.PAINT.is(stack);
    }

    private static boolean isBrokenFacade(ItemStack stack) {
        return stack.getItem() instanceof FacadeItem && !stack.has(AEComponents.FACADE_ITEM);
    }

    private static boolean isFacade(ItemStack stack) {
        return stack.getItem() instanceof FacadeItem;
    }

    private static boolean isDeveloperTool(ItemStack stack) {
        return AEBlocks.DEBUG_CUBE_GEN.is(stack) || AEBlocks.DEBUG_ENERGY_GEN.is(stack) || AEBlocks.DEBUG_ITEM_GEN.is(stack) || AEBlocks.DEBUG_PHANTOM_NODE.is(stack) || AEItems.DEBUG_CARD.is(stack) || AEItems.DEBUG_ERASER.is(stack) || AEItems.DEBUG_METEORITE_PLACER.is(stack) || AEItems.DEBUG_REPLICATOR_CARD.is(stack);
    }

    private static boolean isColoredCable(ItemStack stack) {
        for (AEColor color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT || stack.getItem() != AEParts.COVERED_CABLE.item(color) && stack.getItem() != AEParts.COVERED_DENSE_CABLE.item(color) && stack.getItem() != AEParts.GLASS_CABLE.item(color) && stack.getItem() != AEParts.SMART_CABLE.item(color) && stack.getItem() != AEParts.SMART_DENSE_CABLE.item(color)) continue;
            return true;
        }
        return false;
    }
}

