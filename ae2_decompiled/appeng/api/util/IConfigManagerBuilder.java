/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.util;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;

public interface IConfigManagerBuilder {
    public <T extends Enum<T>> IConfigManagerBuilder registerSetting(Setting<T> var1, T var2);

    public IConfigManager build();
}

