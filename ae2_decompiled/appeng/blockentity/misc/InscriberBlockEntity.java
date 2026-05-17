/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.api.config.Actionable;
import appeng.api.config.InscriberInputCapacity;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ItemTransfer;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InscriberBlockEntity
extends AENetworkedPoweredBlockEntity
implements IGridTickable,
IUpgradeableObject,
IConfigurableObject {
    private static final int MAX_PROCESSING_STEPS = 200;
    private final IUpgradeInventory upgrades;
    private final IConfigManager configManager;
    private int processingTime = 0;
    private boolean smash;
    private boolean repeatSmash;
    private int finalStep;
    private long clientStart;
    private final IAEItemFilter baseFilter = new BaseFilter();
    private final AppEngInternalInventory topItemHandler = new AppEngInternalInventory(this, 1, 64, this.baseFilter);
    private final AppEngInternalInventory bottomItemHandler = new AppEngInternalInventory(this, 1, 64, this.baseFilter);
    private final AppEngInternalInventory sideItemHandler = new AppEngInternalInventory(this, 2, 64, this.baseFilter);
    private final InternalInventory inv = new CombinedInternalInventory(this.topItemHandler, this.bottomItemHandler, this.sideItemHandler);
    private final Map<InternalInventory, ItemStack> lastStacks = new IdentityHashMap<AppEngInternalInventory, ItemStack>(Map.of(this.topItemHandler, ItemStack.EMPTY, this.bottomItemHandler, ItemStack.EMPTY, this.sideItemHandler, ItemStack.EMPTY));
    private final InternalInventory topItemHandlerExtern;
    private final InternalInventory bottomItemHandlerExtern;
    private final InternalInventory sideItemHandlerExtern;
    private final InternalInventory combinedItemHandlerExtern;
    private InscriberRecipe cachedTask = null;

    public InscriberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0.0).addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600.0);
        this.upgrades = UpgradeInventories.forMachine(AEBlocks.INSCRIBER, 4, this::saveChanges);
        this.configManager = IConfigManager.builder(this::onConfigChanged).registerSetting(Settings.INSCRIBER_SEPARATE_SIDES, YesNo.NO).registerSetting(Settings.AUTO_EXPORT, YesNo.NO).registerSetting(Settings.INSCRIBER_INPUT_CAPACITY, InscriberInputCapacity.SIXTY_FOUR).build();
        AutomationFilter automationFilter = new AutomationFilter();
        this.topItemHandlerExtern = new FilteredInternalInventory(this.topItemHandler, automationFilter);
        this.bottomItemHandlerExtern = new FilteredInternalInventory(this.bottomItemHandler, automationFilter);
        this.sideItemHandlerExtern = new FilteredInternalInventory(this.sideItemHandler, automationFilter);
        this.combinedItemHandlerExtern = new CombinedInternalInventory(this.topItemHandlerExtern, this.bottomItemHandlerExtern, this.sideItemHandlerExtern);
        this.setPowerSides(this.getGridConnectableSides(this.getOrientation()));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        this.configManager.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.configManager.readFromNBT(data, registries);
        if ("NO".equals(data.getString("inscriber_buffer_size"))) {
            this.configManager.putSetting(Settings.INSCRIBER_INPUT_CAPACITY, InscriberInputCapacity.FOUR);
        }
        this.lastStacks.put(this.topItemHandler, this.topItemHandler.getStackInSlot(0));
        this.lastStacks.put(this.bottomItemHandler, this.bottomItemHandler.getStackInSlot(0));
        this.lastStacks.put(this.sideItemHandler, this.sideItemHandler.getStackInSlot(0));
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean newSmash;
        boolean c = super.readFromStream(data);
        boolean oldSmash = this.isSmash();
        if (oldSmash != (newSmash = data.readBoolean()) && newSmash) {
            this.setSmash(true);
        }
        for (int i = 0; i < this.inv.size(); ++i) {
            this.inv.setItemDirect(i, (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode((Object)data));
        }
        this.cachedTask = null;
        return c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isSmash());
        for (int i = 0; i < this.inv.size(); ++i) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode((Object)data, (Object)this.inv.getStackInSlot(i));
        }
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        data.putBoolean("smash", this.isSmash());
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.setSmash(data.getBoolean("smash"));
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)));
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        this.setPowerSides(this.getGridConnectableSides(orientation));
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack upgrade : this.upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (slot == 0) {
            boolean sameItemSameTags = ItemStack.isSameItemSameComponents((ItemStack)inv.getStackInSlot(0), (ItemStack)this.lastStacks.get(inv));
            this.lastStacks.put(inv, inv.getStackInSlot(0).copy());
            if (sameItemSameTags) {
                return;
            }
            this.setProcessingTime(0);
            this.cachedTask = null;
        }
        if (!this.isSmash()) {
            this.markForUpdate();
        }
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Inscriber, !this.hasAutoExportWork() && !this.hasCraftWork());
    }

    private boolean hasAutoExportWork() {
        return !this.sideItemHandler.getStackInSlot(1).isEmpty() && this.configManager.getSetting(Settings.AUTO_EXPORT) == YesNo.YES;
    }

    private boolean hasCraftWork() {
        InscriberRecipe task = this.getTask();
        if (task != null) {
            return this.sideItemHandler.insertItem(1, task.getResultItem().copy(), true).isEmpty();
        }
        this.setProcessingTime(0);
        return this.isSmash();
    }

    @Nullable
    public InscriberRecipe getTask() {
        if (this.cachedTask == null && this.level != null) {
            ItemStack input = this.sideItemHandler.getStackInSlot(0);
            ItemStack plateA = this.topItemHandler.getStackInSlot(0);
            ItemStack plateB = this.bottomItemHandler.getStackInSlot(0);
            if (input.isEmpty()) {
                return null;
            }
            this.cachedTask = InscriberRecipes.findRecipe(this.level, input, plateA, plateB, true);
        }
        return this.cachedTask;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.isSmash()) {
            ++this.finalStep;
            if (this.finalStep == 8) {
                ItemStack outputCopy;
                InscriberRecipe out = this.getTask();
                if (out != null && this.sideItemHandler.insertItem(1, outputCopy = out.getResultItem().copy(), false).isEmpty()) {
                    this.setProcessingTime(0);
                    if (out.getProcessType() == InscriberProcessType.PRESS) {
                        this.topItemHandler.extractItem(0, 1, false);
                        this.bottomItemHandler.extractItem(0, 1, false);
                    }
                    this.sideItemHandler.extractItem(0, 1, false);
                }
                this.saveChanges();
            } else if (this.finalStep == 16) {
                this.finalStep = 0;
                this.setSmash(false);
                this.markForUpdate();
            }
        } else if (this.hasCraftWork()) {
            this.getMainNode().ifPresent(grid -> {
                IEnergyService eg = grid.getEnergyService();
                IEnergySource src = this;
                int speedFactor = switch (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
                    default -> 2;
                    case 1 -> 3;
                    case 2 -> 5;
                    case 3 -> 10;
                    case 4 -> 50;
                };
                int powerConsumption = 10 * speedFactor;
                double powerThreshold = (double)powerConsumption - 0.01;
                double powerReq = this.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                if (powerReq <= powerThreshold) {
                    src = eg;
                    powerReq = eg.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                }
                if (powerReq > powerThreshold) {
                    src.extractAEPower(powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    this.setProcessingTime(this.getProcessingTime() + speedFactor);
                }
            });
            if (this.getProcessingTime() > this.getMaxProcessingTime()) {
                ItemStack outputCopy;
                this.setProcessingTime(this.getMaxProcessingTime());
                InscriberRecipe out = this.getTask();
                if (out != null && this.sideItemHandler.insertItem(1, outputCopy = out.getResultItem().copy(), true).isEmpty()) {
                    this.setSmash(true);
                    this.finalStep = 0;
                    this.markForUpdate();
                }
            }
        }
        if (this.pushOutResult()) {
            return TickRateModulation.URGENT;
        }
        return this.hasCraftWork() ? TickRateModulation.URGENT : (this.hasAutoExportWork() ? TickRateModulation.SLOWER : TickRateModulation.SLEEP);
    }

    private boolean pushOutResult() {
        if (!this.hasAutoExportWork()) {
            return false;
        }
        EnumSet<Direction> pushSides = EnumSet.allOf(Direction.class);
        if (this.isSeparateSides()) {
            pushSides.remove(this.getTop());
            pushSides.remove(this.getTop().getOpposite());
        }
        for (Direction dir : pushSides) {
            ItemTransfer target = InternalInventory.wrapExternal(this.level, this.getBlockPos().relative(dir), dir.getOpposite());
            if (target == null) continue;
            int startItems = this.sideItemHandler.getStackInSlot(1).getCount();
            this.sideItemHandler.insertItem(1, target.addItems(this.sideItemHandler.extractItem(1, 64, false)), false);
            int endItems = this.sideItemHandler.getStackInSlot(1).getCount();
            if (startItems == endItems) continue;
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        }
        if (id.equals((Object)ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    private boolean isSeparateSides() {
        return this.configManager.getSetting(Settings.INSCRIBER_SEPARATE_SIDES) == YesNo.YES;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction facing) {
        if (this.isSeparateSides()) {
            if (facing == this.getTop()) {
                return this.topItemHandlerExtern;
            }
            if (facing == this.getTop().getOpposite()) {
                return this.bottomItemHandlerExtern;
            }
            return this.sideItemHandlerExtern;
        }
        return this.combinedItemHandlerExtern;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    private void onConfigChanged(IConfigManager manager, Setting<?> setting) {
        if (setting == Settings.AUTO_EXPORT) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice((IGridNode)node));
        }
        if (setting == Settings.INSCRIBER_SEPARATE_SIDES) {
            this.invalidateCapabilities();
        }
        if (setting == Settings.INSCRIBER_INPUT_CAPACITY) {
            int capacity = this.configManager.getSetting(Settings.INSCRIBER_INPUT_CAPACITY).capacity;
            this.topItemHandler.setMaxStackSize(0, capacity);
            this.sideItemHandler.setMaxStackSize(0, capacity);
            this.bottomItemHandler.setMaxStackSize(0, capacity);
        }
        this.saveChanges();
    }

    public long getClientStart() {
        return this.clientStart;
    }

    private void setClientStart(long clientStart) {
        this.clientStart = clientStart;
    }

    public boolean isSmash() {
        return this.smash;
    }

    public void setSmash(boolean smash) {
        if (smash && !this.smash) {
            this.setClientStart(System.currentTimeMillis());
        }
        this.smash = smash;
    }

    public boolean isRepeatSmash() {
        return this.repeatSmash;
    }

    public void setRepeatSmash(boolean repeatSmash) {
        this.repeatSmash = repeatSmash;
    }

    public int getMaxProcessingTime() {
        return 200;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    private void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    @Nullable
    public ICrankable getCrankable(Direction direction) {
        if (direction != this.getFront()) {
            return new AENetworkedPoweredBlockEntity.Crankable(this);
        }
        return null;
    }

    public class BaseFilter
    implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (slot == 1) {
                return true;
            }
            if ((inv == InscriberBlockEntity.this.topItemHandler || inv == InscriberBlockEntity.this.bottomItemHandler) && AEItems.NAME_PRESS.is(stack)) {
                return true;
            }
            if (inv == InscriberBlockEntity.this.sideItemHandler && (AEItems.NAME_PRESS.is(InscriberBlockEntity.this.topItemHandler.getStackInSlot(0)) || AEItems.NAME_PRESS.is(InscriberBlockEntity.this.bottomItemHandler.getStackInSlot(0)))) {
                return true;
            }
            ItemStack bot = InscriberBlockEntity.this.bottomItemHandler.getStackInSlot(0);
            ItemStack middle = InscriberBlockEntity.this.sideItemHandler.getStackInSlot(0);
            ItemStack top = InscriberBlockEntity.this.topItemHandler.getStackInSlot(0);
            if (inv == InscriberBlockEntity.this.bottomItemHandler) {
                bot = stack;
            }
            if (inv == InscriberBlockEntity.this.sideItemHandler) {
                middle = stack;
            }
            if (inv == InscriberBlockEntity.this.topItemHandler) {
                top = stack;
            }
            for (RecipeHolder<InscriberRecipe> holder : InscriberRecipes.getRecipes(InscriberBlockEntity.this.level)) {
                InscriberRecipe recipe = (InscriberRecipe)holder.value();
                if (!middle.isEmpty() && !recipe.getMiddleInput().test(middle)) continue;
                if (bot.isEmpty() && top.isEmpty()) {
                    return true;
                }
                if (!(bot.isEmpty() ? recipe.getTopOptional().test(top) || recipe.getBottomOptional().test(top) : (top.isEmpty() ? recipe.getBottomOptional().test(bot) || recipe.getTopOptional().test(bot) : recipe.getTopOptional().test(top) && recipe.getBottomOptional().test(bot) || recipe.getBottomOptional().test(top) && recipe.getTopOptional().test(bot)))) continue;
                return true;
            }
            return false;
        }
    }

    public class AutomationFilter
    implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            if (slot == 1) {
                return true;
            }
            if (InscriberBlockEntity.this.isSmash()) {
                return false;
            }
            return InscriberBlockEntity.this.isSeparateSides() && (inv == InscriberBlockEntity.this.topItemHandler || inv == InscriberBlockEntity.this.bottomItemHandler);
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (slot == 1) {
                return false;
            }
            return !InscriberBlockEntity.this.isSmash();
        }
    }
}

