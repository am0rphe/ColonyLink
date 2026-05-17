/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.storage;

import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import appeng.me.storage.NullInventory;
import java.util.function.Supplier;

public class LinkStatusRespectingInventory
extends DelegatingMEInventory {
    private final Supplier<ILinkStatus> linkStatusSupplier;

    public LinkStatusRespectingInventory(MEStorage delegate, Supplier<ILinkStatus> linkStatusSupplier) {
        super(delegate);
        this.linkStatusSupplier = linkStatusSupplier;
    }

    @Override
    protected MEStorage getDelegate() {
        if (this.linkStatusSupplier.get().connected()) {
            return super.getDelegate();
        }
        return NullInventory.of();
    }
}

