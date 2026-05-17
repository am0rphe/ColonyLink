/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  guideme.color.ColorValue
 *  guideme.color.ConstantColor
 *  guideme.document.LytRect
 *  guideme.render.SimpleRenderContext
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.locale.Language
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.client.gui.me.patternaccess;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.TerminalStyle;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ILinkStatus;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.me.patternaccess.PatternSlot;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.core.network.serverbound.QuickMovePatternPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.collect.HashMultimap;
import guideme.color.ColorValue;
import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.render.SimpleRenderContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternAccessTermScreen<C extends PatternAccessTermMenu>
extends AEBaseScreen<C> {
    private static final Logger LOG = LoggerFactory.getLogger(PatternAccessTermScreen.class);
    private static final int GUI_WIDTH = 195;
    private static final int GUI_TOP_AND_BOTTOM_PADDING = 54;
    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;
    private static final int GUI_HEADER_HEIGHT = 17;
    private static final int GUI_FOOTER_HEIGHT = 99;
    private static final int COLUMNS = 9;
    private static final int PATTERN_PROVIDER_NAME_MARGIN_X = 2;
    private static final int TEXT_MAX_WIDTH = 155;
    private static final int ROW_HEIGHT = 18;
    private static final int SLOT_SIZE = 18;
    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, 195, 17);
    private static final Rect2i ROW_TEXT_TOP_BBOX = new Rect2i(0, 17, 195, 18);
    private static final Rect2i ROW_TEXT_MIDDLE_BBOX = new Rect2i(0, 53, 195, 18);
    private static final Rect2i ROW_TEXT_BOTTOM_BBOX = new Rect2i(0, 89, 195, 18);
    private static final Rect2i ROW_INVENTORY_TOP_BBOX = new Rect2i(0, 35, 195, 18);
    private static final Rect2i ROW_INVENTORY_MIDDLE_BBOX = new Rect2i(0, 71, 195, 18);
    private static final Rect2i ROW_INVENTORY_BOTTOM_BBOX = new Rect2i(0, 107, 195, 18);
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 125, 195, 99);
    private static final Comparator<PatternContainerGroup> GROUP_COMPARATOR = Comparator.comparing(group -> group.name().getString().toLowerCase(Locale.ROOT));
    private final HashMap<Long, PatternContainerRecord> byId = new HashMap();
    private final HashMultimap<PatternContainerGroup, PatternContainerRecord> byGroup = HashMultimap.create();
    private final ArrayList<PatternContainerGroup> groups = new ArrayList();
    private final ArrayList<Row> rows = new ArrayList();
    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<String, Set<Object>>();
    private final Scrollbar scrollbar;
    private final AETextField searchField;
    private final Map<ItemStack, String> patternSearchText = new WeakHashMap<ItemStack, String>();
    private int visibleRows = 0;
    private final ServerSettingToggleButton<ShowPatternProviders> showPatternProviders;

    public PatternAccessTermScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.imageWidth = 195;
        TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        this.addToLeftToolbar(new SettingToggleButton<TerminalStyle>(Settings.TERMINAL_STYLE, terminalStyle, this::toggleTerminalStyle));
        this.showPatternProviders = new ServerSettingToggleButton<ShowPatternProviders>(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE);
        this.addToLeftToolbar(this.showPatternProviders);
        this.searchField = this.widgets.addTextField("search");
        this.searchField.setResponder(str -> this.refreshList());
        this.searchField.setPlaceholder((Component)GuiText.SearchPlaceholder.text());
    }

    @Override
    public void init() {
        this.visibleRows = Math.max(2, this.config.getTerminalStyle().getRows((this.height - 17 - 99 - 54) / 18));
        this.imageHeight = 116 + this.visibleRows * 18;
        super.init();
        this.setInitialFocus((GuiEventListener)this.searchField);
        this.resetScrollbar();
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        ((PatternAccessTermMenu)this.menu).slots.removeIf(slot -> slot instanceof PatternSlot);
        int textColor = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        ClientLevel level = Minecraft.getInstance().level;
        int scrollLevel = this.scrollbar.getCurrentScroll();
        for (int i = 0; i < this.visibleRows; ++i) {
            int rows;
            if (scrollLevel + i >= this.rows.size()) continue;
            Row row = this.rows.get(scrollLevel + i);
            if (row instanceof SlotsRow) {
                SlotsRow slotsRow = (SlotsRow)row;
                PatternContainerRecord container = slotsRow.container;
                for (int col = 0; col < slotsRow.slots; ++col) {
                    PatternSlot slot2 = new PatternSlot(container, slotsRow.offset + col, col * 18 + 8, (i + 1) * 18);
                    ((PatternAccessTermMenu)this.menu).slots.add((Object)slot2);
                    ItemStack pattern = container.getInventory().getStackInSlot(slotsRow.offset + col);
                    if (pattern.isEmpty() || PatternDetailsHelper.decodePattern(pattern, (Level)level) != null) continue;
                    guiGraphics.fill(slot2.x, slot2.y, slot2.x + 16, slot2.y + 16, 0x7FFF0000);
                }
                continue;
            }
            if (!(row instanceof GroupHeaderRow)) continue;
            GroupHeaderRow headerRow = (GroupHeaderRow)row;
            PatternContainerGroup group = headerRow.group;
            if (group.icon() != null) {
                SimpleRenderContext renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);
                renderContext.renderItem(group.icon().getReadOnlyStack(), 10, 23 + i * 18, 8.0f, 8.0f);
            }
            Object displayName = (rows = this.byGroup.get((Object)group).size()) > 1 ? Component.empty().append(group.name()).append((Component)Component.literal((String)(" (" + rows + ")"))) : group.name();
            FormattedCharSequence text = Language.getInstance().getVisualOrder(this.font.substrByWidth((FormattedText)displayName, 145));
            guiGraphics.drawString(this.font, text, 20, 23 + i * 18, textColor, false);
        }
        this.renderLinkStatus(guiGraphics, ((PatternAccessTermMenu)this.getMenu()).getLinkStatus());
    }

    private void renderLinkStatus(GuiGraphics guiGraphics, ILinkStatus linkStatus) {
        if (!linkStatus.connected()) {
            SimpleRenderContext renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);
            LytRect rect = new LytRect(7, 17, 162, this.visibleRows * 18);
            renderContext.fillRect(rect, (ColorValue)new ConstantColor(0x3F000000));
            Component statusDescription = linkStatus.statusDescription();
            if (statusDescription != null) {
                renderContext.renderTextCenteredIn(statusDescription.getString(), ERROR_TEXT_STYLE, rect);
            }
        }
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        Row row;
        int hoveredLineIndex;
        if (this.hoveredSlot == null && (hoveredLineIndex = this.getHoveredLineIndex(x, y)) != -1 && (row = this.rows.get(hoveredLineIndex)) instanceof GroupHeaderRow) {
            GroupHeaderRow headerRow = (GroupHeaderRow)row;
            if (!headerRow.group.tooltip().isEmpty()) {
                guiGraphics.renderTooltip(this.font, headerRow.group.tooltip(), Optional.empty(), x, y);
                return;
            }
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    private int getHoveredLineIndex(int x, int y) {
        x = x - this.leftPos - 8;
        y = y - this.topPos - 18;
        if (x < 0 || y < 0) {
            return -1;
        }
        if (x >= 162 || y >= this.visibleRows * 18) {
            return -1;
        }
        int rowIndex = this.scrollbar.getCurrentScroll() + y / 18;
        if (rowIndex < 0 || rowIndex >= this.rows.size()) {
            return -1;
        }
        return rowIndex;
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if (btn == 1 && this.searchField.isMouseOver(xCoord, yCoord)) {
            this.searchField.setValue("");
        }
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof PatternSlot) {
            InventoryAction action = null;
            switch (clickType) {
                case PICKUP: {
                    action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    break;
                }
                case QUICK_MOVE: {
                    action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;
                }
                case CLONE: {
                    if (!this.getPlayer().getAbilities().instabuild) break;
                    action = InventoryAction.CREATIVE_DUPLICATE;
                    break;
                }
            }
            if (action != null) {
                PatternSlot machineSlot = (PatternSlot)slot;
                InventoryActionPacket p = new InventoryActionPacket(action, machineSlot.slot, machineSlot.getMachineInv().getServerId());
                PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (clickType == ClickType.QUICK_MOVE && ((PatternAccessTermMenu)this.menu).isPlayerSideSlot(slot)) {
            LinkedHashSet<Long> visiblePatternContainers = new LinkedHashSet<Long>();
            for (Row row : this.rows) {
                if (!(row instanceof SlotsRow)) continue;
                SlotsRow slotsRow = (SlotsRow)row;
                visiblePatternContainers.add(slotsRow.container.getServerId());
            }
            int clickedSlot = slot.getContainerSlot();
            QuickMovePatternPacket packet = new QuickMovePatternPacket(((PatternAccessTermMenu)this.menu).containerId, clickedSlot, List.copyOf(visiblePatternContainers));
            PacketDistributor.sendToServer((CustomPacketPayload)packet, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.blit(guiGraphics, offsetX, offsetY, HEADER_BBOX);
        int scrollLevel = this.scrollbar.getCurrentScroll();
        int currentY = offsetY + 17;
        this.blit(guiGraphics, offsetX, currentY + this.visibleRows * 18, FOOTER_BBOX);
        for (int i = 0; i < this.visibleRows; ++i) {
            Row row;
            boolean firstLine = i == 0;
            boolean lastLine = i == this.visibleRows - 1;
            Rect2i bbox = this.selectRowBackgroundBox(false, firstLine, lastLine);
            this.blit(guiGraphics, offsetX, currentY, bbox);
            if (scrollLevel + i < this.rows.size() && (row = this.rows.get(scrollLevel + i)) instanceof SlotsRow) {
                SlotsRow slotsRow = (SlotsRow)row;
                bbox = this.selectRowBackgroundBox(true, firstLine, lastLine);
                bbox.setWidth(8 + 18 * slotsRow.slots - 1);
                this.blit(guiGraphics, offsetX, currentY, bbox);
            }
            currentY += 18;
        }
    }

    private Rect2i selectRowBackgroundBox(boolean isInvLine, boolean firstLine, boolean lastLine) {
        if (isInvLine) {
            if (firstLine) {
                return ROW_INVENTORY_TOP_BBOX;
            }
            if (lastLine) {
                return ROW_INVENTORY_BOTTOM_BBOX;
            }
            return ROW_INVENTORY_MIDDLE_BBOX;
        }
        if (firstLine) {
            return ROW_TEXT_TOP_BBOX;
        }
        if (lastLine) {
            return ROW_TEXT_BOTTOM_BBOX;
        }
        return ROW_TEXT_MIDDLE_BBOX;
    }

    public boolean charTyped(char character, int key) {
        if (character == ' ' && this.searchField.getValue().isEmpty()) {
            return true;
        }
        return super.charTyped(character, key);
    }

    public void clear() {
        this.byId.clear();
        this.cachedSearches.clear();
        this.refreshList();
    }

    public void postFullUpdate(long inventoryId, long sortBy, PatternContainerGroup group, int inventorySize, Int2ObjectMap<ItemStack> slots) {
        PatternContainerRecord record = new PatternContainerRecord(inventoryId, inventorySize, sortBy, group);
        this.byId.put(inventoryId, record);
        AppEngInternalInventory inventory = record.getInventory();
        for (Int2ObjectMap.Entry entry : slots.int2ObjectEntrySet()) {
            inventory.setItemDirect(entry.getIntKey(), (ItemStack)entry.getValue());
        }
        this.cachedSearches.clear();
        this.refreshList();
    }

    public void postIncrementalUpdate(long inventoryId, Int2ObjectMap<ItemStack> slots) {
        PatternContainerRecord record = this.byId.get(inventoryId);
        if (record == null) {
            LOG.warn("Ignoring incremental update for unknown inventory id {}", (Object)inventoryId);
            return;
        }
        AppEngInternalInventory inventory = record.getInventory();
        for (Int2ObjectMap.Entry entry : slots.int2ObjectEntrySet()) {
            inventory.setItemDirect(entry.getIntKey(), (ItemStack)entry.getValue());
        }
    }

    @Override
    public void updateBeforeRender() {
        this.showPatternProviders.set(((PatternAccessTermMenu)this.menu).getShownProviders());
    }

    private void refreshList() {
        this.byGroup.clear();
        String searchFilterLowerCase = this.searchField.getValue().toLowerCase();
        Set<Object> cachedSearch = this.getCacheForSearchTerm(searchFilterLowerCase);
        boolean rebuild = cachedSearch.isEmpty();
        for (PatternContainerRecord entry : this.byId.values()) {
            if (!rebuild && !cachedSearch.contains(entry)) continue;
            boolean found = searchFilterLowerCase.isEmpty();
            if (!found) {
                ItemStack itemStack;
                Iterator<Object> iterator = entry.getInventory().iterator();
                while (iterator.hasNext() && !(found = this.itemStackMatchesSearchTerm(itemStack = iterator.next(), searchFilterLowerCase))) {
                }
            }
            if (found || entry.getSearchName().contains(searchFilterLowerCase)) {
                this.byGroup.put((Object)entry.getGroup(), (Object)entry);
                cachedSearch.add(entry);
                continue;
            }
            cachedSearch.remove(entry);
        }
        this.groups.clear();
        this.groups.addAll(this.byGroup.keySet());
        this.groups.sort(GROUP_COMPARATOR);
        this.rows.clear();
        this.rows.ensureCapacity(this.getMaxRows());
        for (PatternContainerGroup group : this.groups) {
            this.rows.add(new GroupHeaderRow(group));
            ArrayList containers = new ArrayList(this.byGroup.get((Object)group));
            Collections.sort(containers);
            for (PatternContainerRecord container : containers) {
                AppEngInternalInventory inventory = container.getInventory();
                for (int offset = 0; offset < inventory.size(); offset += 9) {
                    int slots = Math.min(inventory.size() - offset, 9);
                    SlotsRow containerRow = new SlotsRow(container, offset, slots);
                    this.rows.add(containerRow);
                }
            }
        }
        this.resetScrollbar();
    }

    private void resetScrollbar() {
        this.scrollbar.setHeight(this.visibleRows * 18 - 2);
        this.scrollbar.setRange(0, this.rows.size() - this.visibleRows, 2);
    }

    private boolean itemStackMatchesSearchTerm(ItemStack itemStack, String searchTerm) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return this.patternSearchText.computeIfAbsent(itemStack, this::getPatternSearchText).contains(searchTerm);
    }

    private String getPatternSearchText(ItemStack stack) {
        Level level = ((PatternAccessTermMenu)this.menu).getPlayer().level();
        StringBuilder text = new StringBuilder();
        IPatternDetails pattern = PatternDetailsHelper.decodePattern(stack, level);
        if (pattern != null) {
            for (GenericStack output : pattern.getOutputs()) {
                output.what().getDisplayName().visit(content -> {
                    text.append(content.toLowerCase());
                    return Optional.empty();
                });
                text.append('\n');
            }
        }
        return text.toString();
    }

    private Set<Object> getCacheForSearchTerm(String searchTerm) {
        Set<Object> cache;
        if (!this.cachedSearches.containsKey(searchTerm)) {
            this.cachedSearches.put(searchTerm, new HashSet());
        }
        if ((cache = this.cachedSearches.get(searchTerm)).isEmpty() && searchTerm.length() > 1) {
            cache.addAll(this.getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
        }
        return cache;
    }

    private void reinitialize() {
        this.children().removeAll(this.renderables);
        this.renderables.clear();
        this.init();
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> btn, boolean backwards) {
        TerminalStyle next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        btn.set(next);
        this.reinitialize();
    }

    private int getMaxRows() {
        return this.groups.size() + this.byId.size();
    }

    private void blit(GuiGraphics guiGraphics, int offsetX, int offsetY, Rect2i srcRect) {
        ResourceLocation texture = AppEng.makeId("textures/guis/patternaccessterminal.png");
        guiGraphics.blit(texture, offsetX, offsetY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(), srcRect.getHeight());
    }

    protected int getVisibleRows() {
        return this.visibleRows;
    }

    static sealed interface Row
    permits GroupHeaderRow, SlotsRow {
    }

    record SlotsRow(PatternContainerRecord container, int offset, int slots) implements Row
    {
    }

    record GroupHeaderRow(PatternContainerGroup group) implements Row
    {
    }
}

