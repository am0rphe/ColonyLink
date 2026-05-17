/*
 * Decompiled with CFR 0.152.
 */
package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class PartTooltipProviders {
    private static final Comparator<Registration<?>> COMPARATOR = Comparator.comparingInt(Registration::priority);
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private static final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static final List<Registration<ServerDataProvider<?>>> serverDataProviders = new ArrayList();
    private static final List<Registration<BodyProvider<?>>> bodyProviders = new ArrayList();
    private static final List<Registration<NameProvider<?>>> nameProviders = new ArrayList();
    private static final List<Registration<IconProvider<?>>> iconProviders = new ArrayList();
    private static final Map<Class<?>, CachedProviders<?>> cache = new IdentityHashMap();

    private PartTooltipProviders() {
    }

    public static <T> void addServerData(Class<T> baseClass, ServerDataProvider<? super T> provider, int priority) {
        PartTooltipProviders.add(serverDataProviders, baseClass, provider, priority);
    }

    public static <T> void addBody(Class<T> baseClass, BodyProvider<? super T> provider, int priority) {
        PartTooltipProviders.add(bodyProviders, baseClass, provider, priority);
    }

    public static <T> void addName(Class<T> baseClass, NameProvider<? super T> provider, int priority) {
        PartTooltipProviders.add(nameProviders, baseClass, provider, priority);
    }

    public static <T> void addIcon(Class<T> baseClass, IconProvider<? super T> provider, int priority) {
        PartTooltipProviders.add(iconProviders, baseClass, provider, priority);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static <T> void add(List<Registration<T>> registrations, Class<?> baseClass, T provider, int priority) {
        writeLock.lock();
        try {
            registrations.add(new Registration<T>(baseClass, provider, priority));
            registrations.sort(COMPARATOR);
            cache.clear();
        }
        finally {
            writeLock.unlock();
        }
    }

    public static <T> CachedProviders<T> getProviders(T object) {
        return PartTooltipProviders.getProviders(object.getClass());
    }

    public static <U> CachedProviders<U> getProviders(Class<U> objectClass) {
        CachedProviders providers;
        readLock.lock();
        try {
            providers = cache.get(objectClass);
        }
        finally {
            readLock.unlock();
        }
        if (providers == null) {
            writeLock.lock();
            try {
                providers = cache.computeIfAbsent(objectClass, PartTooltipProviders::createProviderLists);
            }
            finally {
                writeLock.unlock();
            }
        }
        return providers;
    }

    private static <U> CachedProviders<U> createProviderLists(Class<U> clazz) {
        ArrayList compatibleNameProviders = new ArrayList();
        for (Registration<NameProvider<?>> registration : nameProviders) {
            if (!registration.baseClass.isAssignableFrom(clazz)) continue;
            compatibleNameProviders.add(registration.provider());
        }
        ArrayList compatibleBodyProviders = new ArrayList();
        for (Registration<BodyProvider<?>> registration : bodyProviders) {
            if (!registration.baseClass.isAssignableFrom(clazz)) continue;
            compatibleBodyProviders.add(registration.provider());
        }
        ArrayList arrayList = new ArrayList();
        for (Registration<IconProvider<?>> registration : iconProviders) {
            if (!registration.baseClass.isAssignableFrom(clazz)) continue;
            arrayList.add(registration.provider());
        }
        ArrayList arrayList2 = new ArrayList();
        for (Registration<ServerDataProvider<?>> registration : serverDataProviders) {
            if (!registration.baseClass.isAssignableFrom(clazz)) continue;
            arrayList2.add(registration.provider());
        }
        return new CachedProviders(arrayList2, compatibleBodyProviders, arrayList, compatibleNameProviders);
    }

    private record Registration<T>(Class<?> baseClass, T provider, int priority) {
    }

    record CachedProviders<U>(List<ServerDataProvider<? super U>> serverDataProviders, List<BodyProvider<? super U>> bodyProviders, List<IconProvider<? super U>> iconProviders, List<NameProvider<? super U>> nameProviders) {
    }
}

