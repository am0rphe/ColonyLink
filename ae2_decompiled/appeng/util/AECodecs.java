/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Codec$ResultFunction
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.util;

import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEItems;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AECodecs {
    private static final Logger LOG = LoggerFactory.getLogger(AECodecs.class);
    private static final Codec.ResultFunction<ItemStack> MISSING_CONTENT_ITEMSTACK_RESULT = new Codec.ResultFunction<ItemStack>(){

        public <T> DataResult<Pair<ItemStack, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<ItemStack, T>> a) {
            if (a instanceof DataResult.Error) {
                DataResult.Error error = (DataResult.Error)a;
                ItemStack missingContent = AEItems.MISSING_CONTENT.stack();
                Tag convert = (Tag)Dynamic.convert(ops, (DynamicOps)NbtOps.INSTANCE, input);
                if (convert instanceof CompoundTag) {
                    CompoundTag compoundTag = (CompoundTag)convert;
                    missingContent.set(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA, (Object)CustomData.of((CompoundTag)compoundTag));
                }
                LOG.error("Failed to deserialize ItemStack: {}", (Object)error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, (Object)error.message());
                return DataResult.success((Object)Pair.of((Object)missingContent, input), (Lifecycle)Lifecycle.stable());
            }
            return a;
        }

        public <T> DataResult<T> coApply(DynamicOps<T> ops, ItemStack input, DataResult<T> t) {
            CustomData originalData;
            if (t instanceof DataResult.Error) {
                DataResult.Error error = (DataResult.Error)t;
                ItemStack missingContent = AEItems.MISSING_CONTENT.stack();
                LOG.error("Failed to serialize ItemStack {}: {}", (Object)input, (Object)error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, (Object)error.message());
                return ItemStack.SINGLE_ITEM_CODEC.encodeStart(ops, (Object)missingContent).setLifecycle(t.lifecycle());
            }
            if (AEItems.MISSING_CONTENT.is(input) && (originalData = (CustomData)input.get(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA)) != null) {
                return DataResult.success((Object)Dynamic.convert((DynamicOps)NbtOps.INSTANCE, ops, (Object)originalData.getUnsafe()), (Lifecycle)t.lifecycle());
            }
            return t;
        }
    };
    public static final Codec<ItemStack> FAULT_TOLERANT_SIMPLE_ITEM_CODEC = ItemStack.SINGLE_ITEM_CODEC.mapResult(MISSING_CONTENT_ITEMSTACK_RESULT);
    public static final Codec<ItemStack> FAULT_TOLERANT_ITEMSTACK_CODEC = ItemStack.CODEC.mapResult(MISSING_CONTENT_ITEMSTACK_RESULT);
    public static final Codec<ItemStack> FAULT_TOLERANT_OPTIONAL_ITEMSTACK_CODEC = ItemStack.OPTIONAL_CODEC.mapResult(MISSING_CONTENT_ITEMSTACK_RESULT);

    private AECodecs() {
    }
}

