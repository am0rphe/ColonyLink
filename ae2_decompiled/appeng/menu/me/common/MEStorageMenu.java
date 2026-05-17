/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.google.common.primitives.Ints
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.common;

import appeng.api.config.Actionable;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.core.AELog;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.core.network.clientbound.SetLinkStatusPacket;
import appeng.core.network.serverbound.MEInteractionPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ActionHostEnergySource;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.LinkStatusAwareMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.IMEInteractionHandler;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.Platform;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class MEStorageMenu
extends AEBaseMenu
implements IConfigurableObject,
IMEInteractionHandler,
LinkStatusAwareMenu,
KeyTypeSelectionMenu {
    public static final MenuType<MEStorageMenu> TYPE = MenuTypeBuilder.create(MEStorageMenu::new, ITerminalHost.class).build("item_terminal");
    public static final MenuType<MEStorageMenu> PORTABLE_ITEM_CELL_TYPE = MenuTypeBuilder.create(MEStorageMenu::new, IPortableTerminal.class).build("portable_item_cell");
    public static final MenuType<MEStorageMenu> PORTABLE_FLUID_CELL_TYPE = MenuTypeBuilder.create(MEStorageMenu::new, IPortableTerminal.class).build("portable_fluid_cell");
    public static final MenuType<MEStorageMenu> WIRELESS_TYPE = MenuTypeBuilder.create(MEStorageMenu::new, IPortableTerminal.class).build("wirelessterm");
    private final List<RestrictedInputSlot> viewCellSlots;
    private final IConfigManager clientCM;
    private final ToolboxMenu toolboxMenu;
    private final ITerminalHost host;
    @GuiSync(value=100)
    public int activeCraftingJobs = -1;
    private static final short SEARCH_KEY_TYPES_ID = 101;
    @GuiSync(value=101)
    public KeyTypeSelectionMenu.SyncedKeyTypes searchKeyTypes = new KeyTypeSelectionMenu.SyncedKeyTypes();
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected(null);
    @Nullable
    private Runnable gui;
    private IConfigManager serverCM;
    protected final MEStorage storage;
    protected final IEnergySource energySource;
    private final IncrementalUpdateHelper updateHelper = new IncrementalUpdateHelper();
    @Nullable
    private IClientRepo clientRepo;
    private Set<AEKey> previousCraftables = Collections.emptySet();
    private KeyCounter previousAvailableStacks = new KeyCounter();

    public MEStorageMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        this(menuType, id, ip, host, true);
    }

    protected MEStorageMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host, boolean bindInventory) {
        super(menuType, id, ip, host);
        this.host = host;
        if (host instanceof IEnergySource) {
            IEnergySource hostEnergySource;
            this.energySource = hostEnergySource = (IEnergySource)((Object)host);
        } else if (host instanceof IActionHost) {
            IActionHost actionHost = (IActionHost)((Object)host);
            this.energySource = new ActionHostEnergySource(actionHost);
        } else {
            this.energySource = IEnergySource.empty();
        }
        this.storage = Objects.requireNonNull(host.getInventory(), "host inventory is null");
        this.clientCM = IConfigManager.builder(this::onSettingChanged).registerSetting(Settings.SORT_BY, SortOrder.NAME).registerSetting(Settings.VIEW_MODE, ViewItems.ALL).registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING).build();
        if (this.isServerSide()) {
            this.serverCM = host.getConfigManager();
        }
        if (!this.hideViewCells() && host instanceof IViewCellStorage) {
            InternalInventory viewCellStorage = ((IViewCellStorage)((Object)host)).getViewCellStorage();
            this.viewCellSlots = new ArrayList<RestrictedInputSlot>(viewCellStorage.size());
            for (int i = 0; i < viewCellStorage.size(); ++i) {
                RestrictedInputSlot slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL, viewCellStorage, i);
                this.addSlot(slot, SlotSemantics.VIEW_CELL);
                this.viewCellSlots.add(slot);
            }
        } else {
            this.viewCellSlots = Collections.emptyList();
        }
        this.toolboxMenu = new ToolboxMenu(this);
        this.setupUpgrades(host.getUpgrades());
        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }
    }

    public ToolboxMenu getToolbox() {
        return this.toolboxMenu;
    }

    protected boolean hideViewCells() {
        return false;
    }

    @Nullable
    public IGridNode getGridNode() {
        ITerminalHost iTerminalHost = this.host;
        if (iTerminalHost instanceof IActionHost) {
            IActionHost actionHost = (IActionHost)((Object)iTerminalHost);
            return actionHost.getActionableNode();
        }
        return null;
    }

    public boolean isKeyVisible(AEKey key) {
        Object t;
        if (this.itemMenuHost != null && (t = this.itemMenuHost.getItem()) instanceof IBasicCellItem) {
            IBasicCellItem basicCellItem = (IBasicCellItem)t;
            return basicCellItem.getKeyType().contains(key);
        }
        return true;
    }

    @Override
    public void broadcastChanges() {
        this.toolboxMenu.tick();
        if (this.isServerSide()) {
            this.updateLinkStatus();
            this.updateActiveCraftingJobs();
            for (Setting<?> setting : this.serverCM.getSettings()) {
                Object sideRemote;
                Object sideLocal = this.serverCM.getSetting(setting);
                if (sideLocal == (sideRemote = this.clientCM.getSetting(setting))) continue;
                setting.copy(this.serverCM, this.clientCM);
                this.sendPacketToClient(new ConfigValuePacket(setting, this.serverCM));
            }
            ITerminalHost iTerminalHost = this.host;
            if (iTerminalHost instanceof KeyTypeSelectionHost) {
                KeyTypeSelectionHost keyTypeSelectionHost = (KeyTypeSelectionHost)((Object)iTerminalHost);
                this.searchKeyTypes = new KeyTypeSelectionMenu.SyncedKeyTypes(keyTypeSelectionHost.getKeyTypeSelection().enabled());
            }
            Set<AEKey> craftables = this.getCraftablesFromGrid();
            KeyCounter keyCounter = this.storage.getAvailableStacks();
            KeyCounter requestables = new KeyCounter();
            try {
                Sets.difference(this.previousCraftables, craftables).forEach(this.updateHelper::addChange);
                Sets.difference(craftables, this.previousCraftables).forEach(this.updateHelper::addChange);
                this.previousAvailableStacks.removeAll(keyCounter);
                this.previousAvailableStacks.removeZeros();
                this.previousAvailableStacks.keySet().forEach(this.updateHelper::addChange);
                if (this.updateHelper.hasChanges()) {
                    MEInventoryUpdatePacket.Builder builder = MEInventoryUpdatePacket.builder(this.containerId, this.updateHelper.isFullUpdate(), this.getPlayer().registryAccess());
                    builder.setFilter(this::isKeyVisible);
                    builder.addChanges(this.updateHelper, keyCounter, craftables, requestables);
                    builder.buildAndSend(x$0 -> this.sendPacketToClient((ClientboundPacket)x$0));
                    this.updateHelper.commitChanges();
                }
            }
            catch (Exception e) {
                AELog.warn(e, "Failed to send incremental inventory update to client");
            }
            this.previousCraftables = ImmutableSet.copyOf(craftables);
            this.previousAvailableStacks = keyCounter;
            super.broadcastChanges();
        }
    }

    @Override
    public void onServerDataSync(ShortSet updatedFields) {
        super.onServerDataSync(updatedFields);
        if (updatedFields.contains((short)101) && this.getGui() != null) {
            this.getGui().run();
        }
    }

    protected boolean showsCraftables() {
        return true;
    }

    private Set<AEKey> getCraftablesFromGrid() {
        ITerminalHost iTerminalHost;
        IGridNode hostNode = this.getGridNode();
        if (hostNode == null && (iTerminalHost = this.host) instanceof IActionHost) {
            IActionHost actionHost = (IActionHost)((Object)iTerminalHost);
            hostNode = actionHost.getActionableNode();
        }
        if (!this.showsCraftables()) {
            return Collections.emptySet();
        }
        if (hostNode != null && hostNode.isActive()) {
            return hostNode.getGrid().getCraftingService().getCraftables(this::isKeyVisible);
        }
        return Collections.emptySet();
    }

    private void updateActiveCraftingJobs() {
        IGridNode hostNode = this.getGridNode();
        IGrid grid = null;
        if (hostNode != null) {
            grid = hostNode.getGrid();
        }
        if (grid == null) {
            this.activeCraftingJobs = -1;
            return;
        }
        int activeJobs = 0;
        for (ICraftingCPU cpus : grid.getCraftingService().getCpus()) {
            if (!cpus.isBusy()) continue;
            ++activeJobs;
        }
        this.activeCraftingJobs = activeJobs;
    }

    private void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        if (this.getGui() != null) {
            this.getGui().run();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (this.isServerSide()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public List<ItemStack> getViewCells() {
        return this.viewCellSlots.stream().map(AppEngSlot::getItem).collect(Collectors.toList());
    }

    protected final boolean canInteractWithGrid() {
        return this.getLinkStatus().connected();
    }

    @Override
    public final void handleInteraction(long serial, InventoryAction action) {
        if (this.isClientSide()) {
            MEInteractionPacket message = new MEInteractionPacket(this.containerId, serial, action);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (!this.canInteractWithGrid()) {
            return;
        }
        ServerPlayer player = (ServerPlayer)this.getPlayerInventory().player;
        if (serial == -1L) {
            this.handleNetworkInteraction(player, null, action);
            return;
        }
        AEKey stack = this.getStackBySerial(serial);
        if (stack == null) {
            return;
        }
        this.handleNetworkInteraction(player, stack, action);
    }

    protected void handleNetworkInteraction(ServerPlayer player, @Nullable AEKey clickedKey, InventoryAction action) {
        if (!this.canInteractWithGrid()) {
            return;
        }
        if (action == InventoryAction.AUTO_CRAFT) {
            MenuHostLocator locator = this.getLocator();
            if (locator != null && clickedKey != null) {
                CraftAmountMenu.open(player, locator, clickedKey, clickedKey.getAmountPerUnit());
            }
            return;
        }
        switch (action) {
            case FILL_ITEM: {
                this.tryFillContainerItem(clickedKey, false, false);
                break;
            }
            case FILL_ITEM_MOVE_TO_PLAYER: {
                this.tryFillContainerItem(clickedKey, true, false);
                break;
            }
            case FILL_ENTIRE_ITEM: {
                this.tryFillContainerItem(clickedKey, false, true);
                break;
            }
            case FILL_ENTIRE_ITEM_MOVE_TO_PLAYER: {
                this.tryFillContainerItem(clickedKey, true, true);
                break;
            }
            case EMPTY_ITEM: {
                this.handleEmptyHeldItem((what, amount, mode) -> StorageHelper.poweredInsert(this.energySource, this.storage, what, amount, this.getActionSource(), mode), false);
                break;
            }
            case EMPTY_ENTIRE_ITEM: {
                this.handleEmptyHeldItem((what, amount, mode) -> StorageHelper.poweredInsert(this.energySource, this.storage, what, amount, this.getActionSource(), mode), true);
            }
        }
        if (clickedKey == null) {
            if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE || action == InventoryAction.ROLL_DOWN) {
                this.putCarriedItemIntoNetwork(true);
            } else if (action == InventoryAction.PICKUP_OR_SET_DOWN) {
                this.putCarriedItemIntoNetwork(false);
            }
            return;
        }
        if (!(clickedKey instanceof AEItemKey)) {
            return;
        }
        AEItemKey clickedItem = (AEItemKey)clickedKey;
        switch (action) {
            case SHIFT_CLICK: {
                this.moveOneStackToPlayer(clickedItem);
                break;
            }
            case ROLL_DOWN: {
                AEItemKey what2;
                long inserted;
                ItemStack carried = this.getCarried();
                if (carried.isEmpty() || (inserted = StorageHelper.poweredInsert(this.energySource, this.storage, what2 = AEItemKey.of(carried), 1L, this.getActionSource())) <= 0L) break;
                this.getCarried().shrink(1);
                break;
            }
            case ROLL_UP: 
            case PICKUP_SINGLE: {
                long extracted;
                ItemStack item = this.getCarried();
                if (!item.isEmpty()) {
                    if (item.getCount() >= item.getMaxStackSize()) {
                        return;
                    }
                    if (!clickedItem.matches(item)) {
                        return;
                    }
                }
                if ((extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, clickedItem, 1L, this.getActionSource())) <= 0L) break;
                if (item.isEmpty()) {
                    this.setCarried(clickedItem.toStack());
                    break;
                }
                item.grow(1);
                break;
            }
            case PICKUP_OR_SET_DOWN: {
                if (!this.getCarried().isEmpty()) {
                    this.putCarriedItemIntoNetwork(false);
                    break;
                }
                long extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, clickedItem, clickedItem.getMaxStackSize(), this.getActionSource());
                if (extracted > 0L) {
                    this.setCarried(clickedItem.toStack((int)extracted));
                    break;
                }
                this.setCarried(ItemStack.EMPTY);
                break;
            }
            case SPLIT_OR_PLACE_SINGLE: {
                if (!this.getCarried().isEmpty()) {
                    this.putCarriedItemIntoNetwork(true);
                    break;
                }
                long extracted = this.storage.extract(clickedItem, clickedItem.getMaxStackSize(), Actionable.SIMULATE, this.getActionSource());
                if (extracted > 0L) {
                    extracted = extracted + 1L >> 1;
                    extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, clickedItem, extracted, this.getActionSource());
                }
                if (extracted > 0L) {
                    this.setCarried(clickedItem.toStack((int)extracted));
                    break;
                }
                this.setCarried(ItemStack.EMPTY);
                break;
            }
            case CREATIVE_DUPLICATE: {
                if (!player.getAbilities().instabuild) break;
                ItemStack is = clickedItem.toStack();
                is.setCount(is.getMaxStackSize());
                this.setCarried(is);
                break;
            }
            case MOVE_REGION: {
                int playerInv = player.getInventory().items.size();
                for (int slotNum = 0; slotNum < playerInv && this.moveOneStackToPlayer(clickedItem); ++slotNum) {
                }
                break;
            }
            default: {
                AELog.warn("Received unhandled inventory action %s from client in %s", new Object[]{action, this.getClass()});
            }
        }
    }

    private void tryFillContainerItem(@Nullable AEKey clickedKey, boolean moveToPlayer, boolean fillAll) {
        AEFluidKey fluidKey;
        boolean grabbedEmptyBucket = false;
        if (this.getCarried().isEmpty() && clickedKey instanceof AEFluidKey && (fluidKey = (AEFluidKey)clickedKey).getFluid().getBucket() != Items.AIR && this.storage.extract(AEItemKey.of((ItemLike)Items.BUCKET), 1L, Actionable.MODULATE, this.getActionSource()) >= 1L) {
            ItemStack bucket = Items.BUCKET.getDefaultInstance();
            this.setCarried(bucket);
            grabbedEmptyBucket = true;
        }
        Item carriedBefore = this.getCarried().getItem();
        this.handleFillingHeldItem((amount, mode) -> StorageHelper.poweredExtraction(this.energySource, this.storage, clickedKey, amount, this.getActionSource(), mode), clickedKey, fillAll);
        if (grabbedEmptyBucket && this.getCarried().is(Items.BUCKET)) {
            long inserted = this.storage.insert(AEItemKey.of(this.getCarried()), this.getCarried().getCount(), Actionable.MODULATE, this.getActionSource());
            ItemStack newCarried = this.getCarried().copy();
            newCarried.shrink(Ints.saturatedCast((long)inserted));
            this.setCarried(newCarried);
        }
        if (moveToPlayer && !this.getCarried().is(carriedBefore) && this.getPlayer().addItem(this.getCarried())) {
            this.setCarried(ItemStack.EMPTY);
        }
    }

    protected void putCarriedItemIntoNetwork(boolean singleItem) {
        ItemStack heldStack = this.getCarried();
        AEItemKey what = AEItemKey.of(heldStack);
        if (what == null) {
            return;
        }
        int amount = heldStack.getCount();
        if (singleItem) {
            amount = 1;
        }
        long inserted = StorageHelper.poweredInsert(this.energySource, this.storage, what, amount, this.getActionSource());
        this.setCarried(Platform.getInsertionRemainder(heldStack, inserted));
    }

    private boolean moveOneStackToPlayer(AEItemKey what) {
        long potentialAmount = this.storage.extract(what, what.getMaxStackSize(), Actionable.SIMULATE, this.getActionSource());
        if (potentialAmount <= 0L) {
            return false;
        }
        List<Slot> destinationSlots = this.getQuickMoveDestinationSlots(what.toStack(), false);
        for (Slot destinationSlot : destinationSlots) {
            int amount = this.getPlaceableAmount(destinationSlot, what);
            if (amount <= 0) continue;
            long extracted = StorageHelper.poweredExtraction(this.energySource, this.storage, what, amount, this.getActionSource());
            if (extracted == 0L) {
                return false;
            }
            ItemStack currentItem = destinationSlot.getItem();
            if (!currentItem.isEmpty()) {
                destinationSlot.setByPlayer(currentItem.copyWithCount(currentItem.getCount() + (int)extracted));
            } else {
                destinationSlot.setByPlayer(what.toStack((int)extracted));
            }
            return true;
        }
        return false;
    }

    @Nullable
    protected final AEKey getStackBySerial(long serial) {
        return this.updateHelper.getBySerial(serial);
    }

    public ILinkStatus getLinkStatus() {
        return this.linkStatus;
    }

    @Nullable
    private Runnable getGui() {
        return this.gui;
    }

    public void setGui(@Nullable Runnable gui) {
        this.gui = gui;
    }

    @Nullable
    public IClientRepo getClientRepo() {
        return this.clientRepo;
    }

    public void setClientRepo(@Nullable IClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

    @Override
    protected int transferStackToMenu(ItemStack input) {
        if (!this.canInteractWithGrid()) {
            return super.transferStackToMenu(input);
        }
        AEItemKey key = AEItemKey.of(input);
        if (key == null || !this.isKeyVisible(key)) {
            return 0;
        }
        return (int)StorageHelper.poweredInsert(this.energySource, this.storage, key, input.getCount(), this.getActionSource());
    }

    public boolean hasIngredient(Ingredient ingredient, Object2IntOpenHashMap<Object> reservedAmounts) {
        IClientRepo clientRepo = this.getClientRepo();
        if (clientRepo != null && this.getLinkStatus().connected()) {
            for (GridInventoryEntry stack : clientRepo.getByIngredient(ingredient)) {
                int reservedAmount = reservedAmounts.getOrDefault((Object)stack, 0);
                if (stack.getStoredAmount() - (long)reservedAmount < 1L) continue;
                reservedAmounts.merge((Object)stack, 1, Integer::sum);
                return true;
            }
        }
        return false;
    }

    protected final KeyCounter getPreviousAvailableStacks() {
        Preconditions.checkState((boolean)this.isServerSide());
        return this.previousAvailableStacks;
    }

    public boolean canConfigureTypeFilter() {
        return this.host instanceof KeyTypeSelectionHost;
    }

    public ITerminalHost getHost() {
        return this.host;
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

    @Override
    public KeyTypeSelection getServerKeyTypeSelection() {
        return ((KeyTypeSelectionHost)((Object)this.host)).getKeyTypeSelection();
    }

    @Override
    public KeyTypeSelectionMenu.SyncedKeyTypes getClientKeyTypeSelection() {
        return this.searchKeyTypes;
    }
}

