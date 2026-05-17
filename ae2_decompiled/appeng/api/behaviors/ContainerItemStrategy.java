/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface ContainerItemStrategy<T extends AEKey, C> {
    @Nullable
    public GenericStack getContainedStack(ItemStack var1);

    @Nullable
    public C findCarriedContext(Player var1, AbstractContainerMenu var2);

    @Nullable
    default public C findPlayerSlotContext(Player player, int slot) {
        return null;
    }

    public long extract(C var1, T var2, long var3, Actionable var5);

    public long insert(C var1, T var2, long var3, Actionable var5);

    public void playFillSound(Player var1, T var2);

    public void playEmptySound(Player var1, T var2);

    @Nullable
    public GenericStack getExtractableContent(C var1);

    public static <T extends AEKey> void register(AEKeyType keyType, Class<T> keyClass, ContainerItemStrategy<T, ?> strategy) {
        ContainerItemStrategies.register(keyType, keyClass, strategy);
    }
}

