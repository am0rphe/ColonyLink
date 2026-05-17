/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.neoforged.neoforge.capabilities.BlockCapabilityCache
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers.patternprovider;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

class PatternProviderTargetCache {
    private final BlockCapabilityCache<MEStorage, Direction> cache;
    private final IActionSource src;
    private final Map<AEKeyType, ExternalStorageStrategy> strategies;

    PatternProviderTargetCache(ServerLevel l, BlockPos pos, Direction direction, IActionSource src) {
        this.cache = BlockCapabilityCache.create(AECapabilities.ME_STORAGE, (ServerLevel)l, (BlockPos)pos, (Object)direction);
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(l, pos, direction);
    }

    @Nullable
    PatternProviderTarget find() {
        MEStorage meStorage = (MEStorage)this.cache.getCapability();
        if (meStorage != null) {
            return this.wrapMeStorage(meStorage);
        }
        IdentityHashMap<AEKeyType, MEStorage> externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (Map.Entry<AEKeyType, ExternalStorageStrategy> entry : this.strategies.entrySet()) {
            MEStorage wrapper = entry.getValue().createWrapper(false, () -> {});
            if (wrapper == null) continue;
            externalStorages.put(entry.getKey(), wrapper);
        }
        if (!externalStorages.isEmpty()) {
            return this.wrapMeStorage(new CompositeStorage(externalStorages));
        }
        return null;
    }

    private PatternProviderTarget wrapMeStorage(final MEStorage storage) {
        return new PatternProviderTarget(){

            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, PatternProviderTargetCache.this.src);
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
}

