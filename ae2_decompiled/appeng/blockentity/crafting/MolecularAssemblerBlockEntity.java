/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.inventory.TransientCraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingInput$Positioned
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.crafting;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ItemTransfer;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.core.network.clientbound.AssemblerAnimationPacket;
import appeng.crafting.CraftingEvent;
import appeng.menu.AutoCraftingMenu;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class MolecularAssemblerBlockEntity
extends AENetworkedInvBlockEntity
implements IUpgradeableObject,
IGridTickable,
ICraftingMachine,
IPowerChannelState {
    public static final ResourceLocation INV_MAIN = AppEng.makeId("molecular_assembler");
    private final CraftingContainer craftingInv;
    private final AppEngInternalInventory gridInv = new AppEngInternalInventory(this, 10, 1);
    private final AppEngInternalInventory patternInv = new AppEngInternalInventory(this, 1, 1);
    private final InternalInventory gridInvExt = new FilteredInternalInventory(this.gridInv, new CraftingGridFilter());
    private final InternalInventory internalInv = new CombinedInternalInventory(this.gridInv, this.patternInv);
    private final IUpgradeInventory upgrades;
    private boolean isPowered = false;
    private Direction pushDirection = null;
    private ItemStack myPattern = ItemStack.EMPTY;
    private IMolecularAssemblerSupportedPattern myPlan = null;
    private double progress = 0.0;
    private boolean isAwake = false;
    private boolean forcePlan = false;
    private boolean reboot = true;
    @OnlyIn(value=Dist.CLIENT)
    private AssemblerAnimationStatus animationStatus;

    public MolecularAssemblerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0.0).addService(IGridTickable.class, this);
        this.upgrades = UpgradeInventories.forMachine(AEBlocks.MOLECULAR_ASSEMBLER, this.getUpgradeSlots(), this::saveChanges);
        this.craftingInv = new TransientCraftingContainer((AbstractContainerMenu)new AutoCraftingMenu(), 3, 3);
    }

    private int getUpgradeSlots() {
        return 5;
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        Component name = this.hasCustomName() ? this.getCustomName() : AEBlocks.MOLECULAR_ASSEMBLER.asItem().getDescription();
        AEItemKey icon = AEItemKey.of(AEBlocks.MOLECULAR_ASSEMBLER);
        int accelerationCards = this.getInstalledUpgrades(AEItems.SPEED_CARD);
        List<Object> tooltip = accelerationCards == 0 ? List.of() : List.of(GuiText.CompatibleUpgrade.text(Tooltips.of(AEItems.SPEED_CARD.asItem().getDescription()), Tooltips.ofUnformattedNumber(accelerationCards)));
        return new PatternContainerGroup(icon, name, tooltip);
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] table, Direction where) {
        if (this.myPattern.isEmpty()) {
            boolean isEmpty;
            boolean bl = isEmpty = this.gridInv.isEmpty() && this.patternInv.isEmpty();
            if (isEmpty && patternDetails instanceof IMolecularAssemblerSupportedPattern) {
                IMolecularAssemblerSupportedPattern pattern = (IMolecularAssemblerSupportedPattern)patternDetails;
                this.forcePlan = true;
                this.myPlan = pattern;
                this.pushDirection = where;
                this.fillGrid(table, pattern);
                this.updateSleepiness();
                this.saveChanges();
                return true;
            }
        }
        return false;
    }

    private void fillGrid(KeyCounter[] table, IMolecularAssemblerSupportedPattern adapter) {
        adapter.fillCraftingGrid(table, this.gridInv::setItemDirect);
        for (KeyCounter list : table) {
            list.removeZeros();
            if (list.isEmpty()) continue;
            throw new RuntimeException("Could not fill grid with some items, including " + String.valueOf(list.iterator().next()));
        }
    }

    private void updateSleepiness() {
        boolean wasEnabled = this.isAwake;
        boolean bl = this.isAwake = this.myPlan != null && this.hasMats() || this.canPush();
        if (wasEnabled != this.isAwake) {
            this.getMainNode().ifPresent((grid, node) -> {
                if (this.isAwake) {
                    grid.getTickManager().wakeDevice((IGridNode)node);
                } else {
                    grid.getTickManager().sleepDevice((IGridNode)node);
                }
            });
        }
    }

    private boolean canPush() {
        return !this.gridInv.getStackInSlot(9).isEmpty();
    }

    private boolean hasMats() {
        if (this.myPlan == null) {
            return false;
        }
        for (int x = 0; x < this.craftingInv.getContainerSize(); ++x) {
            this.craftingInv.setItem(x, this.gridInv.getStackInSlot(x));
        }
        return !this.myPlan.assemble(this.craftingInv.asCraftInput(), this.getLevel()).isEmpty();
    }

    @Override
    public boolean acceptsPlans() {
        return this.patternInv.isEmpty();
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        boolean oldPower = this.isPowered;
        this.isPowered = data.readBoolean();
        return this.isPowered != oldPower || c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isPowered);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        if (this.forcePlan) {
            ItemStack pattern;
            ItemStack itemStack = pattern = this.myPlan != null ? this.myPlan.getDefinition().toStack() : this.myPattern;
            if (!pattern.isEmpty()) {
                data.put("myPlan", pattern.save(registries));
                data.putInt("pushDirection", this.pushDirection.ordinal());
            }
        }
        this.upgrades.writeToNBT(data, "upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        ItemStack pattern;
        super.loadTag(data, registries);
        this.forcePlan = false;
        this.myPattern = ItemStack.EMPTY;
        this.myPlan = null;
        if (data.contains("myPlan") && !(pattern = ItemStack.parseOptional((HolderLookup.Provider)registries, (CompoundTag)data.getCompound("myPlan"))).isEmpty()) {
            this.forcePlan = true;
            this.myPattern = pattern;
            this.pushDirection = Direction.values()[data.getInt("pushDirection")];
        }
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.recalculatePlan();
    }

    private void recalculatePlan() {
        this.reboot = true;
        if (this.forcePlan) {
            if (this.getLevel() != null && this.myPlan == null) {
                IPatternDetails iPatternDetails;
                if (!this.myPattern.isEmpty() && (iPatternDetails = PatternDetailsHelper.decodePattern(this.myPattern, this.getLevel())) instanceof IMolecularAssemblerSupportedPattern) {
                    IMolecularAssemblerSupportedPattern supportedPlan;
                    this.myPlan = supportedPlan = (IMolecularAssemblerSupportedPattern)iPatternDetails;
                }
                this.myPattern = ItemStack.EMPTY;
                if (this.myPlan == null) {
                    AELog.warn("Unable to restore auto-crafting pattern after load: %s", this.myPattern);
                    this.forcePlan = false;
                }
            }
            return;
        }
        ItemStack is = this.patternInv.getStackInSlot(0);
        boolean reset = true;
        if (!is.isEmpty()) {
            if (ItemStack.isSameItemSameComponents((ItemStack)is, (ItemStack)this.myPattern)) {
                reset = false;
            } else {
                IPatternDetails iPatternDetails = PatternDetailsHelper.decodePattern(is, this.getLevel());
                if (iPatternDetails instanceof IMolecularAssemblerSupportedPattern) {
                    IMolecularAssemblerSupportedPattern supportedPattern = (IMolecularAssemblerSupportedPattern)iPatternDetails;
                    reset = false;
                    this.progress = 0.0;
                    this.myPattern = is;
                    this.myPlan = supportedPattern;
                }
            }
        }
        if (reset) {
            this.progress = 0.0;
            this.forcePlan = false;
            this.myPlan = null;
            this.myPattern = ItemStack.EMPTY;
            this.pushDirection = null;
        }
        this.updateSleepiness();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        if (id.equals((Object)INV_MAIN)) {
            return this.internalInv;
        }
        return super.getSubInventory(id);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.internalInv;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction side) {
        return this.gridInvExt;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == this.gridInv || inv == this.patternInv) {
            this.recalculatePlan();
        }
    }

    public int getCraftingProgress() {
        return (int)this.progress;
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
    public TickingRequest getTickingRequest(IGridNode node) {
        this.recalculatePlan();
        this.updateSleepiness();
        return new TickingRequest(1, 1, !this.isAwake);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.gridInv.getStackInSlot(9).isEmpty()) {
            this.pushOut(this.gridInv.getStackInSlot(9));
            if (this.gridInv.getStackInSlot(9).isEmpty()) {
                this.saveChanges();
            }
            this.ejectHeldItems();
            this.updateSleepiness();
            this.progress = 0.0;
            return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
        }
        if (this.myPlan == null) {
            this.updateSleepiness();
            return TickRateModulation.SLEEP;
        }
        if (this.reboot) {
            ticksSinceLastCall = 1;
        }
        if (!this.isAwake) {
            return TickRateModulation.SLEEP;
        }
        this.reboot = false;
        int speed = 10;
        switch (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
            case 0: {
                speed = 10;
                this.progress += (double)this.userPower(ticksSinceLastCall, 10, 1.0);
                break;
            }
            case 1: {
                speed = 13;
                this.progress += (double)this.userPower(ticksSinceLastCall, 13, 1.3);
                break;
            }
            case 2: {
                speed = 17;
                this.progress += (double)this.userPower(ticksSinceLastCall, 17, 1.7);
                break;
            }
            case 3: {
                speed = 20;
                this.progress += (double)this.userPower(ticksSinceLastCall, 20, 2.0);
                break;
            }
            case 4: {
                speed = 25;
                this.progress += (double)this.userPower(ticksSinceLastCall, 25, 2.5);
                break;
            }
            case 5: {
                speed = 50;
                this.progress += (double)this.userPower(ticksSinceLastCall, 50, 5.0);
            }
        }
        if (this.progress >= 100.0) {
            for (int x = 0; x < this.craftingInv.getContainerSize(); ++x) {
                this.craftingInv.setItem(x, this.gridInv.getStackInSlot(x));
            }
            CraftingInput.Positioned positionedInput = this.craftingInv.asPositionedCraftInput();
            CraftingInput craftinginput = positionedInput.input();
            this.progress = 0.0;
            ItemStack output = this.myPlan.assemble(craftinginput, this.getLevel());
            if (!output.isEmpty()) {
                int idx;
                int x;
                int y;
                output.onCraftedBySystem(this.level);
                CraftingEvent.fireAutoCraftingEvent(this.getLevel(), this.myPlan, output, (Container)this.craftingInv);
                NonNullList<ItemStack> craftingRemainders = this.myPlan.getRemainingItems(craftinginput);
                this.pushOut(output.copy());
                int craftingInputLeft = positionedInput.left();
                int craftingInputTop = positionedInput.top();
                for (y = 0; y < this.craftingInv.getHeight(); ++y) {
                    for (x = 0; x < this.craftingInv.getWidth(); ++x) {
                        if (y >= craftingInputTop && x >= craftingInputLeft) continue;
                        idx = x + y * this.craftingInv.getWidth();
                        this.gridInv.setItemDirect(idx, ItemStack.EMPTY);
                    }
                }
                for (y = 0; y < craftinginput.height(); ++y) {
                    for (x = 0; x < craftinginput.width(); ++x) {
                        idx = x + craftingInputLeft + (y + craftingInputTop) * this.craftingInv.getWidth();
                        this.gridInv.setItemDirect(idx, (ItemStack)craftingRemainders.get(x + y * craftinginput.width()));
                    }
                }
                if (this.patternInv.isEmpty()) {
                    this.forcePlan = false;
                    this.myPlan = null;
                    this.pushDirection = null;
                }
                this.ejectHeldItems();
                AEItemKey item = AEItemKey.of(output);
                if (item != null) {
                    PacketDistributor.sendToPlayersNear((ServerLevel)node.getLevel(), null, (double)this.worldPosition.getX(), (double)this.worldPosition.getY(), (double)this.worldPosition.getZ(), (double)32.0, (CustomPacketPayload)new AssemblerAnimationPacket(this.worldPosition, (byte)speed, item), (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
                this.saveChanges();
                this.updateSleepiness();
                return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
            }
        }
        return TickRateModulation.FASTER;
    }

    private void ejectHeldItems() {
        if (this.gridInv.getStackInSlot(9).isEmpty()) {
            for (int x = 0; x < 9; ++x) {
                ItemStack is = this.gridInv.getStackInSlot(x);
                if (is.isEmpty() || this.myPlan != null && this.myPlan.isItemValid(x, AEItemKey.of(is), this.level)) continue;
                this.gridInv.setItemDirect(9, is);
                this.gridInv.setItemDirect(x, ItemStack.EMPTY);
                this.saveChanges();
                return;
            }
        }
    }

    private int userPower(int ticksPassed, int bonusValue, double acceleratorTax) {
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            return (int)(grid.getEnergyService().extractAEPower((double)(ticksPassed * bonusValue) * acceleratorTax, Actionable.MODULATE, PowerMultiplier.CONFIG) / acceleratorTax);
        }
        return 0;
    }

    private void pushOut(ItemStack output) {
        if (this.pushDirection == null) {
            for (Direction d : Direction.values()) {
                output = this.pushTo(output, d);
            }
        } else {
            output = this.pushTo(output, this.pushDirection);
        }
        if (output.isEmpty() && this.forcePlan) {
            this.forcePlan = false;
            this.recalculatePlan();
        }
        this.gridInv.setItemDirect(9, output);
    }

    private ItemStack pushTo(ItemStack output, Direction d) {
        int newSize;
        if (output.isEmpty()) {
            return output;
        }
        ItemTransfer adaptor = InternalInventory.wrapExternal(this.getLevel(), this.worldPosition.relative(d), d.getOpposite());
        if (adaptor == null) {
            return output;
        }
        int size = output.getCount();
        int n = newSize = (output = adaptor.addItems(output)).isEmpty() ? 0 : output.getCount();
        if (size != newSize) {
            this.saveChanges();
        }
        return output;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            boolean newState = false;
            IGrid grid = this.getMainNode().getGrid();
            if (grid != null) {
                boolean bl = newState = this.getMainNode().isPowered() && grid.getEnergyService().extractAEPower(1.0, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 1.0E-4;
            }
            if (newState != this.isPowered) {
                this.isPowered = newState;
                this.markForUpdate();
            }
        }
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.isPowered;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setAnimationStatus(@Nullable AssemblerAnimationStatus status) {
        this.animationStatus = status;
    }

    @OnlyIn(value=Dist.CLIENT)
    @Nullable
    public AssemblerAnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Nullable
    public IMolecularAssemblerSupportedPattern getCurrentPattern() {
        if (this.isClientSide()) {
            ItemStack patternItem = this.patternInv.getStackInSlot(0);
            IPatternDetails pattern = PatternDetailsHelper.decodePattern(patternItem, this.level);
            if (pattern instanceof IMolecularAssemblerSupportedPattern) {
                IMolecularAssemblerSupportedPattern supportedPattern = (IMolecularAssemblerSupportedPattern)pattern;
                return supportedPattern;
            }
            return null;
        }
        return this.myPlan;
    }

    private class CraftingGridFilter
    implements IAEItemFilter {
        private CraftingGridFilter() {
        }

        private boolean hasPattern() {
            return MolecularAssemblerBlockEntity.this.myPlan != null && !MolecularAssemblerBlockEntity.this.patternInv.isEmpty();
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return slot == 9;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (slot >= 9) {
                return false;
            }
            if (this.hasPattern()) {
                return MolecularAssemblerBlockEntity.this.myPlan.isItemValid(slot, AEItemKey.of(stack), MolecularAssemblerBlockEntity.this.getLevel());
            }
            return false;
        }
    }
}

