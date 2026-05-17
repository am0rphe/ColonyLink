/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.util;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerListener;
import appeng.api.util.UnsupportedSettingException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigManager
implements IConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);
    private final Map<Setting<?>, Enum<?>> settings = new IdentityHashMap();
    @Nullable
    private final IConfigManagerListener listener;

    public ConfigManager(IConfigManagerListener listener) {
        this.listener = listener;
    }

    public ConfigManager(Runnable changeListener) {
        this.listener = (manager, setting) -> changeListener.run();
    }

    @Override
    public Set<Setting<?>> getSettings() {
        return this.settings.keySet();
    }

    public <T extends Enum<T>> void registerSetting(Setting<T> setting, T defaultValue) {
        this.settings.put(setting, defaultValue);
    }

    @Override
    public <T extends Enum<T>> T getSetting(Setting<T> setting) {
        Enum<?> oldValue = this.settings.get(setting);
        if (oldValue == null) {
            throw new UnsupportedSettingException("Setting " + setting.getName() + " is not supported.");
        }
        return (T)((Enum)setting.getEnumClass().cast(oldValue));
    }

    @Override
    public <T extends Enum<T>> void putSetting(Setting<T> setting, T newValue) {
        if (!this.settings.containsKey(setting)) {
            throw new UnsupportedSettingException("Setting " + setting.getName() + " is not supported.");
        }
        this.settings.put(setting, newValue);
        if (this.listener != null) {
            this.listener.onSettingChanged(this, setting);
        }
    }

    @Override
    public void writeToNBT(CompoundTag tagCompound, HolderLookup.Provider registries) {
        for (Map.Entry<Setting<?>, Enum<?>> entry : this.settings.entrySet()) {
            tagCompound.putString(entry.getKey().getName(), this.settings.get(entry.getKey()).toString());
        }
    }

    @Override
    public boolean readFromNBT(CompoundTag tagCompound, HolderLookup.Provider registries) {
        boolean anythingRead = false;
        for (Setting<?> setting : this.settings.keySet()) {
            if (!tagCompound.contains(setting.getName(), 8)) continue;
            String value = tagCompound.getString(setting.getName());
            try {
                setting.setFromString(this, value);
                anythingRead = true;
            }
            catch (IllegalArgumentException e) {
                LOG.warn("Failed to load setting {} from value '{}': {}", new Object[]{setting, value, e.getMessage()});
            }
        }
        return anythingRead;
    }

    @Override
    public boolean importSettings(Map<String, String> settings) {
        boolean anythingRead = false;
        for (Setting<?> setting : this.settings.keySet()) {
            String value = settings.get(setting.getName());
            if (value == null) continue;
            try {
                setting.setFromString(this, value);
                anythingRead = true;
            }
            catch (IllegalArgumentException e) {
                LOG.warn("Failed to load setting {} from value '{}': {}", new Object[]{setting, value, e.getMessage()});
            }
        }
        return anythingRead;
    }

    @Override
    public Map<String, String> exportSettings() {
        HashMap<String, String> result = null;
        for (Map.Entry<Setting<?>, Enum<?>> entry : this.settings.entrySet()) {
            if (result == null) {
                result = new HashMap<String, String>();
            }
            result.put(entry.getKey().getName(), this.settings.get(entry.getKey()).toString());
        }
        return result == null ? Map.of() : Map.copyOf(result);
    }
}

