/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.cluster;

import appeng.me.cluster.IAECluster;

public interface IAEMultiBlock<Cluster extends IAECluster> {
    public void disconnect(boolean var1);

    public Cluster getCluster();

    public boolean isValid();
}

