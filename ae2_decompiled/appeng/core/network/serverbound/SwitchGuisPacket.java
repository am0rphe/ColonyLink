/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public record SwitchGuisPacket(@Nullable MenuType<? extends ISubMenu> newGui) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchGuisPacket> STREAM_CODEC = StreamCodec.ofMember(SwitchGuisPacket::write, SwitchGuisPacket::decode);
    public static final CustomPacketPayload.Type<SwitchGuisPacket> TYPE = CustomAppEngPayload.createType("switch_guis");

    public CustomPacketPayload.Type<SwitchGuisPacket> type() {
        return TYPE;
    }

    public static SwitchGuisPacket decode(RegistryFriendlyByteBuf stream) {
        MenuType newGui = null;
        if (stream.readBoolean()) {
            newGui = (MenuType)BuiltInRegistries.MENU.get(stream.readResourceLocation());
        }
        return new SwitchGuisPacket(newGui);
    }

    public void write(RegistryFriendlyByteBuf data) {
        if (this.newGui != null) {
            data.writeBoolean(true);
            data.writeResourceLocation(BuiltInRegistries.MENU.getKey(this.newGui));
        } else {
            data.writeBoolean(false);
        }
    }

    public static SwitchGuisPacket openSubMenu(MenuType<? extends ISubMenu> menuType) {
        return new SwitchGuisPacket(menuType);
    }

    public static SwitchGuisPacket returnToParentMenu() {
        return new SwitchGuisPacket((MenuType<? extends ISubMenu>)((MenuType)null));
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (this.newGui != null) {
            this.doOpenSubMenu(player);
        } else {
            this.doReturnToParentMenu(player);
        }
    }

    private void doOpenSubMenu(ServerPlayer player) {
        AEBaseMenu bc;
        MenuHostLocator locator;
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof AEBaseMenu && (locator = (bc = (AEBaseMenu)abstractContainerMenu).getLocator()) != null) {
            MenuOpener.open(this.newGui, (Player)player, locator);
        }
    }

    private void doReturnToParentMenu(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof ISubMenu) {
            ISubMenu subMenu = (ISubMenu)abstractContainerMenu;
            subMenu.getHost().returnToMainMenu((Player)player, subMenu);
        }
    }
}

