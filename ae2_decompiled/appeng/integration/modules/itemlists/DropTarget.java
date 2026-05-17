/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.integration.modules.itemlists;

import appeng.api.stacks.GenericStack;
import net.minecraft.client.renderer.Rect2i;

public interface DropTarget {
    public Rect2i area();

    public boolean canDrop(GenericStack var1);

    public boolean drop(GenericStack var1);
}

