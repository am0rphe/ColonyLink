/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.bus.api.Event
 *  net.neoforged.fml.event.IModBusEvent
 *  net.neoforged.neoforge.capabilities.BlockCapability
 *  net.neoforged.neoforge.capabilities.ICapabilityProvider
 */
package appeng.api.parts;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

public class RegisterPartCapabilitiesEvent
extends Event
implements IModBusEvent {
    final Set<BlockEntityType<? extends IPartHost>> hostTypes = new HashSet<BlockEntityType<? extends IPartHost>>();
    final Map<BlockCapability<?, ?>, Function<?, Direction>> contextMappers = new HashMap();
    final Map<BlockCapability<?, ?>, BlockCapabilityRegistration<?, ?>> capabilityRegistrations = new HashMap();

    public <T, C> void registerContext(BlockCapability<T, C> capability, Function<C, Direction> directionGetter) {
        this.contextMappers.put(capability, directionGetter);
    }

    public <T, C, P extends IPart> void register(BlockCapability<T, C> capability, ICapabilityProvider<P, C, T> provider, Class<P> partClass) {
        Objects.requireNonNull(capability, "capability");
        Objects.requireNonNull(partClass, "partClass");
        Objects.requireNonNull(provider, "provider");
        if (partClass.isInterface() || Modifier.isAbstract(partClass.getModifiers())) {
            throw new IllegalArgumentException("Capabilities can only be registered for concrete part classes: " + partClass.getCanonicalName());
        }
        Function<Object, Direction> mapper = this.contextMappers.getOrDefault(capability, c -> (Direction)c);
        BlockCapabilityRegistration registrations = this.capabilityRegistrations.computeIfAbsent(capability, ignored -> new BlockCapabilityRegistration(capability, mapper));
        registrations.add(partClass, provider);
    }

    public <T extends BlockEntity> void addHostType(BlockEntityType<T> hostType) {
        this.hostTypes.add(hostType);
    }

    record BlockCapabilityRegistration<T, C>(BlockCapability<T, C> capability, Function<C, Direction> contextToSide, Map<Class<? extends IPart>, ICapabilityProvider<?, C, T>> parts) {
        public BlockCapabilityRegistration(BlockCapability<T, C> capability, Function<C, Direction> contextToSide) {
            this(capability, contextToSide, new HashMap());
        }

        <P extends IPart> void add(Class<P> partClass, ICapabilityProvider<P, C, T> provider) {
            if (this.parts.putIfAbsent(partClass, provider) != null) {
                throw new IllegalStateException("Cannot register an additional capability provider for part " + String.valueOf(partClass) + " since there already is one for capability " + String.valueOf(this.capability));
            }
        }

        public ICapabilityProvider<IPartHost, C, T> buildProvider() {
            return (partHost, context) -> {
                Direction side = this.contextToSide.apply(context);
                IPart part = partHost.getPart(side);
                if (part != null) {
                    return this.handlePart(part, context);
                }
                return null;
            };
        }

        private <P extends IPart> T handlePart(P part, C context) {
            ICapabilityProvider<?, C, T> partProvider = this.parts.get(part.getClass());
            if (partProvider != null) {
                return (T)partProvider.getCapability(part, context);
            }
            return null;
        }
    }
}

