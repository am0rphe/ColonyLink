/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.neoforged.neoforge.capabilities.Capabilities$EnergyStorage
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 */
package appeng.init.internal;

import appeng.api.features.P2PTunnelAttunement;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class InitP2PAttunements {
    private InitP2PAttunements() {
    }

    public static void init() {
        P2PTunnelAttunement.registerAttunementTag(AEParts.ME_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.FE_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.REDSTONE_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.FLUID_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.ITEM_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.LIGHT_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementApi(P2PTunnelAttunement.ENERGY_TUNNEL, Capabilities.EnergyStorage.ITEM, (Component)GuiText.P2PAttunementEnergy.text());
        P2PTunnelAttunement.registerAttunementApi(P2PTunnelAttunement.FLUID_TUNNEL, Capabilities.FluidHandler.ITEM, (Component)GuiText.P2PAttunementFluid.text());
    }
}

