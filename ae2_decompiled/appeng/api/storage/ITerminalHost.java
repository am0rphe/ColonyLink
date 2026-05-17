/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage;

import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ISubMenuHost;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigurableObject;
import org.jetbrains.annotations.Nullable;

public interface ITerminalHost
extends IUpgradeableObject,
IConfigurableObject,
ISubMenuHost {
    public MEStorage getInventory();

    public ILinkStatus getLinkStatus();

    @Nullable
    default public String getCloseHotkey() {
        return null;
    }
}

