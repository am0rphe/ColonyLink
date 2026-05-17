/*
 * Decompiled with CFR 0.152.
 */
package appeng.init.internal;

import appeng.api.features.GridLinkables;
import appeng.core.definitions.AEItems;
import appeng.items.tools.powered.WirelessTerminalItem;

public final class InitGridLinkables {
    private InitGridLinkables() {
    }

    public static void init() {
        GridLinkables.register(AEItems.WIRELESS_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
        GridLinkables.register(AEItems.WIRELESS_CRAFTING_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
    }
}

