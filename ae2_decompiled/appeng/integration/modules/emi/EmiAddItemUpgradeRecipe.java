/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.EmiCraftingRecipe
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package appeng.integration.modules.emi;

import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.AppEng;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class EmiAddItemUpgradeRecipe
extends EmiCraftingRecipe {
    public EmiAddItemUpgradeRecipe(IUpgradeableItem baseItem, Item upgrade) {
        super(List.of(EmiStack.of((ItemLike)baseItem), EmiStack.of((ItemLike)upgrade)), EmiAddItemUpgradeRecipe.makeUpgraded(baseItem, upgrade), EmiAddItemUpgradeRecipe.makeId(baseItem, upgrade), true);
    }

    private static ResourceLocation makeId(IUpgradeableItem baseItem, Item upgrade) {
        ResourceLocation baseItemId = BuiltInRegistries.ITEM.getKey((Object)baseItem.asItem());
        ResourceLocation upgradeId = BuiltInRegistries.ITEM.getKey((Object)upgrade.asItem());
        return AppEng.makeId("/add_item_upgrade/" + baseItemId.getPath() + "/" + upgradeId.getPath());
    }

    private static EmiStack makeUpgraded(IUpgradeableItem baseItem, Item upgrade) {
        ItemStack upgraded = new ItemStack((ItemLike)baseItem);
        baseItem.getUpgrades(upgraded).addItems(new ItemStack((ItemLike)upgrade));
        return EmiStack.of((ItemStack)upgraded);
    }
}

