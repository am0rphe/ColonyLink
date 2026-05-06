package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ColonyLinkRedirectorScreen extends AbstractContainerScreen<ColonyLinkRedirectorMenu>
{
    private static final int GUI_WIDTH = 230;
    private static final int GUI_HEIGHT = 305;

    public ColonyLinkRedirectorScreen(ColonyLinkRedirectorMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init()
    {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 211;
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

        // Barre de titre
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 16, 0xFF6B6B6B);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 4, 0xFF8B8B8B);

        // Infos redirector
        ColonyLinkRedirectorBlockEntity be = menu.getBlockEntity();
        if (be != null)
        {
            // Statut wand — gauche ligne 2
            boolean wandLinked = false;
            if (Minecraft.getInstance().player != null)
            {
                for (ItemStack stack : Minecraft.getInstance().player.getInventory().items)
                {
                    if (stack.getItem() instanceof ColonyLinkWand)
                    {
                        wandLinked = ColonyLinkWandLinkableHandler.isLinked(stack);
                        break;
                    }
                }
            }
            String wandText = "Wand: " + (wandLinked ? "Linked" : "Unlinked");
            int wandColor = wandLinked ? 0x00FF00 : 0xFF4444;
            graphics.drawString(this.font, wandText, x + 8, y + 18, wandColor, false);
        }

        // Zone buffer
        graphics.fill(x + 6, y + 25, x + imageWidth - 6, y + 205, 0xFF373737);
        graphics.fill(x + 6, y + 25, x + imageWidth - 6, y + 26, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 25, x + 7, y + 205, 0xFF8B8B8B);

        // Slots buffer
        int bufferCols = ColonyLinkRedirectorBlockEntity.BUFFER_COLS;
        int bufferRows = ColonyLinkRedirectorBlockEntity.BUFFER_ROWS;
        for (int row = 0; row < bufferRows; row++)
        {
            for (int col = 0; col < bufferCols; col++)
            {
                int slotX = x + 8 + col * 18;
                int slotY = y + 27 + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF4A4A4A);
                graphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF373737);
                graphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF373737);
                graphics.fill(slotX, slotY + 15, slotX + 16, slotY + 16, 0xFF8B8B8B);
                graphics.fill(slotX + 15, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            }
        }

        // Séparateur
        graphics.fill(x + 6, y + 207, x + imageWidth - 6, y + 208, 0xFF555555);

        // Zone inventaire joueur
        graphics.fill(x + 6, y + 209, x + imageWidth - 6, y + 301, 0xFF373737);

        // Slots inventaire joueur
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int slotX = x + 8 + col * 18;
                int slotY = y + 219 + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF4A4A4A);
                graphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF373737);
                graphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF373737);
                graphics.fill(slotX, slotY + 15, slotX + 16, slotY + 16, 0xFF8B8B8B);
                graphics.fill(slotX + 15, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            }
        }

        // Slots hotbar
        for (int col = 0; col < 9; col++)
        {
            int slotX = x + 8 + col * 18;
            int slotY = y + 277;
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
    }
}