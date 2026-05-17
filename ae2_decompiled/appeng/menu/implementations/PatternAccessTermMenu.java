/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.implementations;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.IPatternAccessTermMenuHost;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import appeng.core.network.clientbound.SetLinkStatusPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.LinkStatusAwareMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PatternAccessTermMenu
extends AEBaseMenu
implements LinkStatusAwareMenu {
    private final IPatternAccessTermMenuHost host;
    @GuiSync(value=1)
    public ShowPatternProviders showPatternProviders = ShowPatternProviders.VISIBLE;
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected();
    public static final MenuType<PatternAccessTermMenu> TYPE = MenuTypeBuilder.create(PatternAccessTermMenu::new, IPatternAccessTermMenuHost.class).build("patternaccessterminal");
    private static long inventorySerial = Long.MIN_VALUE;
    private final Map<PatternContainer, ContainerTracker> diList = new IdentityHashMap<PatternContainer, ContainerTracker>();
    private final Long2ObjectOpenHashMap<ContainerTracker> byId = new Long2ObjectOpenHashMap();
    private final Set<PatternContainer> pinnedHosts = Collections.newSetFromMap(new IdentityHashMap());

    public ShowPatternProviders getShownProviders() {
        return this.showPatternProviders;
    }

    public PatternAccessTermMenu(int id, Inventory ip, IPatternAccessTermMenuHost anchor) {
        this(TYPE, id, ip, anchor, true);
    }

    public PatternAccessTermMenu(MenuType<?> menuType, int id, Inventory ip, IPatternAccessTermMenuHost host, boolean bindInventory) {
        super(menuType, id, ip, host);
        this.host = host;
        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.isClientSide()) {
            return;
        }
        this.showPatternProviders = this.host.getConfigManager().getSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS);
        super.broadcastChanges();
        this.updateLinkStatus();
        if (this.showPatternProviders != ShowPatternProviders.NOT_FULL) {
            this.pinnedHosts.clear();
        }
        IGrid grid = this.getGrid();
        VisitorState state = new VisitorState();
        if (grid != null) {
            for (Class<?> machineClass : grid.getMachineClasses()) {
                if (!PatternContainer.class.isAssignableFrom(machineClass)) continue;
                this.visitPatternProviderHosts(grid, machineClass, state);
            }
            this.pinnedHosts.removeIf(host -> host.getGrid() != grid);
        } else {
            this.pinnedHosts.clear();
        }
        if (state.total != this.diList.size() || state.forceFullUpdate) {
            this.sendFullUpdate(grid);
        } else {
            this.sendIncrementalUpdate();
        }
    }

    @Nullable
    private IGrid getGrid() {
        IGridNode agn = this.host.getGridNode();
        if (agn != null && agn.isActive()) {
            return agn.getGrid();
        }
        return null;
    }

    public ILinkStatus getLinkStatus() {
        return this.linkStatus;
    }

    private boolean isFull(PatternContainer logic) {
        for (int i = 0; i < logic.getTerminalPatternInventory().size(); ++i) {
            if (!logic.getTerminalPatternInventory().getStackInSlot(i).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean isVisible(PatternContainer container) {
        boolean isVisible = container.isVisibleInTerminal();
        return switch (this.getShownProviders()) {
            default -> throw new MatchException(null, null);
            case ShowPatternProviders.VISIBLE -> isVisible;
            case ShowPatternProviders.NOT_FULL -> {
                if (isVisible && (this.pinnedHosts.contains(container) || !this.isFull(container))) {
                    yield true;
                }
                yield false;
            }
            case ShowPatternProviders.ALL -> true;
        };
    }

    private <T extends PatternContainer> void visitPatternProviderHosts(IGrid grid, Class<T> machineClass, VisitorState state) {
        for (PatternContainer container : grid.getActiveMachines(machineClass)) {
            ContainerTracker t;
            if (!this.isVisible(container)) continue;
            if (this.getShownProviders() == ShowPatternProviders.NOT_FULL) {
                this.pinnedHosts.add(container);
            }
            if ((t = this.diList.get(container)) == null || !t.group.equals(container.getTerminalGroup())) {
                state.forceFullUpdate = true;
            }
            ++state.total;
        }
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        ContainerTracker inv = (ContainerTracker)this.byId.get(id);
        if (inv == null) {
            return;
        }
        if (slot < 0 || slot >= inv.server.size()) {
            AELog.warn("Client refers to invalid slot %d of inventory %s", slot, inv.container);
            return;
        }
        ItemStack is = inv.server.getStackInSlot(slot);
        FilteredInternalInventory patternSlot = new FilteredInternalInventory(inv.server.getSlotInv(slot), new PatternSlotFilter());
        ItemStack carried = this.getCarried();
        switch (action) {
            case PICKUP_OR_SET_DOWN: {
                if (!carried.isEmpty()) {
                    ItemStack inSlot = patternSlot.getStackInSlot(0);
                    if (inSlot.isEmpty()) {
                        this.setCarried(patternSlot.addItems(carried));
                        break;
                    }
                    inSlot = inSlot.copy();
                    ItemStack inHand = carried.copy();
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                    this.setCarried(ItemStack.EMPTY);
                    this.setCarried(patternSlot.addItems(inHand.copy()));
                    if (this.getCarried().isEmpty()) {
                        this.setCarried(inSlot);
                        break;
                    }
                    this.setCarried(inHand);
                    patternSlot.setItemDirect(0, inSlot);
                    break;
                }
                this.setCarried(patternSlot.getStackInSlot(0));
                patternSlot.setItemDirect(0, ItemStack.EMPTY);
                break;
            }
            case SPLIT_OR_PLACE_SINGLE: {
                if (!carried.isEmpty()) {
                    ItemStack extra = carried.split(1);
                    if (!extra.isEmpty()) {
                        extra = patternSlot.addItems(extra);
                    }
                    if (extra.isEmpty()) break;
                    carried.grow(extra.getCount());
                    break;
                }
                if (is.isEmpty()) break;
                this.setCarried(patternSlot.extractItem(0, (is.getCount() + 1) / 2, false));
                break;
            }
            case SHIFT_CLICK: {
                ItemStack stack = patternSlot.getStackInSlot(0).copy();
                if (!player.getInventory().add(stack)) {
                    patternSlot.setItemDirect(0, stack);
                    break;
                }
                patternSlot.setItemDirect(0, ItemStack.EMPTY);
                break;
            }
            case MOVE_REGION: {
                for (int x = 0; x < inv.server.size(); ++x) {
                    ItemStack stack = inv.server.getStackInSlot(x);
                    if (!player.getInventory().add(stack)) {
                        patternSlot.setItemDirect(0, stack);
                        continue;
                    }
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                }
                break;
            }
            case CREATIVE_DUPLICATE: {
                if (!player.getAbilities().instabuild || !carried.isEmpty()) break;
                this.setCarried(is.isEmpty() ? ItemStack.EMPTY : is.copy());
            }
        }
    }

    public void quickMovePattern(ServerPlayer player, int clickedSlot, List<Long> allowedPatternContainers) {
        if (clickedSlot < 0 || clickedSlot >= this.slots.size()) {
            return;
        }
        Slot sourceSlot = this.getSlot(clickedSlot);
        if (!this.isPlayerSideSlot(sourceSlot)) {
            return;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        if (sourceStack.getCount() != 1) {
            return;
        }
        IPatternDetails pattern = PatternDetailsHelper.decodePattern(sourceStack, player.level());
        if (pattern == null) {
            return;
        }
        boolean molecularAssemblerPattern = pattern instanceof IMolecularAssemblerSupportedPattern;
        ArrayList<ContainerTracker> targets = new ArrayList<ContainerTracker>();
        for (Long id : allowedPatternContainers) {
            AEItemKey icon;
            boolean molecularAssembler;
            ContainerTracker inv = (ContainerTracker)this.byId.get(id.longValue());
            if (inv == null || !this.isVisible(inv.container) || molecularAssemblerPattern != (molecularAssembler = (icon = inv.group.icon()) != null && icon.is(AEBlocks.MOLECULAR_ASSEMBLER))) continue;
            targets.add(inv);
        }
        if (targets.stream().map(t -> t.group).distinct().count() != 1L) {
            return;
        }
        for (ContainerTracker target : targets) {
            FilteredInternalInventory targetContainer = new FilteredInternalInventory(target.server, new PatternSlotFilter());
            if (!targetContainer.addItems(sourceStack).isEmpty()) continue;
            sourceSlot.set(ItemStack.EMPTY);
            return;
        }
    }

    private void sendFullUpdate(@Nullable IGrid grid) {
        this.byId.clear();
        this.diList.clear();
        this.sendPacketToClient(new ClearPatternAccessTerminalPacket());
        if (grid == null) {
            return;
        }
        for (Class<?> machineClass : grid.getMachineClasses()) {
            Class<? extends PatternContainer> containerClass = PatternAccessTermMenu.tryCastMachineToContainer(machineClass);
            if (containerClass == null) continue;
            for (PatternContainer patternContainer : grid.getActiveMachines(containerClass)) {
                if (!this.isVisible(patternContainer)) continue;
                this.diList.put(patternContainer, new ContainerTracker(patternContainer, patternContainer.getTerminalPatternInventory(), patternContainer.getTerminalGroup()));
            }
        }
        for (ContainerTracker inv : this.diList.values()) {
            this.byId.put(inv.serverId, (Object)inv);
            this.sendPacketToClient(inv.createFullPacket());
        }
    }

    private void sendIncrementalUpdate() {
        for (ContainerTracker inv : this.diList.values()) {
            PatternAccessTerminalPacket packet = inv.createUpdatePacket();
            if (packet == null) continue;
            this.sendPacketToClient(packet);
        }
    }

    private static Class<? extends PatternContainer> tryCastMachineToContainer(Class<?> machineClass) {
        if (PatternContainer.class.isAssignableFrom(machineClass)) {
            return machineClass.asSubclass(PatternContainer.class);
        }
        return null;
    }

    protected void updateLinkStatus() {
        ILinkStatus linkStatus = this.host.getLinkStatus();
        if (!Objects.equals(this.linkStatus, linkStatus)) {
            this.linkStatus = linkStatus;
            this.sendPacketToClient(new SetLinkStatusPacket(linkStatus));
        }
    }

    @Override
    public void setLinkStatus(ILinkStatus linkStatus) {
        this.linkStatus = linkStatus;
    }

    private static class VisitorState {
        int total;
        boolean forceFullUpdate;

        private VisitorState() {
        }
    }

    private static class ContainerTracker {
        private final PatternContainer container;
        private final long sortBy;
        private final long serverId = inventorySerial++;
        private final PatternContainerGroup group;
        private final InternalInventory client;
        private final InternalInventory server;

        public ContainerTracker(PatternContainer container, InternalInventory patterns, PatternContainerGroup group) {
            this.container = container;
            this.server = patterns;
            this.client = new AppEngInternalInventory(this.server.size());
            this.group = group;
            this.sortBy = container.getTerminalSortOrder();
        }

        public PatternAccessTerminalPacket createFullPacket() {
            Int2ObjectArrayMap slots = new Int2ObjectArrayMap(this.server.size());
            for (int i = 0; i < this.server.size(); ++i) {
                ItemStack stack = this.server.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                slots.put(i, (Object)stack);
            }
            return PatternAccessTerminalPacket.fullUpdate(this.serverId, this.server.size(), this.sortBy, this.group, (Int2ObjectMap<ItemStack>)slots);
        }

        @Nullable
        public PatternAccessTerminalPacket createUpdatePacket() {
            IntList changedSlots = this.detectChangedSlots();
            if (changedSlots == null) {
                return null;
            }
            Int2ObjectArrayMap slots = new Int2ObjectArrayMap(changedSlots.size());
            for (int i = 0; i < changedSlots.size(); ++i) {
                int slot;
                ItemStack stack = this.server.getStackInSlot(slot = changedSlots.getInt(i));
                this.client.setItemDirect(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                slots.put(slot, (Object)stack);
            }
            return PatternAccessTerminalPacket.incrementalUpdate(this.serverId, (Int2ObjectMap<ItemStack>)slots);
        }

        @Nullable
        private IntList detectChangedSlots() {
            IntArrayList changedSlots = null;
            for (int x = 0; x < this.server.size(); ++x) {
                if (!ContainerTracker.isDifferent(this.server.getStackInSlot(x), this.client.getStackInSlot(x))) continue;
                if (changedSlots == null) {
                    changedSlots = new IntArrayList();
                }
                changedSlots.add(x);
            }
            return changedSlots;
        }

        private static boolean isDifferent(ItemStack a, ItemStack b) {
            if (a.isEmpty() && b.isEmpty()) {
                return false;
            }
            if (a.isEmpty() || b.isEmpty()) {
                return true;
            }
            return !ItemStack.matches((ItemStack)a, (ItemStack)b);
        }
    }

    private static class PatternSlotFilter
    implements IAEItemFilter {
        private PatternSlotFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && PatternDetailsHelper.isEncodedPattern(stack);
        }
    }
}

