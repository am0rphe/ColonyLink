/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 */
package appeng.core.network;

import appeng.core.AppEng;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface CustomAppEngPayload
extends CustomPacketPayload {
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String name) {
        return new CustomPacketPayload.Type(AppEng.makeId(name));
    }
}

