/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.locator;

import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.core.AELog;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

record PartLocator(BlockPos pos, @Nullable Direction side) implements MenuHostLocator
{
    @Override
    @Nullable
    public <T> T locate(Player player, Class<T> hostInterface) {
        IPart part = PartHelper.getPart((BlockGetter)player.level(), this.pos, this.side);
        if (hostInterface.isInstance(part)) {
            return hostInterface.cast(part);
        }
        if (part != null) {
            AELog.warn("Part at %s does not implement host interface %s", part, hostInterface);
        }
        return null;
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.side != null);
        if (this.side != null) {
            buf.writeByte(this.side.ordinal());
        }
    }

    public static PartLocator readFromPacket(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction side = null;
        if (buf.readBoolean()) {
            side = Direction.values()[buf.readByte()];
        }
        return new PartLocator(pos, side);
    }
}

