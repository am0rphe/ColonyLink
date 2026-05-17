/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.interfaces.IProgressProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MolecularAssemblerScreen
extends UpgradeableScreen<MolecularAssemblerMenu> {
    private final ProgressBar pb;

    public MolecularAssemblerScreen(MolecularAssemblerMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb = new ProgressBar((IProgressProvider)this.menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        this.widgets.add("progressBar", this.pb);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.pb.setFullMsg((Component)Component.literal((String)(((MolecularAssemblerMenu)this.menu).getCurrentProgress() + "%")));
    }
}

