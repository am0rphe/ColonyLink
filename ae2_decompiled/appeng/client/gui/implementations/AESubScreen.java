/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.implementations;

import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.widgets.TabButton;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.menu.ISubMenu;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class AESubScreen {
    private AESubScreen() {
    }

    public static void addBackButton(ISubMenu subMenu, String id, WidgetContainer widgets) {
        AESubScreen.addBackButton(subMenu, id, widgets, null);
    }

    public static void addBackButton(ISubMenu subMenu, String id, WidgetContainer widgets, @Nullable Component label) {
        if (label == null) {
            label = subMenu.getHost().getMainMenuIcon().getHoverName();
        }
        TabButton button = new TabButton(Icon.BACK, label, btn -> AESubScreen.goBack());
        widgets.add(id, (AbstractWidget)button);
    }

    public static void goBack() {
        SwitchGuisPacket message = SwitchGuisPacket.returnToParentMenu();
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

