/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.parts.automation.StackWorldBehaviors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface ExternalStorageStrategy {
    @Nullable
    public MEStorage createWrapper(boolean var1, Runnable var2);

    public static void register(AEKeyType type, Factory factory) {
        StackWorldBehaviors.registerExternalStorageStrategy(type, factory);
    }

    @FunctionalInterface
    public static interface Factory {
        public ExternalStorageStrategy create(ServerLevel var1, BlockPos var2, Direction var3);
    }
}

