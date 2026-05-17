/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Mth
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.Color;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.InfoBar;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.menu.me.crafting.CraftingStatusMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class CPUSelectionList
implements ICompositeWidget {
    private static final int ROWS = 6;
    private final Blitter background;
    private final Blitter buttonBg;
    private final Blitter buttonBgSelected;
    private final CraftingStatusMenu menu;
    private final Color textColor;
    private final int selectedColor;
    private final Scrollbar scrollbar;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public CPUSelectionList(CraftingStatusMenu menu, Scrollbar scrollbar, ScreenStyle style) {
        this.menu = menu;
        this.scrollbar = scrollbar;
        this.background = style.getImage("cpuList");
        this.buttonBg = style.getImage("cpuListButton");
        this.buttonBgSelected = style.getImage("cpuListButtonSelected");
        this.textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR);
        this.selectedColor = style.getColor(PaletteColor.SELECTION_COLOR).toARGB();
        this.scrollbar.setCaptureMouseWheel(false);
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rect2i(position.getX(), position.getY(), this.bounds.getWidth(), this.bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rect2i(this.bounds.getX(), this.bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return this.bounds;
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        this.scrollbar.onMouseWheel(mousePos, delta);
        return true;
    }

    @Override
    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        CraftingStatusMenu.CraftingCpuListEntry cpu = this.hitTestCpu(new Point(mouseX, mouseY));
        if (cpu != null) {
            GenericStack currentJob;
            MutableComponent modeText;
            ArrayList<Component> tooltipLines = new ArrayList<Component>();
            tooltipLines.add(this.getCpuName(cpu));
            int coProcessors = cpu.coProcessors();
            if (coProcessors == 1) {
                tooltipLines.add((Component)ButtonToolTips.CpuStatusCoProcessor.text(Tooltips.ofNumber(coProcessors)).withStyle(ChatFormatting.GRAY));
            } else if (coProcessors > 1) {
                tooltipLines.add((Component)ButtonToolTips.CpuStatusCoProcessors.text(Tooltips.ofNumber(coProcessors)).withStyle(ChatFormatting.GRAY));
            }
            tooltipLines.add((Component)ButtonToolTips.CpuStatusStorage.text(Tooltips.ofBytes(cpu.storage())).withStyle(ChatFormatting.GRAY));
            switch (cpu.mode()) {
                case PLAYER_ONLY: {
                    MutableComponent mutableComponent = ButtonToolTips.CpuSelectionModePlayersOnly.text();
                    break;
                }
                case MACHINE_ONLY: {
                    MutableComponent mutableComponent = ButtonToolTips.CpuSelectionModeAutomationOnly.text();
                    break;
                }
                default: {
                    MutableComponent mutableComponent = modeText = null;
                }
            }
            if (modeText != null) {
                tooltipLines.add((Component)modeText);
            }
            if ((currentJob = cpu.currentJob()) != null) {
                tooltipLines.add((Component)ButtonToolTips.CpuStatusCrafting.text(Tooltips.ofAmount(currentJob)).append(" ").append(currentJob.what().getDisplayName()));
                tooltipLines.add((Component)ButtonToolTips.CpuStatusCraftedIn.text(Tooltips.ofPercent(cpu.progress()), Tooltips.ofDuration(cpu.elapsedTimeNanos(), TimeUnit.NANOSECONDS)));
            }
            return new Tooltip(tooltipLines);
        }
        return null;
    }

    @Override
    public boolean onMouseUp(Point mousePos, int button) {
        CraftingStatusMenu.CraftingCpuListEntry cpu = this.hitTestCpu(mousePos);
        if (cpu != null) {
            this.menu.selectCpu(cpu.serial());
            return true;
        }
        return false;
    }

    @Nullable
    private CraftingStatusMenu.CraftingCpuListEntry hitTestCpu(Point mousePos) {
        int relX = mousePos.getX() - this.bounds.getX();
        int relY = mousePos.getY() - this.bounds.getY();
        if ((relX -= 8) < 0 || relX >= this.buttonBg.getSrcWidth()) {
            return null;
        }
        int buttonIdx = this.scrollbar.getCurrentScroll() + (relY -= 19) / (this.buttonBg.getSrcHeight() + 1);
        if (relY % (this.buttonBg.getSrcHeight() + 1) == this.buttonBg.getSrcHeight()) {
            return null;
        }
        if (relY < 0 || buttonIdx >= this.menu.cpuList.cpus().size()) {
            return null;
        }
        List<CraftingStatusMenu.CraftingCpuListEntry> cpus = this.menu.cpuList.cpus();
        if (buttonIdx >= 0 && buttonIdx < cpus.size()) {
            return cpus.get(buttonIdx);
        }
        return null;
    }

    @Override
    public void updateBeforeRender() {
        int hiddenRows = Math.max(0, this.menu.cpuList.cpus().size() - 6);
        this.scrollbar.setRange(0, hiddenRows, 2);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        int x = bounds.getX() + this.bounds.getX();
        int y = bounds.getY() + this.bounds.getY();
        this.background.dest(x, y, this.bounds.getWidth(), this.bounds.getHeight()).blit(guiGraphics);
        x += 8;
        y += 19;
        PoseStack pose = guiGraphics.pose();
        Font font = Minecraft.getInstance().font;
        List<CraftingStatusMenu.CraftingCpuListEntry> cpus = this.menu.cpuList.cpus().subList(Mth.clamp((int)this.scrollbar.getCurrentScroll(), (int)0, (int)this.menu.cpuList.cpus().size()), Mth.clamp((int)(this.scrollbar.getCurrentScroll() + 6), (int)0, (int)this.menu.cpuList.cpus().size()));
        for (CraftingStatusMenu.CraftingCpuListEntry cpu : cpus) {
            if (cpu.serial() == this.menu.getSelectedCpuSerial()) {
                this.buttonBgSelected.dest(x, y).blit(guiGraphics);
            } else {
                this.buttonBg.dest(x, y).blit(guiGraphics);
            }
            Component name = this.getCpuName(cpu);
            pose.pushPose();
            pose.translate((float)(x + 3), (float)(y + 2), 0.0f);
            pose.scale(0.666f, 0.666f, 1.0f);
            guiGraphics.drawString(font, name, 0, 0, this.textColor.toARGB(), false);
            pose.popPose();
            InfoBar infoBar = new InfoBar();
            GenericStack currentJob = cpu.currentJob();
            if (currentJob != null) {
                infoBar.add(Icon.S_CRAFT, 1.0f, x + 2, y + 9);
                String craftAmt = currentJob.what().formatAmount(currentJob.amount(), AmountFormat.SLOT);
                infoBar.add(craftAmt, this.textColor.toARGB(), 0.666f, x + 14, y + 13);
                infoBar.add(currentJob.what(), 0.666f, x + 55, y + 9);
                int progress = (int)(cpu.progress() * (float)(this.buttonBg.getSrcWidth() - 1));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(1.0f, -1.0f, 0.0f);
                guiGraphics.fill(x, y + this.buttonBg.getSrcHeight() - 2, x + progress, y + this.buttonBg.getSrcHeight() - 1, this.menu.getSelectedCpuSerial() == cpu.serial() ? -8541742 : this.selectedColor);
                guiGraphics.pose().popPose();
            } else {
                infoBar.add(Icon.S_STORAGE, 1.0f, x + 27, y + 9);
                String storageAmount = this.formatStorage(cpu);
                infoBar.add(storageAmount, this.textColor.toARGB(), 0.666f, x + 39, y + 13);
                if (cpu.coProcessors() > 0) {
                    infoBar.add(Icon.S_PROCESSOR, 1.0f, x + 2, y + 9);
                    String coProcessorCount = String.valueOf(cpu.coProcessors());
                    infoBar.add(coProcessorCount, this.textColor.toARGB(), 0.666f, x + 14, y + 13);
                }
                switch (cpu.mode()) {
                    case PLAYER_ONLY: {
                        infoBar.add(Icon.S_TERMINAL, 1.0f, x + 55, y + 9);
                        break;
                    }
                    case MACHINE_ONLY: {
                        infoBar.add(Icon.S_MACHINE, 1.0f, x + 55, y + 9);
                    }
                }
            }
            infoBar.render(guiGraphics, x + 2, y + this.buttonBg.getSrcHeight() - 12);
            y += this.buttonBg.getSrcHeight() + 1;
        }
    }

    private String formatStorage(CraftingStatusMenu.CraftingCpuListEntry cpu) {
        long storage = cpu.storage();
        if (storage >= 0x100000L) {
            return storage / 0x100000L + "M";
        }
        return storage / 1024L + "k";
    }

    private Component getCpuName(CraftingStatusMenu.CraftingCpuListEntry cpu) {
        return cpu.name() != null ? cpu.name() : GuiText.CPUs.text().append(String.format(" #%d", cpu.serial()));
    }
}

