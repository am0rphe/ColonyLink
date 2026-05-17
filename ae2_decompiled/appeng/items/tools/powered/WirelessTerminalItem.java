/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.GlobalPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.items.tools.powered;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.networking.IGrid;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.PoweredContainerItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.Platform;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WirelessTerminalItem
extends PoweredContainerItem
implements IMenuItem,
IUpgradeableItem {
    private static final Logger LOG = LoggerFactory.getLogger(WirelessTerminalItem.class);
    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public WirelessTerminalItem(DoubleSupplier powerCapacity, Item.Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800.0 + 800.0 * (double)Upgrades.getEnergyCardMultiplier(this.getUpgrades(stack));
    }

    public boolean openFromInventory(Player player, ItemMenuHostLocator locator) {
        return this.openFromInventory(player, locator, false);
    }

    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubmenu) {
        ItemStack is = locator.locateItem(player);
        if (!player.level().isClientSide() && this.checkPreconditions(is)) {
            return MenuOpener.open(this.getMenuType(), player, locator, returningFromSubmenu);
        }
        return false;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack is = player.getItemInHand(hand);
        if (!player.level().isClientSide() && this.checkPreconditions(is) && MenuOpener.open(this.getMenuType(), player, MenuLocators.forHand(player, hand))) {
            return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)is);
        }
        return new InteractionResultHolder(InteractionResult.FAIL, (Object)is);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        if (this.getLinkedPosition(stack) == null) {
            lines.add((Component)Tooltips.of(GuiText.Unlinked, Tooltips.RED, new Object[0]));
        } else {
            lines.add((Component)Tooltips.of(GuiText.Linked, Tooltips.GREEN, new Object[0]));
        }
    }

    @Nullable
    public GlobalPos getLinkedPosition(ItemStack item) {
        return (GlobalPos)item.get(AEComponents.WIRELESS_LINK_TARGET);
    }

    @Nullable
    public IGrid getLinkedGrid(ItemStack item, Level level, @Nullable Consumer<Component> errorConsumer) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        GlobalPos linkedPos = this.getLinkedPosition(item);
        if (linkedPos == null) {
            if (errorConsumer != null) {
                errorConsumer.accept((Component)PlayerMessages.DeviceNotLinked.text());
            }
            return null;
        }
        ServerLevel linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            if (errorConsumer != null) {
                errorConsumer.accept((Component)PlayerMessages.LinkedNetworkNotFound.text());
            }
            return null;
        }
        BlockEntity be = Platform.getTickingBlockEntity((Level)linkedLevel, linkedPos.pos());
        if (!(be instanceof IWirelessAccessPoint)) {
            if (errorConsumer != null) {
                errorConsumer.accept((Component)PlayerMessages.LinkedNetworkNotFound.text());
            }
            return null;
        }
        IWirelessAccessPoint accessPoint = (IWirelessAccessPoint)be;
        IGrid grid = accessPoint.getGrid();
        if (grid == null && errorConsumer != null) {
            errorConsumer.accept((Component)PlayerMessages.LinkedNetworkNotFound.text());
        }
        return grid;
    }

    public MenuType<?> getMenuType() {
        return MEStorageMenu.WIRELESS_TYPE;
    }

    @Nullable
    public WirelessTerminalMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new WirelessTerminalMenuHost<WirelessTerminalItem>(this, player, locator, (p, subMenu) -> this.openFromInventory((Player)p, locator, true));
    }

    protected boolean checkPreconditions(ItemStack item) {
        return !item.isEmpty() && item.getItem() == this;
    }

    public boolean usePower(Player player, double amount, ItemStack is) {
        return this.extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    public boolean hasPower(Player player, double amt, ItemStack is) {
        return this.getAECurrentPower(is) >= amt;
    }

    public IConfigManager getConfigManager(Supplier<ItemStack> target) {
        return IConfigManager.builder(target).registerSetting(Settings.SORT_BY, SortOrder.NAME).registerSetting(Settings.VIEW_MODE, ViewItems.ALL).registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING).build();
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 2, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        this.setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades));
    }

    private static class LinkableHandler
    implements IGridLinkableHandler {
        private LinkableHandler() {
        }

        @Override
        public boolean canLink(ItemStack stack) {
            return stack.getItem() instanceof WirelessTerminalItem;
        }

        @Override
        public void link(ItemStack itemStack, GlobalPos pos) {
            itemStack.set(AEComponents.WIRELESS_LINK_TARGET, (Object)pos);
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.remove(AEComponents.WIRELESS_LINK_TARGET);
        }
    }
}

