/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.me.items;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.core.AEConfig;
import appeng.menu.me.items.CraftingTermMenu;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftingTermScreen<C extends CraftingTermMenu>
extends MEStorageScreen<C> {
    public CraftingTermScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        ActionButton clearBtn = new ActionButton(ActionItems.S_STASH, btn -> menu.clearCraftingGrid());
        clearBtn.setHalfSize(true);
        clearBtn.setDisableBackground(true);
        this.widgets.add("clearCraftingGrid", (AbstractWidget)clearBtn);
        ActionButton clearToPlayerInvBtn = new ActionButton(ActionItems.S_STASH_TO_PLAYER_INV, btn -> menu.clearToPlayerInventory());
        clearToPlayerInvBtn.setHalfSize(true);
        clearToPlayerInvBtn.setDisableBackground(true);
        this.widgets.add("clearToPlayerInv", (AbstractWidget)clearToPlayerInvBtn);
    }

    public void onClose() {
        if (AEConfig.instance().isClearGridOnClose()) {
            ((CraftingTermMenu)this.getMenu()).clearCraftingGrid();
        }
        super.onClose();
    }
}

