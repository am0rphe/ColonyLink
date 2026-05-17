/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.Registry
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.stacks;

import appeng.api.stacks.AEFluidKeys;
import appeng.api.stacks.AEItemKeys;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.api.stacks.AmountFormat;
import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.util.ReadableNumberConverter;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.text.NumberFormat;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

public abstract class AEKeyType {
    public static final ResourceKey<Registry<AEKeyType>> REGISTRY_KEY = ResourceKey.createRegistryKey((ResourceLocation)AppEng.makeId("keytypes"));
    public static final Codec<AEKeyType> CODEC = Codec.lazyInitialized(() -> AEKeyTypesInternal.getRegistry().byNameCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, AEKeyType> STREAM_CODEC = ByteBufCodecs.registry(REGISTRY_KEY);
    private final ResourceLocation id;
    private final Class<? extends AEKey> keyClass;
    private final AEKeyFilter filter;
    private final Component description;

    public AEKeyType(ResourceLocation id, Class<? extends AEKey> keyClass, Component description) {
        Preconditions.checkArgument((!keyClass.equals(AEKey.class) ? 1 : 0) != 0, (Object)"Can't register a key type for AEKey itself");
        this.id = id;
        this.keyClass = keyClass;
        this.filter = what -> what.getType() == this;
        this.description = description;
    }

    public abstract MapCodec<? extends AEKey> codec();

    public static AEKeyType items() {
        return AEItemKeys.INSTANCE;
    }

    @Nullable
    public static AEKeyType fromRawId(int id) {
        Preconditions.checkArgument((id >= 0 && id <= 127 ? 1 : 0) != 0, (String)"id out of range: %d", (int)id);
        return (AEKeyType)AEKeyTypesInternal.getRegistry().byId(id);
    }

    public static AEKeyType fluids() {
        return AEFluidKeys.INSTANCE;
    }

    public final ResourceLocation getId() {
        return this.id;
    }

    public final Class<? extends AEKey> getKeyClass() {
        return this.keyClass;
    }

    public final byte getRawId() {
        int id = AEKeyTypesInternal.getRegistry().getId((Object)this);
        if (id < 0 || id > 127) {
            throw new IllegalStateException("Key type " + String.valueOf(this) + " has an invalid numeric id: " + id);
        }
        return (byte)id;
    }

    public int getAmountPerOperation() {
        return 1;
    }

    public int getAmountPerByte() {
        return 8;
    }

    @Nullable
    public abstract AEKey readFromPacket(RegistryFriendlyByteBuf var1);

    @Nullable
    public AEKey loadKeyFromTag(HolderLookup.Provider registries, CompoundTag tag) {
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        try {
            return (AEKey)((Pair)this.codec().codec().decode((DynamicOps)ops, (Object)tag).getOrThrow()).getFirst();
        }
        catch (Exception e) {
            AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
            return null;
        }
    }

    @Nullable
    public final AEKey tryCast(AEKey key) {
        return this.keyClass.isInstance(key) ? this.keyClass.cast(key) : null;
    }

    public final boolean contains(AEKey key) {
        return this.keyClass.isInstance(key);
    }

    public boolean supportsFuzzyRangeSearch() {
        return false;
    }

    public final AEKeyFilter filter() {
        return this.filter;
    }

    public String toString() {
        return this.id.toString();
    }

    public Component getDescription() {
        return this.description;
    }

    @Nullable
    public String getUnitSymbol() {
        return null;
    }

    public int getAmountPerUnit() {
        return 1;
    }

    public final String formatAmount(long amount, AmountFormat format) {
        return switch (format) {
            default -> throw new MatchException(null, null);
            case AmountFormat.FULL -> this.formatFullAmount(amount);
            case AmountFormat.SLOT -> this.formatShortAmount(amount, 4);
            case AmountFormat.SLOT_LARGE_FONT -> this.formatShortAmount(amount, 3);
        };
    }

    private String formatFullAmount(long amount) {
        StringBuilder result = new StringBuilder();
        if (this.getAmountPerUnit() > 1) {
            double units = (double)amount / (double)this.getAmountPerUnit();
            result.append(NumberFormat.getNumberInstance().format(units));
        } else {
            result.append(NumberFormat.getNumberInstance().format(amount));
        }
        String unit = this.getUnitSymbol();
        if (unit != null) {
            result.append(' ').append(unit);
        }
        return result.toString();
    }

    private String formatShortAmount(long amount, int maxWidth) {
        if (this.getAmountPerUnit() > 1) {
            double units = (double)amount / (double)this.getAmountPerUnit();
            return ReadableNumberConverter.format(units, maxWidth);
        }
        return ReadableNumberConverter.format(amount, maxWidth);
    }

    public Stream<TagKey<?>> getTagNames() {
        return Stream.empty();
    }
}

