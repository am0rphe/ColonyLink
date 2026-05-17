/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Longs
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.me.items;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.google.common.primitives.Longs;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SetProcessingPatternAmountScreen<C extends PatternEncodingTermMenu>
extends AESubScreen<C, PatternEncodingTermScreen<C>> {
    private final NumberEntryWidget amount;
    private final GenericStack currentStack;
    private final Consumer<GenericStack> setter;

    public SetProcessingPatternAmountScreen(PatternEncodingTermScreen<C> parentScreen, GenericStack currentStack, Consumer<GenericStack> setter) {
        super(parentScreen, "/screens/set_processing_pattern_amount.json");
        this.currentStack = currentStack;
        this.setter = setter;
        this.widgets.addButton("save", (Component)GuiText.Set.text(), this::confirm);
        ItemStack icon = ((PatternEncodingTermMenu)this.getMenu()).getHost().getMainMenuIcon();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        TabButton button = new TabButton(Icon.BACK, icon.getHoverName(), btn -> this.returnToParent());
        this.widgets.add("back", (AbstractWidget)button);
        this.amount = this.widgets.addNumberEntryWidget("amountToStock", NumberEntryType.of(currentStack.what()));
        this.amount.setLongValue(currentStack.amount());
        this.amount.setMaxValue(this.getMaxAmount());
        this.amount.setTextFieldStyle(this.style.getWidget("amountToStockInput"));
        this.amount.setMinValue(0L);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);
        this.addClientSideSlot(new ClientDisplaySlot(currentStack), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void init() {
        super.init();
        this.setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void confirm() {
        this.amount.getLongValue().ifPresent(newAmount -> {
            if ((newAmount = Longs.constrainToRange((long)newAmount, (long)0L, (long)this.getMaxAmount())) <= 0L) {
                this.setter.accept(null);
            } else {
                this.setter.accept(new GenericStack(this.currentStack.what(), newAmount));
            }
            this.returnToParent();
        });
    }

    private long getMaxAmount() {
        return 999999L * (long)this.currentStack.what().getAmountPerUnit();
    }
}

