/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public class DelegatingMEInventory
implements MEStorage {
    private MEStorage delegate;

    public DelegatingMEInventory(MEStorage delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    protected MEStorage getDelegate() {
        return this.delegate;
    }

    protected void setDelegate(MEStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey input, IActionSource source) {
        return this.getDelegate().isPreferredStorageFor(input, source);
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
    public KeyCounter getAvailableStacks() {
        return this.getDelegate().getAvailableStacks();
    }

    @Override
    public Component getDescription() {
        return this.getDelegate().getDescription();
    }
}

