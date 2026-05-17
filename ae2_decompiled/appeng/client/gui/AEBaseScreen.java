/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.blaze3d.platform.InputConstants
 *  guideme.Guide
 *  guideme.GuidesCommon
 *  guideme.PageAnchor
 *  guideme.color.ColorValue
 *  guideme.color.SymbolicColor
 *  guideme.compiler.IdUtils
 *  guideme.document.DefaultStyles
 *  guideme.indices.ItemIndex
 *  guideme.style.ResolvedTextStyle
 *  guideme.style.TextStyle
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.ComponentPath
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.ComponentRenderUtils
 *  net.minecraft.client.gui.components.Renderable
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.client.gui;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPart;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.TextOverride;
import appeng.client.gui.Tooltip;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.BackgroundGenerator;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.GeneratedBackground;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.Text;
import appeng.client.gui.style.TextAlignment;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.ITickingWidget;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.OpenGuideButton;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEngClient;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.core.network.serverbound.SwapSlotsPacket;
import appeng.helpers.InventoryAction;
import appeng.items.tools.GuideItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.DisabledSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.IOptionalSlot;
import appeng.menu.slot.ResizableSlot;
import appeng.util.ConfigMenuInventory;
import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.InputConstants;
import guideme.Guide;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import guideme.color.ColorValue;
import guideme.color.SymbolicColor;
import guideme.compiler.IdUtils;
import guideme.document.DefaultStyles;
import guideme.indices.ItemIndex;
import guideme.style.ResolvedTextStyle;
import guideme.style.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AEBaseScreen<T extends AEBaseMenu>
extends AbstractContainerScreen<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AEBaseScreen.class);
    private static final Point HIDDEN_SLOT_POS = new Point(-9999, -9999);
    public static final String TEXT_ID_DIALOG_TITLE = "dialog_title";
    protected static final ResolvedTextStyle ERROR_TEXT_STYLE = TextStyle.builder().color((ColorValue)SymbolicColor.ERROR_TEXT).font(Minecraft.DEFAULT_FONT).dropShadow(Boolean.valueOf(true)).build().mergeWith(DefaultStyles.BASE_STYLE);
    private final VerticalButtonBar verticalToolbar;
    private final OpenGuideButton helpButton;
    private final Set<Slot> drag_click = new HashSet<Slot>();
    private boolean disableShiftClick = false;
    private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
    private ItemStack dbl_whichItem = ItemStack.EMPTY;
    private Slot bl_clicked;
    private boolean handlingRightClick;
    private final Map<String, TextOverride> textOverrides = new HashMap<String, TextOverride>();
    private final Set<SlotSemantic> hiddenSlots = new HashSet<SlotSemantic>();
    protected final WidgetContainer widgets;
    protected final ScreenStyle style;
    protected final AEConfig config = AEConfig.instance();
    private final List<SavedSlotInfo> savedSlotInfos = new ArrayList<SavedSlotInfo>();
    private boolean focusChangedToSomething = false;

    public AEBaseScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title);
        this.font = Minecraft.getInstance().font;
        this.style = Objects.requireNonNull(style, "style");
        this.widgets = new WidgetContainer(style);
        this.verticalToolbar = new VerticalButtonBar();
        if (this.shouldAddToolbar()) {
            this.widgets.add("verticalToolbar", this.verticalToolbar);
        }
        this.helpButton = this.addToLeftToolbar(new OpenGuideButton(btn -> this.openHelp()));
        if (style.getGeneratedBackground() != null) {
            this.imageWidth = style.getGeneratedBackground().getWidth();
            this.imageHeight = style.getGeneratedBackground().getHeight();
        } else if (style.getBackground() != null) {
            this.imageWidth = style.getBackground().getSrcWidth();
            this.imageHeight = style.getBackground().getSrcHeight();
        }
    }

    @MustBeInvokedByOverriders
    protected void init() {
        super.init();
        this.positionSlots();
        this.widgets.populateScreen(x$0 -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget((GuiEventListener)x$0);
        }, this.getBounds(true), this);
    }

    protected boolean shouldAddToolbar() {
        return true;
    }

    private void positionSlots() {
        for (Map.Entry<String, SlotPosition> entry : this.style.getSlots().entrySet()) {
            SlotSemantic semantic = SlotSemantics.getOrThrow(entry.getKey());
            if (this.hiddenSlots.contains(semantic)) continue;
            this.repositionSlots(semantic);
        }
    }

    private Point getSlotPosition(SlotPosition position, int semanticIndex) {
        Point pos = position.resolve(this.getBounds(false));
        SlotGridLayout grid = position.getGrid();
        if (grid != null) {
            pos = grid.getPosition(pos.getX(), pos.getY(), semanticIndex);
        }
        return pos;
    }

    public final void repositionSlots(SlotSemantic semantic) {
        SlotPosition position = this.style.getSlots().get(semantic.id());
        if (position.isHidden()) {
            ((AEBaseMenu)this.menu).hideSlot(semantic.id());
            this.setSlotsHidden(semantic, true);
            return;
        }
        List<Slot> slots = ((AEBaseMenu)this.menu).getSlots(semantic);
        for (int i = 0; i < slots.size(); ++i) {
            Slot slot = slots.get(i);
            if (slot instanceof ResizableSlot) {
                ResizableSlot resizableSlot = (ResizableSlot)slot;
                WidgetStyle widgetStyle = this.style.getWidget(resizableSlot.getStyleId());
                Point pos = widgetStyle.resolve(this.getBounds(false));
                slot.x = pos.getX();
                slot.y = pos.getY();
                resizableSlot.setWidth(widgetStyle.getWidth());
                resizableSlot.setHeight(widgetStyle.getHeight());
                continue;
            }
            Point pos = this.getSlotPosition(position, i);
            slot.x = pos.getX();
            slot.y = pos.getY();
        }
    }

    private Rect2i getBounds(boolean absolute) {
        if (absolute) {
            return new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        }
        return new Rect2i(0, 0, this.imageWidth, this.imageHeight);
    }

    private List<Slot> getInventorySlots() {
        return ((AEBaseMenu)this.menu).slots;
    }

    @MustBeInvokedByOverriders
    protected void updateBeforeRender() {
        this.helpButton.setVisibility(this.getHelpTopic() != null);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.updateBeforeRender();
        this.widgets.updateBeforeRender();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltips(guiGraphics, mouseX, mouseY);
        if (AEConfig.instance().isShowDebugGuiOverlays()) {
            List<Rect2i> exclusionZones = this.getExclusionZones();
            for (Rect2i rectangle2d : exclusionZones) {
                this.fillRect(guiGraphics, rectangle2d, 0x7F00FF00);
            }
            guiGraphics.hLine(this.leftPos, this.leftPos + this.imageWidth - 1, this.topPos, -1);
            guiGraphics.hLine(this.leftPos, this.leftPos + this.imageWidth - 1, this.topPos + this.imageHeight - 1, -1);
            guiGraphics.vLine(this.leftPos, this.topPos, this.topPos + this.imageHeight, -1);
            guiGraphics.vLine(this.leftPos + this.imageWidth - 1, this.topPos, this.topPos + this.imageHeight - 1, -1);
        }
    }

    protected EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
        ItemStack wrappedStack;
        AppEngSlot appEngSlot;
        block6: {
            block5: {
                if (!(slot instanceof AppEngSlot)) break block5;
                appEngSlot = (AppEngSlot)slot;
                if (!carried.isEmpty()) break block6;
            }
            return null;
        }
        InternalInventory internalInventory = appEngSlot.getInventory();
        if (!(internalInventory instanceof ConfigMenuInventory)) {
            return null;
        }
        ConfigMenuInventory configInv = (ConfigMenuInventory)internalInventory;
        EmptyingAction emptyingAction = ContainerItemStrategies.getEmptyingAction(carried);
        if (emptyingAction != null && configInv.isItemValid(slot.slot, wrappedStack = GenericStack.wrapInItemStack(new GenericStack(emptyingAction.what(), 1L)))) {
            return emptyingAction;
        }
        return null;
    }

    private boolean renderEmptyingTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        EmptyingAction emptyingAction = this.getEmptyingAction(this.hoveredSlot, ((AEBaseMenu)this.menu).getCarried());
        if (emptyingAction != null) {
            this.drawTooltip(guiGraphics, mouseX, mouseY, Tooltips.getEmptyingTooltip(ButtonToolTips.SetAction, ((AEBaseMenu)this.menu).getCarried(), emptyingAction));
            return true;
        }
        return false;
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Object appEngSlot;
        List<Component> customTooltip;
        if (this.renderEmptyingTooltip(guiGraphics, mouseX, mouseY)) {
            return;
        }
        Slot slot = this.hoveredSlot;
        if (slot instanceof AppEngSlot && (customTooltip = ((AppEngSlot)((Object)(appEngSlot = (AppEngSlot)slot))).getCustomTooltip(((AEBaseMenu)this.menu).getCarried())) != null) {
            this.drawTooltip(guiGraphics, mouseX, mouseY, customTooltip);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            return;
        }
        for (Renderable c : this.renderables) {
            Tooltip tooltip;
            Rect2i area;
            ITooltip tooltipWidget;
            if (!(c instanceof ITooltip) || !(tooltipWidget = (ITooltip)c).isTooltipAreaVisible() || mouseX < (area = tooltipWidget.getTooltipArea()).getX() || mouseY < area.getY() || mouseX >= area.getX() + area.getWidth() || mouseY >= area.getY() + area.getHeight() || (tooltip = new Tooltip(tooltipWidget.getTooltipMessage())).getContent().isEmpty()) continue;
            this.drawTooltipWithHeader(guiGraphics, tooltip, mouseX, mouseY);
        }
        Tooltip tooltip = this.widgets.getTooltip(mouseX - this.leftPos, mouseY - this.topPos);
        if (tooltip != null) {
            this.drawTooltipWithHeader(guiGraphics, tooltip, mouseX, mouseY);
        }
    }

    private void drawTooltipWithHeader(GuiGraphics guiGraphics, Tooltip tooltip, int mouseX, int mouseY) {
        this.drawTooltipWithHeader(guiGraphics, mouseX, mouseY, tooltip.getContent());
    }

    public void drawTooltip(GuiGraphics guiGraphics, int x, int y, List<Component> lines) {
        if (lines.isEmpty()) {
            return;
        }
        int maxWidth = this.width / 2 - 40;
        ArrayList styledLines = new ArrayList(lines.size());
        for (Component line : lines) {
            styledLines.addAll(ComponentRenderUtils.wrapComponents((FormattedText)line, (int)maxWidth, (Font)this.font));
        }
        guiGraphics.renderTooltip(this.font, styledLines, x, y);
    }

    public void drawTooltipWithHeader(GuiGraphics guiGraphics, int x, int y, List<Component> lines) {
        if (lines.isEmpty()) {
            return;
        }
        ArrayList<Component> formattedLines = new ArrayList<Component>(lines.size());
        for (int i = 0; i < lines.size(); ++i) {
            if (i == 0) {
                formattedLines.add((Component)lines.get(i).copy().withStyle(s -> s.withColor(ChatFormatting.WHITE)));
                continue;
            }
            formattedLines.add((Component)lines.get(i).copy().withStyle(s -> {
                if (s.getColor() != null) {
                    return s;
                }
                return s.withColor(ChatFormatting.GRAY);
            }));
        }
        this.drawTooltip(guiGraphics, x, y, formattedLines);
    }

    protected final void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        int ox = this.leftPos;
        int oy = this.topPos;
        this.widgets.drawForegroundLayer(guiGraphics, this.getBounds(false), new Point(x - ox, y - oy));
        this.drawFG(guiGraphics, ox, oy, x, y);
        if (this.style != null) {
            for (Map.Entry<String, Text> entry : this.style.getText().entrySet()) {
                TextOverride override = this.textOverrides.get(entry.getKey());
                this.drawText(guiGraphics, entry.getValue(), override);
            }
        }
    }

    private void drawText(GuiGraphics guiGraphics, Text text, @Nullable TextOverride override) {
        List lines;
        if (override != null && override.isHidden()) {
            return;
        }
        int color = this.style.getColor(text.getColor()).toARGB();
        Point pos = text.getPosition().resolve(this.getBounds(false));
        float scale = text.getScale();
        Component content = text.getText();
        if (override != null && override.getContent() != null) {
            content = override.getContent().copy().withStyle(content.getStyle());
        }
        if (text.getMaxWidth() <= 0) {
            FormattedCharSequence line = content.getVisualOrderText();
            lines = List.of(line);
        } else {
            lines = this.font.split((FormattedText)content, text.getMaxWidth());
        }
        int y = pos.getY();
        for (FormattedCharSequence line : lines) {
            int lineWidth = this.font.width(line);
            int x = pos.getX();
            if (text.getAlign() == TextAlignment.CENTER) {
                textWidth = Math.round((float)lineWidth * scale);
                x -= textWidth / 2;
            } else if (text.getAlign() == TextAlignment.RIGHT) {
                textWidth = Math.round((float)lineWidth * scale);
                x -= textWidth;
            }
            if (text.getScale() == 1.0f) {
                guiGraphics.drawString(this.font, line, x, y, color, false);
            } else {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float)x, (float)y, 1.0f);
                guiGraphics.pose().scale(scale, scale, 1.0f);
                guiGraphics.drawString(this.font, line, 0, 0, color, false);
                guiGraphics.pose().popPose();
            }
            float f = y;
            float f2 = text.getScale();
            Objects.requireNonNull(this.font);
            y = (int)(f + f2 * 9.0f);
        }
    }

    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    protected final void renderBg(GuiGraphics guiGraphics, float f, int x, int y) {
        this.drawBG(guiGraphics, this.leftPos, this.topPos, x, y, f);
        this.widgets.drawBackgroundLayer(guiGraphics, this.getBounds(true), new Point(x - this.leftPos, y - this.topPos));
        for (Slot slot : this.getInventorySlots()) {
            if (!(slot instanceof IOptionalSlot)) continue;
            this.drawOptionalSlotBackground(guiGraphics, (IOptionalSlot)slot, false);
        }
    }

    private void drawOptionalSlotBackground(GuiGraphics guiGraphics, IOptionalSlot slot, boolean alwaysDraw) {
        if (alwaysDraw || slot.isRenderDisabled()) {
            float alpha = slot.isSlotEnabled() ? 1.0f : 0.2f;
            Point pos = slot.getBackgroundPos();
            Icon.SLOT_BACKGROUND.getBlitter().dest(this.leftPos + pos.getX(), this.topPos + pos.getY()).color(1.0f, 1.0f, 1.0f, alpha).blit(guiGraphics);
        }
    }

    private Point getMousePoint(double x, double y) {
        return new Point((int)Math.round(x - (double)this.leftPos), (int)Math.round(y - (double)this.topPos));
    }

    public void setFocused(@Nullable GuiEventListener listener) {
        if (listener != null) {
            this.focusChangedToSomething = true;
        }
        super.setFocused(listener);
    }

    public boolean mouseScrolled(double x, double y, double deltaX, double deltaY) {
        return deltaY != 0.0 && this.widgets.onMouseWheel(this.getMousePoint(x, y), deltaY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        ComponentPath currentFocus;
        this.drag_click.clear();
        if (btn == 1) {
            this.handlingRightClick = true;
            try {
                for (GuiEventListener widget : this.children()) {
                    if (!widget.isMouseOver(xCoord, yCoord)) continue;
                    boolean bl = super.mouseClicked(xCoord, yCoord, 0);
                    return bl;
                }
            }
            finally {
                this.handlingRightClick = false;
            }
        }
        if (this.widgets.onMouseDown(this.getMousePoint(xCoord, yCoord), btn)) {
            return true;
        }
        this.focusChangedToSomething = false;
        boolean result = super.mouseClicked(xCoord, yCoord, btn);
        if (!this.focusChangedToSomething && (currentFocus = this.getCurrentFocusPath()) != null) {
            currentFocus.applyFocus(false);
        }
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.widgets.onMouseUp(this.getMousePoint(mouseX, mouseY), button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        Slot slot = this.findSlot(mouseX, mouseY);
        ItemStack itemstack = ((AEBaseMenu)this.getMenu()).getCarried();
        Point mousePos = new Point((int)Math.round(mouseX - (double)this.leftPos), (int)Math.round(mouseY - (double)this.topPos));
        if (this.widgets.onMouseDrag(mousePos, mouseButton)) {
            return true;
        }
        if (slot instanceof FakeSlot && !itemstack.isEmpty()) {
            this.drag_click.add(slot);
            if (this.drag_click.size() > 1) {
                for (Slot dr : this.drag_click) {
                    InventoryActionPacket p = new InventoryActionPacket(mouseButton == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.index, 0L);
                    PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    protected void slotClicked(@Nullable Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (((AEBaseMenu)this.getMenu()).isClientSideSlot(slot)) {
            return;
        }
        if (slot instanceof DisabledSlot) {
            return;
        }
        if (clickType == ClickType.CLONE && slot != null && GenericStack.isWrapped(slot.getItem())) {
            return;
        }
        if (this.drag_click.size() <= 1 && mouseButton == 1 && this.getEmptyingAction(slot, ((AEBaseMenu)this.menu).getCarried()) != null) {
            InventoryActionPacket p = new InventoryActionPacket(InventoryAction.EMPTY_ITEM, slotIdx, 0L);
            PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (slot instanceof FakeSlot) {
            if (this.drag_click.size() > 1) {
                return;
            }
            InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
            InventoryActionPacket p = new InventoryActionPacket(action, slotIdx, 0L);
            PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (slot instanceof CraftingTermSlot) {
            InventoryAction action = AEBaseScreen.hasShiftDown() ? InventoryAction.CRAFT_SHIFT : (InputConstants.isKeyDown((long)this.getMinecraft().getWindow().getWindow(), (int)32) ? InventoryAction.CRAFT_ALL : (mouseButton == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM));
            InventoryActionPacket p = new InventoryActionPacket(action, slotIdx, 0L);
            PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (slot != null && InputConstants.isKeyDown((long)this.getMinecraft().getWindow().getWindow(), (int)32)) {
            int slotNum = slot.index;
            InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slotNum, 0L);
            PacketDistributor.sendToServer((CustomPacketPayload)p, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (slot != null && !this.disableShiftClick && AEBaseScreen.hasShiftDown() && mouseButton == 0) {
            this.disableShiftClick = true;
            if (this.dbl_whichItem.isEmpty() || this.bl_clicked != slot || this.dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) > 250L) {
                this.bl_clicked = slot;
                this.dbl_clickTimer = Stopwatch.createStarted();
                this.dbl_whichItem = slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
            } else if (!this.dbl_whichItem.isEmpty()) {
                List<Slot> slots = this.getInventorySlots();
                for (Slot inventorySlot : slots) {
                    if (inventorySlot == null || !inventorySlot.mayPickup((Player)this.getPlayer()) || !inventorySlot.hasItem() || !AEBaseScreen.isSameInventory(inventorySlot, slot) || !AbstractContainerMenu.canItemQuickReplace((Slot)inventorySlot, (ItemStack)this.dbl_whichItem, (boolean)true)) continue;
                    this.slotClicked(inventorySlot, inventorySlot.index, 0, ClickType.QUICK_MOVE);
                }
                this.dbl_whichItem = ItemStack.EMPTY;
            }
            this.disableShiftClick = false;
        }
        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int screenX, int screenY, int button) {
        Point mousePos = new Point((int)Math.round(mouseX - (double)screenX), (int)Math.round(mouseY - (double)screenY));
        if (this.widgets.hitTest(mousePos)) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, screenX, screenY, button);
    }

    protected LocalPlayer getPlayer() {
        return Objects.requireNonNull(this.getMinecraft().player);
    }

    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        Slot theSlot = this.getSlotUnderMouse();
        if (((AEBaseMenu)this.getMenu()).getCarried().isEmpty() && theSlot != null) {
            if (this.minecraft.options.keySwapOffhand.matches(keyCode, scanCode)) {
                this.slotClicked(theSlot, theSlot.index, 40, ClickType.SWAP);
                return true;
            }
            for (int j = 0; j < 9; ++j) {
                if (!this.getMinecraft().options.keyHotbarSlots[j].matches(keyCode, scanCode)) continue;
                List<Slot> slots = this.getInventorySlots();
                for (Slot s : slots) {
                    if (s.slot != j || s.container != ((AEBaseMenu)this.menu).getPlayerInventory() || s.mayPickup(((AEBaseMenu)this.menu).getPlayerInventory().player)) continue;
                    return false;
                }
                if (theSlot.getMaxStackSize() == 64) {
                    this.slotClicked(theSlot, theSlot.index, j, ClickType.SWAP);
                    return true;
                }
                for (Slot s : slots) {
                    if (s.slot != j || s.container != ((AEBaseMenu)this.menu).getPlayerInventory()) continue;
                    SwapSlotsPacket message = new SwapSlotsPacket(s.index, theSlot.index);
                    PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isHovering(Slot slot, double x, double y) {
        if (slot instanceof ResizableSlot) {
            ResizableSlot resizableSlot = (ResizableSlot)slot;
            int width = resizableSlot.getWidth();
            int height = resizableSlot.getHeight();
            return this.isHovering(slot.x, slot.y, width, height, x, y);
        }
        return super.isHovering(slot, x, y);
    }

    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        Blitter background;
        GeneratedBackground generatedBackground = this.style.getGeneratedBackground();
        if (generatedBackground != null) {
            BackgroundGenerator.draw(generatedBackground.getWidth(), generatedBackground.getHeight(), guiGraphics, offsetX, offsetY);
        }
        if ((background = this.style.getBackground()) != null) {
            background.dest(offsetX, offsetY).blit(guiGraphics);
        }
    }

    public void drawItem(GuiGraphics guiGraphics, int x, int y, ItemStack is) {
        guiGraphics.renderItem(is, x, y);
        guiGraphics.renderItemDecorations(this.font, is, x, y);
    }

    protected Component getGuiDisplayName(Component in) {
        return this.title.getString().isEmpty() ? in : this.title;
    }

    public void renderSlot(GuiGraphics guiGraphics, Slot s) {
        if (s instanceof AppEngSlot) {
            AppEngSlot appEngSlot = (AppEngSlot)s;
            try {
                this.renderAppEngSlot(guiGraphics, appEngSlot);
            }
            catch (Exception err) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + String.valueOf(err), new Object[0]);
            }
        } else {
            super.renderSlot(guiGraphics, s);
        }
    }

    private void renderAppEngSlot(GuiGraphics guiGraphics, AppEngSlot s) {
        ItemStack is = s.getItem();
        if ((s.renderIconWithItem() || is.isEmpty()) && s.isSlotEnabled() && s.getIcon() != null) {
            s.getIcon().getBlitter().dest(s.x, s.y).opacity(s.getOpacityOfIcon()).blit(guiGraphics);
        }
        if (!s.isValid()) {
            guiGraphics.fill(s.x, s.y, 16 + s.x, 16 + s.y, 0x66FF6666);
        }
        super.renderSlot(guiGraphics, (Slot)s);
    }

    public void containerTick() {
        super.containerTick();
        this.widgets.tick();
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof ITickingWidget)) continue;
            ((ITickingWidget)child).tick();
        }
    }

    public boolean isHandlingRightClick() {
        return this.handlingRightClick;
    }

    protected final <B extends Button> B addToLeftToolbar(B button) {
        this.verticalToolbar.add(button);
        return button;
    }

    public List<Rect2i> getExclusionZones() {
        ArrayList<Rect2i> result = new ArrayList<Rect2i>(2);
        this.widgets.addExclusionZones(result, this.getBounds(true));
        return result;
    }

    protected void fillRect(GuiGraphics guiGraphics, Rect2i rect, int color) {
        guiGraphics.fill(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color);
    }

    private TextOverride getOrCreateTextOverride(String id) {
        return this.textOverrides.computeIfAbsent(id, x -> new TextOverride());
    }

    protected final void setTextHidden(String id, boolean hidden) {
        this.getOrCreateTextOverride(id).setHidden(hidden);
    }

    public final void setSlotsHidden(SlotSemantic semantic, boolean hidden) {
        if (hidden) {
            if (this.hiddenSlots.add(semantic)) {
                for (Slot slot : ((AEBaseMenu)this.menu).getSlots(semantic)) {
                    slot.x = HIDDEN_SLOT_POS.getX();
                    slot.y = HIDDEN_SLOT_POS.getY();
                }
            }
        } else if (this.hiddenSlots.remove(semantic) && this.style != null) {
            this.positionSlots();
        }
    }

    protected final void setTextContent(String id, Component content) {
        this.getOrCreateTextOverride(id).setContent(content);
    }

    public ScreenStyle getStyle() {
        return this.style;
    }

    @Nullable
    public StackWithBounds getStackUnderMouse(double mouseX, double mouseY) {
        if (this.hoveredSlot != null) {
            return StackWithBounds.fromSlot(this, this.hoveredSlot);
        }
        return null;
    }

    public final int getGuiLeft() {
        return this.leftPos;
    }

    public final int getGuiTop() {
        return this.topPos;
    }

    public final Minecraft getMinecraft() {
        return this.minecraft;
    }

    public final Slot getSlotUnderMouse() {
        return this.hoveredSlot;
    }

    public static boolean isSameInventory(Slot a, Slot b) {
        if (a instanceof AppEngSlot) {
            AppEngSlot appEngSlotA = (AppEngSlot)a;
            if (b instanceof AppEngSlot) {
                AppEngSlot appEngSlotB = (AppEngSlot)b;
                return appEngSlotA.container == appEngSlotB.container;
            }
        }
        return a.container == b.container;
    }

    protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        int h;
        int w;
        if (!slot.isHighlightable()) {
            return;
        }
        int x = slot.x;
        int y = slot.y;
        if (slot instanceof ResizableSlot) {
            ResizableSlot resizableSlot = (ResizableSlot)slot;
            w = resizableSlot.getWidth();
            h = resizableSlot.getHeight();
        } else {
            w = 16;
            h = 16;
        }
        guiGraphics.hLine(x, x + w, y - 1, -2424833);
        guiGraphics.hLine(x - 1, x + w, y + h, -2424833);
        guiGraphics.vLine(x - 1, y - 2, y + h, -2424833);
        guiGraphics.vLine(x + w, y - 2, y + h, -2424833);
        guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + w, y + h, 1721553919, 1721553919, 0);
    }

    public final void switchToScreen(AEBaseScreen<?> screen) {
        this.savedSlotInfos.clear();
        for (Slot slot : ((AEBaseMenu)this.menu).slots) {
            this.savedSlotInfos.add(new SavedSlotInfo(slot));
            slot.x = HIDDEN_SLOT_POS.getX();
            slot.y = HIDDEN_SLOT_POS.getY();
        }
        this.minecraft.screen = null;
        this.minecraft.setScreen(screen);
        if (!screen.savedSlotInfos.isEmpty()) {
            for (SavedSlotInfo savedSlotInfo : screen.savedSlotInfos) {
                savedSlotInfo.restore();
            }
            screen.savedSlotInfos.clear();
        }
    }

    protected <P extends AEBaseScreen<T>> void onReturnFromSubScreen(AESubScreen<T, P> subScreen) {
    }

    protected void openHelp() {
        PageAnchor topic = this.getHelpTopic();
        if (topic != null) {
            GuidesCommon.openGuide((Player)this.getPlayer(), (ResourceLocation)GuideItem.GUIDE_ID, (PageAnchor)topic);
        } else {
            LOG.warn("No topic assigned to screen {}, but button was clicked", (Object)this);
        }
    }

    @Nullable
    protected PageAnchor getHelpTopic() {
        String helpTopic = this.style.getHelpTopic();
        if (helpTopic != null) {
            int sep = helpTopic.indexOf(35);
            String fragment = null;
            if (sep != -1) {
                fragment = helpTopic.substring(sep + 1);
                helpTopic = helpTopic.substring(0, sep);
            }
            try {
                return new PageAnchor(IdUtils.resolveId((String)helpTopic, (String)"ae2"), fragment);
            }
            catch (Exception e) {
                LOG.warn("Invalid helpTopic for screen {}: {}", (Object)this, (Object)helpTopic);
            }
        }
        Guide guide = AppEngClient.instance().getGuide();
        ItemIndex itemIndex = (ItemIndex)guide.getIndex(ItemIndex.class);
        Object target = ((AEBaseMenu)this.getMenu()).getTarget();
        if (target instanceof BlockEntity) {
            BlockEntity be = (BlockEntity)target;
            Block block = be.getBlockState().getBlock();
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey((Object)block);
            return (PageAnchor)itemIndex.get((Object)blockId);
        }
        if (target instanceof IPart) {
            IPart part = (IPart)target;
            Item item = part.getPartItem().asItem();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey((Object)item);
            return (PageAnchor)itemIndex.get((Object)itemId);
        }
        if (target instanceof ItemMenuHost) {
            ItemMenuHost menuHost = (ItemMenuHost)target;
            Object item = menuHost.getItem();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            return (PageAnchor)itemIndex.get((Object)itemId);
        }
        return null;
    }

    record SavedSlotInfo(Slot slot, boolean active, int x, int y) {
        public SavedSlotInfo(Slot slot) {
            this(slot, slot.isActive(), slot.x, slot.y);
        }

        public void restore() {
            Slot slot = this.slot;
            if (slot instanceof AppEngSlot) {
                AppEngSlot appEngSlot = (AppEngSlot)slot;
                appEngSlot.setActive(this.active);
            }
            this.slot.x = this.x;
            this.slot.y = this.y;
        }
    }
}

