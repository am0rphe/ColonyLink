/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package appeng.menu.slot;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AETags;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.client.gui.Icon;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.menu.slot.AppEngSlot;
import appeng.util.Platform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class RestrictedInputSlot
extends AppEngSlot {
    private final PlacableItemType which;
    private boolean allowEdit = true;
    private int stackLimit = -1;

    public RestrictedInputSlot(PlacableItemType valid, InternalInventory inv, int invSlot) {
        super(inv, invSlot);
        this.which = valid;
        this.setIcon(valid.icon);
    }

    @Override
    public int getMaxStackSize() {
        if (this.stackLimit != -1) {
            return this.stackLimit;
        }
        return super.getMaxStackSize();
    }

    public Slot setStackLimit(int i) {
        this.stackLimit = i;
        return this;
    }

    private Level getLevel() {
        return this.getMenu().getPlayerInventory().player.getCommandSenderWorld();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!this.getMenu().isValidForSlot(this, stack)) {
            return false;
        }
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.AIR) {
            return false;
        }
        if (!super.mayPlace(stack)) {
            return false;
        }
        if (!this.isAllowEdit()) {
            return false;
        }
        switch (this.which.ordinal()) {
            case 6: {
                return PatternDetailsHelper.decodePattern(stack, this.getLevel()) instanceof IMolecularAssemblerSupportedPattern;
            }
            case 7: 
            case 8: {
                return PatternDetailsHelper.isEncodedPattern(stack);
            }
            case 5: {
                return AEItems.CRAFTING_PATTERN.is(stack) || AEItems.PROCESSING_PATTERN.is(stack) || AEItems.SMITHING_TABLE_PATTERN.is(stack) || AEItems.STONECUTTING_PATTERN.is(stack);
            }
            case 10: {
                return AEItems.BLANK_PATTERN.is(stack);
            }
            case 20: {
                if (AEItems.NAME_PRESS.is(stack)) {
                    return true;
                }
                return InscriberRecipes.isValidOptionalIngredient(this.getLevel(), stack);
            }
            case 21: {
                return true;
            }
            case 22: {
                return RestrictedInputSlot.isMetalIngot(stack);
            }
            case 19: {
                return AEItems.VIEW_CELL.is(stack);
            }
            case 16: {
                return VibrationChamberBlockEntity.hasBurnTime(stack);
            }
            case 11: {
                return Platform.isChargeable(stack);
            }
            case 13: {
                return QuantumBridgeBlockEntity.isValidEntangledSingularity(stack);
            }
            case 12: {
                return AEItems.WIRELESS_BOOSTER.is(stack);
            }
            case 14: {
                return stack.getItem() instanceof ISpatialStorageCell && ((ISpatialStorageCell)stack.getItem()).isSpatialStorage(stack);
            }
            case 0: {
                return StorageCells.isCellHandled(stack);
            }
            case 18: {
                return stack.getItem() instanceof ICellWorkbenchItem && ((ICellWorkbenchItem)stack.getItem()).isEditable(stack);
            }
            case 2: {
                return stack.getItem() instanceof IStorageComponent && ((IStorageComponent)stack.getItem()).isStorageComponent(stack);
            }
            case 4: {
                if (StorageCells.isCellHandled(stack)) {
                    return false;
                }
                return !(stack.getItem() instanceof IStorageComponent) || !((IStorageComponent)stack.getItem()).isStorageComponent(stack);
            }
            case 3: {
                IGridLinkableHandler handler = GridLinkables.get((ItemLike)stack.getItem());
                return handler != null && handler.canLink(stack);
            }
            case 17: {
                return Upgrades.isUpgradeCardItem(stack);
            }
        }
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.isAllowEdit();
    }

    @Override
    public ItemStack getDisplayStack() {
        EncodedPatternItem iep;
        ItemStack out;
        Item item;
        ItemStack is;
        if (this.isRemote() && (this.which == PlacableItemType.ENCODED_PATTERN || this.which == PlacableItemType.PROVIDER_PATTERN) && !(is = super.getDisplayStack()).isEmpty() && (item = is.getItem()) instanceof EncodedPatternItem && !(out = (iep = (EncodedPatternItem)item).getOutput(is)).isEmpty()) {
            return out;
        }
        return super.getDisplayStack();
    }

    public static boolean isMetalIngot(ItemStack i) {
        return i.getItem().builtInRegistryHolder().is(AETags.METAL_INGOTS);
    }

    private boolean isAllowEdit() {
        return this.allowEdit;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public static enum PlacableItemType {
        STORAGE_CELLS(Icon.BACKGROUND_STORAGE_CELL),
        ORE(Icon.BACKGROUND_ORE),
        STORAGE_COMPONENT(Icon.BACKGROUND_STORAGE_COMPONENT),
        GRID_LINKABLE_ITEM(Icon.BACKGROUND_WIRELESS_TERM),
        TRASH(Icon.BACKGROUND_TRASH),
        ENCODED_AE_PATTERN(Icon.BACKGROUND_ENCODED_PATTERN),
        MOLECULAR_ASSEMBLER_PATTERN(Icon.BACKGROUND_BLANK_PATTERN),
        PROVIDER_PATTERN(Icon.BACKGROUND_BLANK_PATTERN),
        ENCODED_PATTERN(Icon.BACKGROUND_ENCODED_PATTERN),
        PATTERN(Icon.BACKGROUND_BLANK_PATTERN),
        BLANK_PATTERN(Icon.BACKGROUND_BLANK_PATTERN),
        POWERED_TOOL(Icon.BACKGROUND_CHARGABLE),
        RANGE_BOOSTER(Icon.BACKGROUND_WIRELESS_BOOSTER),
        QE_SINGULARITY(Icon.BACKGROUND_SINGULARITY),
        SPATIAL_STORAGE_CELLS(Icon.BACKGROUND_SPATIAL_CELL),
        SPATIAL_STORAGE_CELLS_NO_SHADOW(Icon.BACKGROUND_SPATIAL_CELL_NO_SHADOW),
        FUEL(Icon.BACKGROUND_FUEL),
        UPGRADES(Icon.BACKGROUND_UPGRADE),
        WORKBENCH_CELL(Icon.BACKGROUND_STORAGE_CELL),
        VIEW_CELL(Icon.BACKGROUND_VIEW_CELL),
        INSCRIBER_PLATE(Icon.BACKGROUND_PLATE),
        INSCRIBER_INPUT(Icon.BACKGROUND_INGOT),
        METAL_INGOTS(Icon.BACKGROUND_INGOT);

        public final Icon icon;

        private PlacableItemType(Icon o) {
            this.icon = o;
        }
    }
}

