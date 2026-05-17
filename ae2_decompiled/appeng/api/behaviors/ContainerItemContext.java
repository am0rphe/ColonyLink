/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import com.google.common.base.Preconditions;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class ContainerItemContext {
    private final Map<AEKeyType, Entry<?>> entries;

    ContainerItemContext(Map<AEKeyType, Entry<?>> entries) {
        this.entries = entries;
    }

    @Nullable
    public GenericStack getExtractableContent() {
        for (Map.Entry<AEKeyType, Entry<?>> entry : this.entries.entrySet()) {
            GenericStack content = entry.getValue().getExtractableContent();
            if (content == null) continue;
            return content;
        }
        return null;
    }

    private Entry<?> getEntry(AEKey key) {
        AEKeyType keyType = key.getType();
        Preconditions.checkArgument((boolean)this.entries.containsKey(keyType), (Object)"Internal logic error: mismatched key and type");
        return this.entries.get(keyType);
    }

    public long insert(AEKey key, long amount, Actionable mode) {
        return this.getEntry(key).insert(key, amount, mode);
    }

    public long extract(AEKey key, long amount, Actionable mode) {
        return this.getEntry(key).extract(key, amount, mode);
    }

    public void playFillSound(Player player, AEKey key) {
        this.getEntry(key).playFillSound(player, key);
    }

    public void playEmptySound(Player player, AEKey key) {
        this.getEntry(key).playEmptySound(player, key);
    }

    static class Entry<C> {
        private final ContainerItemStrategy<AEKey, C> strategy;
        private final C context;
        private final AEKeyType type;

        Entry(ContainerItemStrategy<AEKey, C> strategy, C context, AEKeyType type) {
            this.strategy = strategy;
            this.context = context;
            this.type = type;
        }

        @Nullable
        public GenericStack getExtractableContent() {
            return this.strategy.getExtractableContent(this.context);
        }

        public long insert(AEKey key, long amount, Actionable mode) {
            Preconditions.checkArgument((boolean)this.type.contains(key), (Object)"Internal logic error: mismatched key and type");
            return this.strategy.insert(this.context, key, amount, mode);
        }

        public long extract(AEKey key, long amount, Actionable mode) {
            Preconditions.checkArgument((boolean)this.type.contains(key), (Object)"Internal logic error: mismatched key and type");
            return this.strategy.extract(this.context, key, amount, mode);
        }

        public void playFillSound(Player player, AEKey what) {
            this.strategy.playFillSound(player, what);
        }

        public void playEmptySound(Player player, AEKey what) {
            this.strategy.playEmptySound(player, what);
        }
    }
}

