/*
 * Decompiled with CFR 0.152.
 */
package appeng.server;

import appeng.server.ISubCommand;
import appeng.server.services.compass.TestCompassCommand;
import appeng.server.subcommands.ChannelModeCommand;
import appeng.server.subcommands.ChunkLogger;
import appeng.server.subcommands.GridsCommand;
import appeng.server.subcommands.SetupTestWorldCommand;
import appeng.server.subcommands.SpatialStorageCommand;
import appeng.server.subcommands.TestMeteoritesCommand;
import appeng.server.subcommands.TickMonitoring;
import java.util.Locale;

public enum Commands {
    CHUNK_LOGGER(4, "chunklogger", new ChunkLogger()),
    SPATIAL(4, "spatial", new SpatialStorageCommand()),
    CHANNEL_MODE(4, "channelmode", new ChannelModeCommand()),
    TICK_MONITORING(4, "tickmonitor", new TickMonitoring()),
    GRIDS(4, "grids", new GridsCommand()),
    COMPASS(4, "compass", new TestCompassCommand(), true),
    TEST_METEORITES(4, "testmeteorites", new TestMeteoritesCommand(), true),
    SETUP_TEST_WORLD(4, "setuptestworld", new SetupTestWorldCommand(), true);

    public final int level;
    public final ISubCommand command;
    public final boolean test;
    public final String literal;

    private Commands(int level, String literal, ISubCommand w) {
        this(level, literal, w, false);
    }

    private Commands(int level, String literal, ISubCommand w, boolean test) {
        this.level = level;
        this.command = w;
        this.test = test;
        this.literal = literal;
    }

    public String literal() {
        return this.literal.toLowerCase(Locale.ROOT);
    }
}

