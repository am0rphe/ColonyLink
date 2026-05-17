/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.collect.ArrayListMultimap
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  it.unimi.dsi.fastutil.shorts.ShortOpenHashSet
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu;

import appeng.api.behaviors.ContainerItemContext;
import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.AELog;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.clientbound.GuiDataSyncPacket;
import appeng.core.network.serverbound.GuiActionPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.PlayerSource;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.DataSynchronization;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.DisabledSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.parts.AEBasePart;
import appeng.util.ConfigMenuInventory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class AEBaseMenu
extends AbstractContainerMenu {
    private static final int MAX_STRING_LENGTH = Short.MAX_VALUE;
    private static final int MAX_CONTAINER_TRANSFER_ITERATIONS = 256;
    private static final String HIDE_SLOT = "HideSlot";
    private final IActionSource mySrc;
    @Nullable
    private final BlockEntity blockEntity;
    @Nullable
    private final IPart part;
    @Nullable
    protected final ItemMenuHost<?> itemMenuHost;
    private final DataSynchronization dataSync = new DataSynchronization((Object)this);
    private final Inventory playerInventory;
    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<Integer>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<Slot, SlotSemantic>();
    private final ArrayListMultimap<SlotSemantic, Slot> slotsBySemantic = ArrayListMultimap.create();
    private final Map<String, ClientAction<?>> clientActions = new HashMap();
    private boolean menuValid = true;
    private MenuHostLocator locator;
    private final Set<Slot> clientSideSlot = new HashSet<Slot>();
    private boolean returnedFromSubScreen;

    public AEBaseMenu(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id);
        this.playerInventory = playerInventory;
        this.blockEntity = host instanceof BlockEntity ? (BlockEntity)host : null;
        this.part = host instanceof IPart ? (IPart)host : null;
        ItemMenuHost itemMenuHost = this.itemMenuHost = host instanceof ItemMenuHost ? (ItemMenuHost)host : null;
        if (host != null && this.blockEntity == null && this.part == null && this.itemMenuHost == null) {
            throw new IllegalArgumentException("Must have a valid host, instead " + String.valueOf(host) + " in " + String.valueOf(playerInventory));
        }
        if (this.itemMenuHost != null && this.itemMenuHost.getPlayerInventorySlot() != null) {
            this.lockPlayerInventorySlot(this.itemMenuHost.getPlayerInventorySlot());
        }
        this.mySrc = new PlayerSource(this.getPlayer(), this.getActionHost());
        this.registerClientAction(HIDE_SLOT, String.class, this::hideSlot);
    }

    protected final IActionHost getActionHost() {
        if (this.itemMenuHost instanceof IActionHost) {
            return (IActionHost)((Object)this.itemMenuHost);
        }
        if (this.blockEntity instanceof IActionHost) {
            return (IActionHost)this.blockEntity;
        }
        if (this.part instanceof IActionHost) {
            return (IActionHost)((Object)this.part);
        }
        return null;
    }

    protected final boolean isActionHost() {
        return this.itemMenuHost instanceof IActionHost || this.blockEntity instanceof IActionHost || this.part instanceof IActionHost;
    }

    public Player getPlayer() {
        return this.getPlayerInventory().player;
    }

    protected final RegistryAccess registryAccess() {
        return this.getPlayer().level().registryAccess();
    }

    public IActionSource getActionSource() {
        return this.mySrc;
    }

    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void lockPlayerInventorySlot(int invSlot) {
        Preconditions.checkArgument((invSlot >= 0 && invSlot < this.playerInventory.getContainerSize() ? 1 : 0) != 0, (String)"cannot lock player inventory slot: %s", (int)invSlot);
        this.lockedPlayerInventorySlots.add(invSlot);
    }

    public final boolean isPlayerInventorySlotLocked(int invSlot) {
        return this.lockedPlayerInventorySlots.contains(invSlot);
    }

    public Object getTarget() {
        if (this.blockEntity != null) {
            return this.blockEntity;
        }
        if (this.part != null) {
            return this.part;
        }
        return this.itemMenuHost;
    }

    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    protected final void createPlayerInventorySlots(Inventory playerInventory) {
        Preconditions.checkState((boolean)this.getSlots(SlotSemantics.PLAYER_INVENTORY).isEmpty(), (Object)"Player inventory was already created");
        for (int i = 0; i < playerInventory.items.size(); ++i) {
            DisabledSlot slot = this.lockedPlayerInventorySlots.contains(i) ? new DisabledSlot((Container)playerInventory, i) : new Slot((Container)playerInventory, i, 0, 0);
            SlotSemantic s = i < Inventory.getSelectionSize() ? SlotSemantics.PLAYER_HOTBAR : SlotSemantics.PLAYER_INVENTORY;
            this.addSlot(slot, s);
        }
    }

    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && this.isPlayerInventorySlotLocked(40)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    protected Slot addSlot(Slot slot, SlotSemantic semantic) {
        Preconditions.checkState((!this.semanticBySlot.containsKey(slot = this.addSlot(slot)) ? 1 : 0) != 0);
        this.semanticBySlot.put(slot, semantic);
        this.slotsBySemantic.put((Object)semantic, (Object)slot);
        return slot;
    }

    public Slot addClientSideSlot(Slot slot, SlotSemantic semantic) {
        Preconditions.checkState((boolean)this.isClientSide(), (Object)"Can only add client-side slots on the client");
        if (!this.clientSideSlot.add(slot)) {
            throw new IllegalStateException("Client-side slot already exists");
        }
        slot.index = this.slots.size();
        this.slots.add((Object)slot);
        if (semantic != null) {
            this.semanticBySlot.put(slot, semantic);
            this.slotsBySemantic.put((Object)semantic, (Object)slot);
        }
        return slot;
    }

    public void removeClientSideSlot(Slot slot) {
        if (this.slots.get(slot.index) != slot) {
            throw new IllegalStateException("Trying to remove slot which isn't currently in the menu");
        }
        if (!this.clientSideSlot.remove(slot)) {
            throw new IllegalStateException("Trying to remove slot which isn't a client-side slot");
        }
        this.slots.remove(slot.index);
        this.semanticBySlot.remove(slot);
        this.slotsBySemantic.values().remove(slot);
        for (int i = slot.index; i < this.slots.size(); ++i) {
            ((Slot)this.slots.get((int)i)).index = i;
        }
    }

    public boolean isClientSideSlot(Slot slot) {
        return this.clientSideSlot.contains(slot);
    }

    public List<Slot> getSlots(SlotSemantic semantic) {
        return this.slotsBySemantic.get((Object)semantic);
    }

    protected Slot addSlot(Slot newSlot) {
        if (newSlot instanceof AppEngSlot) {
            AppEngSlot s = (AppEngSlot)newSlot;
            s.setMenu(this);
        }
        return super.addSlot(newSlot);
    }

    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        for (int i = 0; i < items.size(); ++i) {
            Slot slot = this.getSlot(i);
            if (slot instanceof AppEngSlot) {
                AppEngSlot aeSlot = (AppEngSlot)slot;
                aeSlot.initialize(items.get(i));
                continue;
            }
            slot.set(items.get(i));
        }
        this.setCarried(carried);
        this.stateId = stateId;
    }

    public void broadcastChanges() {
        if (!this.isValidMenu()) {
            return;
        }
        if (this.itemMenuHost != null) {
            if (!this.itemMenuHost.isValid()) {
                this.setValidMenu(false);
                return;
            }
            this.itemMenuHost.tick();
        }
        if (this.isServerSide()) {
            AEBasePart basePart;
            IPartHost host;
            IPart iPart;
            if (this.blockEntity != null && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) != this.blockEntity) {
                this.setValidMenu(false);
            }
            if ((iPart = this.part) instanceof AEBasePart && ((host = (basePart = (AEBasePart)iPart).getHost()) == null || !host.isInWorld() || host.getPart(basePart.getSide()) != basePart)) {
                this.setValidMenu(false);
            }
            if (this.dataSync.hasChanges()) {
                this.sendPacketToClient(new GuiDataSyncPacket(this.containerId, this.dataSync::writeUpdate, this.registryAccess()));
            }
        }
        super.broadcastChanges();
    }

    protected int getQuickMovePriority(Slot slot) {
        SlotSemantic semantic = this.getSlotSemantic(slot);
        if (semantic == null) {
            return 0;
        }
        return semantic.quickMovePriority();
    }

    public boolean isPlayerSideSlot(Slot slot) {
        if (slot.container == this.playerInventory) {
            return true;
        }
        SlotSemantic slotSemantic = this.semanticBySlot.get(slot);
        return slotSemantic != null && slotSemantic.playerSide();
    }

    @Nullable
    public SlotSemantic getSlotSemantic(Slot s) {
        return this.semanticBySlot.get(s);
    }

    public void hideSlot(String semantic) {
        SlotSemantic slotSemantic;
        if (this.isClientSide()) {
            this.sendClientAction(HIDE_SLOT, semantic);
        }
        if ((slotSemantic = SlotSemantics.get(semantic)) == null) {
            return;
        }
        if (this.canSlotsBeHidden(slotSemantic)) {
            for (Slot s : this.getSlots(slotSemantic)) {
                if (!(s instanceof AppEngSlot)) continue;
                AppEngSlot slot = (AppEngSlot)s;
                slot.setSlotEnabled(false);
            }
        }
    }

    protected boolean canSlotsBeHidden(SlotSemantic semantic) {
        return false;
    }

    public ItemStack quickMoveStack(Player player, int idx) {
        int transferred;
        if (this.isClientSide()) {
            return ItemStack.EMPTY;
        }
        Slot clickSlot = (Slot)this.slots.get(idx);
        if (!clickSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stackToMove = clickSlot.getItem();
        if (stackToMove.isEmpty()) {
            return ItemStack.EMPTY;
        }
        boolean fromPlayerSide = this.isPlayerSideSlot(clickSlot);
        if (fromPlayerSide && (transferred = this.transferStackToMenu(stackToMove.copy())) > 0) {
            clickSlot.remove(transferred);
        }
        if ((stackToMove = clickSlot.getItem()).isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack originalStackToMove = stackToMove.copy();
        if (!ItemStack.matches((ItemStack)originalStackToMove, (ItemStack)(stackToMove = this.quickMoveToOtherSlots(stackToMove, this.isPlayerSideSlot(clickSlot))))) {
            clickSlot.setByPlayer(stackToMove.isEmpty() ? ItemStack.EMPTY : stackToMove);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack quickMoveToOtherSlots(ItemStack stackToMove, boolean fromPlayerSide) {
        List<Slot> destinationSlots = this.getQuickMoveDestinationSlots(stackToMove, fromPlayerSide);
        if (destinationSlots.isEmpty() && fromPlayerSide) {
            for (Slot cs : this.slots) {
                if (!(cs instanceof FakeSlot) || this.isPlayerSideSlot(cs)) continue;
                ItemStack destination = cs.getItem();
                if (ItemStack.isSameItemSameComponents((ItemStack)destination, (ItemStack)stackToMove)) break;
                if (!destination.isEmpty()) continue;
                cs.set(stackToMove.copy());
                this.broadcastChanges();
                break;
            }
            return stackToMove;
        }
        for (Slot dest : destinationSlots) {
            if (!dest.hasItem() || !(stackToMove = dest.safeInsert(stackToMove)).isEmpty()) continue;
            return stackToMove;
        }
        for (Slot dest : destinationSlots) {
            if (dest.hasItem() || !(stackToMove = dest.safeInsert(stackToMove)).isEmpty()) continue;
            return stackToMove;
        }
        return stackToMove;
    }

    protected List<Slot> getQuickMoveDestinationSlots(ItemStack stackToMove, boolean fromPlayerSide) {
        ArrayList<Slot> destinationSlots = new ArrayList<Slot>();
        for (Slot candidateSlot : this.slots) {
            if (!this.isValidQuickMoveDestination(candidateSlot, stackToMove, fromPlayerSide)) continue;
            destinationSlots.add(candidateSlot);
        }
        destinationSlots.sort(Comparator.comparingInt(this::getQuickMovePriority).reversed());
        return destinationSlots;
    }

    protected boolean isValidQuickMoveDestination(Slot candidateSlot, ItemStack stackToMove, boolean fromPlayerSide) {
        return this.isPlayerSideSlot(candidateSlot) != fromPlayerSide && !(candidateSlot instanceof FakeSlot) && !(candidateSlot instanceof CraftingMatrixSlot) && candidateSlot.mayPlace(stackToMove);
    }

    protected int getPlaceableAmount(Slot s, AEItemKey what) {
        if (!s.mayPlace(what.toStack())) {
            return 0;
        }
        ItemStack currentItem = s.getItem();
        if (currentItem.isEmpty()) {
            return s.getMaxStackSize(what.getReadOnlyStack());
        }
        if (what.matches(currentItem)) {
            return Math.max(0, s.getMaxStackSize(currentItem) - currentItem.getCount());
        }
        return 0;
    }

    public boolean stillValid(Player player) {
        if (this.isValidMenu()) {
            if (this.blockEntity instanceof Container) {
                return ((Container)this.blockEntity).stillValid(player);
            }
            return true;
        }
        return false;
    }

    public boolean canDragTo(Slot s) {
        if (s instanceof AppEngSlot) {
            return ((AppEngSlot)s).isDraggable();
        }
        return super.canDragTo(s);
    }

    public void setFilter(int slotIndex, ItemStack item) {
        FakeSlot fakeSlot;
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }
        Slot s = this.getSlot(slotIndex);
        if (!(s instanceof AppEngSlot)) {
            return;
        }
        AppEngSlot appEngSlot = (AppEngSlot)s;
        if (!appEngSlot.isSlotEnabled()) {
            return;
        }
        if (s instanceof FakeSlot && (fakeSlot = (FakeSlot)s).canSetFilterTo(item)) {
            s.set(item);
        }
    }

    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        ConfigMenuInventory configInv;
        AppEngSlot appEngSlot;
        Object object;
        if (slot < 0 || slot >= this.slots.size()) {
            return;
        }
        Slot s = this.getSlot(slot);
        if (s instanceof CraftingTermSlot) {
            CraftingTermSlot craftingTermSlot = (CraftingTermSlot)s;
            switch (action) {
                case CRAFT_SHIFT: 
                case CRAFT_ALL: 
                case CRAFT_ITEM: 
                case CRAFT_STACK: {
                    craftingTermSlot.doClick(action, (Player)player);
                }
            }
        }
        if (s instanceof FakeSlot) {
            FakeSlot fakeSlot = (FakeSlot)s;
            this.handleFakeSlotAction(fakeSlot, action);
            return;
        }
        if (s instanceof AppEngSlot && (object = (appEngSlot = (AppEngSlot)s).getInventory()) instanceof ConfigMenuInventory && (configInv = (ConfigMenuInventory)object).getDelegate().getMode() == GenericStackInv.Mode.STORAGE) {
            GenericStackInv realInv = configInv.getDelegate();
            int realInvSlot = appEngSlot.slot;
            if (action == InventoryAction.FILL_ITEM || action == InventoryAction.FILL_ENTIRE_ITEM) {
                AEKey what2 = realInv.getKey(realInvSlot);
                this.handleFillingHeldItem((amount, mode) -> realInv.extract(realInvSlot, what2, amount, mode), what2, action == InventoryAction.FILL_ENTIRE_ITEM);
            } else if (action == InventoryAction.EMPTY_ITEM || action == InventoryAction.EMPTY_ENTIRE_ITEM) {
                this.handleEmptyHeldItem((what, amount, mode) -> realInv.insert(realInvSlot, what, amount, mode), action == InventoryAction.EMPTY_ENTIRE_ITEM);
            }
        }
        if (action == InventoryAction.MOVE_REGION) {
            SlotSemantic slotSemantic = this.getSlotSemantic(s);
            if (slotSemantic != null) {
                List<Slot> slotsToMove = List.copyOf(this.getSlots(slotSemantic));
                for (Slot slotToMove : slotsToMove) {
                    this.quickMoveStack((Player)player, slotToMove.index);
                }
            } else {
                this.quickMoveStack((Player)player, s.index);
            }
        }
    }

    protected final void handleFillingHeldItem(FillingSource source, AEKey what, boolean fillAll) {
        long amountAllowed;
        long canPull;
        int maxIterations;
        ContainerItemContext ctx = ContainerItemStrategies.findCarriedContextForKey(what, this.getPlayer(), this);
        if (ctx == null) {
            return;
        }
        long amount = fillAll ? Long.MAX_VALUE : (long)what.getAmountPerUnit();
        boolean filled = false;
        int n = maxIterations = fillAll ? 256 : 1;
        while (maxIterations > 0 && (canPull = source.extract(amount, Actionable.SIMULATE)) > 0L && (amountAllowed = ctx.insert(what, canPull, Actionable.SIMULATE)) != 0L) {
            long extracted = source.extract(amountAllowed, Actionable.MODULATE);
            if (extracted <= 0L) {
                AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ", new Object[0]);
                break;
            }
            long inserted = ctx.insert(what, extracted, Actionable.MODULATE);
            if (inserted == 0L) break;
            filled = true;
            --maxIterations;
        }
        if (filled) {
            ctx.playFillSound(this.getPlayer(), what);
        }
    }

    protected final void handleEmptyHeldItem(EmptyingSink sink, boolean emptyAll) {
        long amountAllowed;
        long canExtract;
        ContainerItemContext ctx = ContainerItemStrategies.findCarriedContext(null, this.getPlayer(), this);
        if (ctx == null) {
            return;
        }
        GenericStack content = ctx.getExtractableContent();
        if (content == null || content.amount() == 0L) {
            return;
        }
        AEKey what = content.what();
        long amount = emptyAll ? Long.MAX_VALUE : (long)what.getAmountPerUnit();
        boolean emptied = false;
        for (int maxIterations = emptyAll ? 256 : 1; maxIterations > 0 && (canExtract = ctx.extract(what, amount, Actionable.SIMULATE)) > 0L && (amountAllowed = sink.insert(what, canExtract, Actionable.SIMULATE)) > 0L; --maxIterations) {
            long extracted = ctx.extract(what, amountAllowed, Actionable.MODULATE);
            if (extracted != amountAllowed) {
                AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.", this.getCarried());
                break;
            }
            if (sink.insert(what, extracted, Actionable.MODULATE) != extracted) {
                AELog.error("Failed to insert previously simulated %s into ME system", what);
                break;
            }
            emptied = true;
        }
        if (emptied) {
            ctx.playEmptySound(this.getPlayer(), what);
        }
    }

    private void handleFakeSlotAction(FakeSlot fakeSlot, InventoryAction action) {
        ItemStack hand = this.getCarried();
        switch (action) {
            case PICKUP_OR_SET_DOWN: {
                fakeSlot.increase(hand);
                break;
            }
            case PLACE_SINGLE: {
                if (hand.isEmpty()) break;
                ItemStack is = hand.copy();
                is.setCount(1);
                fakeSlot.increase(is);
                break;
            }
            case SPLIT_OR_PLACE_SINGLE: {
                ItemStack is = fakeSlot.getItem();
                if (!is.isEmpty()) {
                    fakeSlot.decrease(hand);
                    break;
                }
                if (hand.isEmpty()) break;
                is = hand.copy();
                is.setCount(1);
                fakeSlot.set(is);
                break;
            }
            case EMPTY_ITEM: {
                EmptyingAction emptyingAction = ContainerItemStrategies.getEmptyingAction(hand);
                if (emptyingAction == null) break;
                fakeSlot.set(GenericStack.wrapInItemStack(emptyingAction.what(), emptyingAction.maxAmount()));
                break;
            }
        }
    }

    protected int transferStackToMenu(ItemStack input) {
        return 0;
    }

    public void swapSlotContents(int slotA, int slotB) {
        ItemStack testB;
        Slot a = this.getSlot(slotA);
        Slot b = this.getSlot(slotB);
        if (a == null || b == null) {
            return;
        }
        ItemStack isA = a.getItem();
        ItemStack isB = b.getItem();
        if (isA.isEmpty() && isB.isEmpty()) {
            return;
        }
        if (!isA.isEmpty() && !a.mayPickup(this.getPlayerInventory().player)) {
            return;
        }
        if (!isB.isEmpty() && !b.mayPickup(this.getPlayerInventory().player)) {
            return;
        }
        if (!isB.isEmpty() && !a.mayPlace(isB)) {
            return;
        }
        if (!isA.isEmpty() && !b.mayPlace(isA)) {
            return;
        }
        ItemStack testA = isB.isEmpty() ? ItemStack.EMPTY : isB.copy();
        ItemStack itemStack = testB = isA.isEmpty() ? ItemStack.EMPTY : isA.copy();
        if (!testA.isEmpty() && testA.getCount() > a.getMaxStackSize()) {
            if (!testB.isEmpty()) {
                return;
            }
            int totalA = testA.getCount();
            testA.setCount(a.getMaxStackSize());
            testB = testA.copy();
            testB.setCount(totalA - testA.getCount());
        }
        if (!testB.isEmpty() && testB.getCount() > b.getMaxStackSize()) {
            if (!testA.isEmpty()) {
                return;
            }
            int totalB = testB.getCount();
            testB.setCount(b.getMaxStackSize());
            testA = testB.copy();
            testA.setCount(totalB - testA.getCount());
        }
        a.set(testA);
        b.set(testB);
    }

    @MustBeInvokedByOverriders
    public void onServerDataSync(ShortSet updatedFields) {
    }

    public void onSlotChange(Slot s) {
    }

    public boolean isValidForSlot(Slot s, ItemStack i) {
        return true;
    }

    public boolean isValidMenu() {
        return this.menuValid;
    }

    public void setValidMenu(boolean isContainerValid) {
        this.menuValid = isContainerValid;
    }

    public MenuHostLocator getLocator() {
        return this.locator;
    }

    public void setLocator(MenuHostLocator locator) {
        this.locator = locator;
    }

    public boolean isClientSide() {
        return this.getPlayer().getCommandSenderWorld().isClientSide();
    }

    protected boolean isServerSide() {
        return !this.isClientSide();
    }

    protected final void sendPacketToClient(ClientboundPacket packet) {
        Player player = this.getPlayer();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            serverPlayer.connection.send((CustomPacketPayload)packet);
        }
    }

    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        if (this.dataSync.hasFields()) {
            this.sendPacketToClient(new GuiDataSyncPacket(this.containerId, this.dataSync::writeFull, this.registryAccess()));
        }
    }

    public final void receiveServerSyncData(RegistryFriendlyByteBuf data) {
        ShortOpenHashSet updatedFields = new ShortOpenHashSet();
        this.dataSync.readUpdate(data, (ShortSet)updatedFields);
        this.onServerDataSync((ShortSet)updatedFields);
    }

    public final void receiveClientAction(String actionName, @Nullable String jsonPayload) {
        ClientAction<?> action = this.clientActions.get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("Unknown client action: '" + actionName + "'");
        }
        action.handle(jsonPayload);
    }

    protected final <T> void registerClientAction(String name, Class<T> argClass, Consumer<T> handler) {
        if (this.clientActions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate client action registered: " + name);
        }
        this.clientActions.put(name, new ClientAction<T>(name, argClass, handler));
    }

    protected final void registerClientAction(String name, Runnable callback) {
        this.registerClientAction(name, Void.class, arg -> callback.run());
    }

    protected final <T> void sendClientAction(String action, T arg) {
        String jsonPayload;
        ClientAction<?> clientAction = this.clientActions.get(action);
        if (clientAction == null) {
            throw new IllegalArgumentException("Trying to send unregistered client action: " + action);
        }
        if (clientAction.argClass == Void.class) {
            if (arg != null) {
                throw new IllegalArgumentException("Client action " + action + " requires no argument, but it was given");
            }
            jsonPayload = null;
        } else {
            if (arg == null) {
                throw new IllegalArgumentException("Client action " + action + " requires an argument, but none was given");
            }
            if (clientAction.argClass != arg.getClass()) {
                throw new IllegalArgumentException("Trying to send client action " + action + " with wrong argument type " + String.valueOf(arg.getClass()) + ", expected: " + String.valueOf(clientAction.argClass));
            }
            jsonPayload = clientAction.gson.toJson(arg);
        }
        if (jsonPayload != null && jsonPayload.length() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot send client action " + action + " because serialized argument is longer than 32767 (" + jsonPayload.length() + ")");
        }
        GuiActionPacket message = new GuiActionPacket(this.containerId, clientAction.name, jsonPayload);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    protected final void sendClientAction(String action) {
        this.sendClientAction(action, null);
    }

    protected final void setupUpgrades(IUpgradeInventory upgrades) {
        for (int i = 0; i < upgrades.size(); ++i) {
            RestrictedInputSlot slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, i);
            slot.setNotDraggable();
            this.addSlot(slot, SlotSemantics.UPGRADE);
        }
    }

    public boolean isReturnedFromSubScreen() {
        return this.returnedFromSubScreen;
    }

    public void setReturnedFromSubScreen(boolean returnedFromSubScreen) {
        this.returnedFromSubScreen = returnedFromSubScreen;
    }

    protected static interface FillingSource {
        public long extract(long var1, Actionable var3);
    }

    protected static interface EmptyingSink {
        public long insert(AEKey var1, long var2, Actionable var4);
    }

    private static class ClientAction<T> {
        private final Gson gson = new GsonBuilder().create();
        private final String name;
        private final Class<T> argClass;
        private final Consumer<T> handler;

        public ClientAction(String name, Class<T> argClass, Consumer<T> handler) {
            this.name = name;
            this.argClass = argClass;
            this.handler = handler;
        }

        public void handle(@Nullable String jsonPayload) {
            Object arg = null;
            if (this.argClass != Void.class) {
                AELog.debug("Handling client action '%s' with payload %s", this.name, jsonPayload);
                arg = this.gson.fromJson(jsonPayload, this.argClass);
            } else {
                AELog.debug("Handling client action '%s'", this.name);
            }
            this.handler.accept(arg);
        }
    }
}

