/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.GameRules$BooleanValue
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.entity.EntityTypeTest
 *  net.minecraft.world.level.levelgen.FlatLevelSource
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.NeoForge
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.subcommands;

import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.server.ISubCommand;
import appeng.server.testplots.KitOutPlayerEvent;
import appeng.server.testplots.TestPlots;
import appeng.server.testworld.TestWorldGenerator;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class SetupTestWorldCommand
implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (ResourceLocation plotId : TestPlots.getPlotIds()) {
            builder.then(Commands.literal((String)plotId.toString()).executes(ctx -> {
                this.setupTestWorld(((CommandSourceStack)ctx.getSource()).getServer(), (CommandSourceStack)ctx.getSource(), plotId);
                return 1;
            }));
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        this.setupTestWorld(srv, sender, null);
    }

    private void setupTestWorld(MinecraftServer srv, CommandSourceStack sender, @Nullable ResourceLocation plotId) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            ServerPlayer player = sender.getPlayerOrException();
            if (!player.isCreative()) {
                sender.sendFailure((Component)PlayerMessages.TestWorldNotInCreativeMode.text());
                return;
            }
            ServerLevel level = player.serverLevel();
            if (!SetupTestWorldCommand.isSuperflatWorld(level)) {
                sender.sendFailure((Component)PlayerMessages.TestWorldNotInSuperflat.text());
                return;
            }
            SetupTestWorldCommand.changeGameRules(srv);
            this.removeAllEntitiesButPlayer(srv);
            BlockPos origin = player.blockPosition();
            if (origin.getY() - 3 < level.getMinBuildHeight()) {
                origin = origin.atY(level.getMinBuildHeight() + 3);
            }
            TestWorldGenerator generator = new TestWorldGenerator(level, player, origin, plotId);
            generator.generate();
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            this.kitOutPlayer(player);
            if (!generator.isWithinBounds(player.blockPosition())) {
                BlockPos goodStartPos = generator.getSuitableStartPos();
                player.teleportTo(level, (double)goodStartPos.getX(), (double)goodStartPos.getY(), (double)goodStartPos.getZ(), 0.0f, 0.0f);
            }
            sender.sendSuccess(() -> PlayerMessages.TestWorldSetupComplete.text(sw.toString()), true);
        }
        catch (CommandSyntaxException | RuntimeException e) {
            AELog.error(e);
            sender.sendFailure((Component)PlayerMessages.TestWorldSetupFailed.text(e.toString()));
        }
    }

    private void removeAllEntitiesButPlayer(MinecraftServer srv) {
        for (ServerLevel level : srv.getAllLevels()) {
            ArrayList entities = new ArrayList();
            level.getEntities(EntityTypeTest.forClass(Entity.class), e -> true, entities);
            for (Entity entity : entities) {
                if (entity instanceof Player) continue;
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private void kitOutPlayer(ServerPlayer player) {
        Inventory playerInv = player.getInventory();
        ItemStack fullApplicator = ColorApplicatorItem.createFullColorApplicator();
        if (!playerInv.hasAnyOf(Collections.singleton(AEItems.COLOR_APPLICATOR.asItem()))) {
            playerInv.placeItemBackInInventory(fullApplicator);
        }
        NeoForge.EVENT_BUS.post((Event)new KitOutPlayerEvent(player));
    }

    private static void changeGameRules(MinecraftServer srv) {
        SetupTestWorldCommand.makeAlwaysDaytime(srv);
        SetupTestWorldCommand.disableWeather(srv);
        SetupTestWorldCommand.disableMobSpawning(srv);
    }

    private static void makeAlwaysDaytime(MinecraftServer srv) {
        ((GameRules.BooleanValue)srv.getGameRules().getRule(GameRules.RULE_DAYLIGHT)).set(false, srv);
        srv.overworld().setDayTime(1000L);
    }

    private static void disableWeather(MinecraftServer srv) {
        ((GameRules.BooleanValue)srv.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE)).set(false, srv);
        srv.overworld().setWeatherParameters(9999, 0, false, false);
    }

    private static void disableMobSpawning(MinecraftServer srv) {
        ((GameRules.BooleanValue)srv.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING)).set(false, srv);
    }

    private static boolean isSuperflatWorld(ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        return generator instanceof FlatLevelSource;
    }
}

