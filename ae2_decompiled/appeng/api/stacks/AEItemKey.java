/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentPatch
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.stacks;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class AEItemKey
extends AEKey {
    public static final MapCodec<AEItemKey> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)BuiltInRegistries.ITEM.holderByNameCodec().validate(item -> item.is((Holder)Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success((Object)item)).fieldOf("id").forGetter(key -> key.stack.getItemHolder()), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(key -> key.stack.getComponentsPatch())).apply((Applicative)builder, (item, componentPatch) -> new AEItemKey(new ItemStack(item, 1, componentPatch))));
    public static final Codec<AEItemKey> CODEC = MAP_CODEC.codec();
    private final ItemStack stack;
    private final int hashCode;
    private final int maxStackSize;
    private final int damage;

    private AEItemKey(ItemStack stack) {
        Preconditions.checkArgument((!stack.isEmpty() ? 1 : 0) != 0, (Object)"stack is empty");
        this.stack = stack;
        this.hashCode = ItemStack.hashItemAndComponents((ItemStack)stack);
        this.maxStackSize = stack.getMaxStackSize();
        this.damage = stack.getDamageValue();
    }

    @Nullable
    public static AEItemKey of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return new AEItemKey(stack.copy());
    }

    public static boolean matches(AEKey what, ItemStack itemStack) {
        AEItemKey itemKey;
        return what instanceof AEItemKey && (itemKey = (AEItemKey)what).matches(itemStack);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEItemKey;
    }

    public static AEKeyFilter filter() {
        return AEItemKey::is;
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.items();
    }

    @Override
    public AEItemKey dropSecondary() {
        return AEItemKey.of(this.stack.getItem().getDefaultInstance());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AEItemKey aeItemKey = (AEItemKey)o;
        return this.hashCode == aeItemKey.hashCode && ItemStack.isSameItemSameComponents((ItemStack)this.stack, (ItemStack)aeItemKey.stack);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public static AEItemKey of(ItemLike item) {
        return AEItemKey.of(item.asItem().getDefaultInstance());
    }

    public boolean is(ItemLike item) {
        return this.stack.is(item.asItem());
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && ItemStack.isSameItemSameComponents((ItemStack)this.stack, (ItemStack)stack);
    }

    public boolean matches(Ingredient ingredient) {
        return ingredient.test(this.getReadOnlyStack());
    }

    public ItemStack getReadOnlyStack() {
        return this.stack;
    }

    public ItemStack toStack() {
        return this.toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }
        return this.stack.copyWithCount(count);
    }

    public Item getItem() {
        return this.stack.getItem();
    }

    @Nullable
    public static AEItemKey fromTag(HolderLookup.Provider registries, CompoundTag tag) {
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        try {
            return (AEItemKey)((Pair)CODEC.decode((DynamicOps)ops, (Object)tag).getOrThrow()).getFirst();
        }
        catch (Exception e) {
            AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
            return null;
        }
    }

    @Override
    public CompoundTag toTag(HolderLookup.Provider registries) {
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        return (CompoundTag)CODEC.encodeStart((DynamicOps)ops, (Object)this).getOrThrow();
    }

    @Override
    public Object getPrimaryKey() {
        return this.stack.getItem();
    }

    @Override
    public int getFuzzySearchValue() {
        return this.damage;
    }

    @Override
    public int getFuzzySearchMaxValue() {
        return this.getReadOnlyStack().getMaxDamage();
    }

    @Override
    public ResourceLocation getId() {
        return BuiltInRegistries.ITEM.getKey((Object)this.stack.getItem());
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return this.toStack();
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        while (amount > 0L) {
            if (drops.size() > 1000) {
                AELog.warn("Tried dropping an excessive amount of items, ignoring %s %ss", amount, this.stack.getItem());
                break;
            }
            long taken = Math.min(amount, (long)this.getMaxStackSize());
            amount -= taken;
            drops.add(this.toStack((int)taken));
        }
    }

    @Override
    protected Component computeDisplayName() {
        return this.getReadOnlyStack().getHoverName();
    }

    @Override
    public boolean isTagged(TagKey<?> tag) {
        return this.stack.is(tag);
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<T> type) {
        return (T)this.stack.get(type);
    }

    @Override
    public boolean hasComponents() {
        return this.stack.getComponents().isEmpty();
    }

    public boolean isDamaged() {
        return this.damage > 0;
    }

    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        ItemStack.STREAM_CODEC.encode((Object)data, (Object)this.stack);
    }

    public static AEItemKey fromPacket(RegistryFriendlyByteBuf data) {
        ItemStack stack = (ItemStack)ItemStack.STREAM_CODEC.decode((Object)data);
        return new AEItemKey(stack);
    }

    public String toString() {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey((Object)this.stack.getItem());
        String idString = id != BuiltInRegistries.ITEM.getDefaultKey() ? id.toString() : this.stack.getItem().getClass().getName() + "(unregistered)";
        return this.stack.isComponentsPatchEmpty() ? idString : idString + " (with patches)";
    }
}

