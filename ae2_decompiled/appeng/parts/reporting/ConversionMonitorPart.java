/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package appeng.parts.reporting;

import appeng.api.networking.energy.IEnergyService;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ISubMenuHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.helpers.PlayerSource;
import appeng.menu.ISubMenu;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractMonitorPart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.PlayerInternalInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ConversionMonitorPart
extends AbstractMonitorPart
implements ISubMenuHost {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppEng.makeId("part/conversion_monitor_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppEng.makeId("part/conversion_monitor_on");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_OFF = AppEng.makeId("part/conversion_monitor_locked_off");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_ON = AppEng.makeId("part/conversion_monitor_locked_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    public static final IPartModel MODELS_LOCKED_OFF = new PartModel(MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_LOCKED_ON = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_LOCKED_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_HAS_CHANNEL);

    public ConversionMonitorPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (this.isClientSide()) {
            return true;
        }
        if (!this.getMainNode().isActive()) {
            return false;
        }
        if (this.isLocked() && !InteractionUtil.isInAlternateUseMode(player)) {
            if (InteractionUtil.canWrenchRotate(heldItem) && (this.getDisplayed() == null || !AEItemKey.matches(this.getDisplayed(), heldItem))) {
                return super.onUseWithoutItem(player, pos);
            }
            if (!heldItem.isEmpty()) {
                this.insertItem(player, heldItem);
                return true;
            }
        } else if (this.getDisplayed() != null && AEItemKey.matches(this.getDisplayed(), heldItem)) {
            this.insertItem(player, heldItem);
            return true;
        }
        return super.onUseItemOn(heldItem, player, hand, pos);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (this.isLocked() && !InteractionUtil.isInAlternateUseMode(player)) {
            if (this.isClientSide()) {
                return true;
            }
            if (!this.getMainNode().isActive()) {
                return false;
            }
            this.insertAllItem(player);
            return true;
        }
        return super.onUseWithoutItem(player, pos);
    }

    @Override
    public boolean onClicked(Player player, Vec3 pos) {
        if (this.isClientSide()) {
            return true;
        }
        if (!this.getMainNode().isActive()) {
            return false;
        }
        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }
        AEKey aEKey = this.getDisplayed();
        if (aEKey instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)aEKey;
            this.extractItem(player, itemKey.getMaxStackSize());
        }
        return true;
    }

    @Override
    public boolean onShiftClicked(Player player, Vec3 pos) {
        if (this.isClientSide()) {
            return true;
        }
        if (!this.getMainNode().isActive()) {
            return false;
        }
        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }
        if (this.getDisplayed() != null) {
            this.extractItem(player, 1);
        }
        return true;
    }

    private void insertAllItem(Player player) {
        this.getMainNode().ifPresent(grid -> {
            IEnergyService energy = grid.getEnergyService();
            MEStorage cell = grid.getStorageService().getInventory();
            AEKey patt0$temp = this.getDisplayed();
            if (patt0$temp instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)patt0$temp;
                PlayerInternalInventory inv = new PlayerInternalInventory(player.getInventory());
                for (int x = 0; x < inv.size(); ++x) {
                    ItemStack canExtract;
                    ItemStack targetStack = inv.getStackInSlot(x);
                    if (!itemKey.matches(targetStack) || (canExtract = inv.extractItem(x, targetStack.getCount(), true)).isEmpty()) continue;
                    long inserted = StorageHelper.poweredInsert(energy, cell, itemKey, canExtract.getCount(), new PlayerSource(player, this));
                    inv.extractItem(x, (int)inserted, false);
                }
            }
        });
    }

    private void insertItem(Player player, ItemStack heldItem) {
        this.getMainNode().ifPresent(grid -> {
            IEnergyService energy = grid.getEnergyService();
            MEStorage cell = grid.getStorageService().getInventory();
            long inserted = StorageHelper.poweredInsert(energy, cell, AEItemKey.of(heldItem), heldItem.getCount(), new PlayerSource(player, this));
            heldItem.shrink((int)inserted);
        });
    }

    private void extractItem(Player player, int count) {
        AEKey aEKey = this.getDisplayed();
        if (!(aEKey instanceof AEItemKey)) {
            return;
        }
        AEItemKey itemKey = (AEItemKey)aEKey;
        if (!this.getMainNode().isActive()) {
            return;
        }
        if (this.getAmount() == 0L && this.canCraft()) {
            CraftAmountMenu.open((ServerPlayer)player, MenuLocators.forPart(this), itemKey, itemKey.getAmountPerUnit());
            return;
        }
        this.getMainNode().ifPresent(grid -> {
            MEStorage cell;
            IEnergyService energy = grid.getEnergyService();
            long retrieved = StorageHelper.poweredExtraction(energy, cell = grid.getStorageService().getInventory(), itemKey, count, new PlayerSource(player, this));
            if (retrieved != 0L) {
                ItemStack newItems = itemKey.toStack((int)retrieved);
                if (!player.getInventory().add(newItems)) {
                    player.drop(newItems, false);
                }
                if (player.containerMenu != null) {
                    player.containerMenu.broadcastChanges();
                }
            }
        });
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL, MODELS_LOCKED_OFF, MODELS_LOCKED_ON, MODELS_LOCKED_HAS_CHANNEL);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        player.closeContainer();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getPartItem());
    }
}

