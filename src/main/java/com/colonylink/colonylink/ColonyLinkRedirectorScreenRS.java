package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen du Redirector RS2 — identique au screen AE2 visuellement,
 * adapté pour pointer sur ColonyLinkRedirectorMenuRS.
 */
public class ColonyLinkRedirectorScreenRS extends AbstractContainerScreen<ColonyLinkRedirectorMenuRS>
{
    private static final int GUI_WIDTH  = 230;
    private static final int GUI_HEIGHT = 319;

    public ColonyLinkRedirectorScreenRS(ColonyLinkRedirectorMenuRS menu,
                                        Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleLabelX     = 8;
        this.titleLabelY     = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 227;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int x = this.leftPos;
        int y = this.topPos;

        // Fond principal
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF8B8B8B);
        graphics.fill(x, y, x + imageWidth, y + 2, 0xFFFFFFFF);
        graphics.fill(x, y, x + 2, y + imageHeight, 0xFFFFFFFF);
        graphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF373737);
        graphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF373737);

        // Barre de titre — teinte légèrement différente pour distinguer RS2 vs AE2
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 36, 0xFF5B6B5B);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 4, 0xFF7B8B7B);

        // Statut wand RS
        ColonyLinkRedirectorBlockEntityRS be = menu.getBlockEntity();
        if (be != null)
        {
            boolean wandLinked = false;
            if (Minecraft.getInstance().player != null)
            {
                for (ItemStack stack : Minecraft.getInstance().player.getInventory().items)
                {
                    if (stack.getItem() instanceof ColonyLinkWandRS)
                    {
                        wandLinked = ColonyLinkWandRSLinkableHandler.isLinked(stack);
                        break;
                    }
                }
            }
            String wandText = "RS Wand: " + (wandLinked ? "Linked" : "Unlinked");
            int wandColor = wandLinked ? 0x00FF00 : 0xFF4444;
            graphics.drawString(this.font, wandText, x + 8, y + 22, wandColor, false);
        }

        // Slot Warehouse Link Card
        int cardSlotX = x + 170;
        int cardSlotY = y + 10;
        graphics.fill(cardSlotX - 1, cardSlotY - 1, cardSlotX + 17, cardSlotY + 17, 0xFF665500);
        graphics.fill(cardSlotX, cardSlotY, cardSlotX + 16, cardSlotY + 16, 0xFF4A3A00);
        graphics.fill(cardSlotX, cardSlotY, cardSlotX + 16, cardSlotY + 1, 0xFF332800);
        graphics.fill(cardSlotX, cardSlotY, cardSlotX + 1, cardSlotY + 16, 0xFF332800);
        graphics.fill(cardSlotX, cardSlotY + 15, cardSlotX + 16, cardSlotY + 16, 0xFF998822);
        graphics.fill(cardSlotX + 15, cardSlotY, cardSlotX + 16, cardSlotY + 16, 0xFF998822);

        boolean hasCard = be != null && be.hasWarehouseCard();
        int cardTextColor = hasCard ? 0x00FF88 : 0x666666;
        graphics.drawString(this.font, hasCard ? "W✔" : "W?", cardSlotX + 20, cardSlotY + 4, cardTextColor);

        // Zone buffer
        graphics.fill(x + 6, y + 40, x + imageWidth - 6, y + 220, 0xFF373737);
        graphics.fill(x + 6, y + 40, x + imageWidth - 6, y + 41, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 40, x + 7, y + 220, 0xFF8B8B8B);

        for (int row = 0; row < ColonyLinkRedirectorBlockEntityRS.BUFFER_ROWS; row++)
        {
            for (int col = 0; col < ColonyLinkRedirectorBlockEntityRS.BUFFER_COLS; col++)
            {
                int slotX = x + 8 + col * 18;
                int slotY = y + 42 + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF4A4A4A);
                graphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF373737);
                graphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF373737);
                graphics.fill(slotX, slotY + 15, slotX + 16, slotY + 16, 0xFF8B8B8B);
                graphics.fill(slotX + 15, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            }
        }

        // Séparateur + inventaire joueur
        graphics.fill(x + 6, y + 222, x + imageWidth - 6, y + 223, 0xFF555555);
        graphics.fill(x + 6, y + 224, x + imageWidth - 6, y + 316, 0xFF373737);

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int slotX = x + 8 + col * 18;
                int slotY = y + 234 + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF4A4A4A);
                graphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF373737);
                graphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF373737);
                graphics.fill(slotX, slotY + 15, slotX + 16, slotY + 16, 0xFF8B8B8B);
                graphics.fill(slotX + 15, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            }
        }

        for (int col = 0; col < 9; col++)
        {
            int slotX = x + 8 + col * 18;
            int slotY = y + 292;
            graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF4A4A4A);
            graphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF373737);
            graphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF373737);
            graphics.fill(slotX, slotY + 15, slotX + 16, slotY + 16, 0xFF8B8B8B);
            graphics.fill(slotX + 15, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int cardSlotX = this.leftPos + 170;
        int cardSlotY = this.topPos + 10;
        if (mouseX >= cardSlotX && mouseX <= cardSlotX + 16
                && mouseY >= cardSlotY && mouseY <= cardSlotY + 16)
        {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§6Warehouse Link Card slot"));
            tooltip.add(Component.literal("§7Insert a §fWarehouse Link Card §7to enable"));
            tooltip.add(Component.literal("§7the §fCheck Warehouse §7button in the RS Wand GUI."));
            boolean hasCard = menu.getBlockEntity() != null && menu.getBlockEntity().hasWarehouseCard();
            if (hasCard)
                tooltip.add(Component.literal("§a✔ Card inserted — Warehouse scanning enabled"));
            else
                tooltip.add(Component.literal("§8✘ Empty — insert a Warehouse Link Card"));
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}
