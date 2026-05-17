/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package appeng.api.networking.crafting;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import com.google.common.collect.ImmutableSet;

public interface ICraftingRequester
extends IActionHost,
IGridNodeService {
    public ImmutableSet<ICraftingLink> getRequestedJobs();

    public long insertCraftedItems(ICraftingLink var1, AEKey var2, long var3, Actionable var5);

    public void jobStateChange(ICraftingLink var1);
}

