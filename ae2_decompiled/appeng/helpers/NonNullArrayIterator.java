/*
 * Decompiled with CFR 0.152.
 */
package appeng.helpers;

import java.util.Iterator;

public class NonNullArrayIterator<E>
implements Iterator<E> {
    private final E[] g;
    private int offset = 0;

    public NonNullArrayIterator(E[] o) {
        this.g = o;
    }

    @Override
    public boolean hasNext() {
        while (this.offset < this.g.length && this.g[this.offset] == null) {
            ++this.offset;
        }
        return this.offset != this.g.length;
    }

    @Override
    public E next() {
        E result = this.g[this.offset];
        ++this.offset;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

