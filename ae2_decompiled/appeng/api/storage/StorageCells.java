/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class StorageCells {
    private static final List<ICellHandler> handlers = new ArrayList<ICellHandler>();

    private StorageCells() {
    }

    public static synchronized void addCellHandler(ICellHandler handler) {
        Objects.requireNonNull(handler, "Called before FMLCommonSetupEvent.");
        Preconditions.checkArgument((!handlers.contains(handler) ? 1 : 0) != 0, (Object)"Tried to register the same handler instance twice.");
        handlers.add(handler);
    }

    public static synchronized boolean isCellHandled(ItemStack is) {
        if (is.isEmpty()) {
            return false;
        }
        for (ICellHandler ch : handlers) {
            if (!ch.isCell(is)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    public static synchronized ICellHandler getHandler(ItemStack is) {
        if (is.isEmpty()) {
            return null;
        }
        for (ICellHandler ch : handlers) {
            if (!ch.isCell(is)) continue;
            return ch;
        }
        return null;
    }

    @Nullable
    public static synchronized StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        if (is.isEmpty()) {
            return null;
        }
        for (ICellHandler ch : handlers) {
            StorageCell inventory = ch.getCellInventory(is, host);
            if (inventory == null) continue;
            return inventory;
        }
        return null;
    }
}

