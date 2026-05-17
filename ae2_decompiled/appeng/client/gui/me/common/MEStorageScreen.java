/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.blaze3d.platform.InputConstants
 *  guideme.color.ColorValue
 *  guideme.color.ConstantColor
 *  guideme.document.LytRect
 *  guideme.render.SimpleRenderContext
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.client.gui.me.common;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.client.AEKeyRendering;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AmountFormat;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.ILinkStatus;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.Hotkey;
import appeng.client.Hotkeys;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.common.RepoSlot;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.KeyTypeSelectionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.serverbound.MEInteractionPacket;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.ItemListMod;
import appeng.items.storage.ViewCellItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import guideme.color.ColorValue;
import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.render.SimpleRenderContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MEStorageScreen<C extends MEStorageMenu>
extends AEBaseScreen<C>
implements ISortSource {
    private static final Logger LOG = LoggerFactory.getLogger(MEStorageScreen.class);
    private static final String TEXT_ID_ENTRIES_SHOWN = "entriesShown";
    private static final int MIN_ROWS = 2;
    private static String rememberedSearch = "";
    private final appeng.client.gui.style.TerminalStyle style;
    protected final Repo repo;
    private final List<ItemStack> currentViewCells = new ArrayList<ItemStack>();
    private final IConfigManager configSrc;
    private final boolean supportsViewCells;
    private TabButton craftingStatusBtn;
    private final AETextField searchField;
    private int rows = 0;
    private SettingToggleButton<ViewItems> viewModeToggle;
    private SettingToggleButton<SortOrder> sortByToggle;
    private final SettingToggleButton<SortDir> sortDirToggle;
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private final Scrollbar scrollbar;

    public MEStorageScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.style = style.getTerminalStyle();
        if (this.style == null) {
            throw new IllegalStateException("Cannot construct screen " + String.valueOf(this.getClass()) + " without a terminalStyles setting");
        }
        this.searchField = this.widgets.addTextField("search");
        this.searchField.setPlaceholder((Component)GuiText.SearchPlaceholder.text());
        this.scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.repo = new Repo(this.scrollbar, this);
        ((MEStorageMenu)menu).setClientRepo(this.repo);
        this.repo.setUpdateViewListener(this::updateScrollbar);
        this.updateScrollbar();
        this.searchField.setResponder(this::setSearchText);
        this.imageWidth = this.style.getScreenWidth();
        this.imageHeight = this.style.getScreenHeight(0);
        this.configSrc = ((IConfigurableObject)this.menu).getConfigManager();
        ((MEStorageMenu)this.menu).setGui(this::onMenuReceivedClientUpdate);
        List<Slot> viewCellSlots = ((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.VIEW_CELL);
        boolean bl = this.supportsViewCells = !viewCellSlots.isEmpty();
        if (this.supportsViewCells) {
            List<MutableComponent> tooltip = Collections.singletonList(GuiText.TerminalViewCellsTooltip.text());
            this.widgets.add("viewCells", new UpgradesPanel(viewCellSlots, () -> tooltip));
        }
        if (this.style.isSupportsAutoCrafting()) {
            this.craftingStatusBtn = new TabButton(Icon.CRAFT_HAMMER, (Component)GuiText.CraftingStatus.text(), btn -> this.showCraftingStatus());
            this.widgets.add("craftingStatus", (AbstractWidget)this.craftingStatusBtn);
        }
        if (this.style.isSortable()) {
            this.sortByToggle = this.addToLeftToolbar(new SettingToggleButton<SortOrder>(Settings.SORT_BY, this.getSortBy(), Platform::isSortOrderAvailable, this::toggleServerSetting));
        }
        if (this.style.isSupportsAutoCrafting()) {
            this.viewModeToggle = this.addToLeftToolbar(new SettingToggleButton<ViewItems>(Settings.VIEW_MODE, this.getSortDisplay(), this::toggleServerSetting));
        }
        if (((MEStorageMenu)this.menu).canConfigureTypeFilter()) {
            this.addToLeftToolbar(KeyTypeSelectionButton.create(this, ((MEStorageMenu)menu).getHost(), (Component)GuiText.ConfigureVisibleTypes.text()));
        }
        this.sortDirToggle = new SettingToggleButton<SortDir>(Settings.SORT_DIRECTION, this.getSortDir(), this::toggleServerSetting);
        this.addToLeftToolbar(this.sortDirToggle);
        this.addToLeftToolbar(new ActionButton(ActionItems.TERMINAL_SETTINGS, this::showSettings));
        TerminalStyle terminalStyle = this.config.getTerminalStyle();
        this.addToLeftToolbar(new SettingToggleButton<TerminalStyle>(Settings.TERMINAL_STYLE, terminalStyle, this::toggleTerminalStyle));
        this.widgets.add("upgrades", new UpgradesPanel(((AEBaseMenu)((Object)menu)).getSlots(SlotSemantics.UPGRADE), ((MEStorageMenu)menu).getHost()));
        if (((MEStorageMenu)menu).getToolbox().isPresent()) {
            this.widgets.add("toolbox", new ToolboxPanel(style, ((MEStorageMenu)menu).getToolbox().getName()));
        }
        if ((((AEBaseMenu)((Object)menu)).isReturnedFromSubScreen() || this.config.isRememberLastSearch()) && rememberedSearch != null && !rememberedSearch.isEmpty()) {
            this.searchField.setValue(rememberedSearch);
            this.searchField.selectAll();
            this.setSearchText(rememberedSearch);
        }
        if (!((AEBaseMenu)((Object)menu)).isReturnedFromSubScreen() && this.config.isUseExternalSearch() && this.config.isClearExternalSearchOnOpen()) {
            ItemListMod.setSearchText("");
        }
    }

    private void showSettings() {
        this.switchToScreen(new TerminalSettingsScreen(this));
    }

    @Nullable
    protected IPartitionList createPartitionList(List<ItemStack> viewCells) {
        return ViewCellItem.createFilter(AEKeyFilter.none(), viewCells);
    }

    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry entry, int mouseButton, ClickType clickType) {
        EmptyingAction emptyingAction;
        if (entry != null) {
            AELog.debug("Clicked on grid inventory entry serial=%s, key=%s", entry.getSerial(), entry.getWhat());
        }
        if (mouseButton == 0 && entry != null && ContainerItemStrategies.isKeySupported(entry.getWhat())) {
            InventoryAction action = clickType != ClickType.QUICK_MOVE ? InventoryAction.FILL_ITEM : (((MEStorageMenu)this.menu).getCarried().isEmpty() ? InventoryAction.FILL_ENTIRE_ITEM_MOVE_TO_PLAYER : InventoryAction.FILL_ENTIRE_ITEM);
            ((MEStorageMenu)this.menu).handleInteraction(entry.getSerial(), action);
            return;
        }
        if (mouseButton == 1 && !((MEStorageMenu)this.menu).getCarried().isEmpty() && (emptyingAction = ContainerItemStrategies.getEmptyingAction(((MEStorageMenu)this.menu).getCarried())) != null && ((MEStorageMenu)this.menu).isKeyVisible(emptyingAction.what())) {
            ((MEStorageMenu)this.menu).handleInteraction(-1L, clickType == ClickType.QUICK_MOVE ? InventoryAction.EMPTY_ENTIRE_ITEM : InventoryAction.EMPTY_ITEM);
            return;
        }
        if (entry == null) {
            if (clickType == ClickType.PICKUP && !((MEStorageMenu)this.getMenu()).getCarried().isEmpty()) {
                InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                ((MEStorageMenu)this.menu).handleInteraction(-1L, action);
            }
            return;
        }
        long serial = entry.getSerial();
        if (InputConstants.isKeyDown((long)Minecraft.getInstance().getWindow().getWindow(), (int)32)) {
            ((MEStorageMenu)this.menu).handleInteraction(serial, InventoryAction.MOVE_REGION);
        } else {
            InventoryAction action = null;
            switch (clickType) {
                case PICKUP: {
                    InventoryAction inventoryAction = action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    if (action != InventoryAction.PICKUP_OR_SET_DOWN || !this.shouldCraftOnClick(entry) || !((MEStorageMenu)this.getMenu()).getCarried().isEmpty()) break;
                    ((MEStorageMenu)this.menu).handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                    return;
                }
                case QUICK_MOVE: {
                    action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;
                }
                case CLONE: {
                    if (entry.isCraftable()) {
                        ((MEStorageMenu)this.menu).handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    }
                    if (!((MEStorageMenu)this.getMenu()).getPlayer().getAbilities().instabuild) break;
                    action = InventoryAction.CREATIVE_DUPLICATE;
                    break;
                }
            }
            if (action != null) {
                ((MEStorageMenu)this.menu).handleInteraction(serial, action);
            }
        }
    }

    private boolean shouldCraftOnClick(GridInventoryEntry entry) {
        if (this.isViewOnlyCraftable()) {
            return true;
        }
        return entry.getStoredAmount() == 0L && entry.isCraftable();
    }

    private void updateScrollbar() {
        this.scrollbar.setHeight(this.rows * this.style.getRow().getSrcHeight() - 2);
        int totalRows = (this.repo.size() + this.getSlotsPerRow() - 1) / this.getSlotsPerRow();
        if (this.repo.hasPinnedRow()) {
            ++totalRows;
        }
        this.scrollbar.setRange(0, totalRows - this.rows, Math.max(1, this.rows / 6));
    }

    private void showCraftingStatus() {
        SwitchGuisPacket message = SwitchGuisPacket.openSubMenu(CraftingStatusMenu.TYPE);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private int getSlotsPerRow() {
        return this.style.getSlotsPerRow();
    }

    @Override
    public void init() {
        int availableHeight = this.height - 2 * AEConfig.instance().getTerminalMargin();
        this.rows = Math.max(2, this.config.getTerminalStyle().getRows(this.style.getPossibleRows(availableHeight)));
        this.imageHeight = this.style.getScreenHeight(this.rows);
        NonNullList slots = ((MEStorageMenu)this.menu).slots;
        slots.removeIf(slot -> slot instanceof RepoSlot);
        int repoIndex = 0;
        for (int row = 0; row < this.rows; ++row) {
            for (int col = 0; col < this.style.getSlotsPerRow(); ++col) {
                Point pos = this.style.getSlotPos(row, col);
                slots.add(new RepoSlot(this.repo, repoIndex++, pos.getX(), pos.getY()));
            }
        }
        super.init();
        if (this.shouldAutoFocus()) {
            this.setInitialFocus((GuiEventListener)this.searchField);
        }
        this.updateScrollbar();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.repo.setPaused(MEStorageScreen.hasShiftDown());
        this.updateSearch();
        if (!this.title.getString().isEmpty()) {
            this.setTextContent("dialog_title", this.title);
        } else if (((MEStorageMenu)this.menu).getTarget() instanceof IMEChest) {
            this.setTextContent("dialog_title", (Component)GuiText.MEChest.text());
        }
    }

    private void updateSearch() {
        if (this.config.isUseExternalSearch()) {
            int visibleEntries;
            int allEntries;
            this.searchField.setVisible(false);
            String externalSearchText = ItemListMod.getSearchText();
            if (!Objects.equals(this.repo.getSearchString(), externalSearchText)) {
                this.setSearchText(externalSearchText);
            }
            if ((allEntries = this.repo.getAllEntries().size()) != (visibleEntries = this.repo.size())) {
                this.setTextHidden(TEXT_ID_ENTRIES_SHOWN, false);
                this.setTextContent(TEXT_ID_ENTRIES_SHOWN, (Component)GuiText.ShowingOf.text(visibleEntries, allEntries));
            } else {
                this.setTextHidden(TEXT_ID_ENTRIES_SHOWN, true);
            }
        } else {
            this.searchField.setVisible(true);
            this.setTextHidden(TEXT_ID_ENTRIES_SHOWN, true);
            this.searchField.setTooltipMessage(List.of(GuiText.SearchTooltip.text(), GuiText.SearchTooltipModId.text(), GuiText.SearchTooltipTag.text(), GuiText.SearchTooltipToolTips.text(), GuiText.SearchTooltipItemId.text()));
            if (this.config.isSyncWithExternalSearch()) {
                String externalSearchText;
                if (this.searchField.isFocused()) {
                    String text = this.searchField.getValue();
                    ItemListMod.setSearchText(text);
                } else if (ItemListMod.hasSearchFocus() && !Objects.equals(externalSearchText = ItemListMod.getSearchText(), this.searchField.getValue())) {
                    this.searchField.setValue(externalSearchText);
                }
            }
        }
    }

    @Override
    protected <P extends AEBaseScreen<C>> void onReturnFromSubScreen(AESubScreen<C, P> subScreen) {
        if (subScreen instanceof TerminalSettingsScreen) {
            this.reinitalize();
            if (!this.config.isUseExternalSearch()) {
                this.setSearchText(this.searchField.getValue());
            }
        }
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;
        if (this.repo.hasPinnedRow()) {
            this.renderPinnedRowDecorations(guiGraphics);
        }
        if (this.craftingStatusBtn != null && ((MEStorageMenu)this.menu).activeCraftingJobs != -1) {
            int x = this.craftingStatusBtn.getX() + (this.craftingStatusBtn.getWidth() - 18) / 2;
            int y = this.craftingStatusBtn.getY() + (this.craftingStatusBtn.getHeight() - 18) / 2;
            StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, x - this.leftPos, y - this.topPos, String.valueOf(((MEStorageMenu)this.menu).activeCraftingJobs));
        }
        this.renderLinkStatus(guiGraphics, ((MEStorageMenu)this.getMenu()).getLinkStatus());
    }

    private void renderLinkStatus(GuiGraphics guiGraphics, ILinkStatus linkStatus) {
        if (!linkStatus.connected()) {
            SimpleRenderContext renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);
            Point firstSlot = this.style.getSlotPos(0, 0);
            Point lastSlot = this.style.getSlotPos(this.rows - 1, this.style.getSlotsPerRow() - 1);
            LytRect rect = new LytRect(firstSlot.getX() - 1, firstSlot.getY() - 1, lastSlot.getX() + 17 - (firstSlot.getX() - 1), lastSlot.getY() + 17 - (firstSlot.getY() - 1));
            renderContext.fillRect(rect, (ColorValue)new ConstantColor(0x3F000000));
            Component statusDescription = linkStatus.statusDescription();
            if (statusDescription != null) {
                renderContext.renderTextCenteredIn(statusDescription.getString(), ERROR_TEXT_STYLE, rect);
            }
        }
    }

    private void renderPinnedRowDecorations(GuiGraphics guiGraphics) {
        for (Slot slot : ((MEStorageMenu)this.menu).slots) {
            RepoSlot repoSlot;
            GridInventoryEntry entry;
            if (!(slot instanceof RepoSlot) || (entry = (repoSlot = (RepoSlot)slot).getEntry()) == null || !PendingCraftingJobs.hasPendingJob(entry.getWhat())) continue;
            TextureAtlasSprite sprite = (TextureAtlasSprite)this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(AppEng.makeId("block/molecular_assembler_lights"));
            Blitter.sprite(sprite).src(sprite.getX() + 2, sprite.getY() + 2, sprite.contents().width() - 4, sprite.contents().height() - 4).dest(slot.x - 1, slot.y - 1, 18, 18).blit(guiGraphics);
        }
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        RepoSlot repoSlot;
        Slot slot;
        if (this.searchField.isMouseOver(xCoord, yCoord) && btn == 1) {
            this.searchField.setValue("");
            this.setSearchText("");
        }
        if (Minecraft.getInstance().options.keyPickItem.matchesMouse(btn) && (slot = this.findSlot(xCoord, yCoord)) instanceof RepoSlot && (repoSlot = (RepoSlot)slot).isCraftable()) {
            this.handleGridInventoryEntryMouseClick(repoSlot.getEntry(), btn, ClickType.CLONE);
            return true;
        }
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double deltaX, double deltaY) {
        Slot slot;
        if (deltaY != 0.0 && MEStorageScreen.hasShiftDown() && (slot = this.findSlot(x, y)) instanceof RepoSlot) {
            RepoSlot repoSlot = (RepoSlot)slot;
            GridInventoryEntry entry = repoSlot.getEntry();
            long serial = entry != null ? entry.getSerial() : -1L;
            InventoryAction direction = deltaY > 0.0 ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
            int times = (int)Math.abs(deltaY);
            for (int h = 0; h < times; ++h) {
                MEInteractionPacket p = new MEInteractionPacket(((MEStorageMenu)this.menu).containerId, serial, direction);
                PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return true;
        }
        return super.mouseScrolled(x, y, deltaX, deltaY);
    }

    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof RepoSlot) {
            RepoSlot repoSlot = (RepoSlot)slot;
            this.handleGridInventoryEntryMouseClick(repoSlot.getEntry(), mouseButton, clickType);
            return;
        }
        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    public void removed() {
        super.removed();
        this.storeState();
        for (GridInventoryEntry entry : this.repo.getPinnedEntries()) {
            PinnedKeys.PinInfo info = PinnedKeys.getPinInfo(entry.getWhat());
            if (info == null || info.reason != PinnedKeys.PinReason.CRAFTING || PendingCraftingJobs.hasPendingJob(entry.getWhat())) continue;
            info.canPrune = true;
        }
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.style.getHeader().dest(offsetX, offsetY).blit(guiGraphics);
        int y = offsetY;
        this.style.getHeader().dest(offsetX, y).blit(guiGraphics);
        y += this.style.getHeader().getSrcHeight();
        int rowsToDraw = Math.max(2, this.rows);
        for (int x = 0; x < rowsToDraw; ++x) {
            Blitter row = this.style.getRow();
            if (x == 0) {
                row = this.style.getFirstRow();
            } else if (x + 1 == rowsToDraw) {
                row = this.style.getLastRow();
            }
            row.dest(offsetX, y).blit(guiGraphics);
            y += this.style.getRow().getSrcHeight();
        }
        this.style.getBottom().dest(offsetX, y).blit(guiGraphics);
        if (this.repo.hasPinnedRow()) {
            Blitter.texture("guis/terminal.png").src(0, 204, 162, 18).dest(offsetX + 7, offsetY + this.style.getHeader().getSrcHeight()).blit(guiGraphics);
        }
        if (this.searchField != null) {
            this.searchField.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot s) {
        if (s instanceof RepoSlot) {
            GridInventoryEntry entry;
            RepoSlot repoSlot = (RepoSlot)s;
            if (((MEStorageMenu)this.menu).getLinkStatus().connected() && (entry = repoSlot.getEntry()) != null) {
                try {
                    AEKeyRendering.drawInGui(this.minecraft, guiGraphics, s.x, s.y, entry.getWhat());
                }
                catch (Exception err) {
                    AELog.warn("[AppEng] AE prevented crash while drawing slot: " + String.valueOf(err), new Object[0]);
                }
                long storedAmount = entry.getStoredAmount();
                boolean craftable = entry.isCraftable();
                boolean useLargeFonts = this.config.isUseLargeFonts();
                if (craftable && (this.isViewOnlyCraftable() || storedAmount <= 0L)) {
                    StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, s.x, s.y, "+");
                } else {
                    AmountFormat format = useLargeFonts ? AmountFormat.SLOT_LARGE_FONT : AmountFormat.SLOT;
                    String text = entry.getWhat().formatAmount(storedAmount, format);
                    StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, (float)s.x, (float)s.y, text, useLargeFonts);
                    if (craftable) {
                        StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, (float)(s.x - 11), (float)(s.y - 11), "+", false);
                    }
                }
            }
            return;
        }
        super.renderSlot(guiGraphics, s);
    }

    protected final boolean isViewOnlyCraftable() {
        return this.viewModeToggle != null && this.viewModeToggle.getCurrentValue() == ViewItems.CRAFTABLE;
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        Slot slot = this.hoveredSlot;
        if (slot instanceof RepoSlot) {
            GridInventoryEntry entry;
            EmptyingAction emptyingAction;
            RepoSlot repoSlot = (RepoSlot)slot;
            ItemStack carried = ((MEStorageMenu)this.menu).getCarried();
            if (!carried.isEmpty() && (emptyingAction = ContainerItemStrategies.getEmptyingAction(carried)) != null && ((MEStorageMenu)this.menu).isKeyVisible(emptyingAction.what())) {
                this.drawTooltip(guiGraphics, x, y, Tooltips.getEmptyingTooltip(ButtonToolTips.StoreAction, carried, emptyingAction));
                return;
            }
            if (carried.isEmpty() && (entry = repoSlot.getEntry()) != null) {
                this.renderGridInventoryEntryTooltip(guiGraphics, entry, x, y);
            }
            return;
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    protected void renderGridInventoryEntryTooltip(GuiGraphics guiGraphics, GridInventoryEntry entry, int x, int y) {
        AEKey aEKey;
        long requestableAmount;
        List<Component> currentToolTip = AEKeyRendering.getTooltip(entry.getWhat());
        if (Tooltips.shouldShowAmountTooltip(entry.getWhat(), entry.getStoredAmount())) {
            currentToolTip.add(Tooltips.getAmountTooltip(ButtonToolTips.StoredAmount, entry.getWhat(), entry.getStoredAmount()));
        }
        if ((requestableAmount = entry.getRequestableAmount()) > 0L) {
            String formattedAmount = entry.getWhat().formatAmount(requestableAmount, AmountFormat.FULL);
            currentToolTip.add((Component)ButtonToolTips.RequestableAmount.text(formattedAmount));
        }
        if (entry.isCraftable() && !this.isViewOnlyCraftable() && entry.getStoredAmount() > 0L) {
            currentToolTip.add((Component)ButtonToolTips.Craftable.text().copy().withStyle(ChatFormatting.DARK_GRAY));
        }
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            currentToolTip.add((Component)ButtonToolTips.Serial.text(entry.getSerial()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if ((aEKey = entry.getWhat()) instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)aEKey;
            ItemStack stack = itemKey.getReadOnlyStack();
            guiGraphics.renderTooltip(this.font, currentToolTip, stack.getTooltipImage(), stack, x, y);
        } else {
            guiGraphics.renderComponentTooltip(this.font, currentToolTip, x, y);
        }
    }

    public boolean charTyped(char character, int modifiers) {
        if (character == ' ' && this.searchField.getValue().isEmpty()) {
            return true;
        }
        return super.charTyped(character, modifiers);
    }

    private boolean shouldAutoFocus() {
        return this.config.isAutoFocusSearch() && !this.config.isUseExternalSearch();
    }

    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (this.searchField.isFocused() && keyCode == 257) {
            this.searchField.setFocused(false);
            this.setFocused(null);
            return true;
        }
        if (!this.searchField.isFocused() && this.isCloseHotkey(keyCode, scanCode)) {
            this.getPlayer().closeContainer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private boolean isHovered() {
        return this.isHovering(0, 0, this.imageWidth, this.imageHeight, this.currentMouseX, this.currentMouseY);
    }

    @Override
    public void containerTick() {
        List<ItemStack> viewCells;
        this.repo.setEnabled(((MEStorageMenu)this.menu).getLinkStatus().connected());
        if (this.supportsViewCells && !this.currentViewCells.equals(viewCells = ((MEStorageMenu)this.menu).getViewCells())) {
            this.currentViewCells.clear();
            this.currentViewCells.addAll(viewCells);
            this.repo.setPartitionList(this.createPartitionList(viewCells));
        }
        super.containerTick();
    }

    @Override
    public SortOrder getSortBy() {
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public SortDir getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public ViewItems getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public Set<AEKeyType> getSortKeyTypes() {
        return ((MEStorageMenu)this.menu).canConfigureTypeFilter() ? new HashSet<AEKeyType>(((MEStorageMenu)this.menu).searchKeyTypes.enabledSet()) : Sets.newHashSet(AEKeyTypes.getAll());
    }

    public void onMenuReceivedClientUpdate() {
        if (this.sortByToggle != null) {
            this.sortByToggle.set(this.getSortBy());
        }
        if (this.sortDirToggle != null) {
            this.sortDirToggle.set(this.getSortDir());
        }
        if (this.viewModeToggle != null) {
            this.viewModeToggle.set(this.getSortDisplay());
        }
        this.repo.updateView();
    }

    protected int getVisibleRows() {
        return this.rows;
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> btn, boolean backwards) {
        TerminalStyle next = btn.getNextValue(backwards);
        this.config.setTerminalStyle(next);
        btn.set(next);
        this.reinitalize();
    }

    private <SE extends Enum<SE>> void toggleServerSetting(SettingToggleButton<SE> btn, boolean backwards) {
        SE next = btn.getNextValue(backwards);
        ConfigValuePacket message = new ConfigValuePacket(btn.getSetting(), next);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
        btn.set(next);
    }

    private void setSearchText(String text) {
        this.repo.setSearchString(text);
        this.repo.updateView();
        this.updateScrollbar();
    }

    private void reinitalize() {
        this.storeState();
        new ArrayList<GuiEventListener>(this.children()).forEach(x$0 -> this.removeWidget((GuiEventListener)x$0));
        this.init();
    }

    private boolean isCloseHotkey(int keyCode, int scanCode) {
        String hotkeyId = ((MEStorageMenu)this.getMenu()).getHost().getCloseHotkey();
        if (hotkeyId != null) {
            Hotkey hotkey = Hotkeys.getHotkeyMapping(hotkeyId);
            if (hotkey != null) {
                return hotkey.mapping().matches(keyCode, scanCode);
            }
            LOG.warn("Terminal host returned unknown hotkey id: {}", (Object)hotkeyId);
        }
        return false;
    }

    public void storeState() {
        rememberedSearch = this.searchField.getValue();
    }
}

