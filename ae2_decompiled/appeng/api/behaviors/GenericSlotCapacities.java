/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package appeng.api.behaviors;

import appeng.api.stacks.AEKeyType;
import appeng.util.CowMap;
import com.google.common.base.Preconditions;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class GenericSlotCapacities {
    private static final CowMap<AEKeyType, Long> map = CowMap.identityHashMap();

    public static void register(AEKeyType type, Long capacity) {
        Preconditions.checkArgument((capacity >= 0L ? 1 : 0) != 0, (Object)"capacity >= 0");
        map.putIfAbsent(type, capacity);
    }

    public static Map<AEKeyType, Long> getMap() {
        return map.getMap();
    }

    private GenericSlotCapacities() {
    }

    static {
        GenericSlotCapacities.register(AEKeyType.items(), 99L);
        GenericSlotCapacities.register(AEKeyType.fluids(), 4000L);
    }
}

