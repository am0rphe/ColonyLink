/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.automation.IOBusPart;
import appeng.parts.automation.StackTransferContextImpl;
import appeng.parts.automation.StackWorldBehaviors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class ImportBusPart
extends IOBusPart
implements KeyTypeSelectionHost {
    @Nullable
    private StackImportStrategy importStrategy;
    private final KeyTypeSelection keyTypeSelection = new KeyTypeSelection(() -> {
        this.getHost().markForSave();
        this.importStrategy = null;
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
    }, StackWorldBehaviors.hasImportStrategyTypeFilter());

    public ImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, StackWorldBehaviors.withImportStrategy(), partItem);
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (this.importStrategy == null) {
            BlockEntity self = this.getHost().getBlockEntity();
            BlockPos fromPos = self.getBlockPos().relative(this.getSide());
            Direction fromSide = this.getSide().getOpposite();
            this.importStrategy = StackWorldBehaviors.createImportFacade((ServerLevel)this.getLevel(), fromPos, fromSide, this.keyTypeSelection.enabledPredicate());
        }
        StackTransferContextImpl context = new StackTransferContextImpl(grid.getStorageService(), grid.getEnergyService(), this.source, this.getOperationsPerTick(), this.getFilter());
        context.setInverted(this.isUpgradedWith(AEItems.INVERTER_CARD));
        this.importStrategy.transfer(context);
        return context.hasDoneWork();
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.IMPORT_TYPE;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 13.0);
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0);
        bch.addBox(4.0, 4.0, 14.0, 12.0, 12.0, 16.0);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        if (this.isPowered()) {
            return MODELS_ON;
        }
        return MODELS_OFF;
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.keyTypeSelection.readFromNBT(extra, registries);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.keyTypeSelection.writeToNBT(extra);
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return this.keyTypeSelection;
    }
}

