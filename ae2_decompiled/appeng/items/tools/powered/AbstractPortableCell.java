/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.DyedItemColor
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools.powered;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.items.contents.PortableCellMenuHost;
import appeng.items.tools.powered.PoweredContainerItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.util.InteractionUtil;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPortableCell
extends PoweredContainerItem
implements ICellWorkbenchItem,
IMenuItem {
    private final MenuType<?> menuType;
    private final int defaultColor;

    public AbstractPortableCell(MenuType<?> menuType, Item.Properties props, int defaultColor) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.menuType = menuType;
        this.defaultColor = defaultColor;
    }

    public abstract ResourceLocation getRecipeId();

    @Override
    public abstract double getChargeRate(ItemStack var1);

    public boolean openFromInventory(Player player, ItemMenuHostLocator locator) {
        return this.openFromInventory(player, locator, false);
    }

    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubmenu) {
        ItemStack is = locator.locateItem(player);
        if (is.getItem() == this) {
            return MenuOpener.open(this.menuType, player, locator, returningFromSubmenu);
        }
        return false;
    }

    @Nullable
    public PortableCellMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new PortableCellMenuHost<AbstractPortableCell>(this, player, locator, (p, sm) -> this.openFromInventory((Player)p, locator, true));
    }

    public int getColor(ItemStack stack) {
        return DyedItemColor.getOrDefault((ItemStack)stack, (int)this.defaultColor);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return context.isSecondaryUseActive() && this.disassembleDrive(stack, context.getLevel(), context.getPlayer()) ? InteractionResult.sidedSuccess((boolean)context.getLevel().isClientSide()) : InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(InteractionUtil.isInAlternateUseMode(player) && this.disassembleDrive(player.getItemInHand(hand), level, player) || level.isClientSide())) {
            MenuOpener.open(this.menuType, player, MenuLocators.forHand(player, hand));
        }
        return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)player.getItemInHand(hand));
    }

    private boolean disassembleDrive(ItemStack stack, Level level, Player player) {
        Inventory playerInventory = player.getInventory();
        List<ItemStack> disassemblyItems = StorageCellDisassemblyRecipe.getDisassemblyResult(level, stack.getItem());
        if (disassemblyItems.isEmpty() || playerInventory.getSelected() != stack || stack.getCount() != 1) {
            return false;
        }
        if (level.isClientSide()) {
            return true;
        }
        StorageCell inv = StorageCells.getCellInventory(stack, null);
        if (inv != null && !inv.getAvailableStacks().isEmpty()) {
            player.displayClientMessage((Component)PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text(), true);
            return true;
        }
        playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);
        double remainingEnergy = this.getAECurrentPower(stack);
        for (ItemStack recipeStack : disassemblyItems) {
            Item item;
            ItemStack droppedStack = recipeStack.copy();
            if (remainingEnergy > 0.0 && (item = droppedStack.getItem()) instanceof EnergyCellBlockItem) {
                EnergyCellBlockItem energyCell = (EnergyCellBlockItem)item;
                remainingEnergy = energyCell.injectAEPower(droppedStack, remainingEnergy, Actionable.MODULATE);
            }
            playerInventory.placeItemBackInInventory(droppedStack);
        }
        this.getUpgrades(stack).forEach(arg_0 -> ((Inventory)playerInventory).placeItemBackInInventory(arg_0));
        return true;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    public void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        this.setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades) * 8);
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        Item item;
        if (tintIndex == 1 && (item = stack.getItem()) instanceof AbstractPortableCell) {
            AbstractPortableCell portableCell = (AbstractPortableCell)item;
            if (portableCell.getAECurrentPower(stack) <= 0.0) {
                return CellState.ABSENT.getStateColor();
            }
            StorageCell cellInv = StorageCells.getCellInventory(stack, null);
            CellState cellStatus = cellInv != null ? cellInv.getStatus() : CellState.EMPTY;
            return cellStatus.getStateColor();
        }
        if (tintIndex == 2 && (item = stack.getItem()) instanceof AbstractPortableCell) {
            AbstractPortableCell portableCell = (AbstractPortableCell)item;
            return portableCell.getColor(stack);
        }
        return 0xFFFFFF;
    }
}

