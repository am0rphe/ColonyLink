/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.me.crafting;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftAmountMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftAmountScreen
extends AEBaseScreen<CraftAmountMenu> {
    private final Button next;
    private final NumberEntryWidget amountToCraft;
    private boolean amountInitialized;

    public CraftAmountScreen(CraftAmountMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.next = this.widgets.addButton("next", (Component)GuiText.Next.text(), this::confirm);
        AESubScreen.addBackButton(menu, "back", this.widgets);
        this.amountToCraft = this.widgets.addNumberEntryWidget("amountToCraft", NumberEntryType.UNITLESS);
        this.amountToCraft.setMinValue(1L);
        this.amountToCraft.setMaxValue(Integer.MAX_VALUE);
        this.amountToCraft.setLongValue(1L);
        this.amountToCraft.setTextFieldStyle(style.getWidget("amountToCraftInput"));
        this.amountToCraft.setHideValidationIcon(true);
        this.amountToCraft.setOnConfirm(this::confirm);
    }

    @Override
    protected void updateBeforeRender() {
        GenericStack whatToCraft;
        super.updateBeforeRender();
        if (!this.amountInitialized && (whatToCraft = ((CraftAmountMenu)this.menu).getWhatToCraft()) != null) {
            this.amountToCraft.setType(NumberEntryType.of(whatToCraft.what()));
            this.amountToCraft.setLongValue(whatToCraft.amount());
            this.amountInitialized = true;
        }
        this.next.setMessage((Component)(CraftAmountScreen.hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text()));
        this.next.active = this.amountToCraft.getIntValue().orElse(0) > 0;
    }

    private void confirm() {
        int amount = this.amountToCraft.getIntValue().orElse(0);
        boolean craftMissingAmount = this.amountToCraft.startsWithEquals();
        if (amount <= 0) {
            return;
        }
        ((CraftAmountMenu)this.menu).confirm(amount, craftMissingAmount, CraftAmountScreen.hasShiftDown());
    }
}

