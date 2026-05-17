/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package appeng.items.materials;

import appeng.api.implementations.items.IStorageComponent;
import appeng.items.AEBaseItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StorageComponentItem
extends AEBaseItem
implements IStorageComponent {
    private final int storageInKb;

    public StorageComponentItem(Item.Properties properties, int storageInKb) {
        super(properties);
        this.storageInKb = storageInKb;
    }

    @Override
    public int getBytes(ItemStack is) {
        return this.storageInKb * 1024;
    }

    @Override
    public boolean isStorageComponent(ItemStack is) {
        return true;
    }
}

