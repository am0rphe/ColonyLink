/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.helpers.MultiCraftingTracker;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;
import appeng.parts.automation.StackTransferContextImpl;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.prioritylist.DefaultPriorityList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExportBusPart
extends IOBusPart
implements ICraftingRequester {
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("part/export_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/export_bus_has_channel"));
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, this.getConfig().size());
    private int nextSlot = 0;
    @Nullable
    private StackExportStrategy exportStrategy;

    public ExportBusPart(IPartItem<?> partItem) {
        super(TickRates.ExportBus, StackWorldBehaviors.withExportStrategy(), partItem);
        this.getMainNode().addService(ICraftingRequester.class, this);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        builder.registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.craftingTracker.readFromNBT(extra);
        this.nextSlot = extra.getInt("nextSlot");
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.craftingTracker.writeToNBT(extra);
        extra.putInt("nextSlot", this.nextSlot);
    }

    protected final StackExportStrategy getExportStrategy() {
        if (this.exportStrategy == null) {
            BlockEntity self = this.getHost().getBlockEntity();
            BlockPos fromPos = self.getBlockPos().relative(this.getSide());
            Direction fromSide = this.getSide().getOpposite();
            this.exportStrategy = StackWorldBehaviors.createExportFacade((ServerLevel)this.getLevel(), fromPos, fromSide);
        }
        return this.exportStrategy;
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        IStorageService storageService = grid.getStorageService();
        ICraftingService cg = grid.getCraftingService();
        FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
        SchedulingMode schedulingMode = this.getConfigManager().getSetting(Settings.SCHEDULING_MODE);
        StackTransferContext context = this.createTransferContext(storageService, grid.getEnergyService());
        int x = 0;
        for (x = 0; x < this.availableSlots() && context.hasOperationsLeft(); ++x) {
            int slotToExport = this.getStartingSlot(schedulingMode, x);
            AEKey what = this.getConfig().getKey(slotToExport);
            if (what == null) continue;
            if (this.craftOnly()) {
                this.attemptCrafting(context, cg, slotToExport, what);
                continue;
            }
            int before = context.getOperationsRemaining();
            if (this.isUpgradedWith(AEItems.FUZZY_CARD)) {
                for (Object2LongMap.Entry fuzzyWhat : ImmutableList.copyOf(storageService.getCachedInventory().findFuzzy(what, fzMode))) {
                    int transferFactory = ((AEKey)fuzzyWhat.getKey()).getAmountPerOperation();
                    long amount = (long)context.getOperationsRemaining() * (long)transferFactory;
                    amount = this.getExportStrategy().transfer(context, (AEKey)fuzzyWhat.getKey(), amount);
                    context.reduceOperationsRemaining(Math.max(1L, amount / (long)transferFactory));
                    if (context.hasOperationsLeft()) continue;
                    break;
                }
            } else {
                int transferFactor = what.getAmountPerOperation();
                long amount = (long)context.getOperationsRemaining() * (long)transferFactor;
                amount = this.getExportStrategy().transfer(context, what, amount);
                if (amount > 0L) {
                    context.reduceOperationsRemaining(Math.max(1L, amount / (long)transferFactor));
                }
            }
            if (before != context.getOperationsRemaining() || !this.isCraftingEnabled()) continue;
            this.attemptCrafting(context, cg, slotToExport, what);
        }
        if (context.hasDoneWork()) {
            this.updateSchedulingMode(schedulingMode, x);
        }
        return context.hasDoneWork();
    }

    private void attemptCrafting(StackTransferContext context, ICraftingService cg, int slotToExport, AEKey what) {
        int maxAmount = context.getOperationsRemaining() * what.getAmountPerOperation();
        long amount = this.getExportStrategy().push(what, maxAmount, Actionable.SIMULATE);
        if (amount > 0L) {
            this.requestCrafting(cg, slotToExport, what, amount);
            context.reduceOperationsRemaining(Math.max(1L, amount / (long)what.getAmountPerOperation()));
        }
    }

    protected final boolean requestCrafting(ICraftingService cg, int configSlot, AEKey what, long amount) {
        return this.craftingTracker.handleCrafting(configSlot, what, amount, this.getBlockEntity().getLevel(), cg, this.source);
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null && this.getMainNode().isActive()) {
            return this.getExportStrategy().push(what, amount, mode);
        }
        return 0L;
    }

    @NotNull
    private StackTransferContext createTransferContext(IStorageService storageService, IEnergyService energyService) {
        return new StackTransferContextImpl(storageService, energyService, this.source, this.getOperationsPerTick(), DefaultPriorityList.INSTANCE);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    protected int getStartingSlot(SchedulingMode schedulingMode, int x) {
        if (schedulingMode == SchedulingMode.RANDOM) {
            return this.getLevel().getRandom().nextInt(this.availableSlots());
        }
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            return (this.nextSlot + x) % this.availableSlots();
        }
        return x;
    }

    protected void updateSchedulingMode(SchedulingMode schedulingMode, int x) {
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            this.nextSlot = (this.nextSlot + x) % this.availableSlots();
        }
    }

    private boolean craftOnly() {
        return this.isCraftingEnabled() && this.getConfigManager().getSetting(Settings.CRAFT_ONLY) == YesNo.YES;
    }

    private boolean isCraftingEnabled() {
        return this.isUpgradedWith(AEItems.CRAFTING_CARD);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.EXPORT_TYPE;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(4.0, 4.0, 12.0, 12.0, 12.0, 14.0);
        bch.addBox(5.0, 5.0, 14.0, 11.0, 11.0, 15.0);
        bch.addBox(6.0, 6.0, 15.0, 10.0, 10.0, 16.0);
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 12.0);
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
}

