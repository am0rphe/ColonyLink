/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.SpatialIOPortMenu;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

public class SpatialIOPortScreen
extends AEBaseScreen<SpatialIOPortMenu> {
    public SpatialIOPortScreen(SpatialIOPortMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.setTextContent("stored_power", (Component)GuiText.StoredPower.text(Platform.formatPowerLong(((SpatialIOPortMenu)this.menu).getCurrentPower(), false)));
        this.setTextContent("max_power", (Component)GuiText.MaxPower.text(Platform.formatPowerLong(((SpatialIOPortMenu)this.menu).getMaxPower(), false)));
        this.setTextContent("required_power", (Component)GuiText.RequiredPower.text(Platform.formatPowerLong(((SpatialIOPortMenu)this.menu).getRequiredPower(), false)));
        this.setTextContent("efficiency", (Component)GuiText.Efficiency.text(Float.valueOf((float)((SpatialIOPortMenu)this.menu).getEfficency() / 100.0f)));
        MutableComponent scsSizeText = ((SpatialIOPortMenu)this.menu).xSize != 0 && ((SpatialIOPortMenu)this.menu).ySize != 0 && ((SpatialIOPortMenu)this.menu).zSize != 0 ? GuiText.SCSSize.text(((SpatialIOPortMenu)this.menu).xSize, ((SpatialIOPortMenu)this.menu).ySize, ((SpatialIOPortMenu)this.menu).zSize) : GuiText.SCSInvalid.text();
        this.setTextContent("scs_size", (Component)scsSizeText);
    }
}

