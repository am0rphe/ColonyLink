/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.crafting.CraftingStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record CraftingStatusPacket(int containerId, CraftingStatus status) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatusPacket> STREAM_CODEC = StreamCodec.ofMember(CraftingStatusPacket::write, CraftingStatusPacket::decode);
    public static final CustomPacketPayload.Type<CraftingStatusPacket> TYPE = CustomAppEngPayload.createType("crafting_status");

    public CustomPacketPayload.Type<CraftingStatusPacket> type() {
        return TYPE;
    }

    public static CraftingStatusPacket decode(RegistryFriendlyByteBuf buffer) {
        return new CraftingStatusPacket(buffer.readInt(), CraftingStatus.read(buffer));
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeInt(this.containerId);
        this.status.write(data);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        if (player.containerMenu == null || player.containerMenu.containerId != this.containerId) {
            return;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof CraftingCPUScreen) {
            CraftingCPUScreen cpuScreen = (CraftingCPUScreen)screen;
            cpuScreen.postUpdate(this.status);
        }
    }
}

