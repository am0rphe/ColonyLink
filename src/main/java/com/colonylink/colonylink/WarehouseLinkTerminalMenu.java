package com.colonylink.colonylink;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Optional;

/**
 * Menu for the Warehouse Link Terminal Part.
 *
 * References WarehouseLinkTerminalPart (not a BlockEntity).
 * The Part is retrieved from the host BlockEntity at the given position + side.
 *
 * Slot indices:
 *   0        Warehouse Link Card
 *   1–9      Crafting grid 3×3
 *   10       Crafting output
 *   11–37    Player inventory 3×9
 *   38–46    Player hotbar 9
 */
public class WarehouseLinkTerminalMenu extends AbstractContainerMenu
{
    public static final int CARD_SLOT_INDEX     = 0;
    public static final int CRAFT_GRID_START    = 1;
    public static final int CRAFT_OUTPUT_SLOT   = 10;
    public static final int PLAYER_INV_START    = 11;
    public static final int PLAYER_HOTBAR_START = 38;

    private final WarehouseLinkTerminalPart part;
    private final Player                    player;
    private final TransientCraftingContainer craftingGrid;
    private final ResultContainer            craftResult = new ResultContainer();

    // ── Client constructor — reads pos + side from buf ────────────────────────

    public WarehouseLinkTerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(containerId, playerInventory,
                resolvePartFromBuf(playerInventory, buf));
    }

    /**
     * Resolves the Part from the host BlockEntity at the position encoded in the buf.
     * The buf contains: BlockPos (3×int) + side ordinal (byte).
     */
    private static WarehouseLinkTerminalPart resolvePartFromBuf(Inventory inv, FriendlyByteBuf buf)
    {
        net.minecraft.core.BlockPos pos = buf.readBlockPos();
        int sideOrd = buf.readByte() & 0xFF;
        net.minecraft.core.Direction side =
                sideOrd < net.minecraft.core.Direction.values().length
                        ? net.minecraft.core.Direction.values()[sideOrd]
                        : net.minecraft.core.Direction.NORTH;

        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (!(be instanceof appeng.api.parts.IPartHost host)) return null;

        appeng.api.parts.IPart raw = host.getPart(side);
        if (raw instanceof WarehouseLinkTerminalPart part) return part;
        return null;
    }

    // ── Server constructor ────────────────────────────────────────────────────

    public WarehouseLinkTerminalMenu(int containerId, Inventory playerInventory,
                                     WarehouseLinkTerminalPart part)
    {
        super(ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_MENU_TYPE.get(), containerId);
        this.part         = part;
        this.player       = playerInventory.player;
        this.craftingGrid = new TransientCraftingContainer(this, 3, 3);

        // ── Slot 0: Warehouse Link Card ────────────────────────────────────
        this.addSlot(new SlotItemHandler(part.getWarehouseCardSlot(), 0, -18, -18)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            { return stack.getItem() instanceof WarehouseLinkCard; }
        });

        // ── Slots 1–9: Crafting grid ───────────────────────────────────────
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                this.addSlot(new Slot(craftingGrid, row * 3 + col,
                        26 + col * 18, 84 + row * 18));

        // ── Slot 10: Crafting output ───────────────────────────────────────
        this.addSlot(new ResultSlot(playerInventory.player, craftingGrid, craftResult, 0,
                134, 84)
        {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack stack)
            {
                super.onTake(player, stack);
                updateCraftResult();
            }
        });

        // ── Slots 11–37: Player inventory ─────────────────────────────────
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInventory,
                        col + row * 9 + 9, 8 + col * 18, 168 + row * 18));

        // ── Slots 38–46: Player hotbar ─────────────────────────────────────
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 226));
    }

    // ── Crafting result ───────────────────────────────────────────────────────

    @Override
    public void slotsChanged(Container container)
    {
        super.slotsChanged(container);
        updateCraftResult();
    }

    private void updateCraftResult()
    {
        if (player.level().isClientSide()) return;
        CraftingInput input = craftingGrid.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipe =
                player.level().getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, input, player.level());
        craftResult.setItem(0, recipe.isPresent()
                ? recipe.get().value().assemble(input, player.level().registryAccess())
                : ItemStack.EMPTY);
    }

    @Override
    public boolean stillValid(Player player)
    {
        // Valid as long as the part is still in the world and online
        return part != null && part.getHostBlockEntity() != null
                && !part.getHostBlockEntity().isRemoved();
    }

    // ── Shift-click ───────────────────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack slotStack   = slot.getItem();
        ItemStack returnStack = slotStack.copy();

        int craftEnd = CRAFT_OUTPUT_SLOT + 1;
        int invEnd   = PLAYER_HOTBAR_START + 9;

        if (index == CARD_SLOT_INDEX)
        {
            if (!this.moveItemStackTo(slotStack, craftEnd, invEnd, true))
                return ItemStack.EMPTY;
        }
        else if (index < craftEnd)
        {
            if (!this.moveItemStackTo(slotStack, craftEnd, invEnd, true))
                return ItemStack.EMPTY;
        }
        else
        {
            if (slotStack.getItem() instanceof WarehouseLinkCard)
            {
                if (!this.moveItemStackTo(slotStack, CARD_SLOT_INDEX, CRAFT_GRID_START, false))
                    return ItemStack.EMPTY;
            }
            else
            {
                if (!this.moveItemStackTo(slotStack, CRAFT_GRID_START, CRAFT_OUTPUT_SLOT, false))
                {
                    if (index < PLAYER_HOTBAR_START)
                    {
                        if (!this.moveItemStackTo(slotStack, PLAYER_HOTBAR_START, invEnd, false))
                            return ItemStack.EMPTY;
                    }
                    else
                    {
                        if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_HOTBAR_START, false))
                            return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return returnStack;
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        if (!player.level().isClientSide())
        {
            for (int i = 0; i < craftingGrid.getContainerSize(); i++)
            {
                ItemStack stack = craftingGrid.getItem(i);
                if (!stack.isEmpty())
                    player.getInventory().placeItemBackInInventory(stack);
            }
            if (part != null) part.onGuiClosed();
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public WarehouseLinkTerminalPart    getPart()         { return part; }
    public TransientCraftingContainer   getCraftingGrid() { return craftingGrid; }
    public ResultContainer              getCraftResult()  { return craftResult; }
}