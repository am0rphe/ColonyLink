package com.colonylink.colonylink;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Redirector screen — UI passe 1: full AE2-theme refonte.
 *
 * <p>Canonical AE2 panel (176 wide, Pattern Provider-like), light body drawn as a
 * runtime nine-slice of AE2's own background.png; 9x3 pattern grid + player
 * inventory + hotbar get the standard AE2 slot well (states.png SLOT_BACKGROUND);
 * the Warehouse Link Card lives in an AE2 upgrade-panel cell (extra_panels.png)
 * overflowing the top-right edge, exactly like AE2's UpgradesPanel. All AE2 assets
 * are referenced at runtime only (CC BY-NC-SA — never copied); presence is probed
 * once in init() with a minimal procedural fallback (never magenta).
 *
 * <p>Kept from the previous screen: the Domum "target block" slot view
 * ({@link #renderSlot}) and the custom tooltips (pattern info + card slot — the
 * card tooltip now derives its position from the menu slot instead of duplicated
 * hardcoded coordinates).
 */
@OnlyIn(Dist.CLIENT)
public class ColonyLinkRedirectorScreen extends AbstractContainerScreen<ColonyLinkRedirectorMenu>
{
    // ── Layout (UI passe 1 — 176 canon, compact) ──────────────────────────────
    private static final int GUI_WIDTH  = 176;
    private static final int GUI_HEIGHT = 189;

    /** Compact header: two short lines under the title (builder / clipboard). */
    private static final int HEADER1_Y = 17;
    private static final int HEADER2_Y = 27;
    private static final int INV_LABEL_Y = 96;

    // The upgrade-cell box (28x30, overflowing the right edge) is NOT a fixed anchor:
    // its top-left is DERIVED from the card slot so the AE2 cell's well always lands on
    // the slot. The cell's item area sits at texel (1,6) inside the box (measured — the
    // cell has no 5px left border), so box origin = slot - (ITEM_DX, ITEM_DY). See
    // upgradeBoxX()/upgradeBoxY(). This fixes the passe-1 misalignment (the old fixed
    // anchor assumed a (6,6) item offset → the well fell 5px left of the item).

    // ── AE2 asset probes (once per screen open, silent fallback) ──────────────
    private boolean aeBackgroundPresent  = false;
    private boolean aeStatesPresent      = false;
    private boolean aeExtraPanelsPresent = false;

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
        this.inventoryLabelY = INV_LABEL_Y;

        // Probe the three AE2 textures ONCE (paths are internal, not API).
        this.aeBackgroundPresent  = AeGuiBlits.probeTexture(AeGuiBlits.AE_BACKGROUND);
        this.aeStatesPresent      = AeGuiBlits.probeTexture(AeGuiBlits.AE_STATES);
        this.aeExtraPanelsPresent = AeGuiBlits.probeTexture(AeGuiBlits.AE_EXTRA_PANELS);
    }

    // ── Background ────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int x = this.leftPos;
        int y = this.topPos;

        // Panel: AE2 light body + frame (runtime nine-slice), opaque.
        if (aeBackgroundPresent)
            AeGuiBlits.drawAeNineSlice(graphics, x, y, GUI_WIDTH, GUI_HEIGHT, 1f, true);
        else
            drawFallbackPanel(graphics, x, y);

        // Slot wells for every slot EXCEPT the card (index 0): buffer 9x3, player
        // inventory, hotbar. Coordinates come from the menu slots — single source,
        // nothing duplicated. The card's well is baked into the upgrade cell below.
        for (int i = 1; i < this.menu.slots.size(); i++)
        {
            Slot s = this.menu.slots.get(i);
            if (aeStatesPresent)
                AeGuiBlits.blitStatesSprite(graphics,
                        AeGuiBlits.SLOT_BG_U, AeGuiBlits.SLOT_BG_V,
                        AeGuiBlits.SLOT_BG_SIZE, AeGuiBlits.SLOT_BG_SIZE,
                        x + s.x - 1, y + s.y - 1, 1f);
            else
                drawFallbackWell(graphics, x + s.x - 1, y + s.y - 1);
        }

        // Warehouse card: one empty AE2 upgrade cell overflowing the right edge, its
        // box origin derived from the card slot so the well lands exactly on the item.
        // Never draws the ghost upgrade icon (see AeGuiBlits.drawUpgradeCell).
        int boxX = upgradeBoxX();
        int boxY = upgradeBoxY();
        if (aeExtraPanelsPresent)
            AeGuiBlits.drawUpgradeCell(graphics, boxX, boxY, 1f);
        else
            drawFallbackUpgradeBox(graphics, boxX, boxY);
    }

    /** Screen-absolute top-left of the upgrade-cell box: the card slot minus the cell's
     *  measured item-area offset, so the AE2 well always sits on the slot (no drift). */
    private int upgradeBoxX()
    {
        return this.leftPos + this.menu.getSlot(ColonyLinkRedirectorMenu.WAREHOUSE_CARD_SLOT_INDEX).x
                - AeGuiBlits.UPGRADE_CELL_ITEM_DX;
    }

    private int upgradeBoxY()
    {
        return this.topPos + this.menu.getSlot(ColonyLinkRedirectorMenu.WAREHOUSE_CARD_SLOT_INDEX).y
                - AeGuiBlits.UPGRADE_CELL_ITEM_DY;
    }

    /** Minimal procedural fallback panel (AE2 assets missing): light fill + dark frame. */
    private void drawFallbackPanel(GuiGraphics g, int x, int y)
    {
        g.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFFC6C6C6);
        g.fill(x, y, x + GUI_WIDTH, y + 1, AeGuiBlits.AE_TEXT);
        g.fill(x, y + GUI_HEIGHT - 1, x + GUI_WIDTH, y + GUI_HEIGHT, AeGuiBlits.AE_TEXT);
        g.fill(x, y, x + 1, y + GUI_HEIGHT, AeGuiBlits.AE_TEXT);
        g.fill(x + GUI_WIDTH - 1, y, x + GUI_WIDTH, y + GUI_HEIGHT, AeGuiBlits.AE_TEXT);
    }

    /** Minimal procedural 18x18 slot well (vanilla-style recess). */
    private void drawFallbackWell(GuiGraphics g, int wx, int wy)
    {
        g.fill(wx, wy, wx + 18, wy + 18, 0xFF373737);
        g.fill(wx + 1, wy + 1, wx + 18, wy + 18, 0xFFFFFFFF);
        g.fill(wx + 1, wy + 1, wx + 17, wy + 17, 0xFF8B8B8B);
    }

    /** Minimal procedural stand-in for the upgrade cell box (28x30 + well). */
    private void drawFallbackUpgradeBox(GuiGraphics g, int bx, int by)
    {
        g.fill(bx, by, bx + AeGuiBlits.UPGRADE_CELL_W, by + AeGuiBlits.UPGRADE_CELL_H, 0xFFC6C6C6);
        g.fill(bx, by, bx + AeGuiBlits.UPGRADE_CELL_W, by + 1, AeGuiBlits.AE_TEXT);
        g.fill(bx, by + AeGuiBlits.UPGRADE_CELL_H - 1, bx + AeGuiBlits.UPGRADE_CELL_W, by + AeGuiBlits.UPGRADE_CELL_H, AeGuiBlits.AE_TEXT);
        g.fill(bx, by, bx + 1, by + AeGuiBlits.UPGRADE_CELL_H, AeGuiBlits.AE_TEXT);
        g.fill(bx + AeGuiBlits.UPGRADE_CELL_W - 1, by, bx + AeGuiBlits.UPGRADE_CELL_W, by + AeGuiBlits.UPGRADE_CELL_H, AeGuiBlits.AE_TEXT);
        // Well aligned so its 16x16 item area matches the real cell's (bx+1, by+6).
        drawFallbackWell(g, bx + AeGuiBlits.UPGRADE_CELL_ITEM_DX - 1, by + AeGuiBlits.UPGRADE_CELL_ITEM_DY - 1);
    }

    // ── Labels (title + compact 2-line header + inventory label) ──────────────

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        // Light AE body → dark palette text, flat (no shadow). Statuses use the
        // light-body semantic tints already calibrated in ColonyLinkGuiConfig.
        ColonyLinkGuiConfig cfg = ColonyLinkGuiConfig.get();

        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AeGuiBlits.AE_TEXT, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AeGuiBlits.AE_TEXT, false);

        ColonyLinkRedirectorBlockEntity be = menu.getBlockEntity();

        // Header line 1 — linked builder (only when known). The lang value embeds
        // §-codes calibrated for the old dark body: strip them and draw in palette.
        if (be != null)
        {
            String bName = (be.getLinkedBuilderPos() != null) ? be.getLinkedBuilderName() : "";
            if (!bName.isEmpty() && !bName.equals("N/A"))
            {
                String line = ChatFormatting.stripFormatting(
                        Component.translatable("colonylink.screen.info.builder", bName).getString());
                graphics.drawString(this.font, ellipsize(line, GUI_WIDTH - 16),
                        8, HEADER1_Y, AeGuiBlits.AE_TEXT, false);
            }
        }

        // Header line 2 — clipboard linked/unlinked status.
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
        String wandText = ChatFormatting.stripFormatting(Component.translatable(
                wandLinked ? "colonylink.redir.clipboard_linked"
                           : "colonylink.redir.clipboard_unlinked").getString());
        graphics.drawString(this.font, ellipsize(wandText, GUI_WIDTH - 16),
                8, HEADER2_Y, wandLinked ? cfg.semGreen() : cfg.semRed(), false);
    }

    /** Truncates to the given pixel width with a trailing ellipsis (long builder names). */
    private String ellipsize(String text, int maxWidth)
    {
        if (this.font.width(text) <= maxWidth) return text;
        String cut = this.font.plainSubstrByWidth(text, maxWidth - this.font.width("…"));
        return cut + "…";
    }

    // ── Slots — Domum "target block" view (kept from the previous screen) ─────

    /**
     * Override renderSlot — same behaviour as AE2 terminals: if the slot contains a
     * DomumPatternItem, render the decoded target item instead. Applies ONLY to the
     * Redirector's own slots (buffer + card = SlotItemHandler), never to the player
     * inventory/hotbar (vanilla Slot), so patterns left in the inventory keep their
     * normal item rendering.
     */
    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot)
    {
        ItemStack inSlot = slot.getItem();
        if (slot instanceof net.neoforged.neoforge.items.SlotItemHandler
                && !inSlot.isEmpty() && inSlot.getItem() instanceof DomumPatternItem)
        {
            ItemStack target = DomumPatternItem.getTargetStackClient(inSlot);
            if (target != null && !target.isEmpty())
            {
                int x = slot.x;
                int y = slot.y;

                // Hover highlight (the vanilla one is skipped when we bypass super)
                if (this.isHovering(slot.x, slot.y, 16, 16, (double) lastMouseX, (double) lastMouseY))
                    graphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);

                graphics.renderItem(target, x, y);
                graphics.renderItemDecorations(this.font, target, x, y, null);
                return;
            }
        }
        super.renderSlot(graphics, slot);
    }

    // Track mouse pos for the highlight in renderSlot
    private int lastMouseX = 0, lastMouseY = 0;

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        super.render(graphics, mouseX, mouseY, partialTick);

        // Custom tooltip for the Domum pattern slots of the buffer.
        boolean domumTooltipShown = false;
        for (Slot slot : menu.slots)
        {
            if (mouseX < this.leftPos + slot.x || mouseX > this.leftPos + slot.x + 16) continue;
            if (mouseY < this.topPos  + slot.y || mouseY > this.topPos  + slot.y + 16) continue;

            ItemStack realStack = slot.getItem();
            // Redirector-only tooltip (SlotItemHandler): a pattern left in the player
            // inventory keeps its normal item tooltip, consistent with its rendering.
            if (!(slot instanceof net.neoforged.neoforge.items.SlotItemHandler)) continue;
            if (!(realStack.getItem() instanceof DomumPatternItem)) continue;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) continue;

            ItemStack target = DomumPatternItem.getTargetStackClient(realStack);

            int outCount = DomumPatternItem.getOutputCount(realStack);
            List<Component> tt = new ArrayList<>();
            tt.add(Component.translatable("colonylink.redir.domum_pattern"));
            tt.add(Component.translatable("colonylink.redir.crafts")
                    .append(target.getDisplayName())
                    .append(Component.literal(outCount > 1 ? " §7(×" + outCount + ")" : "")));

            // Variant — from the TARGET ItemStack (the pattern encodes the target with its blockstate)
            net.minecraft.world.item.component.BlockItemStateProperties bs =
                    target.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
            if (bs != null && !bs.properties().isEmpty())
            {
                for (var entry : bs.properties().entrySet())
                    tt.add(Component.literal("§7" + entry.getKey() + ": §f" + entry.getValue()));
            }
            else
            {
                tt.add(Component.translatable("colonylink.redir.no_variant"));
            }

            if (hasShiftDown())
            {
                // Materials — shift only
                List<DomumPatternItem.MaterialEntry> mats =
                        DomumPatternItem.getMaterials(realStack, mc.level.registryAccess());
                if (!mats.isEmpty())
                {
                    tt.add(Component.translatable("colonylink.redir.materials"));
                    for (var mat : mats)
                        if (mat.resolved())
                            tt.add(Component.literal("§7  • §f")
                                    .append(new ItemStack(mat.block()).getDisplayName())
                                    .append(Component.literal(" ×1")));
                }
            }
            else
            {
                tt.add(Component.translatable("colonylink.redir.hold_shift_materials")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }

            graphics.renderComponentTooltip(this.font, tt, mouseX, mouseY);
            domumTooltipShown = true;
            break;
        }

        // Warehouse Link Card slot tooltip — position derived from the MENU slot
        // (single source of coordinates; the old screen duplicated 170/20 here).
        Slot cardSlot = menu.getSlot(ColonyLinkRedirectorMenu.WAREHOUSE_CARD_SLOT_INDEX);
        int cardSlotX = this.leftPos + cardSlot.x;
        int cardSlotY = this.topPos + cardSlot.y;
        boolean cardTooltipShown = false;
        if (mouseX >= cardSlotX && mouseX <= cardSlotX + 16
                && mouseY >= cardSlotY && mouseY <= cardSlotY + 16)
        {
            // Status ("inserted"/"empty") lives HERE — the old "W✔ / W?" badge is
            // dropped: the tooltip carries the state, the box stays sober (Fab spec).
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("colonylink.redir.card_slot"));
            tooltip.add(Component.translatable("colonylink.redir.card_insert1"));
            tooltip.add(Component.translatable("colonylink.redir.card_insert2"));
            boolean hasCard = menu.getBlockEntity() != null && menu.getBlockEntity().hasWarehouseCard();
            if (hasCard)
                tooltip.add(Component.translatable("colonylink.redir.card_inserted"));
            else
                tooltip.add(Component.translatable("colonylink.redir.card_empty"));
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            cardTooltipShown = true;
        }

        // Vanilla tooltip only when no custom tooltip is shown
        if (!domumTooltipShown && !cardTooltipShown)
            this.renderTooltip(graphics, mouseX, mouseY);
    }

    // ── JEI integration ───────────────────────────────────────────────────────

    /**
     * Screen-absolute areas covered by GUI parts that overflow the panel bounds —
     * the upgrade-cell box on the right. Consumed by ColonyLinkJeiPlugin
     * (IGuiContainerHandler.getGuiExtraAreas) so the JEI overlay never overlaps it,
     * mirroring AE2's UpgradesPanel.addExclusionZones.
     */
    public List<Rect2i> getJeiExclusionAreas()
    {
        return List.of(new Rect2i(upgradeBoxX(), upgradeBoxY(),
                AeGuiBlits.UPGRADE_CELL_W, AeGuiBlits.UPGRADE_CELL_H));
    }
}
