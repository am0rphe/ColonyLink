/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.me.networktool;

import appeng.api.client.AEKeyRendering;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.me.networktool.MachineGroup;
import appeng.menu.me.networktool.NetworkStatus;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.util.Platform;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NetworkStatusScreen
extends AEBaseScreen<NetworkStatusMenu> {
    private static final int ROWS = 4;
    private static final int COLUMNS = 5;
    private static final int TABLE_X = 14;
    private static final int TABLE_Y = 41;
    private static final int CELL_WIDTH = 30;
    private static final int CELL_HEIGHT = 18;
    private final Button exportGridButton;
    private NetworkStatus status = new NetworkStatus();
    private final Scrollbar scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);

    public NetworkStatusScreen(NetworkStatusMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
        this.exportGridButton = this.widgets.addButton("export_grid", (Component)Component.literal((String)"Export Grid"), menu::exportGrid);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.exportGridButton.visible = ((NetworkStatusMenu)this.menu).canExportGrid();
        this.setTextContent("dialog_title", (Component)GuiText.NetworkDetails.text(this.status.getChannelsUsed()));
        this.setTextContent("stored_power", (Component)GuiText.StoredPower.text(Platform.formatPower(this.status.getStoredPower(), false)));
        this.setTextContent("max_power", (Component)GuiText.MaxPower.text(Platform.formatPower(this.status.getMaxStoredPower(), false)));
        this.setTextContent("power_input_rate", (Component)GuiText.PowerInputRate.text(Platform.formatPower(this.status.getAveragePowerInjection(), true)));
        this.setTextContent("power_usage_rate", (Component)GuiText.PowerUsageRate.text(Platform.formatPower(this.status.getAveragePowerUsage(), true)));
        this.setTextContent("channel_power_rate", (Component)GuiText.ChannelEnergyDrain.text(Platform.formatPower(this.status.getChannelPower(), true)));
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        int x = 0;
        int y = 0;
        int viewStart = this.scrollbar.getCurrentScroll() * 5;
        int viewEnd = viewStart + 20;
        ArrayList<Object> tooltip = null;
        ArrayList<MachineGroup> machines = new ArrayList<MachineGroup>(this.status.getGroupedMachines());
        machines.sort(MachineGroup.COMPARATOR);
        for (int i = viewStart; i < Math.min(viewEnd, machines.size()); ++i) {
            MachineGroup entry = (MachineGroup)machines.get(i);
            int cellX = 14 + x * 30;
            int cellY = 41 + y * 18;
            int itemX = cellX + 30 - 17;
            int itemY = cellY + 1;
            if (entry.isMissingChannel()) {
                guiGraphics.fill(cellX, cellY, cellX + 30, cellY + 18, -43691);
            }
            this.drawMachineCount(guiGraphics, itemX, cellY, entry.getCount());
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, itemX, itemY, entry.getDisplay());
            if (this.isHovering(cellX, cellY, 30, 18, mouseX, mouseY)) {
                tooltip = new ArrayList<Object>();
                tooltip.add(entry.getDisplay().getDisplayName());
                if (entry.isMissingChannel()) {
                    tooltip.add(GuiText.NoChannel.text().withStyle(ChatFormatting.RED));
                }
                tooltip.add(GuiText.Installed.text(entry.getCount()));
                if (entry.getIdlePowerUsage() > 0.0) {
                    tooltip.add(GuiText.EnergyDrain.text(Platform.formatPower(entry.getIdlePowerUsage(), true)));
                }
                if (entry.getPowerGenerationCapacity() > 0.0) {
                    tooltip.add(GuiText.EnergyGenerationCapacity.text(Platform.formatPower(entry.getPowerGenerationCapacity(), true)));
                }
            }
            if (++x < 5) continue;
            ++y;
            x = 0;
        }
        if (tooltip != null) {
            this.drawTooltipWithHeader(guiGraphics, mouseX - offsetX, mouseY - offsetY, tooltip);
        }
    }

    private void drawMachineCount(GuiGraphics guiGraphics, int x, int y, long count) {
        Object str = count >= 10000L ? Long.toString(count / 1000L) + "k" : Long.toString(count);
        float textWidth = (float)this.font.width((String)str) / 2.0f;
        Objects.requireNonNull(this.font);
        float textHeight = 9.0f / 2.0f;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)(x - 1) - textWidth, (float)y + (18.0f - textHeight) / 2.0f, 0.0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        guiGraphics.drawString(this.font, (String)str, 0, 0, this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB(), false);
        poseStack.popPose();
    }

    public void processServerUpdate(NetworkStatus status) {
        this.status = status;
        this.setScrollBar();
    }

    private void setScrollBar() {
        int size = this.status.getGroupedMachines().size();
        int overflowRows = (size + 5 - 1) / 5 - 4;
        this.scrollbar.setRange(0, overflowRows, 1);
    }
}

