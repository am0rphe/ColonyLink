package com.colonylink.colonylink;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Handlers côté CLIENT pour les packets du Warehouse Link Terminal.
 *
 * Séparés dans cette classe @OnlyIn(CLIENT) pour éviter que RuntimeDistCleaner
 * ne crash le serveur dédié quand il scanne le bytecode des packets S→C.
 *
 * Les méthodes handle() des packets (WarehouseTerminalSyncPacket, TerminalMeSyncPacket)
 * ne doivent PAS contenir de références directes à Screen ou ses sous-classes.
 * Elles délèguent ici via registerPayloads() côté client uniquement.
 */
@OnlyIn(Dist.CLIENT)
public class TerminalClientPacketHandler
{
    public static void handleWarehouseSync(WarehouseTerminalSyncPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WarehouseLinkTerminalScreen screen)
                screen.updateWarehouseSnapshot(packet);
        });
    }

    public static void handleMeSync(TerminalMeSyncPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WarehouseLinkTerminalScreen screen)
                screen.updateMeSnapshot(packet);
        });
    }
}