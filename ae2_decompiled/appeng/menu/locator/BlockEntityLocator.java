/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.locator;

import appeng.core.AELog;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

record BlockEntityLocator(BlockPos pos) implements MenuHostLocator
{
    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static BlockEntityLocator readFromPacket(FriendlyByteBuf buf) {
        return new BlockEntityLocator(buf.readBlockPos());
    }

    @Override
    @Nullable
    public <T> T locate(Player player, Class<T> hostInterface) {
        BlockEntity blockEntity = player.level().getBlockEntity(this.pos);
        if (hostInterface.isInstance(blockEntity)) {
            return hostInterface.cast(blockEntity);
        }
        if (blockEntity != null) {
            AELog.warn("Cannot locate menu host @ %s, %s does not implement %s", this.pos, blockEntity, hostInterface);
        }
        return null;
    }

    @Override
    public String toString() {
        return "BlockEntity{pos=" + String.valueOf(this.pos) + "}";
    }
}

