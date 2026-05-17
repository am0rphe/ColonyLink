/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.bidirectional;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ConfigValuePacket(String name, String value) implements ClientboundPacket,
ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigValuePacket> STREAM_CODEC = StreamCodec.ofMember(ConfigValuePacket::write, ConfigValuePacket::decode);
    public static final CustomPacketPayload.Type<ConfigValuePacket> TYPE = CustomAppEngPayload.createType("config_value");

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, T value) {
        this(setting.getName(), value.name());
        if (!setting.getValues().contains(value)) {
            throw new IllegalStateException(String.valueOf(value) + " not a valid value for " + String.valueOf(setting));
        }
    }

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, IConfigManager configManager) {
        this(setting, setting.getValue(configManager));
    }

    public CustomPacketPayload.Type<ConfigValuePacket> type() {
        return TYPE;
    }

    public static ConfigValuePacket decode(RegistryFriendlyByteBuf stream) {
        String name = stream.readUtf();
        String value = stream.readUtf();
        return new ConfigValuePacket(name, value);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeUtf(this.name);
        data.writeUtf(this.value);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof IConfigurableObject) {
            IConfigurableObject configurableObject = (IConfigurableObject)abstractContainerMenu;
            this.loadSetting(configurableObject);
        }
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof IConfigurableObject) {
            IConfigurableObject configurableObject = (IConfigurableObject)abstractContainerMenu;
            this.loadSetting(configurableObject);
        }
    }

    private void loadSetting(IConfigurableObject configurableObject) {
        IConfigManager cm = configurableObject.getConfigManager();
        for (Setting<?> setting : cm.getSettings()) {
            if (!setting.getName().equals(this.name)) continue;
            setting.setFromString(cm, this.value);
            break;
        }
    }
}

