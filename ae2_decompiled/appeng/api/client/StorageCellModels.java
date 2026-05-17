/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.client;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class StorageCellModels {
    private static final ResourceLocation MODEL_CELL_DEFAULT = ResourceLocation.parse((String)"ae2:block/drive/drive_cell");
    private static final Map<Item, ResourceLocation> registry = new IdentityHashMap<Item, ResourceLocation>();

    private StorageCellModels() {
    }

    public static synchronized void registerModel(ItemLike itemLike, ResourceLocation model) {
        Objects.requireNonNull(itemLike, "itemLike");
        Item item = Objects.requireNonNull(itemLike.asItem(), "item.asItem()");
        Objects.requireNonNull(model, "model");
        Preconditions.checkArgument((!registry.containsKey(item) ? 1 : 0) != 0, (Object)"Cannot register an item twice.");
        registry.put(item, model);
    }

    @Nullable
    public static synchronized ResourceLocation model(ItemLike itemLike) {
        Objects.requireNonNull(itemLike, "itemLike");
        Item item = Objects.requireNonNull(itemLike.asItem(), "itemLike.asItem()");
        return registry.get(item);
    }

    public static synchronized Map<Item, ResourceLocation> models() {
        return new HashMap<Item, ResourceLocation>(registry);
    }

    public static ResourceLocation getDefaultModel() {
        return MODEL_CELL_DEFAULT;
    }
}

