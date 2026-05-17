/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.features;

import appeng.api.features.IGridLinkableHandler;
import com.google.common.base.Preconditions;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class GridLinkables {
    private static final Map<Item, IGridLinkableHandler> registry = new IdentityHashMap<Item, IGridLinkableHandler>();

    private GridLinkables() {
    }

    public static synchronized void register(ItemLike itemLike, IGridLinkableHandler handler) {
        Objects.requireNonNull(itemLike, "itemLike");
        Objects.requireNonNull(itemLike.asItem(), "itemLike.asItem()");
        Objects.requireNonNull(handler, "handler");
        Item item = itemLike.asItem();
        Preconditions.checkState((!registry.containsKey(item) ? 1 : 0) != 0, (String)"Handler for %s already registered", (Object)item);
        registry.put(item, handler);
    }

    @Nullable
    public static synchronized IGridLinkableHandler get(ItemLike itemLike) {
        Objects.requireNonNull(itemLike, "itemLike");
        Objects.requireNonNull(itemLike.asItem(), "itemLike.asItem()");
        return registry.get(itemLike.asItem());
    }
}

