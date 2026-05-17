/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client;

import appeng.client.Hotkey;
import java.util.HashMap;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;

public class Hotkeys {
    private static final HashMap<String, Hotkey> HOTKEYS = new HashMap();
    private static boolean finalized;

    private static Hotkey createHotkey(String id) {
        if (finalized) {
            throw new IllegalStateException("Hotkey registration already finalized!");
        }
        return new Hotkey(id, new KeyMapping("key.ae2." + id, -1, "key.ae2.category"));
    }

    private static void registerHotkey(Hotkey hotkey) {
        HOTKEYS.put(hotkey.name(), hotkey);
    }

    public static void finalizeRegistration(Consumer<KeyMapping> register) {
        for (Hotkey value : HOTKEYS.values()) {
            register.accept(value.mapping());
        }
        finalized = true;
    }

    public static void registerHotkey(String id) {
        Hotkeys.registerHotkey(Hotkeys.createHotkey(id));
    }

    public static void checkHotkeys() {
        HOTKEYS.forEach((name, hotkey) -> hotkey.check());
    }

    @Nullable
    public static Hotkey getHotkeyMapping(@Nullable String id) {
        return HOTKEYS.get(id);
    }
}

