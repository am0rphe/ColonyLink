/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.locator;

import appeng.menu.locator.ItemMenuHostLocator;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

record InventoryItemLocator(int itemIndex, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator
{
    @Override
    public ItemStack locateItem(Player player) {
        return player.getInventory().getItem(this.itemIndex);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(this.itemIndex);
        buf.writeOptional(Optional.ofNullable(this.hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static InventoryItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new InventoryItemLocator(buf.readInt(), buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("slot ").append(this.itemIndex);
        if (this.hitResult != null) {
            result.append(" used on ").append(this.hitResult.getBlockPos());
        }
        return result.toString();
    }

    @Override
    public Integer getPlayerInventorySlot() {
        return this.itemIndex;
    }
}

