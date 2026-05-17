/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.me.common;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.ClientReadOnlySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ClientDisplaySlot
extends ClientReadOnlySlot {
    private final ItemStack item;

    public ClientDisplaySlot(@Nullable GenericStack stack) {
        this.item = GenericStack.wrapInItemStack(stack);
    }

    public ItemStack getItem() {
        return this.item;
    }
}

