/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongAVLTreeMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 */
package appeng.api.stacks;

import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Comparator;

interface AEKey2LongMap
extends Object2LongMap<AEKey> {
    public long addTo(AEKey var1, long var2);

    public static final class AVLTreeMap
    extends Object2LongAVLTreeMap<AEKey>
    implements AEKey2LongMap {
        public AVLTreeMap(Comparator<? super AEKey> c) {
            super(c);
        }
    }

    public static final class OpenHashMap
    extends Object2LongOpenHashMap<AEKey>
    implements AEKey2LongMap {
    }
}

