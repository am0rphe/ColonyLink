/*
 * Decompiled with CFR 0.152.
 */
package appeng.util.iterators;

import java.util.Iterator;

public final class ChainedIterator<T>
implements Iterator<T> {
    private final T[] list;
    private int offset = 0;

    public ChainedIterator(T ... list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return this.offset < this.list.length;
    }

    @Override
    public T next() {
        T result = this.list[this.offset];
        ++this.offset;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

