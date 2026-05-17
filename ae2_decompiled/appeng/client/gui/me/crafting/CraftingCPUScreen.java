/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  org.apache.commons.lang3.time.DurationFormatUtils
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.client.gui.me.crafting;

import appeng.api.config.CpuSelectionMode;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.me.crafting.CraftingStatusTableRenderer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatus;
import appeng.menu.me.crafting.CraftingStatusEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftingCPUScreen<T extends CraftingCPUMenu>
extends AEBaseScreen<T> {
    private static final Logger LOG = LoggerFactory.getLogger(CraftingCPUScreen.class);
    private final CraftingStatusTableRenderer table = new CraftingStatusTableRenderer(this, 9, 19);
    private final Button cancel;
    private final Button suspend;
    private final Scrollbar scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);
    private final SettingToggleButton<CpuSelectionMode> schedulingModeButton;
    private CraftingStatus status;

    public CraftingCPUScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.cancel = this.widgets.addButton("cancel", (Component)GuiText.Cancel.text(), () -> menu.cancelCrafting());
        this.suspend = this.widgets.addButton("suspend", (Component)GuiText.Suspend.text(), () -> menu.toggleScheduling());
        this.schedulingModeButton = new ServerSettingToggleButton<CpuSelectionMode>(Settings.CPU_SELECTION_MODE, CpuSelectionMode.ANY);
        if (((CraftingCPUMenu)((Object)menu)).allowConfiguration()) {
            this.addToLeftToolbar(this.schedulingModeButton);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        Component title = this.getGuiDisplayName((Component)GuiText.CraftingStatus.text());
        if (this.status != null) {
            long elapsedTime = this.status.getElapsedTime();
            double remainingItems = this.status.getRemainingItemCount();
            double startItems = this.status.getStartItemCount();
            long eta = (long)((double)elapsedTime / Math.max(1.0, startItems - remainingItems) * remainingItems);
            if (eta > 0L && !this.getVisualEntries().isEmpty()) {
                long etaInMilliseconds = TimeUnit.MILLISECONDS.convert(eta, TimeUnit.NANOSECONDS);
                String etaTimeText = DurationFormatUtils.formatDuration((long)etaInMilliseconds, (String)GuiText.ETAFormat.getLocal());
                title = title.copy().append(" - " + etaTimeText);
            }
        }
        if (((CraftingCPUMenu)this.menu).isCantStoreItems()) {
            title = title.copy().append(" - ").append((Component)GuiText.CantStoreItems.text().withStyle(ChatFormatting.RED));
        }
        this.setTextContent("dialog_title", title);
        int size = this.status != null ? this.status.getEntries().size() : 0;
        this.scrollbar.setRange(0, this.table.getScrollableRows(size), 1);
        this.schedulingModeButton.set(((CraftingCPUMenu)this.menu).getSchedulingMode());
    }

    private List<CraftingStatusEntry> getVisualEntries() {
        return this.status != null ? this.status.getEntries() : Collections.emptyList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float btn) {
        this.suspend.active = this.cancel.active = !this.getVisualEntries().isEmpty();
        super.render(guiGraphics, mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        if (this.status != null) {
            this.table.render(guiGraphics, mouseX, mouseY, this.status.getEntries(), this.scrollbar.getCurrentScroll());
        }
    }

    @Override
    @Nullable
    public StackWithBounds getStackUnderMouse(double mouseX, double mouseY) {
        StackWithBounds hovered = this.table.getHoveredStack();
        if (hovered != null) {
            return hovered;
        }
        return super.getStackUnderMouse(mouseX, mouseY);
    }

    public void postUpdate(CraftingStatus status) {
        LinkedHashMap<Long, CraftingStatusEntry> entries;
        if (this.status == null || status.isFullStatus()) {
            entries = new LinkedHashMap<Long, CraftingStatusEntry>();
        } else {
            entries = new LinkedHashMap(this.status.getEntries().size());
            for (CraftingStatusEntry entry : this.status.getEntries()) {
                entries.put(entry.getSerial(), entry);
            }
        }
        for (CraftingStatusEntry entry : status.getEntries()) {
            if (entry.isDeleted()) {
                entries.remove(entry.getSerial());
                continue;
            }
            CraftingStatusEntry existingEntry = (CraftingStatusEntry)entries.get(entry.getSerial());
            if (existingEntry != null) {
                entries.put(entry.getSerial(), new CraftingStatusEntry(existingEntry.getSerial(), existingEntry.getWhat(), entry.getStoredAmount(), entry.getActiveAmount(), entry.getPendingAmount()));
                continue;
            }
            if (entry.getWhat() == null) {
                LOG.warn("Received an updated crafting status entry {}, but no current entry exists. {}", (Object)entry, (Object)status.isFullStatus());
                continue;
            }
            entries.put(entry.getSerial(), entry);
        }
        ArrayList<CraftingStatusEntry> sortedEntries = new ArrayList<CraftingStatusEntry>(entries.values());
        Collections.sort(sortedEntries);
        this.status = new CraftingStatus(true, status.getElapsedTime(), status.getRemainingItemCount(), status.getStartItemCount(), sortedEntries, status.isSuspended());
        this.suspend.setMessage((Component)(status.isSuspended() ? GuiText.Resume.text() : GuiText.Suspend.text()));
    }
}

