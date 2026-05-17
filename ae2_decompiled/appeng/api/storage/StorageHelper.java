/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 */
package appeng.api.storage;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.core.stats.AeStats;
import appeng.crafting.CraftingLink;
import com.google.common.primitives.Ints;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class StorageHelper {
    private StorageHelper() {
    }

    public static ICraftingLink loadCraftingLink(CompoundTag data, ICraftingRequester req) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(req);
        return new CraftingLink(data, req);
    }

    public static long poweredExtraction(IEnergySource energy, MEStorage inv, AEKey request, long amount, IActionSource src) {
        return StorageHelper.poweredExtraction(energy, inv, request, amount, src, Actionable.MODULATE);
    }

    public static long poweredExtraction(IEnergySource energy, MEStorage inv, AEKey request, long amount, IActionSource src, Actionable mode) {
        Objects.requireNonNull(energy, "energy");
        Objects.requireNonNull(inv, "inv");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(src, "src");
        Objects.requireNonNull(mode, "mode");
        long retrieved = inv.extract(request, amount, Actionable.SIMULATE, src);
        double energyFactor = Math.max(1.0, (double)request.getAmountPerOperation());
        double availablePower = energy.extractAEPower((double)retrieved / energyFactor, Actionable.SIMULATE, PowerMultiplier.CONFIG);
        long itemToExtract = Math.min((long)(availablePower * energyFactor + 0.9), retrieved);
        if (itemToExtract > 0L) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower((double)retrieved / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                long ret = inv.extract(request, itemToExtract, Actionable.MODULATE, src);
                if (ret != 0L && request instanceof AEItemKey) {
                    src.player().ifPresent(player -> AeStats.ItemsExtracted.addToPlayer((Player)player, Ints.saturatedCast((long)ret)));
                }
                return ret;
            }
            return itemToExtract;
        }
        return 0L;
    }

    public static long poweredInsert(IEnergySource energy, MEStorage inv, AEKey input, long amount, IActionSource src) {
        return StorageHelper.poweredInsert(energy, inv, input, amount, src, Actionable.MODULATE);
    }

    public static long poweredInsert(IEnergySource energy, MEStorage inv, AEKey input, long amount, IActionSource src, Actionable mode) {
        Objects.requireNonNull(energy);
        Objects.requireNonNull(inv);
        Objects.requireNonNull(input);
        Objects.requireNonNull(src);
        Objects.requireNonNull(mode);
        amount = inv.insert(input, amount, Actionable.SIMULATE, src);
        if (amount <= 0L) {
            return 0L;
        }
        double energyFactor = Math.max(1.0, (double)input.getAmountPerOperation());
        double availablePower = energy.extractAEPower((double)amount / energyFactor, Actionable.SIMULATE, PowerMultiplier.CONFIG);
        if ((amount = Math.min((long)(availablePower * energyFactor + 0.9), amount)) <= 0L) {
            return 0L;
        }
        if (mode == Actionable.MODULATE) {
            energy.extractAEPower((double)amount / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
            long inserted = inv.insert(input, amount, Actionable.MODULATE, src);
            if (input instanceof AEItemKey) {
                src.player().ifPresent(player -> AeStats.ItemsInserted.addToPlayer((Player)player, Ints.saturatedCast((long)inserted)));
            }
            return inserted;
        }
        return amount;
    }
}

