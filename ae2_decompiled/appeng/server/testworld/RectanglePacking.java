/*
 * Decompiled with CFR 0.152.
 */
package appeng.server.testworld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class RectanglePacking {
    private RectanglePacking() {
    }

    public static <T> PositionedArea<T> pack(List<T> objects, Function<T, Size> measurer) {
        ArrayList<Rectangle<T>> rectangles = new ArrayList<Rectangle<T>>(objects.size());
        for (T object : objects) {
            Size size = measurer.apply(object);
            rectangles.add(new Rectangle<T>(0, 0, size.w, size.h, object));
        }
        int area = 0;
        int maxWidth = 0;
        for (Rectangle rectangle : rectangles) {
            area += rectangle.w * rectangle.h;
            maxWidth = Math.max(maxWidth, rectangle.w);
        }
        rectangles.sort((a, b) -> b.h - a.h);
        int startWidth = (int)Math.max(Math.ceil(Math.sqrt((double)area / 0.95)), (double)maxWidth);
        ArrayList<Rectangle<Object>> arrayList = new ArrayList<Rectangle<Object>>();
        arrayList.add(new Rectangle<Object>(0, 0, startWidth, Integer.MAX_VALUE, null));
        block2: for (Rectangle rectangle : rectangles) {
            for (int i = arrayList.size() - 1; i >= 0; --i) {
                Rectangle space = (Rectangle)arrayList.get(i);
                if (rectangle.w > space.w || rectangle.h > space.h) continue;
                rectangle.x = space.x;
                rectangle.y = space.y;
                if (rectangle.w == space.w && rectangle.h == space.h) {
                    Rectangle last = (Rectangle)arrayList.remove(arrayList.size() - 1);
                    if (i >= arrayList.size()) continue block2;
                    arrayList.set(i, last);
                    continue block2;
                }
                if (rectangle.h == space.h) {
                    space.x += rectangle.w;
                    space.w -= rectangle.w;
                    continue block2;
                }
                if (rectangle.w == space.w) {
                    space.y += rectangle.h;
                    space.h -= rectangle.h;
                    continue block2;
                }
                arrayList.add(new Rectangle<Object>(space.x + rectangle.w, space.y, space.w - rectangle.w, rectangle.h, null));
                space.y += rectangle.h;
                space.h -= rectangle.h;
                continue block2;
            }
        }
        List positioned = rectangles.stream().map(Rectangle::toPositioned).toList();
        int n = rectangles.stream().mapToInt(r -> r.x + r.w).max().orElse(0);
        int height = rectangles.stream().mapToInt(r -> r.y + r.h).max().orElse(0);
        return new PositionedArea(n, height, positioned);
    }

    public record Size(int w, int h) {
    }

    private static class Rectangle<T> {
        private int x;
        private int y;
        private int w;
        private int h;
        private final T wrapped;

        public Rectangle(int x, int y, int w, int h, T wrapped) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.wrapped = wrapped;
        }

        public Rectangle<T> copy() {
            return new Rectangle<T>(this.x, this.y, this.w, this.h, this.wrapped);
        }

        public Positioned<T> toPositioned() {
            return new Positioned<T>(this.x, this.y, this.w, this.h, this.wrapped);
        }
    }

    public record PositionedArea<T>(int w, int h, List<Positioned<T>> rectangles) {
    }

    public record Positioned<T>(int x, int y, int w, int h, T what) {
    }
}

