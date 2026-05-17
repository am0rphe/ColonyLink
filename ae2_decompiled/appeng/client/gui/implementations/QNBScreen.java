/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.implementations;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.menu.implementations.QNBMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class QNBScreen
extends AEBaseScreen<QNBMenu> {
    public QNBScreen(QNBMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        ArrayList<MutableComponent> tooltip = super.getTooltipFromContainerItem(stack);
        if (AEItems.QUANTUM_ENTANGLED_SINGULARITY.is(stack) && !QuantumBridgeBlockEntity.isValidEntangledSingularity(stack)) {
            tooltip = new ArrayList<MutableComponent>(tooltip);
            tooltip.add(Tooltips.of(GuiText.InvalidSingularity, Tooltips.RED, new Object[0]));
        }
        return tooltip;
    }
}

