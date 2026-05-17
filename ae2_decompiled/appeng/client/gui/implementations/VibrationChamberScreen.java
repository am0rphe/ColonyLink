/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ProgressBar;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VibrationChamberScreen
extends UpgradeableScreen<VibrationChamberMenu> {
    private static final Blitter BURN_PROGRESS = Blitter.texture("guis/vibchamber.png").src(176, 0, 14, 13);
    private final ProgressBar generationRateBar;

    public VibrationChamberScreen(VibrationChamberMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.generationRateBar = new ProgressBar((IProgressProvider)this.menu, style.getImage("generationRateBar"), ProgressBar.Direction.VERTICAL);
        this.widgets.add("generationRateBar", this.generationRateBar);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        double powerPerTick = ((VibrationChamberMenu)this.menu).getPowerPerTick();
        double efficiency = ((VibrationChamberMenu)this.menu).getFuelEfficiency();
        this.generationRateBar.setFullMsg((Component)Component.literal((String)(Platform.formatPower(powerPerTick, true) + "\nEff: " + efficiency + "%")));
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        if (((VibrationChamberMenu)this.menu).getRemainingBurnTime() > 0) {
            int f = ((VibrationChamberMenu)this.menu).getRemainingBurnTime() * BURN_PROGRESS.getSrcHeight() / 100;
            BURN_PROGRESS.copy().src(BURN_PROGRESS.getSrcX(), BURN_PROGRESS.getSrcY() + BURN_PROGRESS.getSrcHeight() - f, BURN_PROGRESS.getSrcWidth(), f).dest(80, 20 + BURN_PROGRESS.getSrcHeight() - f).blit(guiGraphics);
        }
    }
}

