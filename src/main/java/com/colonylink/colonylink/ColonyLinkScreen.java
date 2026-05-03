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
    private static final int GUI_HEIGHT = 290;

    private boolean isDraggingScrollbar = false;
    private double dragStartY = 0;
    private int dragStartOffset = 0;

    private String builderName = "";
    private String buildingName = "";
    private String workerStatus = "";
    private int availableCpus = 0;
    private String redirectorState = "N/A";

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
    }

    public void updateEntries(List<ColonyLinkPacket.ResourceEntry> newEntries, String builderName,
                              String buildingName, String workerStatus, int availableCpus, String redirectorState)
    {
        this.entries = newEntries;
        this.builderName = builderName;
        this.buildingName = buildingName;
        this.workerStatus = workerStatus;
        this.availableCpus = availableCpus;
        this.redirectorState = redirectorState;
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

    private int getGuiX() { return (this.width - GUI_WIDTH) / 2; }
    private int getGuiY() { return (this.height - GUI_HEIGHT) / 2; }
    private int getGuiWidth() { return GUI_WIDTH; }
    private int getGuiHeight() { return GUI_HEIGHT; }

    private int getScrollbarX() { return getGuiX() + GUI_WIDTH - 16; }
    private int getScrollbarTop() { return getGuiY() + 84; }
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

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        // No blur
    }

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

    private boolean isButtonClickable(ResourceStatus status)
    {
        return status == ResourceStatus.CRAFTABLE
                || status == ResourceStatus.AVAILABLE
                || status == ResourceStatus.MISSING;
    }

    private void getBtnBounds(int i, int[] out)
    {
        int x = getGuiX();
        int y = getGuiY();
        int listWidth = GUI_WIDTH - 26;
        int entryY = y + 83 + i * ENTRY_HEIGHT;
        out[0] = x + 7 + listWidth - 60;
        out[1] = entryY + 2;
        out[2] = 58;
        out[3] = 16;
    }

    private int getCraftAllBtnX() { return getGuiX() + 8; }
    private int getCraftAllBtnY() { return getGuiY() + GUI_HEIGHT - 22; }
    private int getCraftAllBtnW() { return 120; }
    private int getCraftAllBtnH() { return 16; }

    private int getSendAllBtnX() { return getGuiX() + GUI_WIDTH - 128; }
    private int getSendAllBtnY() { return getGuiY() + GUI_HEIGHT - 22; }
    private int getSendAllBtnW() { return 120; }
    private int getSendAllBtnH() { return 16; }

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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        int x = getGuiX();
        int y = getGuiY();
        int guiWidth = getGuiWidth();
        int guiHeight = getGuiHeight();

        // Background
        graphics.fill(x, y, x + guiWidth, y + guiHeight, 0xFF8B8B8B);
        graphics.fill(x, y, x + guiWidth, y + 2, 0xFFFFFFFF);
        graphics.fill(x, y, x + 2, y + guiHeight, 0xFFFFFFFF);
        graphics.fill(x, y + guiHeight - 2, x + guiWidth, y + guiHeight, 0xFF373737);
        graphics.fill(x + guiWidth - 2, y, x + guiWidth, y + guiHeight, 0xFF373737);

        // Title bar
        graphics.fill(x + 2, y + 2, x + guiWidth - 2, y + 22, 0xFF6B6B6B);
        graphics.fill(x + 2, y + 2, x + guiWidth - 2, y + 4, 0xFF8B8B8B);
        graphics.drawString(this.font, this.title, x + 8, y + 7, 0x404040, false);

        // Info panel
        drawInfoPanel(graphics, x, y);

        // List area
        int listWidth = guiWidth - 26;
        graphics.fill(x + 6, y + 82, x + guiWidth - 18, y + 82 + MAX_VISIBLE * ENTRY_HEIGHT, 0xFF373737);
        graphics.fill(x + 6, y + 82, x + guiWidth - 18, y + 83, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 82, x + 7, y + 82 + MAX_VISIBLE * ENTRY_HEIGHT, 0xFF8B8B8B);

        int listY = y + 83;
        int visibleCount = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);

        List<Component> pendingTooltip = null;

        for (int i = 0; i < visibleCount; i++)
        {
            int index = i + scrollOffset;
            ColonyLinkPacket.ResourceEntry entry = entries.get(index);
            ItemStack stack = entry.stack();
            ResourceStatus status = entry.status();
            int realCount = entry.realCount();
            int entryY = listY + i * ENTRY_HEIGHT;

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

            // Tooltip au survol du bouton
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
        graphics.fill(x + 6, y + guiHeight - 26, x + guiWidth - 6, y + guiHeight - 25, 0xFF555555);

        // Craft All
        int caX = getCraftAllBtnX();
        int caY = getCraftAllBtnY();
        int caW = getCraftAllBtnW();
        int caH = getCraftAllBtnH();

        boolean craftAllHovered = mouseX >= caX && mouseX <= caX + caW && mouseY >= caY && mouseY <= caY + caH;
        boolean hasCraftable = hasCraftableItems();

        int craftAllBg = hasCraftable ? (craftAllHovered ? 0xFF007700 : 0xFF005500) : 0xFF333333;
        graphics.fill(caX, caY, caX + caW, caY + caH, craftAllBg);
        graphics.fill(caX, caY, caX + caW, caY + 1, 0xFFFFFFFF);
        graphics.fill(caX, caY, caX + 1, caY + caH, 0xFFFFFFFF);
        graphics.fill(caX, caY + caH - 1, caX + caW, caY + caH, 0xFF373737);
        graphics.fill(caX + caW - 1, caY, caX + caW, caY + caH, 0xFF373737);
        graphics.drawCenteredString(this.font, "Craft All",
                caX + caW / 2, caY + 4, hasCraftable ? 0x00FF00 : 0x888888);

        // Send All
        int saX = getSendAllBtnX();
        int saY = getSendAllBtnY();
        int saW = getSendAllBtnW();
        int saH = getSendAllBtnH();

        boolean sendAllHovered = mouseX >= saX && mouseX <= saX + saW && mouseY >= saY && mouseY <= saY + saH;
        boolean hasAvailable = hasAvailableItems();

        int sendAllBg = hasAvailable ? (sendAllHovered ? 0xFF0066CC : 0xFF004488) : 0xFF333333;
        graphics.fill(saX, saY, saX + saW, saY + saH, sendAllBg);
        graphics.fill(saX, saY, saX + saW, saY + 1, 0xFFFFFFFF);
        graphics.fill(saX, saY, saX + 1, saY + saH, 0xFFFFFFFF);
        graphics.fill(saX, saY + saH - 1, saX + saW, saY + saH, 0xFF373737);
        graphics.fill(saX + saW - 1, saY, saX + saW, saY + saH, 0xFF373737);
        graphics.drawCenteredString(this.font, "Send All",
                saX + saW / 2, saY + 4, hasAvailable ? 0x4488FF : 0x888888);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Tooltip rendu en dernier pour passer au-dessus de tout
        if (pendingTooltip != null && !pendingTooltip.isEmpty())
            graphics.renderComponentTooltip(this.font, pendingTooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        // Craft All
        int caX = getCraftAllBtnX();
        int caY = getCraftAllBtnY();
        int caW = getCraftAllBtnW();
        int caH = getCraftAllBtnH();

        if (mouseX >= caX && mouseX <= caX + caW && mouseY >= caY && mouseY <= caY + caH && hasCraftableItems())
        {
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
                        List<ItemStack> toCraft = new ArrayList<>();
                        List<Integer> counts = new ArrayList<>();
                        toCraft.add(entry.stack());
                        counts.add(entry.realCount());
                        PacketDistributor.sendToServer(new CraftAllRequestPacket(toCraft, counts));
                    }
                }
                else if (entry.status() == ResourceStatus.MISSING)
                {
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true,
                            entry.redirectorPos(), ResourceStatus.MISSING));
                }
            }
            return true;
        }

        // Send All
        int saX = getSendAllBtnX();
        int saY = getSendAllBtnY();
        int saW = getSendAllBtnW();
        int saH = getSendAllBtnH();

        if (mouseX >= saX && mouseX <= saX + saW && mouseY >= saY && mouseY <= saY + saH && hasAvailableItems())
        {
            for (ColonyLinkPacket.ResourceEntry entry : entries)
            {
                if (entry.status() == ResourceStatus.AVAILABLE)
                    PacketDistributor.sendToServer(new SendToBuilderPacket(entry.stack(), builderPos));
            }
            return true;
        }

        // Boutons individuels
        int visibleCount = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
        for (int i = 0; i < visibleCount; i++)
        {
            int index = i + scrollOffset;
            ColonyLinkPacket.ResourceEntry entry = entries.get(index);

            if (!isButtonClickable(entry.status())) continue;

            int[] btn = new int[4];
            getBtnBounds(i, btn);
            int btnX = btn[0], btnY = btn[1], btnW = btn[2], btnH = btn[3];

            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH)
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
                    PacketDistributor.sendToServer(new SendToBuilderPacket(entry.stack(), builderPos));
                return true;
            }
        }

        // Scrollbar
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