/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.items;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.ITerminalHost;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.ICraftingGridMenu;
import appeng.helpers.InventoryAction;
import appeng.me.storage.LinkStatusRespectingInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.PlayerInternalInventory;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class CraftingTermMenu
extends MEStorageMenu
implements ICraftingGridMenu {
    public static final MenuType<CraftingTermMenu> TYPE = MenuTypeBuilder.create(CraftingTermMenu::new, ITerminalHost.class).build("craftingterm");
    private static final String ACTION_CLEAR_TO_PLAYER = "clearToPlayer";
    private final ISegmentedInventory craftingInventoryHost;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    @Nullable
    private CraftingInput lastTestedInput;
    private final CraftingTermSlot outputSlot;
    private RecipeHolder<CraftingRecipe> currentRecipe;

    public CraftingTermMenu(int id, Inventory ip, ITerminalHost host) {
        this(TYPE, id, ip, host, true);
    }

    public CraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host, boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.craftingInventoryHost = (ISegmentedInventory)((Object)host);
        InternalInventory craftingGridInv = this.craftingInventoryHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
        for (int i = 0; i < 9; ++i) {
            this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i);
            this.addSlot(this.craftingSlots[i], SlotSemantics.CRAFTING_GRID);
        }
        LinkStatusRespectingInventory linkStatusInventory = new LinkStatusRespectingInventory(host.getInventory(), this::getLinkStatus);
        this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(), this.energySource, linkStatusInventory, craftingGridInv, craftingGridInv, this);
        this.addSlot(this.outputSlot, SlotSemantics.CRAFTING_RESULT);
        this.updateCurrentRecipeAndOutput(true);
        this.registerClientAction(ACTION_CLEAR_TO_PLAYER, this::clearToPlayerInventory);
    }

    @Override
    public IEnergySource getEnergySource() {
        return this.energySource;
    }

    public void slotsChanged(Container inventory) {
        this.updateCurrentRecipeAndOutput(false);
    }

    private void updateCurrentRecipeAndOutput(boolean forceUpdate) {
        ArrayList<ItemStack> testItems = new ArrayList<ItemStack>(this.craftingSlots.length);
        for (CraftingMatrixSlot craftingSlot : this.craftingSlots) {
            testItems.add(craftingSlot.getItem().copy());
        }
        CraftingInput testInput = CraftingInput.of((int)3, (int)3, testItems);
        if (!forceUpdate && Objects.equals(this.lastTestedInput, testInput)) {
            return;
        }
        Level level = this.getPlayer().level();
        this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)testInput, level).orElse(null);
        this.lastTestedInput = testInput;
        if (this.currentRecipe == null) {
            this.outputSlot.set(ItemStack.EMPTY);
        } else {
            this.outputSlot.set(((CraftingRecipe)this.currentRecipe.value()).assemble((RecipeInput)testInput, (HolderLookup.Provider)level.registryAccess()));
        }
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return this.craftingInventoryHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
    }

    @Override
    public void startAutoCrafting(List<ICraftingGridMenu.AutoCraftEntry> toCraft) {
        CraftConfirmMenu.openWithCraftingList(this.getActionHost(), (ServerPlayer)this.getPlayer(), this.getLocator(), toCraft);
    }

    public RecipeHolder<CraftingRecipe> getCurrentRecipe() {
        return this.currentRecipe;
    }

    public void clearCraftingGrid() {
        Preconditions.checkState((boolean)this.isClientSide());
        CraftingMatrixSlot slot = this.craftingSlots[0];
        InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.index, 0L);
        PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    @Override
    public boolean hasIngredient(Ingredient ingredient, Object2IntOpenHashMap<Object> reservedAmounts) {
        for (Slot slot : this.getSlots(SlotSemantics.CRAFTING_GRID)) {
            ItemStack stackInSlot = slot.getItem();
            if (stackInSlot.isEmpty() || !ingredient.test(stackInSlot)) continue;
            int reservedAmount = reservedAmounts.getOrDefault((Object)slot, 0);
            if (stackInSlot.getCount() <= reservedAmount) continue;
            reservedAmounts.merge((Object)slot, 1, Integer::sum);
            return true;
        }
        return super.hasIngredient(ingredient, reservedAmounts);
    }

    public MissingIngredientSlots findMissingIngredients(Map<Integer, Ingredient> ingredients) {
        HashSet<Integer> missingSlots = new HashSet<Integer>();
        HashSet<Integer> craftableSlots = new HashSet<Integer>();
        Object2IntOpenHashMap reservedGridAmounts = new Object2IntOpenHashMap();
        NonNullList playerItems = this.getPlayerInventory().items;
        int[] reservedPlayerItems = new int[playerItems.size()];
        for (Map.Entry<Integer, Ingredient> entry : ingredients.entrySet()) {
            Ingredient ingredient = entry.getValue();
            boolean found = false;
            for (int i = 0; i < playerItems.size(); ++i) {
                ItemStack stack;
                if (this.isPlayerInventorySlotLocked(i) || (stack = (ItemStack)playerItems.get(i)).getCount() - reservedPlayerItems[i] <= 0 || !ingredient.test(stack)) continue;
                int n = i;
                reservedPlayerItems[n] = reservedPlayerItems[n] + 1;
                found = true;
                break;
            }
            if (!found && this.hasIngredient(ingredient, (Object2IntOpenHashMap<Object>)reservedGridAmounts)) {
                reservedGridAmounts.merge((Object)ingredient, 1, Integer::sum);
                found = true;
            }
            if (!found) {
                for (ItemStack stack : ingredient.getItems()) {
                    if (!this.isCraftable(stack)) continue;
                    craftableSlots.add(entry.getKey());
                    found = true;
                    break;
                }
            }
            if (found) continue;
            missingSlots.add(entry.getKey());
        }
        return new MissingIngredientSlots(missingSlots, craftableSlots);
    }

    protected boolean isCraftable(ItemStack itemStack) {
        IClientRepo clientRepo = this.getClientRepo();
        if (clientRepo != null) {
            for (GridInventoryEntry stack : clientRepo.getAllEntries()) {
                if (!AEItemKey.matches(stack.getWhat(), itemStack) || !stack.isCraftable()) continue;
                return true;
            }
        }
        return false;
    }

    public void clearToPlayerInventory() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR_TO_PLAYER);
            return;
        }
        InternalInventory craftingGridInv = this.craftingInventoryHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
        PlayerInternalInventory playerInv = new PlayerInternalInventory(this.getPlayerInventory());
        for (int i = 0; i < craftingGridInv.size(); ++i) {
            for (int emptyLoop = 0; emptyLoop < 2; ++emptyLoop) {
                boolean allowEmpty = emptyLoop == 1;
                int HOTBAR_SIZE = 9;
                int j = 9;
                while (j-- > 0) {
                    if (playerInv.getStackInSlot(j).isEmpty() != allowEmpty) continue;
                    craftingGridInv.setItemDirect(i, playerInv.getSlotInv(j).addItems(craftingGridInv.getStackInSlot(i)));
                }
                for (j = 9; j < 36; ++j) {
                    if (playerInv.getStackInSlot(j).isEmpty() != allowEmpty) continue;
                    craftingGridInv.setItemDirect(i, playerInv.getSlotInv(j).addItems(craftingGridInv.getStackInSlot(i)));
                }
            }
        }
    }

    public record MissingIngredientSlots(Set<Integer> missingSlots, Set<Integer> craftableSlots) {
        public int totalSize() {
            return this.missingSlots.size() + this.craftableSlots.size();
        }

        public boolean anyMissingOrCraftable() {
            return this.anyMissing() || this.anyCraftable();
        }

        public boolean anyMissing() {
            return !this.missingSlots.isEmpty();
        }

        public boolean anyCraftable() {
            return !this.craftableSlots.isEmpty();
        }
    }
}

