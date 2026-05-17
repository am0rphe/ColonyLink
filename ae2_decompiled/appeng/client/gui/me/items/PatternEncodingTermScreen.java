/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.client.gui.me.items;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.config.ActionItems;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.me.items.CraftingEncodingPanel;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import appeng.client.gui.me.items.SetProcessingPatternAmountScreen;
import appeng.client.gui.me.items.SmithingTableEncodingPanel;
import appeng.client.gui.me.items.StonecuttingEncodingPanel;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class PatternEncodingTermScreen<C extends PatternEncodingTermMenu>
extends MEStorageScreen<C> {
    private final Map<EncodingMode, EncodingModePanel> modePanels = new EnumMap<EncodingMode, EncodingModePanel>(EncodingMode.class);
    private final Map<EncodingMode, TabButton> modeTabButtons = new EnumMap<EncodingMode, TabButton>(EncodingMode.class);

    public PatternEncodingTermScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        for (EncodingMode mode : EncodingMode.values()) {
            EncodingModePanel panel = switch (mode) {
                default -> throw new MatchException(null, null);
                case EncodingMode.CRAFTING -> new CraftingEncodingPanel(this, this.widgets);
                case EncodingMode.PROCESSING -> new ProcessingEncodingPanel(this, this.widgets);
                case EncodingMode.SMITHING_TABLE -> new SmithingTableEncodingPanel(this, this.widgets);
                case EncodingMode.STONECUTTING -> new StonecuttingEncodingPanel(this, this.widgets);
            };
            TabButton tabButton = new TabButton(panel.getIcon(), panel.getTabTooltip(), btn -> ((PatternEncodingTermMenu)this.getMenu()).setMode(mode));
            tabButton.setStyle(TabButton.Style.HORIZONTAL);
            int modeIndex = this.modeTabButtons.size();
            this.widgets.add("modePanel" + modeIndex, panel);
            this.widgets.add("modeTabButton" + modeIndex, (AbstractWidget)tabButton);
            this.modeTabButtons.put(mode, tabButton);
            this.modePanels.put(mode, panel);
        }
        ActionButton encodeBtn = new ActionButton(ActionItems.ENCODE, act -> menu.encode());
        this.widgets.add("encodePattern", (AbstractWidget)encodeBtn);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        for (EncodingMode mode : EncodingMode.values()) {
            boolean selected = ((PatternEncodingTermMenu)this.menu).getMode() == mode;
            this.modeTabButtons.get((Object)mode).setSelected(selected);
            this.modePanels.get((Object)mode).setVisible(selected);
        }
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        GenericStack currentStack;
        Slot slot;
        if (this.minecraft.options.keyPickItem.matchesMouse(btn) && ((PatternEncodingTermMenu)this.menu).canModifyAmountForSlot(slot = this.findSlot(xCoord, yCoord)) && (currentStack = GenericStack.fromItemStack(slot.getItem())) != null) {
            SetProcessingPatternAmountScreen screen = new SetProcessingPatternAmountScreen(this, currentStack, newStack -> {
                InventoryActionPacket message = new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, GenericStack.wrapInItemStack(newStack));
                PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
            });
            this.switchToScreen(screen);
            return true;
        }
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (((PatternEncodingTermMenu)this.menu).getCarried().isEmpty() && ((PatternEncodingTermMenu)this.menu).canModifyAmountForSlot(this.hoveredSlot)) {
            ArrayList<Component> itemTooltip = new ArrayList<Component>(this.getTooltipFromContainerItem(this.hoveredSlot.getItem()));
            GenericStack unwrapped = GenericStack.fromItemStack(this.hoveredSlot.getItem());
            if (unwrapped != null) {
                itemTooltip.add(Tooltips.getAmountTooltip(ButtonToolTips.Amount, unwrapped));
            }
            itemTooltip.add(Tooltips.getSetAmountTooltip());
            this.drawTooltip(guiGraphics, x, y, itemTooltip);
        } else {
            super.renderTooltip(guiGraphics, x, y);
        }
    }

    @Override
    protected EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
        EmptyingAction emptyingAction;
        if (((PatternEncodingTermMenu)this.menu).isProcessingPatternSlot(slot) && (emptyingAction = ContainerItemStrategies.getEmptyingAction(carried)) != null) {
            return emptyingAction;
        }
        return super.getEmptyingAction(slot, carried);
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot s) {
        super.renderSlot(guiGraphics, s);
        if (this.shouldShowCraftableIndicatorForSlot(s)) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 100.0f);
            StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, (float)(s.x - 11), (float)(s.y - 11), "+", false);
            poseStack.popPose();
        }
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        ArrayList<MutableComponent> lines = super.getTooltipFromContainerItem(stack);
        if (this.hoveredSlot != null && this.shouldShowCraftableIndicatorForSlot(this.hoveredSlot)) {
            lines = new ArrayList<MutableComponent>(lines);
            lines.add(ButtonToolTips.Craftable.text().withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    private boolean shouldShowCraftableIndicatorForSlot(Slot s) {
        SlotSemantic semantic = ((PatternEncodingTermMenu)this.menu).getSlotSemantic(s);
        if (semantic == SlotSemantics.CRAFTING_GRID || semantic == SlotSemantics.PROCESSING_INPUTS || semantic == SlotSemantics.SMITHING_TABLE_ADDITION || semantic == SlotSemantics.SMITHING_TABLE_BASE || semantic == SlotSemantics.SMITHING_TABLE_TEMPLATE || semantic == SlotSemantics.STONECUTTING_INPUT) {
            GenericStack slotContent = GenericStack.fromItemStack(s.getItem());
            if (slotContent == null) {
                return false;
            }
            return this.repo.isCraftable(slotContent.what());
        }
        return false;
    }

    public void onClose() {
        if (AEConfig.instance().isClearGridOnClose()) {
            ((PatternEncodingTermMenu)this.getMenu()).clear();
        }
        super.onClose();
    }
}

