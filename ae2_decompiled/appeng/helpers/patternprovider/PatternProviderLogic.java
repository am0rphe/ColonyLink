/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.Nameable
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.helpers.patternprovider;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.settings.TickRates;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.helpers.patternprovider.PatternProviderTargetCache;
import appeng.helpers.patternprovider.UnlockCraftingEvent;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.PlayerInternalInventory;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternProviderLogic
implements InternalInventoryHost,
ICraftingProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PatternProviderLogic.class);
    public static final String NBT_MEMORY_CARD_PATTERNS = "patterns";
    public static final String NBT_UNLOCK_EVENT = "unlockEvent";
    public static final String NBT_UNLOCK_STACK = "unlockStack";
    public static final String NBT_PRIORITY = "priority";
    public static final String NBT_SEND_LIST = "sendList";
    public static final String NBT_SEND_DIRECTION = "sendDirection";
    public static final String NBT_RETURN_INV = "returnInv";
    private final PatternProviderLogicHost host;
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final IConfigManager configManager;
    private int priority;
    private final AppEngInternalInventory patternInventory;
    private final List<IPatternDetails> patterns = new ArrayList<IPatternDetails>();
    private final Set<AEKey> patternInputs = new HashSet<AEKey>();
    private final List<GenericStack> sendList = new ArrayList<GenericStack>();
    private Direction sendDirection;
    private final PatternProviderReturnInventory returnInv;
    private final PatternProviderTargetCache[] targetCaches = new PatternProviderTargetCache[6];
    private YesNo redstoneState = YesNo.UNDECIDED;
    @Nullable
    private UnlockCraftingEvent unlockEvent;
    @Nullable
    private GenericStack unlockStack;
    private int roundRobinIndex = 0;

    public @Nullable PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host) {
        this(mainNode, host, 9);
    }

    public PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize) {
        this.patternInventory = new AppEngInternalInventory(this, patternInventorySize);
        this.host = host;
        this.mainNode = mainNode.setFlags(GridFlags.REQUIRE_CHANNEL).addService(IGridTickable.class, new Ticker()).addService(ICraftingProvider.class, this);
        this.actionSource = new MachineSource(mainNode::getNode);
        this.configManager = IConfigManager.builder(this::configChanged).registerSetting(Settings.BLOCKING_MODE, YesNo.NO).registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES).registerSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE).build();
        this.returnInv = new PatternProviderReturnInventory(() -> {
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
            this.host.saveChanges();
        });
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();
        ICraftingProvider.requestUpdate(this.mainNode);
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.configManager.writeToNBT(tag, registries);
        this.patternInventory.writeToNBT(tag, NBT_MEMORY_CARD_PATTERNS, registries);
        tag.putInt(NBT_PRIORITY, this.priority);
        if (this.unlockEvent == UnlockCraftingEvent.REDSTONE_POWER) {
            tag.putByte(NBT_UNLOCK_EVENT, (byte)1);
        } else if (this.unlockEvent == UnlockCraftingEvent.RESULT) {
            if (this.unlockStack != null) {
                tag.putByte(NBT_UNLOCK_EVENT, (byte)2);
                tag.put(NBT_UNLOCK_STACK, (Tag)GenericStack.writeTag(registries, this.unlockStack));
            } else {
                LOG.error("Saving pattern provider {}, locked waiting for stack, but stack is null!", (Object)this.host);
            }
        } else if (this.unlockEvent == UnlockCraftingEvent.REDSTONE_PULSE) {
            tag.putByte(NBT_UNLOCK_EVENT, (byte)3);
        }
        ListTag sendListTag = new ListTag();
        for (GenericStack toSend : this.sendList) {
            sendListTag.add((Object)GenericStack.writeTag(registries, toSend));
        }
        tag.put(NBT_SEND_LIST, (Tag)sendListTag);
        if (this.sendDirection != null) {
            tag.putByte(NBT_SEND_DIRECTION, (byte)this.sendDirection.get3DDataValue());
        }
        tag.put(NBT_RETURN_INV, (Tag)this.returnInv.writeToTag(registries));
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        this.configManager.readFromNBT(tag, registries);
        this.patternInventory.readFromNBT(tag, NBT_MEMORY_CARD_PATTERNS, registries);
        this.priority = tag.getInt(NBT_PRIORITY);
        byte unlockEventType = tag.getByte(NBT_UNLOCK_EVENT);
        switch (unlockEventType) {
            case 0: {
                UnlockCraftingEvent unlockCraftingEvent = null;
                break;
            }
            case 1: {
                UnlockCraftingEvent unlockCraftingEvent = UnlockCraftingEvent.REDSTONE_POWER;
                break;
            }
            case 2: {
                UnlockCraftingEvent unlockCraftingEvent = UnlockCraftingEvent.RESULT;
                break;
            }
            case 3: {
                UnlockCraftingEvent unlockCraftingEvent = UnlockCraftingEvent.REDSTONE_PULSE;
                break;
            }
            default: {
                LOG.error("Unknown unlock event type {} in NBT for pattern provider: {}", (Object)unlockEventType, (Object)tag);
                UnlockCraftingEvent unlockCraftingEvent = this.unlockEvent = null;
            }
        }
        if (this.unlockEvent == UnlockCraftingEvent.RESULT) {
            this.unlockStack = GenericStack.readTag(registries, tag.getCompound(NBT_UNLOCK_STACK));
            if (this.unlockStack == null) {
                LOG.error("Could not load unlock stack for pattern provider from NBT: {}", (Object)tag);
            }
        } else {
            this.unlockStack = null;
        }
        ListTag sendListTag = tag.getList(NBT_SEND_LIST, 10);
        for (int i = 0; i < sendListTag.size(); ++i) {
            GenericStack stack = GenericStack.readTag(registries, sendListTag.getCompound(i));
            if (stack == null) continue;
            this.addToSendList(stack.what(), stack.amount());
        }
        if (tag.contains(NBT_SEND_DIRECTION)) {
            this.sendDirection = Direction.from3DDataValue((int)tag.getByte(NBT_SEND_DIRECTION));
        }
        this.returnInv.readFromTag(tag.getList(NBT_RETURN_INV, 10), registries);
    }

    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.host.saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.saveChanges();
        this.updatePatterns();
    }

    @Override
    public boolean isClientSide() {
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    public void updatePatterns() {
        this.patterns.clear();
        this.patternInputs.clear();
        for (ItemStack stack : this.patternInventory) {
            IPatternDetails details = PatternDetailsHelper.decodePattern(stack, this.host.getBlockEntity().getLevel());
            if (details == null) continue;
            this.patterns.add(details);
            for (IPatternDetails.IInput iinput : details.getInputs()) {
                for (GenericStack inputCandidate : iinput.getPossibleInputs()) {
                    this.patternInputs.add(inputCandidate.what().dropSecondary());
                }
            }
        }
        ICraftingProvider.requestUpdate(this.mainNode);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    @Override
    public int getPatternPriority() {
        return this.priority;
    }

    private <T> void rearrangeRoundRobin(List<T> list) {
        if (list.isEmpty()) {
            return;
        }
        this.roundRobinIndex %= list.size();
        for (int i = 0; i < this.roundRobinIndex; ++i) {
            list.add(list.get(i));
        }
        list.subList(0, this.roundRobinIndex).clear();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!(this.sendList.isEmpty() && this.mainNode.isActive() && this.patterns.contains(patternDetails))) {
            return false;
        }
        BlockEntity be = this.host.getBlockEntity();
        Level level = be.getLevel();
        if (this.getCraftingLockedReason() != LockCraftingMode.NONE) {
            return false;
        }
        record PushTarget(Direction direction, PatternProviderTarget target) {
        }
        ArrayList<PushTarget> possibleTargets = new ArrayList<PushTarget>();
        for (Direction direction : this.getActiveSides()) {
            Direction adjBeSide;
            BlockPos adjPos = be.getBlockPos().relative(direction);
            ICraftingMachine craftingMachine = ICraftingMachine.of(level, adjPos, adjBeSide = direction.getOpposite());
            if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                if (!craftingMachine.pushPattern(patternDetails, inputHolder, adjBeSide)) continue;
                this.onPushPatternSuccess(patternDetails);
                return true;
            }
            PatternProviderTarget adapter = this.findAdapter(direction);
            if (adapter == null) continue;
            possibleTargets.add(new PushTarget(direction, adapter));
        }
        if (!patternDetails.supportsPushInputsToExternalInventory()) {
            return false;
        }
        this.rearrangeRoundRobin(possibleTargets);
        for (int i = 0; i < possibleTargets.size(); ++i) {
            PushTarget target = (PushTarget)possibleTargets.get(i);
            Direction direction = target.direction();
            PatternProviderTarget adapter = target.target();
            if (this.isBlocking() && adapter.containsPatternInput(this.patternInputs) || !this.adapterAcceptsAll(adapter, inputHolder)) continue;
            patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                long inserted = adapter.insert(what, amount, Actionable.MODULATE);
                if (inserted < amount) {
                    this.addToSendList(what, amount - inserted);
                }
            });
            this.onPushPatternSuccess(patternDetails);
            this.sendDirection = direction;
            this.sendStacksOut();
            this.roundRobinIndex += i + 1;
            return true;
        }
        return false;
    }

    public void resetCraftingLock() {
        if (this.unlockEvent != null) {
            this.unlockEvent = null;
            this.unlockStack = null;
            this.saveChanges();
        }
    }

    private void onPushPatternSuccess(IPatternDetails pattern) {
        this.resetCraftingLock();
        LockCraftingMode lockMode = this.configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        switch (lockMode) {
            case LOCK_UNTIL_PULSE: {
                this.unlockEvent = this.getRedstoneState() ? UnlockCraftingEvent.REDSTONE_PULSE : UnlockCraftingEvent.REDSTONE_POWER;
                this.redstoneState = YesNo.UNDECIDED;
                this.saveChanges();
                break;
            }
            case LOCK_UNTIL_RESULT: {
                this.unlockEvent = UnlockCraftingEvent.RESULT;
                this.unlockStack = pattern.getPrimaryOutput();
                this.saveChanges();
            }
        }
    }

    public LockCraftingMode getCraftingLockedReason() {
        LockCraftingMode lockMode = this.configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        if (lockMode == LockCraftingMode.LOCK_WHILE_LOW && !this.getRedstoneState()) {
            return LockCraftingMode.LOCK_WHILE_LOW;
        }
        if (lockMode == LockCraftingMode.LOCK_WHILE_HIGH && this.getRedstoneState()) {
            return LockCraftingMode.LOCK_WHILE_HIGH;
        }
        if (this.unlockEvent != null) {
            switch (this.unlockEvent) {
                case REDSTONE_POWER: 
                case REDSTONE_PULSE: {
                    return LockCraftingMode.LOCK_UNTIL_PULSE;
                }
                case RESULT: {
                    return LockCraftingMode.LOCK_UNTIL_RESULT;
                }
            }
        }
        return LockCraftingMode.NONE;
    }

    @Nullable
    public GenericStack getUnlockStack() {
        return this.unlockStack;
    }

    private Set<Direction> getActiveSides() {
        EnumSet<Direction> sides = this.host.getTargets();
        IGridNode node = this.mainNode.getNode();
        if (node != null) {
            for (Map.Entry<Direction, IGridConnection> entry : node.getInWorldConnections().entrySet()) {
                IGridNode otherNode = entry.getValue().getOtherSide(node);
                if (!(otherNode.getOwner() instanceof PatternProviderLogicHost) && (!(otherNode.getOwner() instanceof InterfaceLogicHost) || !otherNode.getGrid().equals(this.mainNode.getGrid()))) continue;
                sides.remove(entry.getKey());
            }
        }
        return sides;
    }

    public boolean isBlocking() {
        return this.configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES;
    }

    @Nullable
    private PatternProviderTarget findAdapter(Direction side) {
        if (this.targetCaches[side.get3DDataValue()] == null) {
            BlockEntity thisBe = this.host.getBlockEntity();
            this.targetCaches[side.get3DDataValue()] = new PatternProviderTargetCache((ServerLevel)thisBe.getLevel(), thisBe.getBlockPos().relative(side), side.getOpposite(), this.actionSource);
        }
        return this.targetCaches[side.get3DDataValue()].find();
    }

    private boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
        for (KeyCounter inputList : inputHolder) {
            for (Object2LongMap.Entry<AEKey> input : inputList) {
                long inserted = target.insert((AEKey)input.getKey(), input.getLongValue(), Actionable.SIMULATE);
                if (inserted != 0L) continue;
                return false;
            }
        }
        return true;
    }

    private void addToSendList(AEKey what, long amount) {
        if (amount > 0L) {
            this.sendList.add(new GenericStack(what, amount));
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
        }
    }

    private boolean sendStacksOut() {
        if (this.sendDirection == null) {
            if (!this.sendList.isEmpty()) {
                throw new IllegalStateException("Invalid pattern provider state, this is a bug.");
            }
            return false;
        }
        PatternProviderTarget adapter = this.findAdapter(this.sendDirection);
        if (adapter == null) {
            return false;
        }
        boolean didSomething = false;
        ListIterator<GenericStack> it = this.sendList.listIterator();
        while (it.hasNext()) {
            long amount;
            GenericStack stack = it.next();
            AEKey what = stack.what();
            long inserted = adapter.insert(what, amount = stack.amount(), Actionable.MODULATE);
            if (inserted >= amount) {
                it.remove();
                didSomething = true;
                continue;
            }
            if (inserted <= 0L) continue;
            it.set(new GenericStack(what, amount - inserted));
            didSomething = true;
        }
        if (this.sendList.isEmpty()) {
            this.sendDirection = null;
        }
        return didSomething;
    }

    @Override
    public boolean isBusy() {
        return !this.sendList.isEmpty();
    }

    private boolean hasWorkToDo() {
        return !this.sendList.isEmpty() || !this.returnInv.isEmpty();
    }

    private boolean doWork() {
        return this.returnInv.injectIntoNetwork(this.mainNode.getGrid().getStorageService().getInventory(), this.actionSource, this::onStackReturnedToNetwork) | this.sendStacksOut();
    }

    public InternalInventory getPatternInv() {
        return this.patternInventory;
    }

    public void onMainNodeStateChanged() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice((IGridNode)node));
        }
    }

    public void addDrops(List<ItemStack> drops) {
        for (ItemStack itemStack : this.patternInventory) {
            drops.add(itemStack);
        }
        for (GenericStack genericStack : this.sendList) {
            genericStack.what().addDrops(genericStack.amount(), drops, this.host.getBlockEntity().getLevel(), this.host.getBlockEntity().getBlockPos());
        }
        this.returnInv.addDrops(drops, this.host.getBlockEntity().getLevel(), this.host.getBlockEntity().getBlockPos());
    }

    public void clearContent() {
        this.patternInventory.clear();
        this.sendList.clear();
        this.returnInv.clear();
    }

    public PatternProviderReturnInventory getReturnInv() {
        return this.returnInv;
    }

    public void exportSettings(DataComponentMap.Builder builder) {
        builder.set(AEComponents.EXPORTED_PATTERNS, (Object)this.patternInventory.toItemContainerContents());
    }

    public void importSettings(DataComponentMap input, @Nullable Player player) {
        ItemContainerContents patterns = (ItemContainerContents)input.getOrDefault(AEComponents.EXPORTED_PATTERNS, (Object)ItemContainerContents.EMPTY);
        if (player != null && !player.level().isClientSide) {
            this.clearPatternInventory(player);
            AppEngInternalInventory desiredPatterns = new AppEngInternalInventory(this.patternInventory.size());
            desiredPatterns.fromItemContainerContents(patterns);
            Inventory playerInv = player.getInventory();
            int blankPatternsAvailable = player.getAbilities().instabuild ? Integer.MAX_VALUE : playerInv.countItem((Item)AEItems.BLANK_PATTERN.asItem());
            int blankPatternsUsed = 0;
            for (int i = 0; i < desiredPatterns.size(); ++i) {
                IPatternDetails pattern;
                if (desiredPatterns.getStackInSlot(i).isEmpty() || (pattern = PatternDetailsHelper.decodePattern(desiredPatterns.getStackInSlot(i), this.host.getBlockEntity().getLevel())) == null || blankPatternsAvailable < ++blankPatternsUsed || this.patternInventory.addItems(pattern.getDefinition().toStack()).isEmpty()) continue;
                AELog.warn("Failed to add pattern to pattern provider", new Object[0]);
                --blankPatternsUsed;
            }
            if (blankPatternsUsed > 0 && !player.getAbilities().instabuild) {
                new PlayerInternalInventory(playerInv).removeItems(blankPatternsUsed, AEItems.BLANK_PATTERN.stack(), null);
            }
            if (blankPatternsUsed > blankPatternsAvailable) {
                player.sendSystemMessage((Component)PlayerMessages.MissingBlankPatterns.text(blankPatternsUsed - blankPatternsAvailable));
            }
        }
    }

    private void clearPatternInventory(Player player) {
        if (player.getAbilities().instabuild) {
            for (int i = 0; i < this.patternInventory.size(); ++i) {
                this.patternInventory.setItemDirect(i, ItemStack.EMPTY);
            }
            return;
        }
        Inventory playerInv = player.getInventory();
        int blankPatternCount = 0;
        for (int i = 0; i < this.patternInventory.size(); ++i) {
            ItemStack pattern = this.patternInventory.getStackInSlot(i);
            if (pattern.is(AEItems.CRAFTING_PATTERN.asItem()) || pattern.is(AEItems.PROCESSING_PATTERN.asItem()) || pattern.is(AEItems.SMITHING_TABLE_PATTERN.asItem()) || pattern.is(AEItems.STONECUTTING_PATTERN.asItem()) || pattern.is((Item)AEItems.BLANK_PATTERN.asItem())) {
                blankPatternCount += pattern.getCount();
            } else {
                playerInv.placeItemBackInInventory(pattern);
            }
            this.patternInventory.setItemDirect(i, ItemStack.EMPTY);
        }
        if (blankPatternCount > 0) {
            playerInv.placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(blankPatternCount), false);
        }
    }

    private void onStackReturnedToNetwork(GenericStack genericStack) {
        if (this.unlockEvent != UnlockCraftingEvent.RESULT) {
            return;
        }
        if (this.unlockStack == null) {
            LOG.error("pattern provider was waiting for RESULT, but no result was set");
            this.unlockEvent = null;
        } else if (this.unlockStack.what().equals(genericStack.what())) {
            long remainingAmount = this.unlockStack.amount() - genericStack.amount();
            if (remainingAmount <= 0L) {
                this.unlockEvent = null;
                this.unlockStack = null;
            } else {
                this.unlockStack = new GenericStack(this.unlockStack.what(), remainingAmount);
            }
        }
    }

    public PatternContainerGroup getTerminalGroup() {
        Nameable nameable;
        BlockEntity host = this.host.getBlockEntity();
        Level hostLevel = host.getLevel();
        PatternProviderLogicHost patternProviderLogicHost = this.host;
        if (patternProviderLogicHost instanceof Nameable && (nameable = (Nameable)patternProviderLogicHost).hasCustomName()) {
            Component name = nameable.getCustomName();
            return new PatternContainerGroup(this.host.getTerminalIcon(), name, List.of());
        }
        Set<Direction> sides = this.getActiveSides();
        LinkedHashSet<PatternContainerGroup> groups = new LinkedHashSet<PatternContainerGroup>(sides.size());
        for (Direction direction : sides) {
            BlockPos sidePos = host.getBlockPos().relative(direction);
            PatternContainerGroup group = PatternContainerGroup.fromMachine(hostLevel, sidePos, direction.getOpposite());
            if (group == null) continue;
            groups.add(group);
        }
        if (groups.size() == 1) {
            return (PatternContainerGroup)groups.iterator().next();
        }
        List<Component> tooltip = List.of();
        if (groups.size() > 1) {
            tooltip = new ArrayList();
            tooltip.add((Component)GuiText.AdjacentToDifferentMachines.text().withStyle(ChatFormatting.BOLD));
            for (PatternContainerGroup group : groups) {
                tooltip.add(group.name());
                for (Component line : group.tooltip()) {
                    tooltip.add((Component)Component.literal((String)"  ").append(line));
                }
            }
        }
        AEItemKey aEItemKey = this.host.getTerminalIcon();
        return new PatternContainerGroup(aEItemKey, aEItemKey.getDisplayName(), tooltip);
    }

    public long getSortValue() {
        BlockEntity te = this.host.getBlockEntity();
        return te.getBlockPos().getZ() << 24 ^ te.getBlockPos().getX() << 8 ^ te.getBlockPos().getY();
    }

    @Nullable
    public IGrid getGrid() {
        return this.mainNode.getGrid();
    }

    public void updateRedstoneState() {
        if (this.unlockEvent == UnlockCraftingEvent.REDSTONE_POWER && this.getRedstoneState()) {
            this.unlockEvent = null;
            this.saveChanges();
        } else if (this.unlockEvent == UnlockCraftingEvent.REDSTONE_PULSE && !this.getRedstoneState()) {
            this.unlockEvent = UnlockCraftingEvent.REDSTONE_POWER;
            this.redstoneState = YesNo.UNDECIDED;
            this.saveChanges();
        } else {
            this.redstoneState = YesNo.UNDECIDED;
        }
    }

    private void configChanged(IConfigManager manager, Setting<?> setting) {
        if (setting == Settings.LOCK_CRAFTING_MODE) {
            this.resetCraftingLock();
        } else {
            this.saveChanges();
        }
    }

    private boolean getRedstoneState() {
        if (this.redstoneState == YesNo.UNDECIDED) {
            BlockEntity be = this.host.getBlockEntity();
            this.redstoneState = be.getLevel().hasNeighborSignal(be.getBlockPos()) ? YesNo.YES : YesNo.NO;
        }
        return this.redstoneState == YesNo.YES;
    }

    private class Ticker
    implements IGridTickable {
        private Ticker() {
        }

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !PatternProviderLogic.this.hasWorkToDo());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!PatternProviderLogic.this.mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean couldDoWork = PatternProviderLogic.this.doWork();
            return PatternProviderLogic.this.hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
        }
    }
}

