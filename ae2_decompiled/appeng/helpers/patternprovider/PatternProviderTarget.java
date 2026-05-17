/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.Runnables
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.helpers.patternprovider;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import com.google.common.util.concurrent.Runnables;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface PatternProviderTarget {
    @Nullable
    public static PatternProviderTarget get(Level l, BlockPos pos, @Nullable BlockEntity be, Direction side, IActionSource src) {
        MEStorage storage = be != null ? (MEStorage)l.getCapability(AECapabilities.ME_STORAGE, be.getBlockPos(), be.getBlockState(), be, (Object)side) : (MEStorage)l.getCapability(AECapabilities.ME_STORAGE, pos, (Object)side);
        if (storage != null) {
            return PatternProviderTarget.wrapMeStorage(storage, src);
        }
        Map<AEKeyType, ExternalStorageStrategy> strategies = StackWorldBehaviors.createExternalStorageStrategies((ServerLevel)l, pos, side);
        IdentityHashMap<AEKeyType, MEStorage> externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (Map.Entry<AEKeyType, ExternalStorageStrategy> entry : strategies.entrySet()) {
            MEStorage wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
            if (wrapper == null) continue;
            externalStorages.put(entry.getKey(), wrapper);
        }
        if (!externalStorages.isEmpty()) {
            return PatternProviderTarget.wrapMeStorage(new CompositeStorage(externalStorages), src);
        }
        return null;
    }

    private static PatternProviderTarget wrapMeStorage(final MEStorage storage, final IActionSource src) {
        return new PatternProviderTarget(){

            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, src);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for (Object2LongMap.Entry<AEKey> stack : storage.getAvailableStacks()) {
                    if (!patternInputs.contains(((AEKey)stack.getKey()).dropSecondary())) continue;
                    return true;
                }
                return false;
            }
        };
    }

    public long insert(AEKey var1, long var2, Actionable var4);

    public boolean containsPatternInput(Set<AEKey> var1);
}

