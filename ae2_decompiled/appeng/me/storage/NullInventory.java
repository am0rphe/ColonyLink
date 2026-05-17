/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.me.storage;

import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;

public class NullInventory
implements MEStorage {
    private static final NullInventory NULL_INVENTORY = new NullInventory();

    public static MEStorage of() {
        return NULL_INVENTORY;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
    }

    @Override
    public Component getDescription() {
        return Component.empty();
    }
}

