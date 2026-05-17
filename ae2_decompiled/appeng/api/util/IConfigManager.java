/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.util;

import appeng.api.config.Setting;
import appeng.api.ids.AEComponents;
import appeng.api.util.IConfigManagerBuilder;
import appeng.api.util.IConfigManagerListener;
import appeng.util.ConfigManager;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IConfigManager {
    public Set<Setting<?>> getSettings();

    default public boolean hasSetting(Setting<?> setting) {
        return this.getSettings().contains(setting);
    }

    public <T extends Enum<T>> T getSetting(Setting<T> var1);

    public <T extends Enum<T>> void putSetting(Setting<T> var1, T var2);

    public void writeToNBT(CompoundTag var1, HolderLookup.Provider var2);

    public boolean readFromNBT(CompoundTag var1, HolderLookup.Provider var2);

    public boolean importSettings(Map<String, String> var1);

    public Map<String, String> exportSettings();

    public static IConfigManagerBuilder builder(ItemStack stack) {
        return IConfigManager.builder(() -> stack);
    }

    public static IConfigManagerBuilder builder(final Supplier<ItemStack> stack) {
        final ConfigManager manager = new ConfigManager((mgr, settingName) -> ((ItemStack)stack.get()).set(AEComponents.EXPORTED_SETTINGS, mgr.exportSettings()));
        return new IConfigManagerBuilder(){

            @Override
            public <T extends Enum<T>> IConfigManagerBuilder registerSetting(Setting<T> setting, T defaultValue) {
                manager.registerSetting(setting, defaultValue);
                return this;
            }

            @Override
            public IConfigManager build() {
                manager.importSettings((Map)((ItemStack)stack.get()).getOrDefault(AEComponents.EXPORTED_SETTINGS, Map.of()));
                return manager;
            }
        };
    }

    public static IConfigManagerBuilder builder(Runnable changeListener) {
        return IConfigManager.builder((IConfigManager manager, Setting<?> setting) -> changeListener.run());
    }

    public static IConfigManagerBuilder builder(IConfigManagerListener changeListener) {
        final ConfigManager manager = new ConfigManager(changeListener);
        return new IConfigManagerBuilder(){

            @Override
            public <T extends Enum<T>> IConfigManagerBuilder registerSetting(Setting<T> setting, T defaultValue) {
                manager.registerSetting(setting, defaultValue);
                return this;
            }

            @Override
            public IConfigManager build() {
                return manager;
            }
        };
    }
}

