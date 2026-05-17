/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.api.stacks;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypesInternal;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class AEKeyTypes {
    private AEKeyTypes() {
    }

    public static synchronized void register(AEKeyType keyType) {
        Objects.requireNonNull(keyType, "keyType");
        AEKeyTypesInternal.register(keyType);
    }

    public static AEKeyType get(ResourceLocation id) {
        AEKeyType result = (AEKeyType)AEKeyTypesInternal.getRegistry().get(id);
        if (result == null) {
            throw new IllegalArgumentException("No key type registered for id " + String.valueOf(id));
        }
        return result;
    }

    public static Set<AEKeyType> getAll() {
        return AEKeyTypesInternal.getAllTypes();
    }
}

