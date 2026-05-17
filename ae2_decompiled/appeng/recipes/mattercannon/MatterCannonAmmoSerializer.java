/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package appeng.recipes.mattercannon;

import appeng.recipes.mattercannon.MatterCannonAmmo;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MatterCannonAmmoSerializer
implements RecipeSerializer<MatterCannonAmmo> {
    public static final MatterCannonAmmoSerializer INSTANCE = new MatterCannonAmmoSerializer();

    private MatterCannonAmmoSerializer() {
    }

    public MapCodec<MatterCannonAmmo> codec() {
        return MatterCannonAmmo.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, MatterCannonAmmo> streamCodec() {
        return MatterCannonAmmo.STREAM_CODEC;
    }
}

