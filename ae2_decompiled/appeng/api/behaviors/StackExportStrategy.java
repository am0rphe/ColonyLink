/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package appeng.api.behaviors;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface StackExportStrategy {
    public long transfer(StackTransferContext var1, AEKey var2, long var3);

    public long push(AEKey var1, long var2, Actionable var4);

    public static void register(AEKeyType type, Factory factory) {
        StackWorldBehaviors.registerExportStrategy(type, factory);
    }

    @FunctionalInterface
    public static interface Factory {
        public StackExportStrategy create(ServerLevel var1, BlockPos var2, Direction var3);
    }
}

