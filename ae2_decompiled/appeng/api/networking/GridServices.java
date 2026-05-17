/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 */
package appeng.api.networking;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridServiceProvider;
import appeng.me.helpers.GridServiceContainer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.level.Level;

public final class GridServices {
    private static final List<GridCacheRegistration<?>> registry = new ArrayList();

    private GridServices() {
    }

    public static synchronized <T extends IGridServiceProvider> void register(Class<? super T> publicInterface, Class<T> implClass) {
        if (GridServices.isRegistered(publicInterface)) {
            throw new IllegalArgumentException("Implementation for grid service " + String.valueOf(publicInterface) + " is already registered!");
        }
        GridCacheRegistration<T> registration = new GridCacheRegistration<T>(implClass, publicInterface);
        for (Class<?> dependency : registration.dependencies) {
            if (GridServices.isRegistered(dependency)) continue;
            throw new IllegalStateException("Missing dependency declared in constructor of " + String.valueOf(implClass) + ": " + String.valueOf(dependency));
        }
        registry.add(registration);
    }

    private static boolean isRegistered(Class<?> publicInterface) {
        return registry.stream().anyMatch(r -> r.publicInterface.equals(publicInterface));
    }

    static GridServiceContainer createServices(IGrid g) {
        IdentityHashMap services = new IdentityHashMap(registry.size());
        ArrayList<IGridServiceProvider> serverStartTickServices = new ArrayList<IGridServiceProvider>(registry.size());
        ArrayList<IGridServiceProvider> levelStartTickServices = new ArrayList<IGridServiceProvider>(registry.size());
        ArrayList<IGridServiceProvider> levelEndTickServices = new ArrayList<IGridServiceProvider>(registry.size());
        ArrayList<IGridServiceProvider> serverEndTickServices = new ArrayList<IGridServiceProvider>(registry.size());
        for (GridCacheRegistration<?> registration : registry) {
            IGridServiceProvider service = registration.construct(g, services);
            services.put(registration.publicInterface, service);
            if (registration.hasServerStartTick) {
                serverStartTickServices.add(service);
            }
            if (registration.hasLevelStartTick) {
                levelStartTickServices.add(service);
            }
            if (registration.hasLevelEndTick) {
                levelEndTickServices.add(service);
            }
            if (!registration.hasServerEndTick) continue;
            serverEndTickServices.add(service);
        }
        return new GridServiceContainer(services, (IGridServiceProvider[])serverStartTickServices.toArray(IGridServiceProvider[]::new), (IGridServiceProvider[])levelStartTickServices.toArray(IGridServiceProvider[]::new), (IGridServiceProvider[])levelEndTickServices.toArray(IGridServiceProvider[]::new), (IGridServiceProvider[])serverEndTickServices.toArray(IGridServiceProvider[]::new));
    }

    private static class GridCacheRegistration<T extends IGridServiceProvider> {
        private final Class<T> implClass;
        private final Class<?> publicInterface;
        private final Constructor<T> constructor;
        private final Class<?>[] constructorParameterTypes;
        private final Set<Class<?>> dependencies;
        private final boolean hasServerStartTick;
        private final boolean hasLevelStartTick;
        private final boolean hasLevelEndTick;
        private final boolean hasServerEndTick;

        public GridCacheRegistration(Class<T> implClass, Class<?> publicInterface) {
            this.publicInterface = publicInterface;
            this.implClass = implClass;
            Constructor<?>[] ctors = implClass.getConstructors();
            if (ctors.length != 1) {
                throw new IllegalArgumentException("Grid service implementation " + String.valueOf(implClass) + " has " + ctors.length + " public constructors. It needs exactly 1.");
            }
            this.constructor = ctors[0];
            this.constructorParameterTypes = this.constructor.getParameterTypes();
            this.dependencies = Arrays.stream(this.constructorParameterTypes).filter(t -> !t.equals(IGrid.class)).collect(Collectors.toSet());
            try {
                this.hasServerStartTick = implClass.getMethod("onServerStartTick", new Class[0]).getDeclaringClass() != IGridServiceProvider.class;
                this.hasLevelStartTick = implClass.getMethod("onLevelStartTick", Level.class).getDeclaringClass() != IGridServiceProvider.class;
                this.hasLevelEndTick = implClass.getMethod("onLevelEndTick", Level.class).getDeclaringClass() != IGridServiceProvider.class;
                this.hasServerEndTick = implClass.getMethod("onServerEndTick", new Class[0]).getDeclaringClass() != IGridServiceProvider.class;
            }
            catch (NoSuchMethodException exception) {
                throw new RuntimeException("Failed to check which methods the grid service implements", exception);
            }
        }

        public IGridServiceProvider construct(IGrid g, Map<Class<?>, IGridServiceProvider> createdServices) {
            IGridServiceProvider provider;
            Object[] ctorArgs = new Object[this.constructorParameterTypes.length];
            for (int i = 0; i < this.constructorParameterTypes.length; ++i) {
                Class<?> paramType = this.constructorParameterTypes[i];
                if (paramType.equals(IGrid.class)) {
                    ctorArgs[i] = g;
                    continue;
                }
                ctorArgs[i] = createdServices.get(paramType);
                if (ctorArgs[i] != null) continue;
                throw new IllegalStateException("Unsatisfied constructor dependency " + String.valueOf(paramType) + " in " + String.valueOf(this.constructor));
            }
            try {
                provider = (IGridServiceProvider)this.constructor.newInstance(ctorArgs);
            }
            catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to create grid because grid service " + String.valueOf(this.implClass) + " failed to construct.", e);
            }
            return provider;
        }
    }
}

