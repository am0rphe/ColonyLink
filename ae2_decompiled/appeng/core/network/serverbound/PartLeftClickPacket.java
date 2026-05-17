/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.CommonHooks
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 */
package appeng.core.network.serverbound;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public record PartLeftClickPacket(BlockHitResult hitResult, boolean alternateUseMode) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, PartLeftClickPacket> STREAM_CODEC = StreamCodec.ofMember(PartLeftClickPacket::write, PartLeftClickPacket::decode);
    public static final CustomPacketPayload.Type<PartLeftClickPacket> TYPE = CustomAppEngPayload.createType("part_left_click");

    public CustomPacketPayload.Type<PartLeftClickPacket> type() {
        return TYPE;
    }

    public static PartLeftClickPacket decode(RegistryFriendlyByteBuf stream) {
        BlockHitResult hitResult = stream.readBlockHitResult();
        boolean alternateUseMode = stream.readBoolean();
        return new PartLeftClickPacket(hitResult, alternateUseMode);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockHitResult(this.hitResult);
        data.writeBoolean(this.alternateUseMode);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        PlayerInteractEvent.LeftClickBlock evt = CommonHooks.onLeftClickBlock((Player)player, (BlockPos)this.hitResult.getBlockPos(), (Direction)this.hitResult.getDirection(), (ServerboundPlayerActionPacket.Action)ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
        NeoForge.EVENT_BUS.post((Event)evt);
        if (evt.isCanceled()) {
            return;
        }
        Vec3 localPos = this.hitResult.getLocation().subtract((double)this.hitResult.getBlockPos().getX(), (double)this.hitResult.getBlockPos().getY(), (double)this.hitResult.getBlockPos().getZ());
        BlockEntity blockEntity = player.level().getBlockEntity(this.hitResult.getBlockPos());
        if (blockEntity instanceof IPartHost) {
            IPartHost partHost = (IPartHost)blockEntity;
            SelectedPart selectedPart = partHost.selectPartLocal(localPos);
            if (selectedPart.part != null) {
                if (!this.alternateUseMode) {
                    selectedPart.part.onClicked((Player)player, localPos);
                } else {
                    selectedPart.part.onShiftClicked((Player)player, localPos);
                }
            } else if (selectedPart.facade != null) {
                selectedPart.facade.onClicked((Player)player, localPos);
            }
        }
    }
}

