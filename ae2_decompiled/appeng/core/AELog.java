/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.state.BlockState
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package appeng.core;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.AEConfig;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AELog {
    private static final String LOGGER_PREFIX = "AE2:";
    private static final String SERVER_SUFFIX = "S";
    private static final String CLIENT_SUFFIX = "C";
    private static final Logger SERVER = LogManager.getFormatterLogger((String)"AE2:S");
    private static final Logger CLIENT = LogManager.getFormatterLogger((String)"AE2:C");
    private static final String BLOCK_UPDATE = "Block Update of %s @ ( %s ). State %s -> %s";
    private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception: ";
    private static boolean craftingLogEnabled;
    private static boolean debugLogEnabled;
    private static boolean gridLogEnabled;

    private AELog() {
    }

    private static Logger getLogger() {
        return Platform.isServer() ? SERVER : CLIENT;
    }

    public static boolean isLogEnabled() {
        return true;
    }

    public static void log(Level level, String message, Object ... params) {
        if (AELog.isLogEnabled()) {
            String formattedMessage = String.format(message, params);
            Logger logger = AELog.getLogger();
            logger.log(level, formattedMessage);
        }
    }

    private static void log(Level level, Throwable exception, String message, Object ... params) {
        if (AELog.isLogEnabled()) {
            String formattedMessage = String.format(message, params);
            Logger logger = AELog.getLogger();
            logger.log(level, formattedMessage, exception);
        }
    }

    public static void info(String format, Object ... params) {
        AELog.log(Level.INFO, format, params);
    }

    public static void info(Throwable exception) {
        AELog.log(Level.INFO, exception, DEFAULT_EXCEPTION_MESSAGE, new Object[0]);
    }

    public static void info(Throwable exception, String message) {
        AELog.log(Level.INFO, exception, message, new Object[0]);
    }

    public static void warn(String format, Object ... params) {
        AELog.log(Level.WARN, format, params);
    }

    public static void warn(Throwable exception) {
        AELog.log(Level.WARN, exception, DEFAULT_EXCEPTION_MESSAGE, new Object[0]);
    }

    public static void warn(Throwable exception, String message) {
        AELog.log(Level.WARN, exception, message, new Object[0]);
    }

    public static void error(String format, Object ... params) {
        AELog.log(Level.ERROR, format, params);
    }

    public static void error(Throwable exception) {
        AELog.log(Level.ERROR, exception, DEFAULT_EXCEPTION_MESSAGE, new Object[0]);
    }

    public static void error(Throwable exception, String message) {
        AELog.log(Level.ERROR, exception, message, new Object[0]);
    }

    public static void debug(String format, Object ... data) {
        if (AELog.isDebugLogEnabled()) {
            AELog.log(Level.DEBUG, format, data);
        }
    }

    public static boolean isDebugLogEnabled() {
        return debugLogEnabled;
    }

    public static void blockUpdate(BlockPos pos, BlockState currentState, BlockState newState, AEBaseBlockEntity blockEntity) {
        if (AEConfig.instance().isBlockUpdateLogEnabled()) {
            AELog.info(BLOCK_UPDATE, blockEntity.getClass().getName(), pos, currentState, newState);
        }
    }

    public static boolean isCraftingLogEnabled() {
        return craftingLogEnabled;
    }

    public static void crafting(String message, Object ... params) {
        if (AELog.isCraftingLogEnabled()) {
            AELog.log(Level.INFO, message, params);
        }
    }

    public static boolean isCraftingDebugLogEnabled() {
        return AELog.isCraftingLogEnabled() && AELog.isDebugLogEnabled();
    }

    public static void craftingDebug(String message, Object ... params) {
        if (AELog.isCraftingDebugLogEnabled()) {
            AELog.log(Level.DEBUG, message, params);
        }
    }

    public static boolean isGridLogEnabled() {
        return gridLogEnabled;
    }

    public static void grid(String message, Object ... params) {
        if (AELog.isGridLogEnabled()) {
            AELog.log(Level.INFO, "[AE2 Grid Log] " + message, params);
        }
    }

    public static void setCraftingLogEnabled(boolean newValue) {
        craftingLogEnabled = newValue;
    }

    public static void setDebugLogEnabled(boolean newValue) {
        debugLogEnabled = newValue;
    }

    public static void setGridLogEnabled(boolean newValue) {
        gridLogEnabled = newValue;
    }
}

