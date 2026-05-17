/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.BlockHitResult
 */
package appeng.menu.locator;

import appeng.api.util.DimensionalBlockPos;
import appeng.menu.locator.BlockEntityLocator;
import appeng.menu.locator.CuriosItemLocator;
import appeng.menu.locator.InventoryItemLocator;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.PartLocator;
import appeng.menu.locator.StackItemLocator;
import appeng.parts.AEBasePart;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public final class MenuLocators {
    private static final Map<String, Registration<?>> REGISTRY = new HashMap();

    public static synchronized <T extends MenuHostLocator> void register(Class<T> locatorClass, BiConsumer<T, FriendlyByteBuf> packetWriter, Function<FriendlyByteBuf, T> packetReader) {
        String classKey = locatorClass.getName();
        if (REGISTRY.containsKey(classKey)) {
            throw new IllegalStateException("MenuLocator type " + classKey + " is already registered.");
        }
        REGISTRY.put(classKey, new Registration<T>(locatorClass, packetWriter, packetReader));
    }

    private static synchronized Registration<?> getRegistration(String classKey) {
        Registration<?> registration = REGISTRY.get(classKey);
        if (registration == null) {
            throw new IllegalArgumentException("Unregistered menu locator class: " + classKey);
        }
        return registration;
    }

    public static <T extends MenuHostLocator> void writeToPacket(FriendlyByteBuf buf, T locator) {
        String classKey = locator.getClass().getName();
        Registration<?> registration = MenuLocators.getRegistration(classKey);
        buf.writeUtf(classKey);
        registration.writeToPacket.accept(locator, buf);
    }

    public static MenuHostLocator readFromPacket(FriendlyByteBuf buf) {
        String classKey = buf.readUtf();
        Registration<?> registration = MenuLocators.getRegistration(classKey);
        return (MenuHostLocator)registration.readFromPacket.apply(buf);
    }

    public static MenuHostLocator forBlockEntity(BlockEntity te) {
        if (te.getLevel() == null) {
            throw new IllegalArgumentException("Cannot open a block entity that is not in a level");
        }
        return new BlockEntityLocator(te.getBlockPos());
    }

    public static MenuHostLocator forPart(AEBasePart part) {
        DimensionalBlockPos pos = part.getHost().getLocation();
        return new PartLocator(pos.getPos(), part.getSide());
    }

    public static ItemMenuHostLocator forStack(ItemStack stack) {
        return new StackItemLocator(stack);
    }

    public static ItemMenuHostLocator forItemUseContext(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            throw new IllegalArgumentException("Cannot open a menu without a player");
        }
        int slot = MenuLocators.getPlayerInventorySlotFromHand(player, context.getHand());
        return new InventoryItemLocator(slot, MenuLocators.getHitResult(context));
    }

    public static ItemMenuHostLocator forHand(Player player, InteractionHand hand) {
        int slot = MenuLocators.getPlayerInventorySlotFromHand(player, hand);
        return MenuLocators.forInventorySlot(slot);
    }

    public static ItemMenuHostLocator forInventorySlot(int inventorySlot) {
        return new InventoryItemLocator(inventorySlot, null);
    }

    public static ItemMenuHostLocator forCurioSlot(int curioSlot) {
        return new CuriosItemLocator(curioSlot, null);
    }

    public static ItemMenuHostLocator forCurioSlot(int curioSlot, UseOnContext context) {
        return new CuriosItemLocator(curioSlot, MenuLocators.getHitResult(context));
    }

    private static int getPlayerInventorySlotFromHand(Player player, InteractionHand hand) {
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) {
            throw new IllegalArgumentException("Cannot open an item-inventory with empty hands");
        }
        int invSize = player.getInventory().getContainerSize();
        for (int i = 0; i < invSize; ++i) {
            if (player.getInventory().getItem(i) != is) continue;
            return i;
        }
        throw new IllegalArgumentException("Could not find item held in hand " + String.valueOf(hand) + " in player inventory");
    }

    private static BlockHitResult getHitResult(UseOnContext context) {
        return new BlockHitResult(context.getClickLocation(), context.getHorizontalDirection(), context.getClickedPos(), context.isInside());
    }

    static {
        MenuLocators.register(BlockEntityLocator.class, BlockEntityLocator::writeToPacket, BlockEntityLocator::readFromPacket);
        MenuLocators.register(PartLocator.class, PartLocator::writeToPacket, PartLocator::readFromPacket);
        MenuLocators.register(InventoryItemLocator.class, InventoryItemLocator::writeToPacket, InventoryItemLocator::readFromPacket);
        MenuLocators.register(CuriosItemLocator.class, CuriosItemLocator::writeToPacket, CuriosItemLocator::readFromPacket);
    }

    private record Registration<T extends MenuHostLocator>(Class<T> locatorClass, BiConsumer<T, FriendlyByteBuf> writeToPacket, Function<FriendlyByteBuf, T> readFromPacket) {
    }
}

