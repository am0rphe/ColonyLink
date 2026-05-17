/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.neoforge.capabilities.ICapabilityProvider
 *  net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
 */
package appeng.api.parts;

import appeng.api.parts.IPartHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class RegisterPartCapabilitiesEventInternal {
    private RegisterPartCapabilitiesEventInternal() {
    }

    public static void register(RegisterPartCapabilitiesEvent partEvent, RegisterCapabilitiesEvent event) {
        for (RegisterPartCapabilitiesEvent.BlockCapabilityRegistration<?, ?> registration : partEvent.capabilityRegistrations.values()) {
            RegisterPartCapabilitiesEventInternal.register(partEvent, event, registration);
        }
    }

    private static <T, C> void register(RegisterPartCapabilitiesEvent partEvent, RegisterCapabilitiesEvent event, RegisterPartCapabilitiesEvent.BlockCapabilityRegistration<T, C> registration) {
        ICapabilityProvider<IPartHost, C, T> provider = registration.buildProvider();
        for (BlockEntityType<? extends IPartHost> hostType : partEvent.hostTypes) {
            event.registerBlockEntity(registration.capability(), hostType, provider);
        }
    }
}

