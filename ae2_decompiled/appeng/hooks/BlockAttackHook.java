/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.hooks;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.core.network.serverbound.PartLeftClickPacket;
import appeng.util.InteractionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(value=Dist.CLIENT)
public final class BlockAttackHook {
    private BlockAttackHook() {
    }

    public static void install() {
        NeoForge.EVENT_BUS.addListener(BlockAttackHook::onBlockAttackedOnClientEvent);
    }

    private static void onBlockAttackedOnClientEvent(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (!level.isClientSide()) {
            return;
        }
        InteractionResult result = BlockAttackHook.onBlockAttackedOnClient(event.getEntity(), level);
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
        }
    }

    private static InteractionResult onBlockAttackedOnClient(Player player, Level level) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult)) {
            return InteractionResult.PASS;
        }
        BlockHitResult hitResult2 = (BlockHitResult)hitResult;
        if (BlockAttackHook.onBlockAttackedOnClient(player, level, hitResult2)) {
            Minecraft.getInstance().gameMode.destroyDelay = 5;
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    private static boolean onBlockAttackedOnClient(Player player, Level level, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Vec3 localPos = hitResult.getLocation().subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IPartHost)) {
            return false;
        }
        IPartHost partHost = (IPartHost)blockEntity;
        SelectedPart p = partHost.selectPartLocal(localPos);
        if (p.part != null) {
            boolean alternateUseMode = InteractionUtil.isInAlternateUseMode(player);
            boolean activated = alternateUseMode ? p.part.onShiftClicked(player, localPos) : p.part.onClicked(player, localPos);
            if (activated) {
                PartLeftClickPacket message = new PartLeftClickPacket(hitResult, alternateUseMode);
                PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
                return true;
            }
        } else if (p.facade != null && p.facade.onClicked(player, localPos)) {
            PartLeftClickPacket message = new PartLeftClickPacket(hitResult, false);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return true;
        }
        return false;
    }
}

