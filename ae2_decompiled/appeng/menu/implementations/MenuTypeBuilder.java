/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.Nameable
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.neoforged.neoforge.common.extensions.IMenuTypeExtension
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.implementations;

import appeng.core.AppEng;
import appeng.init.InitMenuTypes;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import com.google.common.base.Preconditions;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import org.jetbrains.annotations.Nullable;

public final class MenuTypeBuilder<M extends AEBaseMenu, I> {
    @Nullable
    private ResourceLocation id;
    private final Class<I> hostInterface;
    private final MenuFactory<M, I> factory;
    private Function<I, Component> menuTitleStrategy = this::getDefaultMenuTitle;
    @Nullable
    private InitialDataSerializer<I> initialDataSerializer;
    @Nullable
    private InitialDataDeserializer<M, I> initialDataDeserializer;
    private MenuType<M> menuType;

    private MenuTypeBuilder(Class<I> hostInterface, TypedMenuFactory<M, I> typedFactory) {
        this.hostInterface = hostInterface;
        this.factory = (containerId, playerInv, accessObj) -> (AEBaseMenu)((Object)((Object)typedFactory.create(this.menuType, containerId, playerInv, accessObj)));
    }

    private MenuTypeBuilder(Class<I> hostInterface, MenuFactory<M, I> factory) {
        this.hostInterface = hostInterface;
        this.factory = factory;
    }

    public static <C extends AEBaseMenu, I> MenuTypeBuilder<C, I> create(MenuFactory<C, I> factory, Class<I> hostInterface) {
        return new MenuTypeBuilder<C, I>(hostInterface, factory);
    }

    public static <C extends AEBaseMenu, I> MenuTypeBuilder<C, I> create(TypedMenuFactory<C, I> factory, Class<I> hostInterface) {
        return new MenuTypeBuilder<C, I>(hostInterface, factory);
    }

    public MenuTypeBuilder<M, I> withMenuTitle(Function<I, Component> menuTitleStrategy) {
        this.menuTitleStrategy = menuTitleStrategy;
        return this;
    }

    public MenuTypeBuilder<M, I> withInitialData(InitialDataSerializer<I> initialDataSerializer, InitialDataDeserializer<M, I> initialDataDeserializer) {
        this.initialDataSerializer = initialDataSerializer;
        this.initialDataDeserializer = initialDataDeserializer;
        return this;
    }

    private M fromNetwork(int containerId, Inventory inv, RegistryFriendlyByteBuf packetBuf) {
        MenuHostLocator locator = MenuLocators.readFromPacket((FriendlyByteBuf)packetBuf);
        I host = locator.locate(inv.player, this.hostInterface);
        if (host == null) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.send((Packet)new ServerboundContainerClosePacket(containerId));
            }
            throw new IllegalStateException("Couldn't find menu host at " + String.valueOf(locator) + " for " + String.valueOf(this.id) + " on client. Closing menu.");
        }
        AEBaseMenu menu = (AEBaseMenu)((Object)this.factory.create(containerId, inv, host));
        menu.setReturnedFromSubScreen(packetBuf.readBoolean());
        if (this.initialDataDeserializer != null) {
            this.initialDataDeserializer.deserializeInitialData(host, (M)((Object)menu), packetBuf);
        }
        return (M)((Object)menu);
    }

    private boolean open(final Player player, final MenuHostLocator locator, boolean fromSubMenu) {
        if (!(player instanceof ServerPlayer)) {
            return false;
        }
        final I accessInterface = locator.locate(player, this.hostInterface);
        if (accessInterface == null) {
            return false;
        }
        final Component title = this.menuTitleStrategy.apply(accessInterface);
        class AppEngMenuProvider
        implements MenuProvider {
            AppEngMenuProvider() {
            }

            public Component getDisplayName() {
                return title;
            }

            @Nullable
            public AbstractContainerMenu createMenu(int wnd, Inventory p, Player pl) {
                AEBaseMenu m = (AEBaseMenu)((Object)MenuTypeBuilder.this.factory.create(wnd, p, accessInterface));
                m.setLocator(locator);
                return m;
            }

            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return !(player.containerMenu instanceof AEBaseMenu);
            }
        }
        player.openMenu((MenuProvider)new AppEngMenuProvider(), buffer -> {
            MenuLocators.writeToPacket((FriendlyByteBuf)buffer, locator);
            buffer.writeBoolean(fromSubMenu);
            if (this.initialDataSerializer != null) {
                this.initialDataSerializer.serializeInitialData(accessInterface, (RegistryFriendlyByteBuf)buffer);
            }
        });
        return true;
    }

    public MenuType<M> build(String id) {
        return this.build(AppEng.makeId(id));
    }

    public MenuType<M> buildUnregistered(ResourceLocation id) {
        Preconditions.checkState((this.menuType == null ? 1 : 0) != 0, (Object)"build was already called");
        Preconditions.checkState((this.id == null ? 1 : 0) != 0, (Object)"id should not be set");
        this.id = id;
        this.menuType = IMenuTypeExtension.create(this::fromNetwork);
        MenuOpener.addOpener(this.menuType, this::open);
        return this.menuType;
    }

    public MenuType<M> build(ResourceLocation id) {
        MenuType<M> menuType = this.buildUnregistered(id);
        InitMenuTypes.queueRegistration(this.id, menuType);
        return menuType;
    }

    private Component getDefaultMenuTitle(I accessInterface) {
        Nameable nameable;
        if (accessInterface instanceof Nameable && (nameable = (Nameable)accessInterface).hasCustomName()) {
            return nameable.getCustomName();
        }
        return Component.empty();
    }

    @FunctionalInterface
    public static interface TypedMenuFactory<C extends AbstractContainerMenu, I> {
        public C create(MenuType<C> var1, int var2, Inventory var3, I var4);
    }

    @FunctionalInterface
    public static interface MenuFactory<C, I> {
        public C create(int var1, Inventory var2, I var3);
    }

    @FunctionalInterface
    public static interface InitialDataSerializer<I> {
        public void serializeInitialData(I var1, RegistryFriendlyByteBuf var2);
    }

    @FunctionalInterface
    public static interface InitialDataDeserializer<C, I> {
        public void deserializeInitialData(I var1, C var2, RegistryFriendlyByteBuf var3);
    }
}

