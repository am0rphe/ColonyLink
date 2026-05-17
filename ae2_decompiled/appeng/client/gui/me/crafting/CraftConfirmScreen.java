/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Inventory
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.me.crafting;

import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.me.crafting.CraftConfirmTableRenderer;
import appeng.client.gui.me.crafting.CraftErrorScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import java.text.NumberFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class CraftConfirmScreen
extends AEBaseScreen<CraftConfirmMenu> {
    private final CraftConfirmTableRenderer table = new CraftConfirmTableRenderer(this, 9, 19);
    private final Button start;
    private final Button selectCPU;
    private final Scrollbar scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);

    public CraftConfirmScreen(CraftConfirmMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.start = this.widgets.addButton("start", (Component)GuiText.Start.text(), this::start);
        this.start.active = false;
        this.selectCPU = this.widgets.addButton("selectCpu", this.getNextCpuButtonLabel(), this::selectNextCpu);
        this.selectCPU.active = false;
        this.widgets.addButton("cancel", (Component)GuiText.Cancel.text(), menu::goBack);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        ICraftingSubmitResult errorResult = ((CraftConfirmMenu)this.menu).submitError.result();
        if (errorResult != null && errorResult.errorCode() != null) {
            this.switchToScreen(new CraftErrorScreen(this, errorResult.errorCode(), errorResult.errorDetail()));
            return;
        }
        this.selectCPU.setMessage(this.getNextCpuButtonLabel());
        CraftingPlanSummary plan = ((CraftConfirmMenu)this.menu).getPlan();
        boolean planIsStartable = plan != null && !plan.isSimulation();
        this.start.active = !((CraftConfirmMenu)this.menu).hasNoCPU() && planIsStartable;
        this.selectCPU.active = planIsStartable;
        MutableComponent planDetails = GuiText.CalculatingWait.text();
        MutableComponent cpuDetails = Component.empty();
        if (plan != null) {
            String byteUsed = NumberFormat.getInstance().format(plan.getUsedBytes());
            planDetails = GuiText.BytesUsed.text(byteUsed);
            cpuDetails = plan.isSimulation() ? GuiText.PartialPlan.text() : (((CraftConfirmMenu)this.menu).getCpuAvailableBytes() > 0L ? GuiText.ConfirmCraftCpuStatus.text(((CraftConfirmMenu)this.menu).getCpuAvailableBytes(), ((CraftConfirmMenu)this.menu).getCpuCoProcessors()) : GuiText.ConfirmCraftNoCpu.text());
        }
        this.setTextContent("dialog_title", (Component)GuiText.CraftingPlan.text(planDetails));
        this.setTextContent("cpu_status", (Component)cpuDetails);
        int size = plan != null ? plan.getEntries().size() : 0;
        this.scrollbar.setRange(0, this.table.getScrollableRows(size), 1);
    }

    private Component getNextCpuButtonLabel() {
        if (((CraftConfirmMenu)this.menu).hasNoCPU()) {
            return GuiText.NoCraftingCPUs.text();
        }
        Object cpuName = ((CraftConfirmMenu)this.menu).cpuName == null ? GuiText.Automatic.text() : ((CraftConfirmMenu)this.menu).cpuName;
        return GuiText.SelectedCraftingCPU.text(cpuName);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        CraftingPlanSummary plan = ((CraftConfirmMenu)this.menu).getPlan();
        if (plan != null) {
            this.table.render(guiGraphics, mouseX, mouseY, plan.getEntries(), this.scrollbar.getCurrentScroll());
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

    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (keyCode == 257 || keyCode == 335) {
            this.start();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private void selectNextCpu() {
        ((CraftConfirmMenu)this.getMenu()).cycleSelectedCPU(!this.isHandlingRightClick());
    }

    private void start() {
        ((CraftConfirmMenu)this.getMenu()).startJob();
    }
}

