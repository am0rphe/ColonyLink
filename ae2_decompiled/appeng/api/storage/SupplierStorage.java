/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NullInventory;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class SupplierStorage
implements MEStorage {
    private final Supplier<@Nullable MEStorage> supplier;

    public SupplierStorage(Supplier<@Nullable MEStorage> supplier) {
        this.supplier = supplier;
    }

    private MEStorage getDelegate() {
        return Objects.requireNonNullElseGet(this.supplier.get(), NullInventory::of);
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return this.getDelegate().isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return this.getDelegate().insert(what, amount, mode, source);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return this.getDelegate().extract(what, amount, mode, source);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        this.getDelegate().getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        return this.getDelegate().getDescription();
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return this.getDelegate().getAvailableStacks();
    }
}

