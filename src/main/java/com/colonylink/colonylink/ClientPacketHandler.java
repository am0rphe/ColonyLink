package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Centralizes all S→C handlers that reference Minecraft/Screen/ColonyLinkScreen.
 *
 * @OnlyIn(CLIENT) so RuntimeDistCleaner never scans it server-side. The packet
 * classes (ColonyLinkPacket etc.) hold no handle() nor Minecraft import, so their
 * bytecode is safe to classload on a dedicated server. The playToClient lambdas in
 * registerPayloads() call these methods, so the class loads only when a lambda runs
 * (client-side).
 */
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler
{
    public static void handleColonyLink(ColonyLinkPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            ClientRFCache.update(packet.rfStored(), packet.rfMax());
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof ColonyLinkScreen cls)
                cls.updateFromPacket(packet);
            else if (screen instanceof ColonyLinkConfigScreen cfgScreen)
                cfgScreen.updateParentPacket(packet);
            // 1.7.0-beta.5 (audit #9/#39) — only open the Clipboard when the player is
            // in-game with no GUI. A packet still in flight when another screen took
            // over (death screen, pause menu, another mod's GUI…) must not pull the
            // Clipboard over it.
            else if (screen == null)
                Minecraft.getInstance().setScreen(new ColonyLinkScreen(packet));
        });
    }

    public static void handleTabCounts(TabCountsPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            for (int i = 0; i < packet.counts().size(); i++)
            {
                if (i == packet.activeTabIndex()) continue;
                int count = packet.counts().get(i);
                ColonyLinkScreen.markTabUnread(i, count);
            }
        });
    }

    public static void handleWarehouseResult(WarehouseResultPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            if (Minecraft.getInstance().screen instanceof ColonyLinkScreen screen)
                screen.updateWarehouseSnapshot(packet);
        });
    }

    public static void handleCitizens(CitizensPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof ColonyLinkScreen cls)
                cls.updateCitizens(packet);
        });
    }

    public static void handleStats(StatsPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof ColonyLinkScreen cls)
                cls.updateStats(packet);
        });
    }

    public static void handlePackageTokenSync(PackageTokenSyncPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() ->
        {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof ColonyLinkScreen cls)
                cls.updatePackageCount(packet.count());
        });
    }
}