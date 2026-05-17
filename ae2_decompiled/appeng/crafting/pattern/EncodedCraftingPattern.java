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
import appeng.util.AECodecs;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record EncodedCraftingPattern(List<ItemStack> inputs, ItemStack result, ResourceLocation recipeId, boolean canSubstitute, boolean canSubstituteFluids) {
    public static final Codec<EncodedCraftingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)AECodecs.FAULT_TOLERANT_OPTIONAL_ITEMSTACK_CODEC.listOf().fieldOf("inputs").forGetter(EncodedCraftingPattern::inputs), (App)AECodecs.FAULT_TOLERANT_ITEMSTACK_CODEC.fieldOf("result").forGetter(EncodedCraftingPattern::result), (App)ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedCraftingPattern::recipeId), (App)Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedCraftingPattern::canSubstitute), (App)Codec.BOOL.fieldOf("canSubstituteFluids").forGetter(EncodedCraftingPattern::canSubstituteFluids)).apply((Applicative)builder, EncodedCraftingPattern::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedCraftingPattern> STREAM_CODEC = StreamCodec.composite((StreamCodec)ItemStack.OPTIONAL_LIST_STREAM_CODEC, EncodedCraftingPattern::inputs, (StreamCodec)ItemStack.STREAM_CODEC, EncodedCraftingPattern::result, (StreamCodec)ResourceLocation.STREAM_CODEC, EncodedCraftingPattern::recipeId, (StreamCodec)ByteBufCodecs.BOOL, EncodedCraftingPattern::canSubstitute, (StreamCodec)ByteBufCodecs.BOOL, EncodedCraftingPattern::canSubstituteFluids, EncodedCraftingPattern::new);

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean containsMissingContent() {
        if (AEItems.MISSING_CONTENT.is(this.result)) return true;
        if (!this.inputs.stream().anyMatch(AEItems.MISSING_CONTENT::is)) return false;
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        EncodedCraftingPattern that = (EncodedCraftingPattern)object;
        return this.canSubstitute == that.canSubstitute && this.canSubstituteFluids == that.canSubstituteFluids && ItemStack.matches((ItemStack)this.result, (ItemStack)that.result) && ItemStack.listMatches(this.inputs, that.inputs) && this.recipeId.equals((Object)that.recipeId);
    }

    @Override
    public int hashCode() {
        int result1 = ItemStack.hashStackList(this.inputs);
        result1 = 31 * result1 + ItemStack.hashItemAndComponents((ItemStack)this.result);
        result1 = 31 * result1 + this.recipeId.hashCode();
        result1 = 31 * result1 + Boolean.hashCode(this.canSubstitute);
        result1 = 31 * result1 + Boolean.hashCode(this.canSubstituteFluids);
        return result1;
    }
}

