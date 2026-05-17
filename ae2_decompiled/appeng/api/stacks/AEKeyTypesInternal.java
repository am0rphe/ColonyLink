/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.registries.callback.BakeCallback
 *  net.neoforged.neoforge.registries.callback.RegistryCallback
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.stacks;

import appeng.api.stacks.AEKeyType;
import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class AEKeyTypesInternal {
    @Nullable
    private static Registry<AEKeyType> registry;
    @Nullable
    private static Set<AEKeyType> allTypes;

    private AEKeyTypesInternal() {
    }

    public static Registry<AEKeyType> getRegistry() {
        Preconditions.checkState((registry != null ? 1 : 0) != 0, (Object)"AE2 isn't initialized yet.");
        return registry;
    }

    public static void setRegistry(Registry<AEKeyType> registry) {
        Preconditions.checkState((AEKeyTypesInternal.registry == null ? 1 : 0) != 0);
        AEKeyTypesInternal.registry = registry;
        registry.addCallback((RegistryCallback)((BakeCallback)ignored -> {
            HashSet<AEKeyType> types = new HashSet<AEKeyType>();
            for (AEKeyType aeKeyType : registry) {
                types.add(aeKeyType);
            }
            allTypes = Set.copyOf(types);
        }));
    }

    public static Set<AEKeyType> getAllTypes() {
        Preconditions.checkState((allTypes != null ? 1 : 0) != 0, (Object)"AE2 isn't initialized yet.");
        return allTypes;
    }

    public static void register(AEKeyType keyType) {
        Registry.register(AEKeyTypesInternal.getRegistry(), (ResourceLocation)keyType.getId(), (Object)keyType);
    }
}

