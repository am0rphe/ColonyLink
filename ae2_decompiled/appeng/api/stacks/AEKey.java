/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapCodec$ResultFunction
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.api.stacks;

import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AEKey {
    public static final String TYPE_FIELD = "#t";
    private static final Logger LOG = LoggerFactory.getLogger(AEKey.class);
    public static final MapCodec<AEKey> MAP_CODEC = AEKeyType.CODEC.dispatchMap("#t", AEKey::getType, AEKeyType::codec).mapResult((MapCodec.ResultFunction)new MapCodec.ResultFunction<AEKey>(){

        public <T> DataResult<AEKey> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<AEKey> a) {
            if (a instanceof DataResult.Error) {
                DataResult.Error error = (DataResult.Error)a;
                ItemStack missingContent = AEItems.MISSING_CONTENT.stack();
                Tag convert = (Tag)ops.convertMap((DynamicOps)NbtOps.INSTANCE, ops.createMap(input.entries()));
                if (convert instanceof CompoundTag) {
                    CompoundTag compoundTag = (CompoundTag)convert;
                    missingContent.set(AEComponents.MISSING_CONTENT_AEKEY_DATA, (Object)CustomData.of((CompoundTag)compoundTag));
                }
                LOG.error("Failed to deserialize AE key: {}", (Object)error.message());
                missingContent.set(AEComponents.MISSING_CONTENT_ERROR, (Object)error.message());
                return DataResult.success((Object)AEItemKey.of(missingContent), (Lifecycle)Lifecycle.stable());
            }
            return a;
        }

        public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, AEKey input, RecordBuilder<T> t) {
            CustomData originalData;
            if (AEItems.MISSING_CONTENT.is(input) && (originalData = input.get(AEComponents.MISSING_CONTENT_AEKEY_DATA)) != null) {
                CompoundTag originalDataMap = originalData.getUnsafe();
                for (String key : originalDataMap.getAllKeys()) {
                    t.add(key, NbtOps.INSTANCE.convertTo(ops, originalDataMap.get(key)));
                }
            }
            return t;
        }
    });
    public static final Codec<AEKey> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, AEKey> STREAM_CODEC = StreamCodec.of(AEKey::writeKey, AEKey::readKey);
    public static final StreamCodec<RegistryFriendlyByteBuf, AEKey> OPTIONAL_STREAM_CODEC = StreamCodec.of(AEKey::writeOptionalKey, AEKey::readOptionalKey);
    private volatile Component cachedDisplayName;

    public static void writeOptionalKey(RegistryFriendlyByteBuf buffer, @Nullable AEKey key) {
        buffer.writeBoolean(key != null);
        if (key != null) {
            AEKey.writeKey(buffer, key);
        }
    }

    public static void writeKey(RegistryFriendlyByteBuf buffer, AEKey key) {
        byte id = key.getType().getRawId();
        buffer.writeVarInt((int)id);
        key.writeToPacket(buffer);
    }

    @Nullable
    public static AEKey readOptionalKey(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        return AEKey.readKey(buffer);
    }

    @Nullable
    public static AEKey readKey(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        AEKeyType type = AEKeyType.fromRawId(id);
        if (type == null) {
            AELog.error("Received unknown key space id %d", id);
            return null;
        }
        return type.readFromPacket(buffer);
    }

    @Nullable
    public static AEKey fromTagGeneric(HolderLookup.Provider registries, CompoundTag tag) {
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        try {
            return (AEKey)((Pair)CODEC.decode((DynamicOps)ops, (Object)tag).getOrThrow()).getFirst();
        }
        catch (Exception e) {
            LOG.warn("Cannot deserialize generic key from {}: {}", (Object)tag, (Object)e);
            return null;
        }
    }

    public final CompoundTag toTagGeneric(HolderLookup.Provider registries) {
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        return (CompoundTag)CODEC.encodeStart((DynamicOps)ops, (Object)this).getOrThrow();
    }

    public final int getAmountPerUnit() {
        return this.getType().getAmountPerUnit();
    }

    @Nullable
    public final String getUnitSymbol() {
        return this.getType().getUnitSymbol();
    }

    public final int getAmountPerOperation() {
        return this.getType().getAmountPerOperation();
    }

    public final int getAmountPerByte() {
        return this.getType().getAmountPerByte();
    }

    public String formatAmount(long amount, AmountFormat format) {
        return this.getType().formatAmount(amount, format);
    }

    public abstract AEKeyType getType();

    public abstract AEKey dropSecondary();

    public abstract CompoundTag toTag(HolderLookup.Provider var1);

    public abstract Object getPrimaryKey();

    public int getFuzzySearchValue() {
        return 0;
    }

    public int getFuzzySearchMaxValue() {
        return 0;
    }

    public final boolean fuzzyEquals(AEKey other, FuzzyMode fuzzyMode) {
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        if (this.getPrimaryKey() != other.getPrimaryKey()) {
            return false;
        }
        if (!this.supportsFuzzyRangeSearch()) {
            return true;
        }
        if (fuzzyMode == FuzzyMode.IGNORE_ALL) {
            return true;
        }
        if (fuzzyMode == FuzzyMode.PERCENT_99) {
            return this.getFuzzySearchValue() > 0 == other.getFuzzySearchValue() > 0;
        }
        float percentA = (float)this.getFuzzySearchValue() / (float)this.getFuzzySearchMaxValue();
        float percentB = (float)other.getFuzzySearchValue() / (float)other.getFuzzySearchMaxValue();
        return percentA > fuzzyMode.breakPoint == percentB > fuzzyMode.breakPoint;
    }

    @Contract(value="null -> false")
    public final boolean matches(@Nullable GenericStack stack) {
        return stack != null && stack.what().equals(this);
    }

    public String getModId() {
        return this.getId().getNamespace();
    }

    public abstract ResourceLocation getId();

    public abstract void writeToPacket(RegistryFriendlyByteBuf var1);

    public ItemStack wrapForDisplayOrFilter() {
        return GenericStack.wrapInItemStack(this, 0L);
    }

    public final boolean supportsFuzzyRangeSearch() {
        return this.getType().supportsFuzzyRangeSearch();
    }

    public final Component getDisplayName() {
        Component ret = this.cachedDisplayName;
        if (ret == null) {
            this.cachedDisplayName = ret = this.computeDisplayName();
        }
        return ret;
    }

    protected abstract Component computeDisplayName();

    public abstract void addDrops(long var1, List<ItemStack> var3, Level var4, BlockPos var5);

    public boolean isTagged(TagKey<?> tag) {
        return false;
    }

    @Nullable
    public <T> T get(DataComponentType<T> type) {
        return null;
    }

    public abstract boolean hasComponents();
}

