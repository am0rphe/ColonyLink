/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.Multiset
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.brigadier.LiteralMessage
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.storage.ChunkSerializer
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.apache.commons.io.output.CloseShieldOutputStream
 *  org.jetbrains.annotations.NotNull
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.server.subcommands;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.core.network.clientbound.ExportedGridContent;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import appeng.me.service.StatisticsService;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.server.ISubCommand;
import appeng.util.Platform;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridsCommand
implements ISubCommand {
    private static final Logger LOG = LoggerFactory.getLogger(GridsCommand.class);

    public static String buildExportCommand(int gridSerial) {
        return "/ae2 grids export " + gridSerial;
    }

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(((LiteralArgumentBuilder)Commands.literal((String)"export").executes(ctx -> {
            this.exportGrids((CommandSourceStack)ctx.getSource());
            return 1;
        })).then(Commands.argument((String)"gridSerial", (ArgumentType)IntegerArgumentType.integer()).executes(context -> {
            Integer gridSerial = (Integer)context.getArgument("gridSerial", Integer.class);
            for (Grid grid : TickHandler.instance().getGridList()) {
                if (grid.getSerialNumber() != gridSerial.intValue()) continue;
                this.exportGrid(grid, (CommandSourceStack)context.getSource());
                return 1;
            }
            throw new SimpleCommandExceptionType((Message)new LiteralMessage("No such grid found")).create();
        })));
    }

    private void exportGrids(CommandSourceStack source) throws CommandSyntaxException {
        Set<Grid> grids = TickHandler.instance().getGridList();
        source.sendSystemMessage((Component)Component.literal((String)("Exporting " + grids.size() + " grids")));
        this.exportGrids(0, grids, source);
    }

    private void exportGrid(Grid startGrid, CommandSourceStack source) throws CommandSyntaxException {
        Set<Grid> reachableGrids = Collections.newSetFromMap(new IdentityHashMap());
        reachableGrids.add(startGrid);
        Set<Grid> openSet = Collections.newSetFromMap(new IdentityHashMap());
        openSet.add(startGrid);
        while (!openSet.isEmpty()) {
            Iterator it = openSet.iterator();
            Grid grid = (Grid)it.next();
            it.remove();
            for (IGridNode node : grid.getNodes()) {
                MEP2PTunnelPart meTunnel;
                Grid tunnelGrid;
                Object object = node.getOwner();
                if (object instanceof AEBasePart) {
                    AEBasePart basePart = (AEBasePart)object;
                    GridsCommand.visitGridInFrontOfPart(basePart, reachableGrids, openSet);
                    continue;
                }
                object = node.getOwner();
                if (object instanceof PatternProviderLogicHost) {
                    PatternProviderLogicHost patternProvider = (PatternProviderLogicHost)object;
                    for (Direction targetSide : patternProvider.getTargets()) {
                        GridsCommand.visitGridAt(patternProvider.getBlockEntity().getLevel(), patternProvider.getBlockEntity().getBlockPos().relative(targetSide), reachableGrids, openSet);
                    }
                    continue;
                }
                object = node.getOwner();
                if (!(object instanceof MEP2PTunnelPart) || (tunnelGrid = (Grid)(meTunnel = (MEP2PTunnelPart)object).getMainNode().getGrid()) == null || !reachableGrids.add(tunnelGrid)) continue;
                openSet.add(tunnelGrid);
            }
        }
        this.exportGrids(startGrid.getSerialNumber(), reachableGrids, source);
    }

    private static void visitGridInFrontOfPart(AEBasePart part, Set<Grid> reachableGrids, Set<Grid> openSet) {
        Direction partSide = part.getSide();
        if (partSide == null) {
            return;
        }
        BlockEntity hostBe = part.getBlockEntity();
        BlockPos targetPos = hostBe.getBlockPos().relative(partSide);
        GridsCommand.visitGridAt(hostBe.getLevel(), targetPos, reachableGrids, openSet);
    }

    private static void visitGridAt(Level level, BlockPos pos, Set<Grid> reachableGrids, Set<Grid> openSet) {
        IInWorldGridNodeHost targetGridHost = GridHelper.getNodeHost(level, pos);
        if (targetGridHost != null) {
            for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
                Grid nodeGrid;
                IGridNode nodeOnSide = targetGridHost.getGridNode(side);
                if (nodeOnSide == null || !reachableGrids.add(nodeGrid = (Grid)nodeOnSide.getGrid())) continue;
                openSet.add(nodeGrid);
            }
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data, CommandSourceStack sender) {
    }

    private void exportGrids(int baseSerialNumber, Collection<Grid> grids, CommandSourceStack source) throws CommandSyntaxException {
        source.sendSystemMessage((Component)Component.literal((String)("Exporting " + grids.size() + " grids")));
        LOG.info("Exporting {} grids for {}", (Object)grids.size(), (Object)source);
        if (source.isPlayer()) {
            ServerPlayer player = source.getPlayerOrException();
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new ExportedGridContent(baseSerialNumber, ExportedGridContent.ContentType.FIRST_CHUNK, new byte[0]), (CustomPacketPayload[])new CustomPacketPayload[0]);
            try (SendToPlayerStream out = new SendToPlayerStream(player, baseSerialNumber);){
                this.exportGrids(grids, out);
            }
        }
        Path targetPath = Paths.get("grids.zip", new String[0]);
        try (OutputStream out = Files.newOutputStream(targetPath, new OpenOption[0]);){
            this.exportGrids(grids, out);
        }
        catch (IOException e) {
            LOG.error("Failed to export grids.", (Throwable)e);
            source.sendFailure((Component)Component.literal((String)("Failed to export grids: " + String.valueOf(e))));
        }
    }

    private void exportGrids(Iterable<Grid> grids, OutputStream out) {
        try (ZipOutputStream zipOut = new ZipOutputStream(out);){
            HashMap<ServerLevel, Set> chunksByLevel = new HashMap<ServerLevel, Set>();
            for (Grid grid : grids) {
                StatisticsService statisticsService = grid.getService(StatisticsService.class);
                for (Map.Entry<ServerLevel, Multiset<ChunkPos>> entry : statisticsService.getChunks().entrySet()) {
                    chunksByLevel.computeIfAbsent(entry.getKey(), level -> new HashSet()).addAll(entry.getValue().elementSet());
                }
                ZipEntry entry = new ZipEntry("grid_" + grid.getSerialNumber() + ".json");
                zipOut.putNextEntry(entry);
                try (JsonWriter writer = new JsonWriter((Writer)new OutputStreamWriter((OutputStream)CloseShieldOutputStream.wrap((OutputStream)zipOut), StandardCharsets.UTF_8));){
                    writer.setIndent(" ");
                    grid.export(writer);
                }
            }
            zipOut.putNextEntry(new ZipEntry("chunks/"));
            for (Map.Entry entry : chunksByLevel.entrySet()) {
                ServerLevel level2 = (ServerLevel)entry.getKey();
                Set chunks = (Set)entry.getValue();
                String baseName = this.sanitizeName(level2.dimension().location().toString());
                for (ChunkPos chunk : chunks) {
                    CompoundTag serializedChunk = ChunkSerializer.write((ServerLevel)level2, (ChunkAccess)level2.getChunk(chunk.x, chunk.z));
                    zipOut.putNextEntry(new ZipEntry("chunks/" + baseName + "_" + chunk.x + "_" + chunk.z + ".nbt"));
                    NbtIo.writeCompressed((CompoundTag)serializedChunk, (OutputStream)CloseShieldOutputStream.wrap((OutputStream)zipOut));
                    zipOut.putNextEntry(new ZipEntry("chunks/" + baseName + "_" + chunk.x + "_" + chunk.z + ".snbt"));
                    zipOut.write(NbtUtils.structureToSnbt((CompoundTag)serializedChunk).getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitizeName(String string) {
        return string.replaceAll("[^A-Za-z0-9-,]", "_");
    }

    private static class SendToPlayerStream
    extends OutputStream {
        private static final int FLUSH_AFTER = 524288;
        private final ByteArrayOutputStream bout;
        private final ServerPlayer player;
        private final int baseSerialNumber;
        private boolean closed;

        public SendToPlayerStream(ServerPlayer player, int baseSerialNumber) {
            this.player = player;
            this.baseSerialNumber = baseSerialNumber;
            this.bout = new ByteArrayOutputStream(524288);
        }

        @Override
        public void write(int b) {
            Preconditions.checkState((!this.closed ? 1 : 0) != 0, (Object)"stream already closed");
            this.bout.write(b);
            if (this.bout.size() > 524288) {
                PacketDistributor.sendToPlayer((ServerPlayer)this.player, (CustomPacketPayload)new ExportedGridContent(this.baseSerialNumber, ExportedGridContent.ContentType.CHUNK, this.bout.toByteArray()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.bout.reset();
            }
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) {
            Preconditions.checkState((!this.closed ? 1 : 0) != 0, (Object)"stream already closed");
            this.bout.write(b, off, len);
            if (this.bout.size() > 524288) {
                PacketDistributor.sendToPlayer((ServerPlayer)this.player, (CustomPacketPayload)new ExportedGridContent(this.baseSerialNumber, ExportedGridContent.ContentType.CHUNK, this.bout.toByteArray()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.bout.reset();
            }
        }

        @Override
        public void close() {
            if (!this.closed) {
                this.closed = true;
                PacketDistributor.sendToPlayer((ServerPlayer)this.player, (CustomPacketPayload)new ExportedGridContent(this.baseSerialNumber, ExportedGridContent.ContentType.LAST_CHUNK, this.bout.toByteArray()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.bout.reset();
            }
        }
    }
}

