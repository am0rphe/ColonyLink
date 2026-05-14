package com.colonylink.colonylink;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menu du Redirector RS2 — identique au menu AE2, pointe sur le BE RS2.
 */
public class ColonyLinkRedirectorMenuRS extends AbstractContainerMenu
{
    private final ColonyLinkRedirectorBlockEntityRS blockEntity;

    public static final int BUFFER_COLS = ColonyLinkRedirectorBlockEntityRS.BUFFER_COLS;
    public static final int BUFFER_ROWS = ColonyLinkRedirectorBlockEntityRS.BUFFER_ROWS;
    public static final int WAREHOUSE_CARD_SLOT_INDEX = 0;

    // Client side constructor
    public ColonyLinkRedirectorMenuRS(int containerId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(containerId, playerInventory, (ColonyLinkRedirectorBlockEntityRS) playerInventory.player.level()
                .getBlockEntity(buf.readBlockPos()));
    }

    // Server side constructor
    public ColonyLinkRedirectorMenuRS(int containerId, Inventory playerInventory,
                                      ColonyLinkRedirectorBlockEntityRS blockEntity)
    {
        super(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE_RS.get(), containerId);
        this.blockEntity = blockEntity;

        // Slot Warehouse Link Card
        this.addSlot(new SlotItemHandler(blockEntity.warehouseCardSlot, 0, 170, 20)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            { return stack.getItem() instanceof WarehouseLinkCard; }
        });

        // Buffer slots
        for (int row = 0; row < BUFFER_ROWS; row++)
            for (int col = 0; col < BUFFER_COLS; col++)
                this.addSlot(new SlotItemHandler(blockEntity.buffer,
                        row * BUFFER_COLS + col,
                        8 + col * 18,
                        52 + row * 18));

        // Inventaire joueur
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 123 + row * 18));

        // Hotbar
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 181));
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            int bufferStart = 1;
            int bufferEnd = bufferStart + BUFFER_ROWS * BUFFER_COLS;

            if (index == WAREHOUSE_CARD_SLOT_INDEX)
            {
                if (!this.moveItemStackTo(slotStack, bufferEnd, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (index >= bufferStart && index < bufferEnd)
            {
                if (!this.moveItemStackTo(slotStack, bufferEnd, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else
            {
                if (slotStack.getItem() instanceof WarehouseLinkCard)
                {
                    if (!this.moveItemStackTo(slotStack, WAREHOUSE_CARD_SLOT_INDEX, bufferStart, false))
                        return ItemStack.EMPTY;
                }
                else
                {
                    if (!this.moveItemStackTo(slotStack, bufferStart, bufferEnd, false))
                        return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }

        return returnStack;
    }

    public ColonyLinkRedirectorBlockEntityRS getBlockEntity() { return blockEntity; }
}