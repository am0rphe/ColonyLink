/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.api.behaviors;

import appeng.api.stacks.AEKey;
import net.minecraft.network.chat.Component;

public record EmptyingAction(Component description, AEKey what, long maxAmount) {
}

