/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.parts;

import appeng.api.parts.IPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public interface IPartItem<P extends IPart>
extends ItemLike {
    public Class<P> getPartClass();

    public P createPart();

    public static ResourceLocation getId(IPartItem<?> item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey((Object)item.asItem());
        if (id == BuiltInRegistries.ITEM.getDefaultKey()) {
            throw new IllegalStateException("Part item " + String.valueOf(item) + " is not registered");
        }
        return id;
    }

    public static int getNetworkId(IPartItem<?> item) {
        int id = BuiltInRegistries.ITEM.getId((Object)item.asItem());
        if (id == 0) {
            throw new IllegalStateException("Part item " + String.valueOf(item) + " is not registered");
        }
        return id;
    }

    @Nullable
    public static IPartItem<?> byId(ResourceLocation id) {
        Item item = (Item)BuiltInRegistries.ITEM.get(id);
        if (item instanceof IPartItem) {
            IPartItem partItem = (IPartItem)item;
            return partItem;
        }
        return null;
    }

    @Nullable
    public static IPartItem<?> byNetworkId(int id) {
        Item item = (Item)BuiltInRegistries.ITEM.byId(id);
        if (item instanceof IPartItem) {
            IPartItem partItem = (IPartItem)item;
            return partItem;
        }
        return null;
    }
}

