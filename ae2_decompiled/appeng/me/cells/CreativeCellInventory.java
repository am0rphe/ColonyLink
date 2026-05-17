/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package appeng.me.cells;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

class CreativeCellInventory
implements StorageCell {
    private final Set<AEKey> configured = new HashSet<AEKey>();
    private final ItemStack stack;

    protected CreativeCellInventory(ItemStack o) {
        this.stack = o;
        ConfigInventory cc = CellConfig.create(o);
        this.configured.addAll(cc.keySet());
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return this.configured.contains(what) ? amount : 0L;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return this.configured.contains(what) ? amount : 0L;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (AEKey key : this.configured) {
            out.add(key, Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey input, IActionSource source) {
        return this.configured.contains(input);
    }

    @Override
    public CellState getStatus() {
        return CellState.TYPES_FULL;
    }

    @Override
    public double getIdleDrain() {
        return 0.0;
    }

    @Override
    public boolean canFitInsideCell() {
        return this.configured.isEmpty();
    }

    @Override
    public Component getDescription() {
        return this.stack.getHoverName();
    }

    @Override
    public void persist() {
    }
}

