/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.StatsAccumulator
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.Registry
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Action
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$Action
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.ChunkGeneratorStructureState
 *  net.minecraft.world.level.chunk.status.ChunkStatus
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.structure.Structure
 *  net.minecraft.world.level.levelgen.structure.StructureStart
 */
package appeng.server.subcommands;

import appeng.server.ISubCommand;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import com.google.common.math.StatsAccumulator;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class TestMeteoritesCommand
implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal((String)"force").executes(ctx -> {
            TestMeteoritesCommand.test(((CommandSourceStack)ctx.getSource()).getServer(), (CommandSourceStack)ctx.getSource(), true);
            return 1;
        }));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        TestMeteoritesCommand.test(srv, sender, false);
    }

    private static void test(MinecraftServer srv, CommandSourceStack sender, boolean force) {
        BlockPos centerBlock;
        ServerLevel level;
        int radius = 100;
        ServerPlayer player = null;
        try {
            player = sender.getPlayerOrException();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        if (player != null) {
            level = player.serverLevel();
            centerBlock = BlockPos.containing((double)player.getX(), (double)0.0, (double)player.getZ());
        } else {
            level = srv.getLevel(Level.OVERWORLD);
            centerBlock = level.getSharedSpawnPos();
        }
        ChunkPos center = new ChunkPos(centerBlock);
        Registry structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure structure = (Structure)structures.get(MeteoriteStructure.KEY);
        Registry structureSets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        Holder.Reference structureSet = structureSets.getHolderOrThrow(MeteoriteStructure.STRUCTURE_SET_KEY);
        ChunkGeneratorStructureState generatorState = level.getChunkSource().getGeneratorState();
        ArrayList<PlacedMeteoriteSettings> found = new ArrayList<PlacedMeteoriteSettings>();
        int chunksChecked = 0;
        for (int cx = center.x - radius; cx <= center.x + radius; ++cx) {
            for (int cz = center.z - radius; cz <= center.z + radius; ++cz) {
                ChunkAccess chunk;
                MeteoriteStructurePiece piece;
                ++chunksChecked;
                if (!generatorState.hasStructureChunkInRange((Holder)structureSet, cx, cz, 0) || (piece = TestMeteoritesCommand.getMeteoritePieceFromChunk(chunk = level.getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS), structure)) == null) continue;
                found.add(piece.getSettings());
            }
        }
        StatsAccumulator stats = new StatsAccumulator();
        for (PlacedMeteoriteSettings settings2 : found) {
            double closestOther = Double.NaN;
            for (PlacedMeteoriteSettings otherSettings : found) {
                if (otherSettings == settings2) continue;
                double d = settings2.getPos().distSqr((Vec3i)otherSettings.getPos());
                if (!Double.isNaN(closestOther) && !(d < closestOther)) continue;
                closestOther = d;
            }
            if (Double.isNaN(closestOther)) continue;
            stats.add(Math.sqrt(closestOther));
        }
        found.sort(Comparator.comparingDouble(settings -> settings.getPos().distSqr((Vec3i)centerBlock)));
        TestMeteoritesCommand.sendLine(sender, "Chunks checked: %d", chunksChecked);
        TestMeteoritesCommand.sendLine(sender, "Meteorites found: %d", found.size());
        if (stats.count() > 0L) {
            TestMeteoritesCommand.sendLine(sender, "Closest: min=%.2f max=%.2f mean=%.2f stddev=%.2f", stats.min(), stats.max(), stats.mean(), stats.populationStandardDeviation());
        }
        int closestCount = Math.min(10, found.size());
        for (int i = 0; i < closestCount; ++i) {
            PlacedMeteoriteSettings settings3 = (PlacedMeteoriteSettings)found.get(i);
            BlockPos pos = settings3.getPos();
            String state = "not final";
            if (force && settings3.getFallout() == null) {
                ChunkAccess chunk = level.getChunk(pos);
                MeteoriteStructurePiece piece = TestMeteoritesCommand.getMeteoritePieceFromChunk(chunk, structure);
                if (piece == null) {
                    state = "removed";
                } else {
                    settings3 = piece.getSettings();
                    pos = settings3.getPos();
                }
            }
            MutableComponent restOfLine = settings3.getFallout() == null ? Component.literal((String)String.format(Locale.ROOT, ", radius=%.2f [%s]", Float.valueOf(settings3.getMeteoriteRadius()), state)) : Component.literal((String)String.format(Locale.ROOT, ", radius=%.2f, crater=%s, fallout=%s", Float.valueOf(settings3.getMeteoriteRadius()), settings3.getCraterType().name().toLowerCase(), settings3.getFallout().name().toLowerCase()));
            MutableComponent msg = Component.literal((String)(" #" + (i + 1) + " "));
            msg.append(TestMeteoritesCommand.getClickablePosition(level, settings3, pos)).append((Component)restOfLine);
            String biomeId = level.getBiome(pos).unwrapKey().map(bk -> bk.location().toString()).orElse("unknown");
            MutableComponent tooltip = Component.literal((String)(String.valueOf(settings3) + "\nBiome: ")).copy().append(biomeId);
            msg.withStyle(arg_0 -> TestMeteoritesCommand.lambda$test$3((Component)tooltip, arg_0));
            sender.sendSuccess(() -> msg, true);
        }
    }

    private static Component getClickablePosition(ServerLevel level, PlacedMeteoriteSettings settings, BlockPos pos) {
        BlockPos tpPos = pos.above((int)Math.ceil(settings.getMeteoriteRadius()));
        int surfaceY = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, tpPos).getY();
        if (surfaceY > tpPos.getY()) {
            tpPos = new BlockPos(tpPos.getX(), surfaceY, tpPos.getZ());
        }
        String displayText = String.format(Locale.ROOT, "pos=%d,%d,%d", tpPos.getX(), tpPos.getY(), tpPos.getZ());
        String tpCommand = String.format(Locale.ROOT, "/tp @s %d %d %d", tpPos.getX(), tpPos.getY(), tpPos.getZ());
        return Component.literal((String)displayText).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand)));
    }

    private static MeteoriteStructurePiece getMeteoritePieceFromChunk(ChunkAccess chunk, Structure structure) {
        StructureStart start = chunk.getStartForStructure(structure);
        if (start != null && start.getPieces().size() > 0 && start.getPieces().get(0) instanceof MeteoriteStructurePiece) {
            return (MeteoriteStructurePiece)((Object)start.getPieces().get(0));
        }
        return null;
    }

    private static void sendLine(CommandSourceStack sender, String text, Object ... args) {
        sender.sendSuccess(() -> Component.literal((String)String.format(Locale.ROOT, text, args)), true);
    }

    private static /* synthetic */ Style lambda$test$3(Component tooltip, Style style) {
        return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)tooltip));
    }
}

