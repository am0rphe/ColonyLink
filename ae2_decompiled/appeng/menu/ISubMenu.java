/*
 * Decompiled with CFR 0.152.
 */
package appeng.menu;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.locator.MenuHostLocator;

public interface ISubMenu {
    public MenuHostLocator getLocator();

    public ISubMenuHost getHost();
}

