/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Action
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$Action
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package appeng.server.subcommands;

import appeng.api.features.IPlayerRegistry;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.server.ISubCommand;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.TransitionInfo;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpatialStorageCommand
implements ISubCommand {
    public static final DynamicCommandExceptionType PLOT_NOT_FOUND = new DynamicCommandExceptionType(xva$0 -> PlayerMessages.PlotNotFound.text(xva$0));
    public static final SimpleCommandExceptionType PLOT_NOT_FOUND_FOR_POSITION = new SimpleCommandExceptionType((Message)PlayerMessages.PlotNotFoundForCurrentPosition.text());
    public static final SimpleCommandExceptionType NOT_IN_SPATIAL_STORAGE_LEVEL = new SimpleCommandExceptionType((Message)PlayerMessages.NotInSpatialStorageLevel.text());
    public static final SimpleCommandExceptionType NOT_STORAGE_CELL = new SimpleCommandExceptionType((Message)PlayerMessages.NotStorageCell.text());
    public static final SimpleCommandExceptionType NO_LAST_TRANSITION = new SimpleCommandExceptionType((Message)PlayerMessages.NoLastTransition.text());

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(((LiteralArgumentBuilder)Commands.literal((String)"info").executes(ctx -> {
            SpatialStorageCommand.showPlotInfo((CommandSourceStack)ctx.getSource(), SpatialStorageCommand.getCurrentPlot((CommandSourceStack)ctx.getSource()));
            return 1;
        })).then(Commands.argument((String)"plotId", (ArgumentType)IntegerArgumentType.integer((int)1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"plotId");
            SpatialStorageCommand.showPlotInfo((CommandSourceStack)ctx.getSource(), SpatialStorageCommand.getPlot(plotId));
            return 1;
        })));
        builder.then(Commands.literal((String)"tp").then(Commands.argument((String)"plotId", (ArgumentType)IntegerArgumentType.integer((int)1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"plotId");
            SpatialStorageCommand.teleportToPlot((CommandSourceStack)ctx.getSource(), plotId);
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)Commands.literal((String)"tpback").executes(ctx -> {
            this.teleportBack((CommandSourceStack)ctx.getSource());
            return 1;
        })).then(Commands.argument((String)"plotId", (ArgumentType)IntegerArgumentType.integer((int)1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"plotId");
            this.teleportBack((CommandSourceStack)ctx.getSource(), SpatialStorageCommand.getPlot(plotId));
            return 1;
        })));
        builder.then(Commands.literal((String)"givecell").then(Commands.argument((String)"plotId", (ArgumentType)IntegerArgumentType.integer((int)1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"plotId");
            this.giveCell((CommandSourceStack)ctx.getSource(), plotId);
            return 1;
        })));
    }

    private void teleportBack(CommandSourceStack source) throws CommandSyntaxException {
        if (source.getLevel().dimension() != SpatialStorageDimensionIds.WORLD_ID) {
            throw NOT_IN_SPATIAL_STORAGE_LEVEL.create();
        }
        BlockPos playerPos = BlockPos.containing((Position)source.getPosition());
        int x = playerPos.getX();
        int z = playerPos.getZ();
        for (SpatialStoragePlot plot : SpatialStoragePlotManager.INSTANCE.getPlots()) {
            BlockPos origin = plot.getOrigin();
            BlockPos size = plot.getSize();
            if (x < origin.getX() || x > origin.getX() + size.getX() || z < origin.getZ() || z > origin.getZ() + size.getZ()) continue;
            this.teleportBack(source, plot);
            return;
        }
        throw PLOT_NOT_FOUND_FOR_POSITION.create();
    }

    private void teleportBack(CommandSourceStack source, SpatialStoragePlot plot) throws CommandSyntaxException {
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition == null) {
            throw NO_LAST_TRANSITION.create();
        }
        String command = SpatialStorageCommand.getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().offset(0, 1, 0));
        SpatialStorageCommand.runCommandFor(source, command);
    }

    private static void showPlotInfo(CommandSourceStack source, SpatialStoragePlot plot) {
        SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.PlotID.text(), String.valueOf(plot.getId()));
        int playerId = plot.getOwner();
        if (playerId != -1) {
            MinecraftServer server = source.getServer();
            UUID profileId = IPlayerRegistry.getMapping(server).getProfileId(playerId);
            if (profileId == null) {
                SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Owner.text(), (Component)PlayerMessages.UnknownAE2Player.text(playerId));
            } else {
                ServerPlayer player = server.getPlayerList().getPlayer(profileId);
                if (player != null) {
                    SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Owner.text(), (Component)PlayerMessages.PlayerConnected.text(player.getGameProfile().getName()));
                } else {
                    Optional cachedProfile = server.getProfileCache().get(profileId);
                    if (cachedProfile.isPresent()) {
                        SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Owner.text(), (Component)PlayerMessages.PlayerDisconnected.text(((GameProfile)cachedProfile.get()).getName()));
                    } else {
                        SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Owner.text(), (Component)PlayerMessages.MinecraftProfile.text(profileId));
                    }
                }
            }
        } else {
            SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Owner.text(), (Component)PlayerMessages.Unknown.text());
        }
        SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Size.text(), SpatialStorageCommand.formatBlockPos(plot.getSize(), "x"));
        String teleportToPlotCommand = SpatialStorageCommand.getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.location(), plot.getOrigin());
        SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Origin.text(), (Component)Component.literal((String)SpatialStorageCommand.formatBlockPos(plot.getOrigin(), ",")).withStyle(SpatialStorageCommand.makeCommandLink(teleportToPlotCommand, PlayerMessages.ClickToTeleport.text())));
        SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.RegionFile.text(), plot.getRegionFilename());
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition != null) {
            source.sendSuccess(() -> PlayerMessages.LastTransition.text().withStyle(new ChatFormatting[]{ChatFormatting.UNDERLINE, ChatFormatting.BOLD}), true);
            String sourceWorldId = lastTransition.getWorldId().toString();
            MutableComponent sourceLink = PlayerMessages.SourceLink.text(sourceWorldId, SpatialStorageCommand.formatBlockPos(lastTransition.getMin(), ","), SpatialStorageCommand.formatBlockPos(lastTransition.getMax(), ","));
            String tpCommand = SpatialStorageCommand.getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().offset(0, 1, 0));
            sourceLink.withStyle(SpatialStorageCommand.makeCommandLink(tpCommand, PlayerMessages.ClickToTeleport.text()));
            SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.Source.text(), (Component)sourceLink);
            SpatialStorageCommand.sendKeyValuePair(source, PlayerMessages.When.text(), lastTransition.getTimestamp().toString());
        } else {
            source.sendSuccess(() -> PlayerMessages.LastTransitionUnknown.text(), true);
        }
    }

    private static void teleportToPlot(CommandSourceStack source, int plotId) throws CommandSyntaxException {
        SpatialStoragePlot plot = SpatialStorageCommand.getPlot(plotId);
        String teleportCommand = SpatialStorageCommand.getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.location(), plot.getOrigin());
        SpatialStorageCommand.runCommandFor(source, teleportCommand);
    }

    private void giveCell(CommandSourceStack source, int plotId) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SpatialStoragePlot plot = SpatialStorageCommand.getPlot(plotId);
        int longestSide = SpatialStorageCommand.getLongestSide(plot.getSize());
        ItemStack cell = longestSide <= 2 ? AEItems.SPATIAL_CELL2.stack() : (longestSide <= 16 ? AEItems.SPATIAL_CELL16.stack() : AEItems.SPATIAL_CELL128.stack());
        Item item = cell.getItem();
        if (!(item instanceof SpatialStorageCellItem)) {
            throw NOT_STORAGE_CELL.create();
        }
        SpatialStorageCellItem spatialCellItem = (SpatialStorageCellItem)item;
        spatialCellItem.setStoredDimension(cell, plotId, plot.getSize());
        player.addItem(cell);
    }

    private static int getLongestSide(BlockPos size) {
        return Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        try {
            SpatialStoragePlotManager.INSTANCE.getLevel();
        }
        catch (IllegalStateException e) {
            sender.sendSuccess(() -> PlayerMessages.NoSpatialIOLevel.text(e.getMessage()), true);
            return;
        }
        ArrayList<SpatialStoragePlot> plots = new ArrayList<SpatialStoragePlot>(SpatialStoragePlotManager.INSTANCE.getPlots());
        if (plots.isEmpty()) {
            sender.sendSuccess(() -> PlayerMessages.NoSpatialIOPlots.text(), true);
            return;
        }
        plots.sort(Comparator.comparing(plot -> {
            TransitionInfo lastTransition = plot.getLastTransition();
            if (lastTransition != null) {
                return lastTransition.getTimestamp();
            }
            return Instant.MIN;
        }).reversed());
        for (int i = 0; i < Math.min(5, plots.size()); ++i) {
            SpatialStoragePlot plot2 = (SpatialStoragePlot)plots.get(i);
            String size = SpatialStorageCommand.formatBlockPos(plot2.getSize(), "x");
            BlockPos originPos = plot2.getOrigin();
            String origin = SpatialStorageCommand.formatBlockPos(originPos, ",");
            MutableComponent infoLink = PlayerMessages.Plot.text().append(" #" + plot2.getId()).withStyle(SpatialStorageCommand.makeCommandLink("/ae2 spatial info " + plot2.getId(), PlayerMessages.ClickToShowDetails.text()));
            MutableComponent tpLink = PlayerMessages.Origin.text().append(": " + origin).withStyle(SpatialStorageCommand.makeCommandLink("/ae2 spatial tp " + plot2.getId(), PlayerMessages.ClickToTeleport.text()));
            MutableComponent message = Component.literal((String)"").append((Component)infoLink).append(" ").append((Component)PlayerMessages.Size.text()).append(": " + size + " ").append((Component)tpLink);
            sender.sendSuccess(() -> message, true);
        }
    }

    private static String formatBlockPos(BlockPos size, String separator) {
        return size.getX() + separator + size.getY() + separator + size.getZ();
    }

    private static UnaryOperator<Style> makeCommandLink(String command, MutableComponent tooltip) {
        return style -> style.applyFormat(ChatFormatting.UNDERLINE).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)tooltip));
    }

    private static void runCommandFor(CommandSourceStack source, String command) {
        Commands commandManager = source.getServer().getCommands();
        commandManager.performPrefixedCommand(source, command);
    }

    private static String getTeleportCommand(ResourceLocation worldId, BlockPos pos) {
        return "/execute in " + String.valueOf(worldId) + " run tp @s " + pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
    }

    private static SpatialStoragePlot getPlot(int plotId) throws CommandSyntaxException {
        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            throw PLOT_NOT_FOUND.create((Object)plotId);
        }
        return plot;
    }

    private static void sendKeyValuePair(CommandSourceStack source, MutableComponent label, Component value) {
        source.sendSuccess(() -> label.append(": ").withStyle(ChatFormatting.BOLD).append(value), true);
    }

    private static void sendKeyValuePair(CommandSourceStack source, MutableComponent label, String value) {
        SpatialStorageCommand.sendKeyValuePair(source, label, (Component)Component.literal((String)value));
    }

    private static SpatialStoragePlot getCurrentPlot(CommandSourceStack source) throws CommandSyntaxException {
        if (source.getLevel().dimension() != SpatialStorageDimensionIds.WORLD_ID) {
            throw NOT_IN_SPATIAL_STORAGE_LEVEL.create();
        }
        BlockPos playerPos = BlockPos.containing((Position)source.getPosition());
        int x = playerPos.getX();
        int z = playerPos.getZ();
        for (SpatialStoragePlot plot : SpatialStoragePlotManager.INSTANCE.getPlots()) {
            BlockPos origin = plot.getOrigin();
            BlockPos size = plot.getSize();
            if (x < origin.getX() || x > origin.getX() + size.getX() || z < origin.getZ() || z > origin.getZ() + size.getZ()) continue;
            return plot;
        }
        throw PLOT_NOT_FOUND_FOR_POSITION.create();
    }
}

