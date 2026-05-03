package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ColonyLinkScreen extends Screen
{
    private List<ColonyLinkPacket.ResourceEntry> entries;
    private final BlockPos builderPos;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int MAX_VISIBLE = 8;
    private static final int SCROLLBAR_WIDTH = 6;

    private static final int GUI_WIDTH = 276;
    // Feature 1 : hauteur augmentée pour la ligne requête PNJ (+24px)
    private static final int GUI_HEIGHT = 320;

    private boolean isDraggingScrollbar = false;
    private double dragStartY = 0;
    private int dragStartOffset = 0;

    private String builderName = "";
    private String buildingName = "";
    private String workerStatus = "";
    private int availableCpus = 0;
    private String redirectorState = "N/A";

    // Feature 1 — requête prioritaire
    private ColonyLinkPacket.BuilderRequest builderRequest = ColonyLinkPacket.BuilderRequest.NONE;

    public ColonyLinkScreen(ColonyLinkPacket packet)
    {
        super(Component.literal("Colony Link - Builder Resources"));
        this.entries = packet.entries();
        this.builderPos = packet.builderPos();
        this.builderName = packet.builderName();
        this.buildingName = packet.buildingName();
        this.workerStatus = packet.workerStatus();
        this.availableCpus = packet.availableCpus();
        this.redirectorState = packet.redirectorState();
        this.builderRequest = packet.builderRequest() != null
                ? packet.builderRequest() : ColonyLinkPacket.BuilderRequest.NONE;
    }

    public void updateEntries(List<ColonyLinkPacket.ResourceEntry> newEntries, String builderName,
                              String buildingName, String workerStatus, int availableCpus,
                              String redirectorState, ColonyLinkPacket.BuilderRequest builderRequest)
    {
        this.entries = newEntries;
        this.builderName = builderName;
        this.buildingName = buildingName;
        this.workerStatus = workerStatus;
        this.availableCpus = availableCpus;
        this.redirectorState = redirectorState;
        this.builderRequest = builderRequest != null ? builderRequest : ColonyLinkPacket.BuilderRequest.NONE;
        int maxOffset = Math.max(0, entries.size() - MAX_VISIBLE);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;
    }

    @Override
    protected void init()
    {
        super.init();
        PacketDistributor.sendToServer(new GuiStatePacket(true, builderPos));
    }

    @Override
    public void onClose()
    {
        PacketDistributor.sendToServer(new GuiStatePacket(false, builderPos));
        super.onClose();
    }

    // ── Coordonnées GUI ───────────────────────────────────────────────────

    private int getGuiX() { return (this.width - GUI_WIDTH) / 2; }
    private int getGuiY() { return (this.height - GUI_HEIGHT) / 2; }

    /** Y de départ de la liste de ressources (après le panel info + panel requête PNJ). */
    private int getListStartY() { return getGuiY() + 112; }

    private int getScrollbarX() { return getGuiX() + GUI_WIDTH - 16; }
    private int getScrollbarTop() { return getListStartY() + 1; }
    private int getScrollbarBottom() { return getScrollbarTop() + MAX_VISIBLE * ENTRY_HEIGHT; }
    private int getScrollbarHeight() { return getScrollbarBottom() - getScrollbarTop(); }

    private int getThumbHeight()
    {
        if (entries.size() <= MAX_VISIBLE) return getScrollbarHeight();
        return Math.max(20, getScrollbarHeight() * MAX_VISIBLE / entries.size());
    }

    private int getThumbY()
    {
        if (entries.size() <= MAX_VISIBLE) return getScrollbarTop();
        int maxOffset = entries.size() - MAX_VISIBLE;
        return getScrollbarTop() + (getScrollbarHeight() - getThumbHeight()) * scrollOffset / maxOffset;
    }

    // ── Couleurs boutons ──────────────────────────────────────────────────

    private int getButtonColor(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> 0xFF004488;
            case CRAFTABLE  -> 0xFF005500;
            case NO_PATTERN -> 0xFF550000;
            case CRAFTING   -> 0xFF885500;
            case MISSING    -> 0xFF5D3A00;
        };
    }

    private int getButtonHoverColor(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> 0xFF0066CC;
            case CRAFTABLE  -> 0xFF007700;
            case NO_PATTERN -> 0xFF660000;
            case CRAFTING   -> 0xFF885500;
            case MISSING    -> 0xFF8B5E00;
        };
    }

    private int getButtonTextColor(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> 0x4488FF;
            case CRAFTABLE  -> 0x00FF00;
            case NO_PATTERN -> 0xFF4444;
            case CRAFTING   -> 0xFFAA00;
            case MISSING    -> 0xFFCC66;
        };
    }

    private String getButtonText(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> "Available";
            case CRAFTABLE  -> "Craft";
            case NO_PATTERN -> "No Pattern";
            case CRAFTING   -> "Crafting...";
            case MISSING    -> "Missing";
        };
    }

    /** Texte du bouton de la requête prioritaire (légèrement différent : "Fulfill" pour AVAILABLE) */
    private String getRequestButtonText(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> "Fulfill";
            case CRAFTABLE  -> "Craft";
            case NO_PATTERN -> "No Pattern";
            case CRAFTING   -> "Crafting...";
            case MISSING    -> "Missing";
        };
    }

    private boolean isButtonClickable(ResourceStatus status)
    {
        return status == ResourceStatus.CRAFTABLE
                || status == ResourceStatus.AVAILABLE
                || status == ResourceStatus.MISSING;
    }

    // ── Bounds boutons liste ──────────────────────────────────────────────

    private void getBtnBounds(int i, int[] out)
    {
        int x = getGuiX();
        int listWidth = GUI_WIDTH - 26;
        int entryY = getListStartY() + i * ENTRY_HEIGHT;
        out[0] = x + 7 + listWidth - 60;
        out[1] = entryY + 2;
        out[2] = 58;
        out[3] = 16;
    }

    // ── Bounds boutons globaux ────────────────────────────────────────────

    private int getCraftAllBtnX() { return getGuiX() + 8; }
    private int getCraftAllBtnY() { return getGuiY() + GUI_HEIGHT - 22; }
    private int getCraftAllBtnW() { return 120; }
    private int getCraftAllBtnH() { return 16; }

    private int getSendAllBtnX() { return getGuiX() + GUI_WIDTH - 128; }
    private int getSendAllBtnY() { return getGuiY() + GUI_HEIGHT - 22; }
    private int getSendAllBtnW() { return 120; }
    private int getSendAllBtnH() { return 16; }

    // Feature 2 — bouton Restart (haut droite)
    private int getRestartBtnX() { return getGuiX() + GUI_WIDTH - 60; }
    private int getRestartBtnY() { return getGuiY() + 4; }
    private int getRestartBtnW() { return 52; }
    private int getRestartBtnH() { return 14; }

    // Feature 1 — bouton Fulfill de la requête PNJ
    private int getReqBtnX() { return getGuiX() + GUI_WIDTH - 76; }
    private int getReqBtnY() { return getGuiY() + 92; }
    private int getReqBtnW() { return 64; }
    private int getReqBtnH() { return 16; }

    // ── Helpers état ──────────────────────────────────────────────────────

    private boolean hasCraftableItems()
    {
        return entries.stream().anyMatch(e ->
                e.status() == ResourceStatus.CRAFTABLE || e.status() == ResourceStatus.MISSING);
    }

    private boolean hasAvailableItems()
    {
        return entries.stream().anyMatch(e -> e.status() == ResourceStatus.AVAILABLE);
    }

    private int getWorkerStatusColor()
    {
        if (workerStatus == null) return 0x888888;
        if (workerStatus.contains("work") || workerStatus.contains("Working")) return 0x00FF00;
        if (workerStatus.contains("sleep") || workerStatus.contains("Sleep")) return 0x4488FF;
        if (workerStatus.contains("eat") || workerStatus.contains("Eat")) return 0xFFAA00;
        if (workerStatus.contains("sick") || workerStatus.contains("Sick")) return 0xFF4444;
        if (workerStatus.contains("Stuck") || workerStatus.contains("STUCK")) return 0xFF0000;
        if (workerStatus.contains("Idle") || workerStatus.contains("IDLE")) return 0xFFFF00;
        return 0xCCCCCC;
    }

    // ── Dessin panel info ─────────────────────────────────────────────────

    private void drawInfoPanel(GuiGraphics graphics, int x, int y)
    {
        int panelH = 58;
        graphics.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, 0xFF3A3A3A);
        graphics.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 23, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 22, x + 7, y + 22 + panelH, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 22 + panelH - 1, x + GUI_WIDTH - 6, y + 22 + panelH, 0xFF373737);
        graphics.fill(x + GUI_WIDTH - 7, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, 0xFF373737);

        graphics.drawString(this.font, "§7Builder: §f" + builderName, x + 10, y + 26, 0xFFFFFF, false);
        graphics.drawString(this.font, "§7Building: §f" + buildingName, x + 10, y + 36, 0xFFFFFF, false);

        String statusLabel = "§7Status: ";
        graphics.drawString(this.font, statusLabel, x + 10, y + 46, 0xFFFFFF, false);
        graphics.drawString(this.font, workerStatus,
                x + 10 + this.font.width(statusLabel), y + 46, getWorkerStatusColor(), false);

        // Bug 2 : CPUs disponibles (info dynamique)
        graphics.drawString(this.font, "§7CPUs: §f" + availableCpus, x + 10, y + 58, 0xFFFFFF, false);

        int redirectorColor = switch (redirectorState)
        {
            case "LINKED"        -> 0x00FF00;
            case "STANDBY"       -> 0xFF8800;
            case "NO_CONTROLLER" -> 0xFF0000;
            default              -> 0x888888;
        };
        String redirectorLabel = "§7Redirector: ";
        graphics.drawString(this.font, redirectorLabel, x + 100, y + 58, 0xFFFFFF, false);
        graphics.drawString(this.font, redirectorState,
                x + 100 + this.font.width(redirectorLabel), y + 58, redirectorColor, false);
    }

    // ── Feature 1 : Dessin panel requête PNJ ─────────────────────────────

    private void drawRequestPanel(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        // Panel : y+80 a y+110 (30px)
        // Ligne 1 (y+80..y+90) : label "Priority Request:"
        // Ligne 2 (y+90..y+110) : icone + nom + bouton
        int panelY = y + 80;
        int panelH = 30;
        graphics.fill(x + 6, panelY, x + GUI_WIDTH - 6, panelY + panelH, 0xFF2E2E4A);
        graphics.fill(x + 6, panelY, x + GUI_WIDTH - 6, panelY + 1, 0xFF6666AA);
        graphics.fill(x + 6, panelY, x + 7, panelY + panelH, 0xFF6666AA);
        graphics.fill(x + 6, panelY + panelH - 1, x + GUI_WIDTH - 6, panelY + panelH, 0xFF1A1A3A);
        graphics.fill(x + GUI_WIDTH - 7, panelY, x + GUI_WIDTH - 6, panelY + panelH, 0xFF1A1A3A);
        // Separateur entre les deux lignes
        graphics.fill(x + 7, panelY + 11, x + GUI_WIDTH - 7, panelY + 12, 0xFF3A3A6A);

        // Ligne 1 : label
        graphics.drawString(this.font, "§9Priority Request:", x + 10, panelY + 3, 0xAAAAFF, false);

        boolean hasRequest = builderRequest != null
                && !builderRequest.stack().isEmpty()
                && builderRequest.count() > 0;

        if (!hasRequest)
        {
            graphics.drawString(this.font, "§8None", x + 10, panelY + 14, 0x666666, false);
            return;
        }

        // Ligne 2 : icone (16x16, centree verticalement dans 18px)
        int itemX = x + 10;
        int itemY = panelY + 12;
        graphics.renderItem(builderRequest.stack(), itemX, itemY);

        // Nom + quantite (a droite de l'icone)
        String reqText = builderRequest.count() + "x " + builderRequest.stack().getDisplayName().getString();
        graphics.drawString(this.font, reqText, itemX + 18, panelY + 17, 0xFFFFFF, false);

        // Bouton Fulfill/Craft/etc. aligne a droite, centre sur la ligne 2
        int rbX = getReqBtnX();
        int rbY = getReqBtnY();
        int rbW = getReqBtnW();
        int rbH = getReqBtnH();
        ResourceStatus reqStatus = builderRequest.status();

        boolean hovered = mouseX >= rbX && mouseX <= rbX + rbW
                && mouseY >= rbY && mouseY <= rbY + rbH;

        int btnBg = hovered && isButtonClickable(reqStatus)
                ? getButtonHoverColor(reqStatus)
                : getButtonColor(reqStatus);

        graphics.fill(rbX, rbY, rbX + rbW, rbY + rbH, btnBg);
        graphics.fill(rbX, rbY, rbX + rbW, rbY + 1, 0xFFFFFFFF);
        graphics.fill(rbX, rbY, rbX + 1, rbY + rbH, 0xFFFFFFFF);
        graphics.fill(rbX, rbY + rbH - 1, rbX + rbW, rbY + rbH, 0xFF373737);
        graphics.fill(rbX + rbW - 1, rbY, rbX + rbW, rbY + rbH, 0xFF373737);
        graphics.drawCenteredString(this.font, getRequestButtonText(reqStatus),
                rbX + rbW / 2, rbY + 4, getButtonTextColor(reqStatus));
    }

    // ── Dessin bouton (helper réutilisable) ───────────────────────────────

    private void drawButton(GuiGraphics graphics, int bx, int by, int bw, int bh,
                            int bgColor, String label, int textColor)
    {
        graphics.fill(bx, by, bx + bw, by + bh, bgColor);
        graphics.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF373737);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF373737);
        graphics.drawCenteredString(this.font, label, bx + bw / 2, by + 3, textColor);
    }

    // ── render() ─────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        // Pas de blur
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        int x = getGuiX();
        int y = getGuiY();

        // Background
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF8B8B8B);
        graphics.fill(x, y, x + GUI_WIDTH, y + 2, 0xFFFFFFFF);
        graphics.fill(x, y, x + 2, y + GUI_HEIGHT, 0xFFFFFFFF);
        graphics.fill(x, y + GUI_HEIGHT - 2, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF373737);
        graphics.fill(x + GUI_WIDTH - 2, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF373737);

        // Title bar
        graphics.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 22, 0xFF6B6B6B);
        graphics.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 4, 0xFF8B8B8B);
        graphics.drawString(this.font, this.title, x + 8, y + 7, 0x404040, false);

        // Feature 2 — bouton Restart (haut droite)
        int rbtnX = getRestartBtnX();
        int rbtnY = getRestartBtnY();
        int rbtnW = getRestartBtnW();
        int rbtnH = getRestartBtnH();
        boolean restartHovered = mouseX >= rbtnX && mouseX <= rbtnX + rbtnW
                && mouseY >= rbtnY && mouseY <= rbtnY + rbtnH;
        drawButton(graphics, rbtnX, rbtnY, rbtnW, rbtnH,
                restartHovered ? 0xFF885500 : 0xFF553300,
                "Restart", 0xFFAA44);

        // Info panel
        drawInfoPanel(graphics, x, y);

        // Feature 1 — panel requête PNJ
        drawRequestPanel(graphics, x, y, mouseX, mouseY);

        // List area
        int listWidth = GUI_WIDTH - 26;
        int listStartY = getListStartY();
        graphics.fill(x + 6, listStartY - 1, x + GUI_WIDTH - 18, listStartY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, 0xFF373737);
        graphics.fill(x + 6, listStartY - 1, x + GUI_WIDTH - 18, listStartY, 0xFF8B8B8B);
        graphics.fill(x + 6, listStartY - 1, x + 7, listStartY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, 0xFF8B8B8B);

        int visibleCount = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);

        List<Component> pendingTooltip = null;

        for (int i = 0; i < visibleCount; i++)
        {
            int index = i + scrollOffset;
            ColonyLinkPacket.ResourceEntry entry = entries.get(index);
            ItemStack stack = entry.stack();
            ResourceStatus status = entry.status();
            int realCount = entry.realCount();
            int entryY = listStartY + i * ENTRY_HEIGHT;

            int rowColor = (i % 2 == 0) ? 0xFF4A4A4A : 0xFF424242;
            graphics.fill(x + 7, entryY, x + 7 + listWidth, entryY + ENTRY_HEIGHT, rowColor);

            graphics.renderItem(stack, x + 9, entryY + 2);

            String text = realCount + "x " + stack.getDisplayName().getString();
            if (entry.isDomum())
                text = "§b[DO] §r" + text;
            graphics.drawString(this.font, text, x + 29, entryY + 6, 0xFFFFFF, false);

            int[] btn = new int[4];
            getBtnBounds(i, btn);
            int btnX = btn[0], btnY = btn[1], btnW = btn[2], btnH = btn[3];

            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH;

            if (hovered && !entry.tooltipLines().isEmpty())
            {
                List<Component> tooltipComponents = new ArrayList<>();
                for (String line : entry.tooltipLines())
                    tooltipComponents.add(Component.literal(line));
                pendingTooltip = tooltipComponents;
            }

            int bgColor = hovered && isButtonClickable(status)
                    ? getButtonHoverColor(status)
                    : getButtonColor(status);

            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor);
            graphics.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFFFFFFFF);
            graphics.fill(btnX, btnY, btnX + 1, btnY + btnH, 0xFFFFFFFF);
            graphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF373737);
            graphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, 0xFF373737);

            graphics.drawCenteredString(this.font, getButtonText(status),
                    btnX + btnW / 2, btnY + 4, getButtonTextColor(status));
        }

        // Scrollbar
        if (entries.size() > MAX_VISIBLE)
        {
            int sbX = getScrollbarX();
            int sbTop = getScrollbarTop();
            int sbBottom = getScrollbarBottom();

            graphics.fill(sbX, sbTop, sbX + SCROLLBAR_WIDTH, sbBottom, 0xFF373737);
            graphics.fill(sbX, sbTop, sbX + 1, sbBottom, 0xFF8B8B8B);
            graphics.fill(sbX, sbTop, sbX + SCROLLBAR_WIDTH, sbTop + 1, 0xFF8B8B8B);

            int thumbY = getThumbY();
            int thumbH = getThumbHeight();

            graphics.fill(sbX + 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF8B8B8B);
            graphics.fill(sbX + 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + 1, 0xFFFFFFFF);
            graphics.fill(sbX + 1, thumbY, sbX + 2, thumbY + thumbH, 0xFFFFFFFF);
            graphics.fill(sbX + 1, thumbY + thumbH - 1, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF373737);
            graphics.fill(sbX + SCROLLBAR_WIDTH - 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF373737);
        }

        // Separator
        graphics.fill(x + 6, y + GUI_HEIGHT - 26, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 25, 0xFF555555);

        // Craft All
        int caX = getCraftAllBtnX();
        int caY = getCraftAllBtnY();
        int caW = getCraftAllBtnW();
        int caH = getCraftAllBtnH();

        boolean craftAllHovered = mouseX >= caX && mouseX <= caX + caW
                && mouseY >= caY && mouseY <= caY + caH;
        boolean hasCraftable = hasCraftableItems();

        int craftAllBg = hasCraftable ? (craftAllHovered ? 0xFF007700 : 0xFF005500) : 0xFF333333;
        graphics.fill(caX, caY, caX + caW, caY + caH, craftAllBg);
        graphics.fill(caX, caY, caX + caW, caY + 1, 0xFFFFFFFF);
        graphics.fill(caX, caY, caX + 1, caY + caH, 0xFFFFFFFF);
        graphics.fill(caX, caY + caH - 1, caX + caW, caY + caH, 0xFF373737);
        graphics.fill(caX + caW - 1, caY, caX + caW, caY + caH, 0xFF373737);
        graphics.drawCenteredString(this.font, "Craft All",
                caX + caW / 2, caY + 4, hasCraftable ? 0x00FF00 : 0x888888);

        // Bug 2 : tooltip Craft All avec nb CPUs
        if (craftAllHovered && hasCraftable)
        {
            List<Component> craftAllTooltip = new ArrayList<>();
            craftAllTooltip.add(Component.literal("§aCraft All craftable items"));
            craftAllTooltip.add(Component.literal("§7" + availableCpus + " CPU"
                    + (availableCpus != 1 ? "s" : "") + " available"));
            craftAllTooltip.add(Component.literal("§8" + availableCpus
                    + " craft" + (availableCpus != 1 ? "s" : "") + " will run simultaneously"));
            pendingTooltip = craftAllTooltip;
        }

        // Send All
        int saX = getSendAllBtnX();
        int saY = getSendAllBtnY();
        int saW = getSendAllBtnW();
        int saH = getSendAllBtnH();

        boolean sendAllHovered = mouseX >= saX && mouseX <= saX + saW
                && mouseY >= saY && mouseY <= saY + saH;
        boolean hasAvailable = hasAvailableItems();

        int sendAllBg = hasAvailable ? (sendAllHovered ? 0xFF0066CC : 0xFF004488) : 0xFF333333;
        graphics.fill(saX, saY, saX + saW, saY + saH, sendAllBg);
        graphics.fill(saX, saY, saX + saW, saY + 1, 0xFFFFFFFF);
        graphics.fill(saX, saY, saX + 1, saY + saH, 0xFFFFFFFF);
        graphics.fill(saX, saY + saH - 1, saX + saW, saY + saH, 0xFF373737);
        graphics.fill(saX + saW - 1, saY, saX + saW, saY + saH, 0xFF373737);
        graphics.drawCenteredString(this.font, "Send All",
                saX + saW / 2, saY + 4, hasAvailable ? 0x4488FF : 0x888888);

        // Tooltip bouton Restart
        if (mouseX >= getRestartBtnX() && mouseX <= getRestartBtnX() + getRestartBtnW()
                && mouseY >= getRestartBtnY() && mouseY <= getRestartBtnY() + getRestartBtnH())
        {
            List<Component> restartTooltip = new ArrayList<>();
            restartTooltip.add(Component.literal("§6Restart Builder"));
            restartTooltip.add(Component.literal("§7Cancels current task and restarts the builder PNJ"));
            pendingTooltip = restartTooltip;
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        // Tooltip rendu en dernier
        if (pendingTooltip != null && !pendingTooltip.isEmpty())
            graphics.renderComponentTooltip(this.font, pendingTooltip, mouseX, mouseY);
    }

    // ── mouseClicked() ────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        // Feature 2 — bouton Restart
        int rbtnX = getRestartBtnX();
        int rbtnY = getRestartBtnY();
        int rbtnW = getRestartBtnW();
        int rbtnH = getRestartBtnH();

        if (mouseX >= rbtnX && mouseX <= rbtnX + rbtnW
                && mouseY >= rbtnY && mouseY <= rbtnY + rbtnH)
        {
            PacketDistributor.sendToServer(new RestartBuilderPacket(builderPos));
            return true;
        }

        // Feature 1 — bouton requête PNJ
        boolean hasRequest = builderRequest != null
                && !builderRequest.stack().isEmpty()
                && builderRequest.count() > 0;

        if (hasRequest)
        {
            int reqBtnX = getReqBtnX();
            int reqBtnY = getReqBtnY();
            int reqBtnW = getReqBtnW();
            int reqBtnH = getReqBtnH();

            if (mouseX >= reqBtnX && mouseX <= reqBtnX + reqBtnW
                    && mouseY >= reqBtnY && mouseY <= reqBtnY + reqBtnH
                    && isButtonClickable(builderRequest.status()))
            {
                switch (builderRequest.status())
                {
                    case AVAILABLE ->
                            PacketDistributor.sendToServer(new SendToBuilderPacket(
                                    builderRequest.stack(), builderPos, builderRequest.count()));
                    case CRAFTABLE ->
                            PacketDistributor.sendToServer(new CraftRequestPacket(
                                    builderRequest.stack(), builderRequest.count(),
                                    false, BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                    case MISSING ->
                            PacketDistributor.sendToServer(new CraftRequestPacket(
                                    builderRequest.stack(), builderRequest.count(),
                                    true, builderRequest.redirectorPos(), ResourceStatus.MISSING));
                    default -> {}
                }
                return true;
            }
        }

        // Craft All
        int caX = getCraftAllBtnX();
        int caY = getCraftAllBtnY();
        int caW = getCraftAllBtnW();
        int caH = getCraftAllBtnH();

        if (mouseX >= caX && mouseX <= caX + caW && mouseY >= caY && mouseY <= caY + caH
                && hasCraftableItems())
        {
            List<ItemStack> toCraft = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            for (ColonyLinkPacket.ResourceEntry entry : entries)
            {
                if (entry.status() == ResourceStatus.CRAFTABLE)
                {
                    if (entry.isDomum())
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                entry.stack(), entry.realCount(), true,
                                entry.redirectorPos(), ResourceStatus.CRAFTABLE));
                    else
                    {
                        toCraft.add(entry.stack());
                        counts.add(entry.realCount());
                    }
                }
                else if (entry.status() == ResourceStatus.MISSING)
                {
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true,
                            entry.redirectorPos(), ResourceStatus.MISSING));
                }
            }

            if (!toCraft.isEmpty())
                PacketDistributor.sendToServer(new CraftAllRequestPacket(toCraft, counts));

            return true;
        }

        // Send All
        int saX = getSendAllBtnX();
        int saY = getSendAllBtnY();
        int saW = getSendAllBtnW();
        int saH = getSendAllBtnH();

        if (mouseX >= saX && mouseX <= saX + saW && mouseY >= saY && mouseY <= saY + saH
                && hasAvailableItems())
        {
            for (ColonyLinkPacket.ResourceEntry entry : entries)
            {
                if (entry.status() == ResourceStatus.AVAILABLE)
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
            }
            return true;
        }

        // Boutons individuels de la liste
        int visibleCount = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
        for (int i = 0; i < visibleCount; i++)
        {
            int index = i + scrollOffset;
            ColonyLinkPacket.ResourceEntry entry = entries.get(index);

            if (!isButtonClickable(entry.status())) continue;

            int[] btn = new int[4];
            getBtnBounds(i, btn);
            int btnX = btn[0], btnY = btn[1], btnW = btn[2], btnH = btn[3];

            if (mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH)
            {
                if (entry.status() == ResourceStatus.CRAFTABLE && entry.isDomum())
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true,
                            entry.redirectorPos(), ResourceStatus.CRAFTABLE));
                else if (entry.status() == ResourceStatus.CRAFTABLE)
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), false,
                            BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                else if (entry.status() == ResourceStatus.MISSING)
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true,
                            entry.redirectorPos(), ResourceStatus.MISSING));
                else if (entry.status() == ResourceStatus.AVAILABLE)
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
                return true;
            }
        }

        // Scrollbar drag
        if (entries.size() > MAX_VISIBLE)
        {
            int sbX = getScrollbarX();
            int thumbY = getThumbY();
            int thumbH = getThumbHeight();

            if (mouseX >= sbX && mouseX <= sbX + SCROLLBAR_WIDTH
                    && mouseY >= thumbY && mouseY <= thumbY + thumbH)
            {
                isDraggingScrollbar = true;
                dragStartY = mouseY;
                dragStartOffset = scrollOffset;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (isDraggingScrollbar && entries.size() > MAX_VISIBLE)
        {
            int maxOffset = entries.size() - MAX_VISIBLE;
            double dragDelta = mouseY - dragStartY;
            double trackHeight = getScrollbarHeight() - getThumbHeight();
            int newOffset = (int) (dragStartOffset + dragDelta / trackHeight * maxOffset);
            scrollOffset = Math.max(0, Math.min(maxOffset, newOffset));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        int maxOffset = entries.size() - MAX_VISIBLE;
        if (scrollY < 0 && scrollOffset < maxOffset)
            scrollOffset++;
        else if (scrollY > 0 && scrollOffset > 0)
            scrollOffset--;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public BlockPos getBuilderPos() { return builderPos; }
}