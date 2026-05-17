/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package appeng.api.config;

import appeng.api.util.IConfigManager;
import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.Set;

public final class Setting<T extends Enum<T>> {
    private final String name;
    private final Class<T> enumClass;
    private final ImmutableSet<T> values;

    public Setting(String name, Class<T> enumClass) {
        this(name, enumClass, EnumSet.allOf(enumClass));
    }

    public Setting(String name, Class<T> enumClass, EnumSet<T> values) {
        this.name = name;
        this.enumClass = enumClass;
        this.values = ImmutableSet.copyOf(values);
    }

    public String getName() {
        return this.name;
    }

    public Set<T> getValues() {
        return this.values;
    }

    public T getValue(IConfigManager configManager) {
        return (T)((Enum)this.enumClass.cast(configManager.getSetting(this)));
    }

    public Class<T> getEnumClass() {
        return this.enumClass;
    }

    public void setFromString(IConfigManager cm, String value) {
        for (Enum allowedValue : this.values) {
            if (!allowedValue.name().equals(value)) continue;
            cm.putSetting(this, allowedValue);
            return;
        }
        throw new IllegalArgumentException("Received invalid value '" + value + "' for setting '" + this.name + "'");
    }

    public void copy(IConfigManager from, IConfigManager to) {
        to.putSetting(this, from.getSetting(this));
    }

    public String toString() {
        return this.name;
    }
}

