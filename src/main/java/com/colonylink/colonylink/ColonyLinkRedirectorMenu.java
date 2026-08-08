package com.colonylink.colonylink;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ColonyLinkRedirectorMenu extends AbstractContainerMenu
{
    private final ColonyLinkRedirectorBlockEntity blockEntity;

    public static final int BUFFER_COLS = ColonyLinkRedirectorBlockEntity.BUFFER_COLS;
    public static final int BUFFER_ROWS = ColonyLinkRedirectorBlockEntity.BUFFER_ROWS;

    /**
     * Index du slot Warehouse Link Card dans la liste des slots du menu.
     * Il est placé en premier pour simplifier les indices.
     */
    public static final int WAREHOUSE_CARD_SLOT_INDEX = 0;

    // Client side constructor
    public ColonyLinkRedirectorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(containerId, playerInventory, (ColonyLinkRedirectorBlockEntity) playerInventory.player.level()
                .getBlockEntity(buf.readBlockPos()));
    }

    // Server side constructor
    public ColonyLinkRedirectorMenu(int containerId, Inventory playerInventory, ColonyLinkRedirectorBlockEntity blockEntity)
    {
        super(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE.get(), containerId);
        this.blockEntity = blockEntity;

        // ── Slot Warehouse Link Card ─────────────────────────────────────────
        // Item area of the AE2 upgrade-cell box overflowing the top-right edge. The
        // Screen DERIVES the box origin from this slot (box = slot - cell item offset
        // (1,6)), so moving the slot moves the whole cell + fallback + JEI zone + tooltip.
        // Nudged 2px left (177→175) so the cell's open left flank tucks under the panel's
        // right frame → a continuous border all the way around (Fab's on-capture call).
        this.addSlot(new SlotItemHandler(blockEntity.warehouseCardSlot, 0, 175, 12)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.getItem() instanceof WarehouseLinkCard;
            }
        });

        // ── Buffer slots — 9 columns x 3 rows (UI passe 1: grid top at y=38) ─
        for (int row = 0; row < BUFFER_ROWS; row++)
        {
            for (int col = 0; col < BUFFER_COLS; col++)
            {
                this.addSlot(new SlotItemHandler(
                        blockEntity.buffer,
                        row * BUFFER_COLS + col,
                        8 + col * 18,
                        38 + row * 18
                ));
            }
        }

        // ── Inventaire joueur ────────────────────────────────────────────────
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 107 + row * 18));
            }
        }

        // ── Hotbar joueur ────────────────────────────────────────────────────
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 165));
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        // v1.6.2 — range check (like a chest, 8 blocks) instead of always-true: the
        // buffer GUI closes if the player walks away or the redirector is removed.
        if (blockEntity == null || blockEntity.isRemoved()) return false;
        var hp = blockEntity.getBlockPos();
        double dx = hp.getX() + 0.5 - player.getX();
        double dy = hp.getY() + 0.5 - player.getY();
        double dz = hp.getZ() + 0.5 - player.getZ();
        return (dx * dx + dy * dy + dz * dz) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            int bufferStart = 1; // après le slot warehouse card
            int bufferEnd = bufferStart + BUFFER_ROWS * BUFFER_COLS;

            if (index == WAREHOUSE_CARD_SLOT_INDEX)
            {
                // Carte → inventaire joueur
                if (!this.moveItemStackTo(slotStack, bufferEnd, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (index >= bufferStart && index < bufferEnd)
            {
                // Buffer → inventaire joueur
                if (!this.moveItemStackTo(slotStack, bufferEnd, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else
            {
                // Inventaire joueur
                if (slotStack.getItem() instanceof WarehouseLinkCard)
                {
                    // Carte → slot dédié
                    if (!this.moveItemStackTo(slotStack, WAREHOUSE_CARD_SLOT_INDEX, bufferStart, false))
                        return ItemStack.EMPTY;
                }
                else
                {
                    // Autre item → buffer
                    if (!this.moveItemStackTo(slotStack, bufferStart, bufferEnd, false))
                        return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return returnStack;
    }

    public ColonyLinkRedirectorBlockEntity getBlockEntity()
    {
        return blockEntity;
    }
}