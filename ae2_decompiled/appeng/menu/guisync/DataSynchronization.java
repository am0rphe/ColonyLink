/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package appeng.menu.guisync;

import appeng.core.AELog;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.SynchronizedField;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DataSynchronization {
    private final Map<Short, SynchronizedField<?>> fields = new HashMap();

    public DataSynchronization(Object host) {
        this.collectFields(host, host.getClass());
    }

    private void collectFields(Object host, Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (!f.isAnnotationPresent(GuiSync.class)) continue;
            GuiSync annotation = f.getAnnotation(GuiSync.class);
            short key = annotation.value();
            if (this.fields.containsKey(key)) {
                throw new IllegalStateException("Class " + String.valueOf(host.getClass()) + " declares the same sync id twice: " + key);
            }
            this.fields.put(key, SynchronizedField.create(host, f));
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != AbstractContainerMenu.class && superclass != Object.class) {
            this.collectFields(host, superclass);
        }
    }

    public boolean hasChanges() {
        for (SynchronizedField<?> value : this.fields.values()) {
            if (!value.hasChanges()) continue;
            return true;
        }
        return false;
    }

    public void writeFull(RegistryFriendlyByteBuf data) {
        this.writeFields(data, true);
    }

    public void writeUpdate(RegistryFriendlyByteBuf data) {
        this.writeFields(data, false);
    }

    private void writeFields(RegistryFriendlyByteBuf data, boolean includeUnchanged) {
        for (Map.Entry<Short, SynchronizedField<?>> entry : this.fields.entrySet()) {
            if (!includeUnchanged && !entry.getValue().hasChanges()) continue;
            data.writeShort((int)entry.getKey().shortValue());
            entry.getValue().write(data);
        }
        data.writeVarInt(-1);
    }

    public void readUpdate(RegistryFriendlyByteBuf data, ShortSet updatedFields) {
        short key = data.readShort();
        while (key != -1) {
            SynchronizedField<?> field = this.fields.get(key);
            if (field == null) {
                AELog.warn("Server sent update for GUI field %d, which we don't know.", key);
            } else {
                field.read(data);
                updatedFields.add(key);
            }
            key = data.readShort();
        }
    }

    public boolean hasFields() {
        return !this.fields.isEmpty();
    }
}

