/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.storage;

import appeng.api.networking.IManagedGridNode;
import appeng.api.storage.LinkStatus;
import appeng.core.localization.GuiText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface ILinkStatus {
    public static ILinkStatus ofConnected() {
        return LinkStatus.CONNECTED;
    }

    public static ILinkStatus ofDisconnected() {
        return new LinkStatus(false, null);
    }

    public static ILinkStatus ofDisconnected(@Nullable Component statusDescription) {
        return new LinkStatus(false, statusDescription);
    }

    public static ILinkStatus ofManagedNode(IManagedGridNode node) {
        if (node.isOnline()) {
            return ILinkStatus.ofConnected();
        }
        if (!node.isPowered()) {
            return ILinkStatus.ofDisconnected((Component)GuiText.OutOfPower.text().withStyle(ChatFormatting.DARK_RED));
        }
        if (node.getNode() != null && !node.getNode().meetsChannelRequirements()) {
            return ILinkStatus.ofDisconnected((Component)GuiText.NoChannel.text().withStyle(ChatFormatting.DARK_RED));
        }
        return ILinkStatus.ofDisconnected(null);
    }

    public boolean connected();

    @Nullable
    public Component statusDescription();
}

