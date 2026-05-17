/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 */
package appeng.api.stacks;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

final class AEItemKeys
extends AEKeyType {
    private static final ResourceLocation ID = AppEng.makeId("i");
    static final AEItemKeys INSTANCE = new AEItemKeys();

    private AEItemKeys() {
        super(ID, AEItemKey.class, (Component)GuiText.Items.text());
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return AEItemKey.MAP_CODEC;
    }

    @Override
    public AEItemKey readFromPacket(RegistryFriendlyByteBuf input) {
        Objects.requireNonNull(input);
        return AEItemKey.fromPacket(input);
    }

    @Override
    public AEItemKey loadKeyFromTag(HolderLookup.Provider registries, CompoundTag tag) {
        return AEItemKey.fromTag(registries, tag);
    }

    @Override
    public boolean supportsFuzzyRangeSearch() {
        return true;
    }

    @Override
    public Stream<TagKey<?>> getTagNames() {
        return BuiltInRegistries.ITEM.getTagNames().map(t -> t);
    }
}

