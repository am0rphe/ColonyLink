/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.core.AEConfig;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record CraftingJobStatusPacket(UUID jobId, AEKey what, long requestedAmount, long remainingAmount, Status status) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingJobStatusPacket> STREAM_CODEC = StreamCodec.ofMember(CraftingJobStatusPacket::write, CraftingJobStatusPacket::decode);
    public static final CustomPacketPayload.Type<CraftingJobStatusPacket> TYPE = CustomAppEngPayload.createType("crafting_job_status");

    public CustomPacketPayload.Type<CraftingJobStatusPacket> type() {
        return TYPE;
    }

    public static CraftingJobStatusPacket decode(RegistryFriendlyByteBuf stream) {
        UUID jobId = stream.readUUID();
        Status status = (Status)stream.readEnum(Status.class);
        AEKey what = AEKey.readKey(stream);
        long requestedAmount = stream.readLong();
        long remainingAmount = stream.readLong();
        return new CraftingJobStatusPacket(jobId, what, requestedAmount, remainingAmount, status);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeUUID(this.jobId);
        data.writeEnum((Enum)this.status);
        AEKey.writeKey(data, this.what);
        data.writeLong(this.requestedAmount);
        data.writeLong(this.remainingAmount);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        if (this.status == Status.STARTED && AEConfig.instance().isPinAutoCraftedItems()) {
            PinnedKeys.pinKey(this.what, PinnedKeys.PinReason.CRAFTING);
        }
        PendingCraftingJobs.jobStatus(this.jobId, this.what, this.requestedAmount, this.remainingAmount, this.status);
    }

    public static enum Status {
        STARTED,
        CANCELLED,
        FINISHED;

    }
}

