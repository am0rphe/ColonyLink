/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.SlotAccess
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickAction
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.INetworkToolAware;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.menu.me.networktool.NetworkToolMenu;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class NetworkToolItem
extends AEBaseItem
implements IMenuItem {
    public NetworkToolItem(Item.Properties properties) {
        super(properties);
    }

    public NetworkToolMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        Level level = player.level();
        if (hitResult == null) {
            return new NetworkToolMenuHost<NetworkToolItem>(this, player, locator, null);
        }
        IInWorldGridNodeHost host = GridHelper.getNodeHost(level, hitResult.getBlockPos());
        return new NetworkToolMenuHost<NetworkToolItem>(this, player, locator, host);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(NetworkToolMenu.TYPE, p, MenuLocators.forHand(p, hand));
        }
        return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)p.getItemInHand(hand));
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        INetworkToolAware toolAgent;
        if (context.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockEntity te = level.getBlockEntity(context.getClickedPos());
        if (te instanceof IPartHost) {
            INetworkToolAware toolAgent2;
            IPart iPart;
            IPartHost partHost = (IPartHost)te;
            SelectedPart part = partHost.selectPartWorld(context.getClickLocation());
            if ((part.part != null || part.facade != null) && (iPart = part.part) instanceof INetworkToolAware && !(toolAgent2 = (INetworkToolAware)((Object)iPart)).showNetworkInfo(context)) {
                return InteractionResult.PASS;
            }
        } else if (te instanceof INetworkToolAware && !(toolAgent = (INetworkToolAware)te).showNetworkInfo(context)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && !this.showNetworkToolGui(context)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    private boolean showNetworkToolGui(UseOnContext useContext) {
        if (useContext.getPlayer() == null) {
            return false;
        }
        BlockPos pos = useContext.getClickedPos();
        Player p = useContext.getPlayer();
        Level level = useContext.getLevel();
        InteractionHand hand = useContext.getHand();
        if (!Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
            return false;
        }
        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(level, pos);
        if (nodeHost != null) {
            MenuOpener.open(NetworkStatusMenu.NETWORK_TOOL_TYPE, p, MenuLocators.forItemUseContext(useContext));
        } else {
            MenuOpener.open(NetworkToolMenu.TYPE, p, MenuLocators.forHand(p, hand));
        }
        return true;
    }

    @Nullable
    public static NetworkToolMenuHost findNetworkToolInv(Player player) {
        Inventory pi = player.getInventory();
        for (int x = 0; x < pi.getContainerSize(); ++x) {
            Item item;
            ItemStack pii = pi.getItem(x);
            if (pii.isEmpty() || !((item = pii.getItem()) instanceof NetworkToolItem)) continue;
            NetworkToolItem networkToolItem = (NetworkToolItem)item;
            return networkToolItem.getMenuHost(pi.player, MenuLocators.forInventorySlot(x), null);
        }
        return null;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        NetworkToolMenuHost<NetworkToolItem> toolHost = new NetworkToolMenuHost<NetworkToolItem>(this, null, MenuLocators.forStack(stack), null);
        if (toolHost.getInventory().isEmpty()) {
            return Optional.empty();
        }
        LinkedHashMap<AEItemKey, Integer> upgradeCards = new LinkedHashMap<AEItemKey, Integer>();
        for (ItemStack card : toolHost.getInventory()) {
            upgradeCards.merge(AEItemKey.of(card), card.getCount(), Integer::sum);
        }
        ArrayList<GenericStack> stacks = new ArrayList<GenericStack>(upgradeCards.size());
        for (Map.Entry entry : upgradeCards.entrySet()) {
            stacks.add(new GenericStack((AEKey)entry.getKey(), ((Integer)entry.getValue()).intValue()));
        }
        stacks.sort(Comparator.comparingLong(GenericStack::amount).reversed());
        return Optional.of(new StorageCellTooltipComponent(List.of(), stacks, false, true));
    }

    public static InternalInventory getInventory(final ItemStack stack) {
        AppEngInternalInventory inv = new AppEngInternalInventory(new InternalInventoryHost(){

            @Override
            public void saveChangedInventory(AppEngInternalInventory inv) {
                stack.set(DataComponents.CONTAINER, (Object)inv.toItemContainerContents());
            }

            @Override
            public boolean isClientSide() {
                return false;
            }
        }, 9);
        inv.setEnableClientEvents(true);
        inv.setFilter(new NetworkToolInventoryFilter());
        inv.fromItemContainerContents((ItemContainerContents)stack.getOrDefault(DataComponents.CONTAINER, (Object)ItemContainerContents.EMPTY));
        return inv;
    }

    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        ItemStack other = slot.getItem();
        if (other.isEmpty()) {
            return true;
        }
        this.insertIntoTool(stack, other, player);
        return true;
    }

    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (other.isEmpty()) {
            return false;
        }
        this.insertIntoTool(stack, other, player);
        return true;
    }

    private void insertIntoTool(ItemStack tool, ItemStack upgrade, Player player) {
        NetworkToolMenuHost<NetworkToolItem> toolHost = new NetworkToolMenuHost<NetworkToolItem>(this, player, MenuLocators.forStack(tool), null);
        int amount = upgrade.getCount();
        ItemStack overflow = toolHost.getInventory().addItems(upgrade);
        upgrade.shrink(amount - overflow.getCount());
    }

    private static class NetworkToolInventoryFilter
    implements IAEItemFilter {
        private NetworkToolInventoryFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return Upgrades.isUpgradeCardItem((ItemLike)stack.getItem());
        }
    }
}

