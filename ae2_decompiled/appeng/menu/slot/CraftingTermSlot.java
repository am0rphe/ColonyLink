/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package appeng.menu.slot;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.helpers.ICraftingGridMenu;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.slot.AppEngCraftingSlot;
import appeng.util.Platform;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;
import appeng.util.prioritylist.IPartitionList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CraftingTermSlot
extends AppEngCraftingSlot {
    private final InternalInventory craftInv;
    private final InternalInventory pattern;
    private final IActionSource mySrc;
    private final IEnergySource energySrc;
    private final MEStorage storage;
    private final ICraftingGridMenu menu;

    public CraftingTermSlot(Player player, IActionSource mySrc, IEnergySource energySrc, MEStorage storage, InternalInventory cMatrix, InternalInventory secondMatrix, ICraftingGridMenu ccp) {
        super(player, cMatrix);
        this.energySrc = energySrc;
        this.storage = storage;
        this.mySrc = mySrc;
        this.pattern = cMatrix;
        this.craftInv = secondMatrix;
        this.menu = ccp;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack is) {
    }

    public void doClick(InventoryAction action, Player who) {
        int maxTimesToCraft;
        InternalInventory target;
        if (this.getItem().isEmpty()) {
            return;
        }
        if (this.isRemote()) {
            return;
        }
        int howManyPerCraft = this.getItem().getCount();
        if (action == InventoryAction.CRAFT_SHIFT || action == InventoryAction.CRAFT_ALL) {
            target = new PlayerInternalInventory(who.getInventory());
            maxTimesToCraft = action == InventoryAction.CRAFT_SHIFT ? (int)Math.floor((double)this.getItem().getMaxStackSize() / (double)howManyPerCraft) : (int)Math.floor((double)this.getItem().getMaxStackSize() / (double)howManyPerCraft * 36.0);
        } else if (action == InventoryAction.CRAFT_STACK) {
            target = new CarriedItemInventory(this.getMenu());
            maxTimesToCraft = (int)Math.floor((double)this.getItem().getMaxStackSize() / (double)howManyPerCraft);
        } else {
            if (this.getMenu().getCarried().isEmpty()) {
                this.getMenu().setCarried(this.craftItem(who, this.storage, this.storage.getAvailableStacks()));
                return;
            }
            target = new CarriedItemInventory(this.getMenu());
            maxTimesToCraft = 1;
        }
        ItemStack itemAtStart = this.getItem().copy();
        if (itemAtStart.isEmpty()) {
            return;
        }
        for (int x = 0; x < maxTimesToCraft; ++x) {
            if (!ItemStack.isSameItemSameComponents((ItemStack)itemAtStart, (ItemStack)this.getItem())) {
                return;
            }
            if (!target.simulateAdd(itemAtStart).isEmpty()) {
                return;
            }
            KeyCounter all = this.storage.getAvailableStacks();
            ItemStack extra = target.addItems(this.craftItem(who, this.storage, all));
            if (extra.isEmpty()) continue;
            Platform.spawnDrops(who.level(), who.blockPosition(), List.of(extra));
            return;
        }
    }

    protected RecipeHolder<CraftingRecipe> findRecipe(CraftingInput ic, Level level) {
        CraftingTermMenu terminalMenu;
        RecipeHolder<CraftingRecipe> recipe;
        ICraftingGridMenu iCraftingGridMenu = this.menu;
        if (iCraftingGridMenu instanceof CraftingTermMenu && (recipe = (terminalMenu = (CraftingTermMenu)iCraftingGridMenu).getCurrentRecipe()) != null && ((CraftingRecipe)recipe.value()).matches((RecipeInput)ic, level)) {
            return terminalMenu.getCurrentRecipe();
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)ic, level).orElse(null);
    }

    @Override
    protected NonNullList<ItemStack> getRemainingItems(CraftingInput ic, Level level) {
        CraftingTermMenu terminalMenu;
        RecipeHolder<CraftingRecipe> recipe;
        ICraftingGridMenu iCraftingGridMenu = this.menu;
        if (iCraftingGridMenu instanceof CraftingTermMenu && (recipe = (terminalMenu = (CraftingTermMenu)iCraftingGridMenu).getCurrentRecipe()) != null && ((CraftingRecipe)recipe.value()).matches((RecipeInput)ic, level)) {
            return ((CraftingRecipe)terminalMenu.getCurrentRecipe().value()).getRemainingItems((RecipeInput)ic);
        }
        return super.getRemainingItems(ic, level);
    }

    private ItemStack craftItem(Player p, MEStorage inv, KeyCounter all) {
        ItemStack is = this.getItem().copy();
        if (is.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Object[] set = new ItemStack[this.getPattern().size()];
        Arrays.fill(set, ItemStack.EMPTY);
        Level level = p.level();
        if (!level.isClientSide()) {
            NonNullList ic = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
            for (int x = 0; x < 9; ++x) {
                ic.set(x, (Object)this.getPattern().getStackInSlot(x));
            }
            CraftingInput recipeInput = CraftingInput.of((int)3, (int)3, (List)ic);
            RecipeHolder<CraftingRecipe> r = this.findRecipe(recipeInput, level);
            this.setRecipeUsed(r);
            if (r == null) {
                return ItemStack.EMPTY;
            }
            is = ((CraftingRecipe)r.value()).assemble((RecipeInput)recipeInput, (HolderLookup.Provider)level.registryAccess());
            if (inv != null) {
                IPartitionList filter = ViewCellItem.createItemFilter(this.menu.getViewCells());
                for (int x = 0; x < this.getPattern().size(); ++x) {
                    if (this.getPattern().getStackInSlot(x).isEmpty()) continue;
                    set[x] = CraftingTermSlot.extractItemsByRecipe(this.energySrc, this.mySrc, inv, level, (Recipe<CraftingInput>)r.value(), is, recipeInput.width(), recipeInput.height(), (List<ItemStack>)ic, this.getPattern().getStackInSlot(x), x, all, filter);
                    ic.set(x, set[x]);
                }
            }
        }
        if (this.preCraft(p, inv, (ItemStack[])set, is)) {
            this.makeItem(p, is);
            this.postCraft(p, inv, (ItemStack[])set, is);
        }
        p.containerMenu.slotsChanged(this.craftInv.toContainer());
        return is;
    }

    private static ItemStack extractItemsByRecipe(IEnergySource energySrc, IActionSource mySrc, MEStorage src, Level level, Recipe<CraftingInput> r, ItemStack output, int gridWidth, int gridHeight, List<ItemStack> craftingItems, ItemStack providedTemplate, int slot, KeyCounter items, IPartitionList filter) {
        if (energySrc.extractAEPower(1.0, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9) {
            boolean checkFuzzy;
            long extracted;
            if (providedTemplate == null) {
                return ItemStack.EMPTY;
            }
            AEItemKey ae_req = AEItemKey.of(providedTemplate);
            if ((filter == null || filter.isListed(ae_req)) && (extracted = src.extract(ae_req, 1L, Actionable.MODULATE, mySrc)) > 0L) {
                energySrc.extractAEPower(1.0, Actionable.MODULATE, PowerMultiplier.CONFIG);
                return ae_req.toStack();
            }
            boolean bl = checkFuzzy = !providedTemplate.getComponents().isEmpty() || providedTemplate.isDamageableItem();
            if (items != null && checkFuzzy) {
                ArrayList<ItemStack> craftingInputItems = new ArrayList<ItemStack>(craftingItems);
                for (Object2LongMap.Entry<AEKey> x : items) {
                    long ex;
                    Object object = x.getKey();
                    if (!(object instanceof AEItemKey)) continue;
                    AEItemKey itemKey = (AEItemKey)object;
                    if (providedTemplate.getItem() != itemKey.getItem() || itemKey.matches(output)) continue;
                    craftingInputItems.set(slot, itemKey.toStack());
                    CraftingInput adjustedCraftingInput = CraftingInput.of((int)gridWidth, (int)gridHeight, craftingInputItems);
                    if (!r.matches((RecipeInput)adjustedCraftingInput, level) || !ItemStack.matches((ItemStack)r.assemble((RecipeInput)adjustedCraftingInput, (HolderLookup.Provider)level.registryAccess()), (ItemStack)output) || filter != null && !filter.isListed(itemKey) || (ex = src.extract(itemKey, 1L, Actionable.MODULATE, mySrc)) <= 0L) continue;
                    energySrc.extractAEPower(1.0, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    return itemKey.toStack();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean preCraft(Player p, MEStorage inv, ItemStack[] set, ItemStack result) {
        return true;
    }

    private void makeItem(Player p, ItemStack is) {
        super.onTake(p, is);
    }

    private void postCraft(Player p, MEStorage inv, ItemStack[] set, ItemStack result) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if (!p.getCommandSenderWorld().isClientSide()) {
            for (int x = 0; x < this.craftInv.size(); ++x) {
                int amount;
                AEItemKey what;
                long inserted;
                if (this.craftInv.getStackInSlot(x).isEmpty()) {
                    this.craftInv.setItemDirect(x, set[x]);
                    continue;
                }
                if (set[x].isEmpty() || (inserted = inv.insert(what = AEItemKey.of(set[x]), amount = set[x].getCount(), Actionable.MODULATE, this.mySrc)) >= (long)amount) continue;
                drops.add(what.toStack((int)((long)amount - inserted)));
            }
        }
        if (drops.size() > 0) {
            Platform.spawnDrops(p.level(), new BlockPos((int)p.getX(), (int)p.getY(), (int)p.getZ()), drops);
        }
    }

    InternalInventory getPattern() {
        return this.pattern;
    }
}

