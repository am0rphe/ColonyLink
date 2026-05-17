/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package appeng.crafting.pattern;

import appeng.core.definitions.AEItems;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record EncodedStonecuttingPattern(ItemStack input, ItemStack output, boolean canSubstitute, ResourceLocation recipeId) {
    public static final Codec<EncodedStonecuttingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)ItemStack.CODEC.fieldOf("input").forGetter(EncodedStonecuttingPattern::input), (App)ItemStack.CODEC.fieldOf("output").forGetter(EncodedStonecuttingPattern::output), (App)Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedStonecuttingPattern::canSubstitute), (App)ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedStonecuttingPattern::recipeId)).apply((Applicative)builder, EncodedStonecuttingPattern::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedStonecuttingPattern> STREAM_CODEC = StreamCodec.composite((StreamCodec)ItemStack.STREAM_CODEC, EncodedStonecuttingPattern::input, (StreamCodec)ItemStack.STREAM_CODEC, EncodedStonecuttingPattern::output, (StreamCodec)ByteBufCodecs.BOOL, EncodedStonecuttingPattern::canSubstitute, (StreamCodec)ResourceLocation.STREAM_CODEC, EncodedStonecuttingPattern::recipeId, EncodedStonecuttingPattern::new);

    public boolean containsMissingContent() {
        return AEItems.MISSING_CONTENT.is(this.input) || AEItems.MISSING_CONTENT.is(this.output);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        EncodedStonecuttingPattern that = (EncodedStonecuttingPattern)object;
        return this.canSubstitute == that.canSubstitute && ItemStack.matches((ItemStack)this.input, (ItemStack)that.input) && ItemStack.matches((ItemStack)this.output, (ItemStack)that.output) && this.recipeId.equals((Object)that.recipeId);
    }

    @Override
    public int hashCode() {
        int result = ItemStack.hashItemAndComponents((ItemStack)this.input);
        result = 31 * result + ItemStack.hashItemAndComponents((ItemStack)this.output);
        result = 31 * result + Boolean.hashCode(this.canSubstitute);
        result = 31 * result + this.recipeId.hashCode();
        return result;
    }
}

