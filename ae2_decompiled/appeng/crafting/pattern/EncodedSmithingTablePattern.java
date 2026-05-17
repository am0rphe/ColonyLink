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

public record EncodedSmithingTablePattern(ItemStack template, ItemStack base, ItemStack addition, ItemStack resultItem, boolean canSubstitute, ResourceLocation recipeId) {
    public static final Codec<EncodedSmithingTablePattern> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)ItemStack.CODEC.fieldOf("template").forGetter(EncodedSmithingTablePattern::template), (App)ItemStack.CODEC.fieldOf("base").forGetter(EncodedSmithingTablePattern::base), (App)ItemStack.CODEC.fieldOf("addition").forGetter(EncodedSmithingTablePattern::addition), (App)ItemStack.CODEC.fieldOf("resultItem").forGetter(EncodedSmithingTablePattern::resultItem), (App)Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedSmithingTablePattern::canSubstitute), (App)ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedSmithingTablePattern::recipeId)).apply((Applicative)builder, EncodedSmithingTablePattern::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedSmithingTablePattern> STREAM_CODEC = StreamCodec.composite((StreamCodec)ItemStack.STREAM_CODEC, EncodedSmithingTablePattern::template, (StreamCodec)ItemStack.STREAM_CODEC, EncodedSmithingTablePattern::base, (StreamCodec)ItemStack.STREAM_CODEC, EncodedSmithingTablePattern::addition, (StreamCodec)ItemStack.STREAM_CODEC, EncodedSmithingTablePattern::resultItem, (StreamCodec)ByteBufCodecs.BOOL, EncodedSmithingTablePattern::canSubstitute, (StreamCodec)ResourceLocation.STREAM_CODEC, EncodedSmithingTablePattern::recipeId, EncodedSmithingTablePattern::new);

    public boolean containsMissingContent() {
        return AEItems.MISSING_CONTENT.is(this.template) || AEItems.MISSING_CONTENT.is(this.base) || AEItems.MISSING_CONTENT.is(this.addition) || AEItems.MISSING_CONTENT.is(this.resultItem);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        EncodedSmithingTablePattern that = (EncodedSmithingTablePattern)object;
        return this.canSubstitute == that.canSubstitute && ItemStack.matches((ItemStack)this.base, (ItemStack)that.base) && ItemStack.matches((ItemStack)this.template, (ItemStack)that.template) && ItemStack.matches((ItemStack)this.addition, (ItemStack)that.addition) && ItemStack.matches((ItemStack)this.resultItem, (ItemStack)that.resultItem) && this.recipeId.equals((Object)that.recipeId);
    }

    @Override
    public int hashCode() {
        int result = ItemStack.hashItemAndComponents((ItemStack)this.template);
        result = 31 * result + ItemStack.hashItemAndComponents((ItemStack)this.base);
        result = 31 * result + ItemStack.hashItemAndComponents((ItemStack)this.addition);
        result = 31 * result + ItemStack.hashItemAndComponents((ItemStack)this.resultItem);
        result = 31 * result + Boolean.hashCode(this.canSubstitute);
        result = 31 * result + this.recipeId.hashCode();
        return result;
    }
}

