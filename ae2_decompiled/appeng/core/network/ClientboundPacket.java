/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package appeng.core.network;

import appeng.core.network.CustomAppEngPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ClientboundPacket
extends CustomAppEngPayload {
    default public void handleOnClient(IPayloadContext context) {
        context.enqueueWork(() -> this.handleOnClient(context.player()));
    }

    default public void handleOnClient(Player player) {
        throw new AbstractMethodError("Unimplemented method on " + String.valueOf(this.getClass()));
    }
}

