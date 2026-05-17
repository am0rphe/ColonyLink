/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.behaviors.ContainerItemContext;
import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.behaviors.FluidContainerItemStrategy;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.util.CowMap;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ContainerItemStrategies {
    private static final CowMap<AEKeyType, ContainerItemStrategy<?, ?>> strategies = CowMap.identityHashMap();

    public static <T extends AEKey> void register(AEKeyType type, Class<T> keyClass, ContainerItemStrategy<T, ?> strategy) {
        Preconditions.checkArgument((type.getKeyClass() == keyClass ? 1 : 0) != 0, (String)"%s != %s", type.getKeyClass(), keyClass);
        Preconditions.checkArgument((type != AEKeyType.items() ? 1 : 0) != 0, (Object)"Can't register container items for AEItemKey");
        strategies.putIfAbsent(type, strategy);
    }

    public static boolean isTypeSupported(AEKeyType type) {
        return strategies.getMap().containsKey(type);
    }

    public static boolean isKeySupported(@Nullable AEKey key) {
        return key != null && ContainerItemStrategies.isTypeSupported(key.getType());
    }

    @Nullable
    public static GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (AEKeyType keyType : AEKeyTypes.getAll()) {
            GenericStack content;
            ContainerItemStrategy<?, ?> strategy = strategies.getMap().get(keyType);
            if (strategy == null || (content = strategy.getContainedStack(stack)) == null) continue;
            return content;
        }
        return null;
    }

    @Nullable
    public static GenericStack getContainedStack(ItemStack stack, AEKeyType keyType) {
        if (stack.isEmpty()) {
            return null;
        }
        ContainerItemStrategy<?, ?> strategy = strategies.getMap().get(keyType);
        if (strategy != null) {
            return strategy.getContainedStack(stack);
        }
        return null;
    }

    @Nullable
    public static EmptyingAction getEmptyingAction(ItemStack stack) {
        GenericStack contents = ContainerItemStrategies.getContainedStack(stack);
        if (contents == null) {
            return null;
        }
        Component description = contents.what().getDisplayName();
        return new EmptyingAction(description, contents.what(), contents.amount());
    }

    public static ContainerItemContext findCarriedContextForKey(@Nullable AEKey key, Player player, AbstractContainerMenu menu) {
        return ContainerItemStrategies.findCarriedContext(key == null ? null : key.getType(), player, menu);
    }

    @Nullable
    private static ContainerItemContext findContext(@Nullable AEKeyType keyType, Function<ContainerItemStrategy<?, ?>, @Nullable Object> contextFinder) {
        Collection<AEKeyType> candidates = keyType == null ? AEKeyTypes.getAll() : List.of(keyType);
        LinkedHashMap entries = new LinkedHashMap();
        for (AEKeyType type : candidates) {
            Object context;
            ContainerItemStrategy<?, ?> strategy = strategies.getMap().get(type);
            if (strategy == null || (context = contextFinder.apply(strategy)) == null) continue;
            entries.put(type, new ContainerItemContext.Entry<Object>(strategy, context, type));
        }
        return entries.isEmpty() ? null : new ContainerItemContext(entries);
    }

    @Nullable
    public static ContainerItemContext findCarriedContext(@Nullable AEKeyType keyType, Player player, AbstractContainerMenu menu) {
        return ContainerItemStrategies.findContext(keyType, strategy -> strategy.findCarriedContext(player, menu));
    }

    public static Set<AEKeyType> getSupportedKeyTypes() {
        return strategies.getMap().keySet();
    }

    @Nullable
    public static ContainerItemContext findOwnedItemContext(@Nullable AEKeyType keyType, Player player, ItemStack stack) {
        if (player.containerMenu != null && player.containerMenu.getCarried() == stack) {
            return ContainerItemStrategies.findCarriedContext(keyType, player, player.containerMenu);
        }
        int slotIdx = -1;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (inventory.getItem(i) != stack) continue;
            slotIdx = i;
            break;
        }
        if (slotIdx == -1) {
            return null;
        }
        int slotIdxCopy = slotIdx;
        return ContainerItemStrategies.findContext(keyType, strategy -> strategy.findPlayerSlotContext(player, slotIdxCopy));
    }

    static {
        ContainerItemStrategies.register(AEKeyType.fluids(), AEFluidKey.class, new FluidContainerItemStrategy());
    }
}

