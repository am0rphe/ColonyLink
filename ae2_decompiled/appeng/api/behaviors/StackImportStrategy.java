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
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface StackImportStrategy {
    public boolean transfer(StackTransferContext var1);

    public static void register(AEKeyType keyType, Factory factory) {
        StackWorldBehaviors.registerImportStrategy(keyType, factory);
    }

    @FunctionalInterface
    public static interface Factory {
        public StackImportStrategy create(ServerLevel var1, BlockPos var2, Direction var3);
    }
}

