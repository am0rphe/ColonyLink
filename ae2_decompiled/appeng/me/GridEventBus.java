/*
 * Decompiled with CFR 0.152.
 */
package appeng.me;

import appeng.api.networking.IGrid;
import appeng.api.networking.events.GridEvent;
import appeng.me.Grid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class GridEventBus {
    private static final Map<Class<? extends GridEvent>, Subscriptions<?>> EVENTS = new HashMap();

    private GridEventBus() {
    }

    private static <T extends GridEvent> Subscriptions<T> getSubscriptions(Class<T> eventClass) {
        return EVENTS.computeIfAbsent(eventClass, Subscriptions::new);
    }

    public static <T extends GridEvent> void subscribe(Class<T> eventClass, BiConsumer<IGrid, T> handler) {
        GridEventBus.getSubscriptions(eventClass).subscribe(handler);
    }

    public static void postEvent(Grid g, GridEvent e) {
        GridEventBus.getSubscriptions(e.getClass()).invoke(g, e);
    }

    private static class Subscriptions<T extends GridEvent> {
        private final Class<T> eventClass;
        private final List<BiConsumer<IGrid, T>> handlers = new ArrayList<BiConsumer<IGrid, T>>();

        private Subscriptions(Class<T> eventClass) {
            this.eventClass = eventClass;
        }

        public void subscribe(BiConsumer<IGrid, T> handler) {
            this.handlers.add(handler);
        }

        public void invoke(IGrid grid, GridEvent event) {
            GridEvent typedEvent = (GridEvent)this.eventClass.cast(event);
            for (BiConsumer<IGrid, IGrid> biConsumer : this.handlers) {
                biConsumer.accept(grid, (IGrid)((Object)typedEvent));
            }
        }
    }
}

