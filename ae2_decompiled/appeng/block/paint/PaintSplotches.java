/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package appeng.block.paint;

import appeng.helpers.Splotch;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

public class PaintSplotches {
    private final List<Splotch> splotches;

    public PaintSplotches(Collection<Splotch> splotches) {
        this.splotches = ImmutableList.copyOf(splotches);
    }

    List<Splotch> getSplotches() {
        return this.splotches;
    }
}

