/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage;

import appeng.api.storage.ILinkStatus;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record LinkStatus(boolean connected, @Nullable Component statusDescription) implements ILinkStatus
{
    static final LinkStatus CONNECTED = new LinkStatus(true, null);
}

