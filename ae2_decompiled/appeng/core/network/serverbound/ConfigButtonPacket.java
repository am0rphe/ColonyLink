/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package appeng.core.network.serverbound;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.AELog;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;
import appeng.util.EnumCycler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ConfigButtonPacket(Setting<?> option, boolean rotationDirection) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigButtonPacket> STREAM_CODEC = StreamCodec.ofMember(ConfigButtonPacket::write, ConfigButtonPacket::decode);
    public static final CustomPacketPayload.Type<ConfigButtonPacket> TYPE = CustomAppEngPayload.createType("config_button");

    public CustomPacketPayload.Type<ConfigButtonPacket> type() {
        return TYPE;
    }

    public static ConfigButtonPacket decode(RegistryFriendlyByteBuf stream) {
        Setting<?> option = Settings.getOrThrow(stream.readUtf());
        boolean rotationDirection = stream.readBoolean();
        return new ConfigButtonPacket(option, rotationDirection);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeUtf(this.option.getName());
        data.writeBoolean(this.rotationDirection);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AEBaseMenu baseMenu;
        Object object;
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof AEBaseMenu && (object = (baseMenu = (AEBaseMenu)abstractContainerMenu).getTarget()) instanceof IConfigurableObject) {
            IConfigurableObject configurableObject = (IConfigurableObject)object;
            IConfigManager cm = configurableObject.getConfigManager();
            if (cm.hasSetting(this.option)) {
                this.cycleSetting(cm, this.option);
            } else {
                AELog.info("Ignoring unsupported setting %s sent by client on %s", this.option, baseMenu.getTarget());
            }
        }
    }

    private <T extends Enum<T>> void cycleSetting(IConfigManager cm, Setting<T> setting) {
        T currentValue = cm.getSetting(setting);
        T nextValue = EnumCycler.rotateEnum(currentValue, this.rotationDirection, setting.getValues());
        cm.putSetting(setting, nextValue);
    }
}

