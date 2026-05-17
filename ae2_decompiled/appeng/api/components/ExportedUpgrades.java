/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.components;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ExportedUpgrades(List<ItemStack> upgrades) {
    public static Codec<ExportedUpgrades> CODEC = ItemStack.CODEC.listOf().xmap(ExportedUpgrades::new, ExportedUpgrades::upgrades);
    public static StreamCodec<RegistryFriendlyByteBuf, ExportedUpgrades> STREAM_CODEC = StreamCodec.composite((StreamCodec)ItemStack.LIST_STREAM_CODEC, ExportedUpgrades::upgrades, ExportedUpgrades::new);

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ExportedUpgrades)) {
            return false;
        }
        ExportedUpgrades that = (ExportedUpgrades)object;
        return ItemStack.listMatches(this.upgrades, that.upgrades);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.upgrades);
    }
}

