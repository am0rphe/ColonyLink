package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Centralise tous les handlers S→C qui référencent Minecraft/Screen/ColonyLinkScreen.
 *
 * Annoté @OnlyIn(CLIENT) — RuntimeDistCleaner ne scanne jamais cette classe côté serveur.
 * Les classes packet (ColonyLinkPacket etc.) ne contiennent plus de handle() ni d'import
 * Minecraft → leur bytecode est safe au classload côté serveur dédié.
 *
 * Dans registerPayloads(), les playToClient utilisent des lambdas qui appellent
 * ces méthodes. La classe n'est chargée qu'à l'exécution du lambda (côté client).
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
            else
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