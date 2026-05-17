/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.hooks;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.network.serverbound.ColorApplicatorSelectColorPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ColorApplicatorPickColorHook {
    private ColorApplicatorPickColorHook() {
    }

    public static boolean onPickColor(Player player, BlockHitResult hitResult) {
        if (!AEItems.COLOR_APPLICATOR.is(player.getOffhandItem()) && !AEItems.COLOR_APPLICATOR.is(player.getMainHandItem())) {
            return false;
        }
        BlockEntity be = player.level().getBlockEntity(hitResult.getBlockPos());
        if (be instanceof IColorableBlockEntity) {
            IColorableBlockEntity colorableBlockEntity = (IColorableBlockEntity)be;
            ColorApplicatorSelectColorPacket message = new ColorApplicatorSelectColorPacket(colorableBlockEntity.getColor());
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return true;
        }
        return false;
    }
}

