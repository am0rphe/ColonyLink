/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.RecipeCraftingHolder
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingInput$Positioned
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.CommonHooks
 *  net.neoforged.neoforge.event.EventHooks
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public class AppEngCraftingSlot
extends AppEngSlot
implements RecipeCraftingHolder {
    private final InternalInventory craftingGrid;
    private final Player player;
    private int amountCrafted;
    @Nullable
    private RecipeHolder<?> recipeUsed;

    public AppEngCraftingSlot(Player player, InternalInventory craftingGrid) {
        super(new AppEngInternalInventory(1), 0);
        this.player = player;
        this.craftingGrid = craftingGrid;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    protected void onQuickCraft(ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.checkTakeAchievements(stack);
    }

    protected void checkTakeAchievements(ItemStack stack) {
        Container craftContainer = this.craftingGrid.toContainer();
        if (this.amountCrafted > 0) {
            stack.onCraftedBy(this.player.level(), this.player, this.amountCrafted);
            EventHooks.firePlayerCraftingEvent((Player)this.player, (ItemStack)stack, (Container)craftContainer);
        }
        ArrayList ingredients = Lists.newArrayList((Iterable)this.craftingGrid);
        this.awardUsedRecipes(this.player, ingredients);
        this.amountCrafted = 0;
    }

    public void onTake(Player player, ItemStack stack) {
        this.amountCrafted += stack.getCount();
        this.checkTakeAchievements(stack);
        NonNullList items = NonNullList.withSize((int)this.craftingGrid.size(), (Object)ItemStack.EMPTY);
        for (int i = 0; i < this.craftingGrid.size(); ++i) {
            items.set(i, (Object)this.craftingGrid.getStackInSlot(i));
        }
        CraftingInput.Positioned positioned = CraftingInput.ofPositioned((int)3, (int)3, (List)items);
        CommonHooks.setCraftingPlayer((Player)player);
        NonNullList<ItemStack> remainingItems = this.getRemainingItems(positioned.input(), player.level());
        CommonHooks.setCraftingPlayer(null);
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                ItemStack remainingInSlot;
                int slotIdx = y * 3 + x;
                int remainderIdx = (y - positioned.top()) * 3 + (x - positioned.left());
                this.craftingGrid.extractItem(slotIdx, 1, false);
                if (remainderIdx < 0 || remainderIdx >= remainingItems.size() || (remainingInSlot = (ItemStack)remainingItems.get(remainderIdx)).isEmpty()) continue;
                if (this.craftingGrid.getStackInSlot(slotIdx).isEmpty()) {
                    this.craftingGrid.setItemDirect(slotIdx, remainingInSlot);
                    continue;
                }
                if (this.player.getInventory().add(remainingInSlot)) continue;
                this.player.drop(remainingInSlot, false);
            }
        }
    }

    public void setDisplayedCraftingOutput(ItemStack stack) {
        this.getInventory().setItemDirect(0, stack);
    }

    @Override
    public ItemStack remove(int par1) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(par1, this.getItem().getCount());
        }
        return super.remove(par1);
    }

    protected NonNullList<ItemStack> getRemainingItems(CraftingInput ic, Level level) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)ic, level).map(recipe -> ((CraftingRecipe)recipe.value()).getRemainingItems((RecipeInput)ic)).orElse(NonNullList.withSize((int)9, (Object)ItemStack.EMPTY));
    }

    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        this.recipeUsed = recipe;
    }

    @Nullable
    public RecipeHolder<?> getRecipeUsed() {
        return this.recipeUsed;
    }
}

