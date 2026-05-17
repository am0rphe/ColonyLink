/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.me.crafting;

import appeng.api.stacks.AEKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.SetStockAmountMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SetStockAmountScreen
extends AEBaseScreen<SetStockAmountMenu> {
    private final NumberEntryWidget amount;
    private boolean amountInitialized;

    public SetStockAmountScreen(SetStockAmountMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.widgets.addButton("save", (Component)GuiText.Set.text(), this::confirm);
        AESubScreen.addBackButton(menu, "back", this.widgets);
        this.amount = this.widgets.addNumberEntryWidget("amountToStock", NumberEntryType.UNITLESS);
        this.amount.setLongValue(1L);
        this.amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        this.amount.setMinValue(0L);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);
    }

    @Override
    protected void updateBeforeRender() {
        AEKey whatToStock;
        super.updateBeforeRender();
        if (!this.amountInitialized && (whatToStock = ((SetStockAmountMenu)this.menu).getWhatToStock()) != null) {
            this.amount.setType(NumberEntryType.of(whatToStock));
            this.amount.setLongValue(((SetStockAmountMenu)this.menu).getInitialAmount());
            this.amount.setMaxValue(((SetStockAmountMenu)this.menu).getMaxAmount());
            this.amountInitialized = true;
        }
    }

    private void confirm() {
        this.amount.getIntValue().ifPresent(((SetStockAmountMenu)this.menu)::confirm);
    }
}

