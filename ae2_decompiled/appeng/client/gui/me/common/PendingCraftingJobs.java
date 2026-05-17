/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.gui.me.common;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.FinishedJobToast;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.util.SearchInventoryEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public final class PendingCraftingJobs {
    private static final Map<UUID, PendingJob> jobs = new HashMap<UUID, PendingJob>();

    private PendingCraftingJobs() {
    }

    public static boolean hasPendingJob(AEKey what) {
        return jobs.entrySet().stream().anyMatch(s -> ((PendingJob)s.getValue()).what.equals(what));
    }

    public static void clearPendingJobs() {
        jobs.clear();
    }

    public static void jobStatus(UUID id, AEKey what, long requestedAmount, long remainingAmount, CraftingJobStatusPacket.Status status) {
        AELog.debug("Crafting job " + String.valueOf(id) + " for " + requestedAmount + "x" + AEKeyRendering.getDisplayName(what).getString() + ". State=" + String.valueOf((Object)status), new Object[0]);
        PendingJob existing = jobs.get(id);
        switch (status) {
            case STARTED: {
                if (existing != null) break;
                jobs.put(id, new PendingJob(id, what, requestedAmount, remainingAmount));
                break;
            }
            case CANCELLED: {
                jobs.remove(id);
                break;
            }
            case FINISHED: {
                jobs.remove(id);
                Minecraft minecraft = Minecraft.getInstance();
                if (!AEConfig.instance().isNotifyForFinishedCraftingJobs() || minecraft.screen instanceof MEStorageScreen || minecraft.player == null || !PendingCraftingJobs.hasNotificationEnablingItem(minecraft.player)) break;
                minecraft.getToasts().addToast((Toast)new FinishedJobToast(what, requestedAmount));
            }
        }
    }

    private static boolean hasNotificationEnablingItem(LocalPlayer player) {
        for (ItemStack stack : SearchInventoryEvent.getItems((Player)player)) {
            WirelessTerminalItem wirelessTerminal;
            Item item;
            if (stack.isEmpty() || !((item = stack.getItem()) instanceof WirelessTerminalItem) || !((wirelessTerminal = (WirelessTerminalItem)item).getAECurrentPower(stack) > 0.0) || wirelessTerminal.getLinkedPosition(stack) == null) continue;
            return true;
        }
        return false;
    }

    record PendingJob(UUID jobId, AEKey what, long requestedAmount, long remainingAmount) {
    }
}

