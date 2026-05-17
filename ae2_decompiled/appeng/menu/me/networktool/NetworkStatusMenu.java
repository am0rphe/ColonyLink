/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.core.Direction
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.me.networktool;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.network.clientbound.NetworkStatusPacket;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.me.Grid;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.networktool.NetworkStatus;
import appeng.server.subcommands.GridsCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class NetworkStatusMenu
extends AEBaseMenu {
    private static final String ACTION_EXPORT_GRID = "export_grid";
    public static final MenuType<NetworkStatusMenu> NETWORK_TOOL_TYPE = MenuTypeBuilder.create(NetworkStatusMenu::new, NetworkToolMenuHost.class).build("networkstatus");
    public static final MenuType<NetworkStatusMenu> CONTROLLER_TYPE = MenuTypeBuilder.create(NetworkStatusMenu::new, ControllerBlockEntity.class).build("controller_networkstatus");
    private IGrid grid;
    private int delay = 40;

    public NetworkStatusMenu(int id, Inventory ip, NetworkToolMenuHost host) {
        super(NETWORK_TOOL_TYPE, id, ip, host);
        this.buildForGridHost(host.getGridHost());
    }

    public NetworkStatusMenu(int id, Inventory ip, ControllerBlockEntity host) {
        super(CONTROLLER_TYPE, id, ip, host);
        this.buildForGridHost(host);
    }

    private void buildForGridHost(IInWorldGridNodeHost gridHost) {
        if (gridHost != null) {
            for (Direction d : Direction.values()) {
                this.findNode(gridHost, d);
            }
        }
        if (this.grid == null && this.isServerSide()) {
            this.setValidMenu(false);
        }
        this.registerClientAction(ACTION_EXPORT_GRID, this::exportGrid);
    }

    private void findNode(IInWorldGridNodeHost host, Direction d) {
        IGridNode node;
        if (this.grid == null && (node = host.getGridNode(d)) != null) {
            this.grid = node.getGrid();
        }
    }

    @Override
    public void broadcastChanges() {
        ++this.delay;
        if (this.isServerSide() && this.delay > 15 && this.grid != null) {
            this.delay = 0;
            NetworkStatus status = NetworkStatus.fromGrid(this.grid);
            this.sendPacketToClient(new NetworkStatusPacket(status));
        }
        super.broadcastChanges();
    }

    public void exportGrid() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_EXPORT_GRID);
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)this.getPlayer();
        MinecraftServer server = serverPlayer.getServer();
        Grid grid = (Grid)this.grid;
        CommandSourceStack commandSource = serverPlayer.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(commandSource, GridsCommand.buildExportCommand(grid.getSerialNumber()));
        this.setValidMenu(false);
    }

    public boolean canExportGrid() {
        String command;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        CommandDispatcher commands = connection.getCommands();
        ParseResults parseResult = commands.parse((command = GridsCommand.buildExportCommand(1)).substring(1), (Object)connection.getSuggestionsProvider());
        return !parseResult.getReader().canRead();
    }
}

