/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.behaviors.PickupSink;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface PickupStrategy {
    public void reset();

    public boolean canPickUpEntity(Entity var1);

    public boolean pickUpEntity(IEnergySource var1, PickupSink var2, Entity var3);

    public Result tryPickup(IEnergySource var1, PickupSink var2);

    public static void register(AEKeyType type, Factory factory) {
        StackWorldBehaviors.registerPickupStrategy(type, factory);
    }

    @FunctionalInterface
    public static interface Factory {
        public PickupStrategy create(ServerLevel var1, BlockPos var2, Direction var3, BlockEntity var4, ItemEnchantments var5, @Nullable UUID var6);
    }

    public static enum Result {
        CANT_PICKUP,
        CANT_STORE,
        PICKED_UP;

    }
}

