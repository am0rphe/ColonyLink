/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 */
package appeng.core.network.serverbound;

import appeng.api.features.HotkeyAction;
import appeng.client.Hotkey;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.hotkeys.HotkeyActions;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record HotkeyPacket(String hotkey) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, HotkeyPacket> STREAM_CODEC = StreamCodec.ofMember(HotkeyPacket::write, HotkeyPacket::decode);
    public static final CustomPacketPayload.Type<HotkeyPacket> TYPE = CustomAppEngPayload.createType("hotkey");

    public HotkeyPacket(Hotkey hotkey) {
        this(hotkey.name());
    }

    public CustomPacketPayload.Type<HotkeyPacket> type() {
        return TYPE;
    }

    public static HotkeyPacket decode(RegistryFriendlyByteBuf stream) {
        String hotkey = stream.readUtf();
        return new HotkeyPacket(hotkey);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeUtf(this.hotkey);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        List<HotkeyAction> actions = HotkeyActions.REGISTRY.get(this.hotkey);
        if (actions == null) {
            player.sendSystemMessage((Component)PlayerMessages.UnknownHotkey.text().copy().append((Component)Component.translatable((String)("key.ae2." + this.hotkey))));
            AELog.warn("Player %s tried using unknown hotkey \"%s\"", player, this.hotkey);
            return;
        }
        for (HotkeyAction action : actions) {
            if (action.run((Player)player)) break;
        }
    }
}

