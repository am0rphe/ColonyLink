/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.SkyChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SkyChestScreen
extends AEBaseScreen<SkyChestMenu> {
    public SkyChestScreen(SkyChestMenu menu, Inventory playerInv, Component title, ScreenStyle style) {
        super(menu, playerInv, title, style);
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }
}

