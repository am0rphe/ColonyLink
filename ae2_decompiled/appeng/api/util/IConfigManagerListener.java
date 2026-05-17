/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.util;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;

@FunctionalInterface
public interface IConfigManagerListener {
    public void onSettingChanged(IConfigManager var1, Setting<?> var2);
}

