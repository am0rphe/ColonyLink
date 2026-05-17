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
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.neoforge.fluids.FluidStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.stacks;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class AEFluidKey
extends AEKey {
    public static final MapCodec<AEFluidKey> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.FLUID.holderByNameCodec().validate(holder -> holder.is((Holder)Fluids.EMPTY.builtInRegistryHolder()) ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success((Object)holder)).fieldOf("id").forGetter(key -> key.stack.getFluidHolder()), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(key -> key.stack.getComponentsPatch())).apply((Applicative)instance, (fluidHolder, dataComponentPatch) -> new AEFluidKey(new FluidStack(fluidHolder, 1, dataComponentPatch))));
    public static final Codec<AEFluidKey> CODEC = MAP_CODEC.codec();
    public static final int AMOUNT_BUCKET = 1000;
    public static final int AMOUNT_BLOCK = 1000;
    private final FluidStack stack;
    private final int hashCode;

    private AEFluidKey(FluidStack stack) {
        Preconditions.checkArgument((!stack.isEmpty() ? 1 : 0) != 0, (Object)"stack was empty");
        this.stack = stack;
        this.hashCode = FluidStack.hashFluidAndComponents((FluidStack)stack);
    }

    public static AEFluidKey of(Fluid fluid) {
        return AEFluidKey.of(new FluidStack(fluid, 1));
    }

    @Nullable
    public static AEFluidKey of(FluidStack fluidVariant) {
        if (fluidVariant.isEmpty()) {
            return null;
        }
        return new AEFluidKey(fluidVariant.copyWithAmount(1));
    }

    public static boolean matches(AEKey what, FluidStack fluid) {
        AEFluidKey fluidKey;
        return what instanceof AEFluidKey && (fluidKey = (AEFluidKey)what).matches(fluid);
    }

    public static boolean is(AEKey what) {
        return what instanceof AEFluidKey;
    }

    public static AEKeyFilter filter() {
        return AEFluidKey::is;
    }

    public boolean matches(FluidStack variant) {
        return FluidStack.isSameFluidSameComponents((FluidStack)this.stack, (FluidStack)variant);
    }

    @Override
    public AEKeyType getType() {
        return AEKeyType.fluids();
    }

    @Override
    public AEFluidKey dropSecondary() {
        return AEFluidKey.of(new FluidStack(this.getFluid(), 1));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AEFluidKey aeFluidKey = (AEFluidKey)o;
        return this.hashCode == aeFluidKey.hashCode && FluidStack.isSameFluidSameComponents((FluidStack)this.stack, (FluidStack)aeFluidKey.stack);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public static AEFluidKey fromTag(HolderLookup.Provider registries, CompoundTag tag) {
        RegistryOps ops = registries.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
        try {
            return (AEFluidKey)((Pair)CODEC.decode((DynamicOps)ops, (Object)tag).getOrThrow()).getFirst();
        }
        catch (Exception e) {
            AELog.debug("Tried to load an invalid fluid key from NBT: %s", tag, e);
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
        return this.getFluid();
    }

    @Override
    public ResourceLocation getId() {
        return BuiltInRegistries.FLUID.getKey((Object)this.getFluid());
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
    }

    @Override
    protected Component computeDisplayName() {
        return this.stack.getHoverName();
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

    public FluidStack toStack(int amount) {
        return this.stack.copyWithAmount(amount);
    }

    public Fluid getFluid() {
        return this.stack.getFluid();
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        FluidStack.STREAM_CODEC.encode((Object)data, (Object)this.stack);
    }

    public static AEFluidKey fromPacket(RegistryFriendlyByteBuf data) {
        FluidStack stack = (FluidStack)FluidStack.STREAM_CODEC.decode((Object)data);
        return new AEFluidKey(stack);
    }

    public static boolean is(@Nullable GenericStack stack) {
        return stack != null && stack.what() instanceof AEFluidKey;
    }

    public String toString() {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey((Object)this.getFluid());
        String idString = id != BuiltInRegistries.FLUID.getDefaultKey() ? id.toString() : this.getFluid().getClass().getName() + "(unregistered)";
        return this.stack.getComponents().isEmpty() ? idString : idString + " (+components)";
    }
}

