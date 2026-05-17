/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.items.tools.powered;

import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.core.AppEng;
import appeng.items.contents.CellConfig;
import appeng.items.storage.StorageTier;
import appeng.items.tools.powered.AbstractPortableCell;
import appeng.util.ConfigInventory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PortableCellItem
extends AbstractPortableCell
implements IBasicCellItem {
    private final StorageTier tier;
    private final AEKeyType keyType;
    private final int totalTypes;

    public PortableCellItem(AEKeyType keyType, int totalTypes, MenuType<?> menuType, StorageTier tier, Item.Properties props, int defaultColor) {
        super(menuType, props, defaultColor);
        this.tier = tier;
        this.keyType = keyType;
        this.totalTypes = totalTypes;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80.0 + 80.0 * (double)Upgrades.getEnergyCardMultiplier(this.getUpgrades(stack));
    }

    @Override
    public ResourceLocation getRecipeId() {
        return AppEng.makeId("tools/" + Objects.requireNonNull(this.getRegistryName()).getPath());
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        this.addCellInformationToTooltip(stack, lines);
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return this.getCellTooltipImage(stack);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return this.tier.bytes() / 2;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.tier.bytes() / 128;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return this.totalTypes;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, this.keyType == AEKeyType.items() ? 4 : 3, (x$0, x$1) -> super.onUpgradesChanged(x$0, x$1));
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(this.keyType), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return (FuzzyMode)((Object)is.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)FuzzyMode.IGNORE_ALL));
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.set(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)fzMode);
    }

    @Override
    public AEKeyType getKeyType() {
        return this.keyType;
    }

    public StorageTier getTier() {
        return this.tier;
    }
}

