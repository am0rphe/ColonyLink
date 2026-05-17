/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record CraftConfirmPlanPacket(CraftingPlanSummary plan) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftConfirmPlanPacket> STREAM_CODEC = StreamCodec.ofMember(CraftConfirmPlanPacket::write, CraftConfirmPlanPacket::decode);
    public static final CustomPacketPayload.Type<CraftConfirmPlanPacket> TYPE = CustomAppEngPayload.createType("craft_confirm_plan");

    public CustomPacketPayload.Type<CraftConfirmPlanPacket> type() {
        return TYPE;
    }

    public static CraftConfirmPlanPacket decode(RegistryFriendlyByteBuf data) {
        return new CraftConfirmPlanPacket(CraftingPlanSummary.read(data));
    }

    public void write(RegistryFriendlyByteBuf data) {
        this.plan.write(data);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof CraftConfirmMenu) {
            CraftConfirmMenu menu = (CraftConfirmMenu)abstractContainerMenu;
            menu.setPlan(this.plan);
        }
    }
}

