/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.ILinkStatus;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ICraftingGridMenu {
    default public ILinkStatus getLinkStatus() {
        return ILinkStatus.ofConnected();
    }

    @Nullable
    public IGridNode getGridNode();

    default public IEnergySource getEnergySource() {
        IGridNode node = this.getGridNode();
        if (node == null) {
            return IEnergySource.empty();
        }
        return node.getGrid().getEnergyService();
    }

    public InternalInventory getCraftingMatrix();

    public IActionSource getActionSource();

    public List<ItemStack> getViewCells();

    default public void startAutoCrafting(List<AutoCraftEntry> toCraft) {
    }

    public boolean isPlayerInventorySlotLocked(int var1);

    public record AutoCraftEntry(AEItemKey what, List<Integer> slots) {
    }
}

