/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.locator;

import appeng.integration.modules.curios.CuriosIntegration;
import appeng.menu.locator.ItemMenuHostLocator;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

record CuriosItemLocator(int curioSlot, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator
{
    @Override
    public ItemStack locateItem(Player player) {
        IItemHandler cap = (IItemHandler)player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null || this.curioSlot >= cap.getSlots()) {
            return ItemStack.EMPTY;
        }
        return cap.getStackInSlot(this.curioSlot);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(this.curioSlot);
        buf.writeOptional(Optional.ofNullable(this.hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(buf.readInt(), buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        return "curios slot " + this.curioSlot;
    }
}

