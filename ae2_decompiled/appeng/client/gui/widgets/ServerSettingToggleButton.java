/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.client.gui.widgets;

import appeng.api.config.Setting;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.network.serverbound.ConfigButtonPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerSettingToggleButton<T extends Enum<T>>
extends SettingToggleButton<T> {
    public ServerSettingToggleButton(Setting<T> setting, T val) {
        super(setting, val, ServerSettingToggleButton::sendToServer);
    }

    private static <T extends Enum<T>> void sendToServer(SettingToggleButton<T> button, boolean backwards) {
        ConfigButtonPacket message = new ConfigButtonPacket(button.getSetting(), backwards);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

