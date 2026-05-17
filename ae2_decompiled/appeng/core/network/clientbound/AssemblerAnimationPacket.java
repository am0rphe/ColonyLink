/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.api.stacks.AEKey;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record AssemblerAnimationPacket(BlockPos pos, byte rate, AEKey what) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblerAnimationPacket> STREAM_CODEC = StreamCodec.ofMember(AssemblerAnimationPacket::write, AssemblerAnimationPacket::decode);
    public static final CustomPacketPayload.Type<AssemblerAnimationPacket> TYPE = CustomAppEngPayload.createType("assembler_animation");

    public CustomPacketPayload.Type<AssemblerAnimationPacket> type() {
        return TYPE;
    }

    public static AssemblerAnimationPacket decode(RegistryFriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        byte rate = data.readByte();
        AEKey what = AEKey.readKey(data);
        return new AssemblerAnimationPacket(pos, rate, what);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockPos(this.pos);
        data.writeByte(this.rate);
        AEKey.writeKey(data, this.what);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        BlockEntity te = player.getCommandSenderWorld().getBlockEntity(this.pos);
        if (te instanceof MolecularAssemblerBlockEntity) {
            MolecularAssemblerBlockEntity ma = (MolecularAssemblerBlockEntity)te;
            ma.setAnimationStatus(new AssemblerAnimationStatus(this.rate, this.what.wrapForDisplayOrFilter()));
        }
    }
}

