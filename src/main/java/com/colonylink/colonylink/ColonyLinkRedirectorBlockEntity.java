package com.colonylink.colonylink;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ColonyLinkRedirectorBlockEntity — v1.4.9
 *
 * History:
 *   v1.4.2: implements ICraftingProvider to expose the buffer's DomumPatterns to the
 *     AE2 network; pushPattern() queues a Domum craft run on the next tick; isBusy() is
 *     true while a craft runs (one at a time per Redirector); the buffer accepts only
 *     DomumPatternItem; notifyPatternChange() calls ICraftingProvider.requestUpdate()
 *     when the buffer changes.
 *
 *   v1.4.9 — chunk-load safety & zero voiding (Option A): pushPattern() refuses (returns
 *     false) when the AE2 node is inactive or the queue is saturated, so AE2 keeps the
 *     materials and retries — nothing is taken without a guaranteed return. isBusy()
 *     reflects saturation. injectIntoME() returns the uninserted remainder. pendingOutputs
 *     holds outputs produced but not yet reinjected (ME full / network offline), retried
 *     each tick. craftQueue and pendingOutputs are persisted in NBT so a chunk unload
 *     loses nothing — the state is replayed on reload (idempotent: materials pulled once
 *     by AE2). Bound: ColonyLinkConfig.REDIRECTOR_CRAFT_QUEUE_MAX.
 *
 *   Pass 2 (rendering): getUpdatePacket()/onDataPacket() make live BE sync work (the
 *     default returned a null packet); workingLinger (5 ticks) + renderStack capture keep
 *     1-tick crafts visible ~0.25s; clientTickSawdust() emits spruce sawdust at the
 *     blade/item contact (client ticker, never run on the logical server).
 *
 *   Pass 3: (A) aeJobActive keeps the animation alive for a whole AE2 job (gaps and recipe
 *     changes included), backed by ICraftingService.isRequesting on our outputs, throttled
 *     5 ticks with a key cache; (B) the AE2 node is not exposed on UP (glass) via
 *     setExposedOnSides + getGridNode(UP) → null; DOWN + 4 sides unchanged.
 */
public class ColonyLinkRedirectorBlockEntity extends BlockEntity
        implements IInWorldGridNodeHost, ICraftingProvider, IActionHost, MenuProvider,
                   net.minecraft.world.Clearable
{
    // Buffer constants

    // v1.6.2 — buffer size is frozen at 3×9 = 27 slots. It must match both the menu
    // slot layout (ColonyLinkRedirectorMenu) and the fixed GUI texture, so it is no
    // longer configurable (the old redirector_buffer_rows/cols options never worked:
    // the texture never adapted).
    public static final int BUFFER_ROWS = 3;
    public static final int BUFFER_COLS = 9;
    public static final int BUFFER_SIZE = BUFFER_ROWS * BUFFER_COLS;

    // State

    private BlockPos targetInventoryPos = null;
    private BlockPos linkedBuilderPos   = null;
    private String  linkedBuilderName  = "N/A";

    private boolean ae2ActiveClientCache = false;
    private boolean warehousePriority = false;
    private boolean warehousePriorityClientCache = false;

    /**
     * Queue of Domum crafts to run (one per tick). Persisted in NBT (v1.4.9): the
     * materials in it were already pulled from the AE2 network by pushPattern(), so
     * they must never be lost.
     */
    private final Queue<PendingDomumCraft> craftQueue = new ConcurrentLinkedQueue<>();

    /**
     * Domum outputs already produced but not (yet) reinjected into the ME (ME full,
     * network offline, no channel…). Retried each tick. Persisted in NBT (v1.4.9) —
     * zero voiding even on chunk unload. Server thread only (ticker + save/load).
     */
    private final List<ItemStack> pendingOutputs = new ArrayList<>();

    // Client mirror for the animated render.
    // These three fields are NOT persisted in NBT (saveAdditional/loadAdditional ignore
    // them): recomputed each server tick, synced to the client only via getUpdateTag()/
    // handleUpdateTag(). transient documents the intent (no Java serialization is used here).
    private transient boolean working = false;
    private transient ItemStack renderStack = ItemStack.EMPTY;
    private transient int shuffleProgress = 0;

    // Pass 2: visual linger + sawdust

    /** How long the block stays visually "working" after the last processed craft (Fab: ~0.25s). */
    private static final int WORKING_LINGER_TICKS = 5;

    /** Server-side countdown armed by processOneCraft(), decremented in
     *  updateShuffleAndSync() BEFORE computing newWorking. Never persisted. */
    private transient int workingLinger = 0;

    /** Set when processOneCraft() captures a fresh renderStack; forces one client sync. */
    private transient boolean renderStackDirty = false;

    /** Sawdust = spruce plank crumbs (fixed choice, no dynamic colour — Fab's spec).
     *  Vanilla Blocks/ParticleTypes are bootstrapped long before mod classes load,
     *  so a plain static final is safe. Common-side classes only. */
    private static final BlockParticleOption SAWDUST =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_PLANKS.defaultBlockState());

    // Pass 3: AE job awareness (visual only, never persisted)

    /** Visual-only: true while an AE2 crafting job still requests one of our patterns' outputs.
     *  Backed by ICraftingService.isRequesting(key) — the set of keys active CPU jobs are
     *  still WAITING FOR (rebuilt from every cluster's craftingLogic.getAllWaitingFor()).
     *  Falls back to false on job completion/cancellation at the next check. */
    private transient boolean aeJobActive = false;

    /** Output of the first requested pattern found by the last check (render fallback
     *  after a relog/chunk reload mid-batch, or during a gap before the first craft). */
    private transient ItemStack aeJobStack = ItemStack.EMPTY;

    private transient int aeJobCheckCooldown = 0;

    /** Never query the grid every tick — one check per 5 ticks matches the shuffle cadence. */
    private static final int AE_JOB_CHECK_INTERVAL = 5;

    /** Primary-output keys of the buffer's patterns, memoised so the 5-tick check does NOT
     *  re-decode up to 27 pattern NBTs each time. Rebuilt lazily; invalidated whenever the
     *  buffer changes (notifyPatternChange() is the single choke point for that). */
    private transient Set<AEItemKey> patternOutputKeys = null;

    // Buffer: accepts ONLY DomumPatternItem

    public final ItemStackHandler buffer = new ItemStackHandler(BUFFER_SIZE)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            // Only DomumPatternItem accepted; standard AE2 encoded patterns are rejected.
            return stack.isEmpty() || stack.getItem() instanceof DomumPatternItem;
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            notifyPatternChange();
        }
    };

    public final ItemStackHandler warehouseCardSlot = new ItemStackHandler(1)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        { return stack.getItem() instanceof WarehouseLinkCard; }

        @Override
        protected void onContentsChanged(int slot)
        {
            if (warehouseCardSlot.getStackInSlot(0).isEmpty())
                warehousePriority = false;
            setChanged();
            if (level != null)
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    // Redirector states

    public enum RedirectorState { NOT_LINKED, LINKED, STANDBY, NO_CONTROLLER }

    private RedirectorState state = RedirectorState.NO_CONTROLLER;

    // Grid node

    private final IManagedGridNode gridNode = GridHelper.createManagedNode(this,
                    new IGridNodeListener<ColonyLinkRedirectorBlockEntity>()
                    {
                        @Override
                        public void onSaveChanges(ColonyLinkRedirectorBlockEntity owner, IGridNode node)
                        { owner.setChanged(); }

                        @Override
                        public void onStateChanged(ColonyLinkRedirectorBlockEntity owner, IGridNode node,
                                                   IGridNodeListener.State state)
                        { owner.onGridStateChanged(); }
                    })
            .setInWorldNode(true)
            // Pass3 (PartB) — the top face is pure glass: the AE2 node must never
            // be reachable from UP. DOWN + the 4 horizontal sides stay connectable
            // (fixed set, independent of the horizontal FACING). Configured on the
            // managed node BEFORE create() runs in onLoad(), so the node is born with
            // the restriction (no transient UP connection at chunk load). On existing
            // worlds InWorldGridNode.setExposedOnSides()/updateState() destroys any
            // now-invalid UP connection cleanly (AE2 handles it natively).
            .setExposedOnSides(EnumSet.complementOf(EnumSet.of(Direction.UP)))
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM.get())
            .setIdlePowerUsage(1.0)
            // ICraftingProvider: expose the Domum patterns to the AE2 network
            .addService(ICraftingProvider.class, this);

    // Constructor

    public ColonyLinkRedirectorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ColonyLinkRegistry.REDIRECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    // 1.7.0-beta.4 — stable Redirector UUID
    //
    // Saved in the block entity NBT ("RedirectorId"), so it follows the block when a
    // mod moves it with its NBT (Create: Aeronautics / Sable ship reassembly gives the
    // block a new position). Clipboard entries store it and RedirectorRegistry resolves
    // it. A Redirector saved before beta.4 gets one on load (and is marked dirty so it
    // is persisted); a Redirector placed by a player gets a fresh one (setPlacedBy), so
    // a creative copy with block-entity data never shares its source's UUID.

    private UUID redirectorId = UUID.randomUUID();
    /** True until a saved RedirectorId was read: the UUID must be written on next save. */
    private boolean redirectorIdUnsaved = true;

    public UUID getRedirectorId() { return redirectorId; }

    /** New UUID (player placement). Re-registers when already loaded. */
    public void regenerateRedirectorId()
    {
        if (level != null && !level.isClientSide()) RedirectorRegistry.unregister(redirectorId, this);
        redirectorId = UUID.randomUUID();
        redirectorIdUnsaved = false;
        setChanged();
        if (level != null && !level.isClientSide() && !isRemoved()) RedirectorRegistry.register(redirectorId, this);
    }

    // MenuProvider

    @Override
    public Component getDisplayName() { return Component.translatable("colonylink.redir.title"); }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    { return new ColonyLinkRedirectorMenu(containerId, playerInventory, this); }

    // ICraftingProvider: expose patterns to the AE2 network

    /**
     * Returns all DomumPatternDetails available in the buffer. AE2 calls this to build
     * the craft plan. Server thread only.
     */
    @Override
    public List<IPatternDetails> getAvailablePatterns()
    {
        if (level == null || level.isClientSide()) return List.of();

        List<IPatternDetails> patterns = new ArrayList<>();
        HolderLookup.Provider provider = level.registryAccess();

        for (int slot = 0; slot < buffer.getSlots(); slot++)
        {
            ItemStack stack = buffer.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof DomumPatternItem)) continue;

            DomumPatternDetails details = new DomumPatternDetails(stack, provider);
            if (details.isValid())
                patterns.add(details);
        }

        return patterns;
    }

    /**
     * AE2 submits a craft to run; inputs[0] = the materials already extracted from the ME.
     *
     * v1.4.9 — AE2 contract: returning {@code false} makes AE2 keep (reinject) the
     * materials into the network. We refuse while the AE2 node is inactive (no network to
     * reinject the output into) or the queue is saturated / outputs are pending
     * (back-pressure), so accepting a pattern guarantees we can produce and render it.
     *
     * @return true if the craft was accepted (materials taken on), false otherwise
     */
    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs)
    {
        if (level == null || level.isClientSide()) return false;
        if (!(patternDetails instanceof DomumPatternDetails domumPattern)) return false;
        if (!domumPattern.isValid()) return false;

        // Refuse when the network is unavailable or we are already saturated: AE2 then
        // keeps the materials and retries, so no voiding is possible.
        IGridNode node = gridNode.getNode();
        if (node == null || !node.isActive()) return false;
        if (isBusy()) return false;

        // KeyCounter implements Iterable<Object2LongMap.Entry<AEKey>>.
        List<ItemStack> extractedMaterials = new ArrayList<>();
        for (KeyCounter counter : inputs)
        {
            for (var entry : counter)
            {
                AEKey key = entry.getKey();
                long amount = entry.getLongValue();
                if (key instanceof AEItemKey itemKey)
                    extractedMaterials.add(itemKey.toStack((int) Math.min(amount, Integer.MAX_VALUE)));
            }
        }

        // Output count from the pattern (e.g. shingles=4)
        long outputCount = 1L;
        if (!domumPattern.getOutputs().isEmpty())
            outputCount = domumPattern.getOutputs().get(0).amount();

        craftQueue.add(new PendingDomumCraft(
                domumPattern.getTargetStack(),
                extractedMaterials,
                (int) Math.max(1L, outputCount)
        ));

        // v1.8.0 — Stats: Domum blocks produced for the linked builder's colony.
        if (linkedBuilderPos != null)
        {
            com.minecolonies.api.colony.IColony _statCol =
                    com.minecolonies.api.colony.IColonyManager.getInstance()
                            .getClosestColony(level, linkedBuilderPos);
            if (_statCol != null)
                ColonyLinkStats.recordDomumCrafts(_statCol, linkedBuilderPos, (int) Math.max(1L, outputCount));
        }

        // Persist the queue now (materials already pulled by AE2).
        setChanged();
        return true;
    }

    /**
     * AE2 asks whether this medium can accept a craft. We report busy while outputs
     * remain to be injected (ME full / network down) or the queue has reached its bound
     * — clean back-pressure.
     */
    @Override
    public boolean isBusy()
    {
        if (level == null || level.isClientSide()) return true;
        // Uninjected outputs => drain first, accept no new work.
        if (!pendingOutputs.isEmpty()) return true;
        return craftQueue.size() >= ColonyLinkConfig.REDIRECTOR_CRAFT_QUEUE_MAX.get();
    }

    // IActionHost

    @Override
    public IGridNode getActionableNode() { return gridNode.getNode(); }

    // Ticker: run crafts + drain pending outputs

    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> serverType)
    {
        if (level.isClientSide())
        {
            // Pass2 (Fix 3) — client ticker: sawdust particles ONLY. No server
            // logic (crafts, sync, linger) may run here. It keys off the WORKING
            // blockstate property, which is natively synced by setBlockAndUpdate(),
            // so it does not depend on the BE data packet (Fix 1) to work.
            return (lvl, pos, blockState, be) ->
            {
                if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
                    redirector.clientTickSawdust(lvl, pos, blockState);
            };
        }
        return (lvl, pos, blockState, be) ->
        {
            if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
            {
                redirector.onFirstTick(lvl, pos);
                // 1) Drain pending outputs first.
                redirector.flushPendingOutputs(lvl);
                // 2) Then run at most one queued craft.
                redirector.processOneCraft(lvl);
                // 3) Recompute display state + sync to client.
                redirector.updateShuffleAndSync(lvl);
            }
        };
    }

    /**
     * Pass2 (Fix 3) — client ticker body: emits spruce-plank "sawdust" crumbs
     * around the blade/item contact point while the block is visually working
     * (which includes the 5-tick linger, since it drives the WORKING property).
     *
     * <p>Dedicated-server safety: only common-side classes are used here
     * ({@link BlockParticleOption}, {@link ParticleTypes}, {@link Level#addParticle}) —
     * no {@code @OnlyIn}, no client-only imports. On the logical server the method
     * is simply never reached (client-only ticker branch).
     */
    private void clientTickSawdust(Level lvl, BlockPos pos, BlockState blockState)
    {
        if (!blockState.hasProperty(ColonyLinkRedirectorBlock.WORKING)
                || !blockState.getValue(ColonyLinkRedirectorBlock.WORKING))
            return;

        RandomSource r = lvl.getRandom();

        // Stretch the jitter along the local slide axis so the dust follows the
        // cut line: the blade plane is local Z=8 and items slide along local X.
        // Facing N/S (axis Z) -> cut line on world X; facing E/W -> on world Z.
        boolean cutAlongX = !blockState.hasProperty(ColonyLinkRedirectorBlock.FACING)
                || blockState.getValue(ColonyLinkRedirectorBlock.FACING).getAxis() == Direction.Axis.Z;
        double longSpread  = 0.30;  // ±0.15 along the cut line
        double shortSpread = 0.12;  // ±0.06 across it

        // 1 guaranteed + 1 at 50% per tick — moderate, "contained" burst.
        int count = 1 + (r.nextBoolean() ? 1 : 0);
        for (int i = 0; i < count; i++)
        {
            double jx = (r.nextDouble() - 0.5) * (cutAlongX ? longSpread : shortSpread);
            double jz = (r.nextDouble() - 0.5) * (cutAlongX ? shortSpread : longSpread);
            double x = pos.getX() + 0.5 + jx;
            double y = pos.getY() + 0.72 + (r.nextDouble() - 0.5) * 0.10; // blade/item contact height
            double z = pos.getZ() + 0.5 + jz;
            double vx = (r.nextDouble() - 0.5) * 0.04;
            double vy = 0.01 + r.nextDouble() * 0.02;   // tiny upward puff, gravity does the rest
            double vz = (r.nextDouble() - 0.5) * 0.04;
            lvl.addParticle(SAWDUST, x, y, z, vx, vy, vz);
        }
    }

    /**
     * Updates the display state (working / renderStack / shuffleProgress) and pushes it
     * to the client. Called each server tick.
     *
     * Sync note: we resync not only on the working transition but also when a new item is
     * picked (shuffle reset at 5) and when going idle — otherwise renderStack would never
     * reach the client during a continuous craft and the shuffle effect would be invisible.
     * Cost (pass 2): one block update per processed craft (capture) plus at most one per
     * 5 ticks (shuffle) per active Redirector — light tag, see getUpdateTag().
     */
    private void updateShuffleAndSync(Level level)
    {
        if (level == null || level.isClientSide()) return;

        // Pass3 (PartA) — refresh the AE-job signal first (throttled internally
        // to one grid query per 5 ticks) so a job detected this tick feeds newWorking.
        refreshAeJobActive();

        // Pass2 (Fix 2) — decrement BEFORE computing newWorking: processOneCraft()
        // ran just before in the same tick and arms linger=WORKING_LINGER_TICKS, so a
        // craft processed this tick yields exactly 5 ticks of visible activity after
        // the LAST craft (single 1-tick crafts no longer flicker or stay dark).
        if (workingLinger > 0) workingLinger--;

        boolean newWorking = !craftQueue.isEmpty() || !pendingOutputs.isEmpty()
                || workingLinger > 0 || aeJobActive;
        boolean needSync = renderStackDirty;   // fresh capture from processOneCraft()
        renderStackDirty = false;

        if (newWorking != this.working)
        {
            this.working = newWorking;
            needSync = true;

            // Toggle the WORKING blockstate property → _off/_on model. ONLY on an
            // idle↔busy transition: one setBlockAndUpdate per change (no spam), which
            // already notifies clients for the re-render.
            BlockState oldState = level.getBlockState(worldPosition);
            if (oldState.hasProperty(ColonyLinkRedirectorBlock.WORKING))
                level.setBlockAndUpdate(worldPosition,
                        oldState.setValue(ColonyLinkRedirectorBlock.WORKING, newWorking));
        }

        if (this.working)
        {
            // Pass3 (PartA) — render fallback: after a relog/chunk reload mid-batch
            // (or during a gap before the first craft) the server-side renderStack can be
            // EMPTY while the job still runs. Repopulate it with the requested output.
            // The capture done in processOneCraft() has priority — never overwritten here.
            if (this.renderStack.isEmpty() && aeJobActive && !aeJobStack.isEmpty())
            {
                this.renderStack = aeJobStack.copy();
                needSync = true;
            }

            this.shuffleProgress++;
            if (this.shuffleProgress >= 5)
            {
                this.shuffleProgress = 0;
                // Pass2 (Fix 2) — random re-pick only while the queue is still
                // populated. When the queue is drained but the linger is running,
                // keep the stack captured at processing time (never overwrite it
                // with EMPTY while working is true).
                if (!craftQueue.isEmpty())
                {
                    updateRenderStack();
                    needSync = true; // new item picked → resync client
                }
            }
        }
        else
        {
            if (!this.renderStack.isEmpty()) needSync = true;
            this.renderStack = ItemStack.EMPTY;
            this.shuffleProgress = 0;
        }

        if (needSync)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    /**
     * Pass3 (PartA) — visual-only signal: does the grid have a crafting job in
     * flight that still requests one of OUR patterns' outputs?
     *
     * <p>API (recon, AE2 19.2.x decompiled) — DUAL signal:
     * (1) {@code ICraftingService.isRequesting(AEKey)}: O(1) lookup into
     * {@code CraftingService.currentlyCrafting}, rebuilt on change from every CPU
     * cluster's {@code craftingLogic.getAllWaitingFor()} — keys pushed to providers
     * and not yet delivered back (fills at pushPattern, drains on insert, cleared on
     * job end/cancel). (2) {@code ICraftingCPU.getJobStatus().crafting()}: the job's
     * requested FINAL output, non-null for the job's whole lifetime — covers the
     * gaps where nothing of ours is in flight (input sub-crafts, scheduling stalls).
     * Grid-wide semantics: if two Redirectors provide the same pattern, both light
     * up during the batch (accepted limit, v1).
     *
     * <p>Throttle: one grid query burst per {@link #AE_JOB_CHECK_INTERVAL} ticks, with
     * early exit on the first requested key. Never called client-side (server ticker).
     * Zero impact on the real craft pipeline (isBusy/pushPattern/processOneCraft).
     */
    private void refreshAeJobActive()
    {
        if (aeJobCheckCooldown > 0) { aeJobCheckCooldown--; return; }
        aeJobCheckCooldown = AE_JOB_CHECK_INTERVAL;

        aeJobActive = false;
        aeJobStack = ItemStack.EMPTY;

        IGridNode node = gridNode.getNode();
        if (node == null || !node.isActive()) return;   // no grid / power off -> stays false

        Set<AEItemKey> keys = this.patternOutputKeys;
        if (keys == null)
            keys = this.patternOutputKeys = buildPatternOutputKeys();
        if (keys.isEmpty()) return;                      // empty buffer -> nothing to ask

        var craftingService = node.getGrid().getCraftingService();

        // Signal 1 — in-flight pushes. isRequesting(key) is an O(1) lookup into the
        // union of every CPU's job.waitingFor: keys whose patterns were PUSHED to
        // providers and not delivered back yet (recon: waitingFor fills at
        // pushPattern time, drains on insert). Also catches our outputs when they
        // are intermediates of a bigger job, on whichever provider was chosen.
        for (AEItemKey key : keys)
        {
            if (craftingService.isRequesting(key))
            {
                aeJobActive = true;
                aeJobStack = key.toStack(1);
                return;                                  // early exit at the first hit
            }
        }

        // Signal 2 — whole-job window. waitingFor goes quiet during input sub-crafts
        // or scheduling stalls (nothing of ours in flight), which would flicker the
        // animation mid-job. For direct orders of our outputs, the CPU's job status
        // carries the requested FINAL output for the job's entire lifetime — the
        // "first to last item" window. Few CPUs per grid -> cheap sweep, early exit.
        for (ICraftingCPU cpu : craftingService.getCpus())
        {
            CraftingJobStatus status = cpu.getJobStatus();
            if (status == null) continue;
            if (status.crafting().what() instanceof AEItemKey itemKey && keys.contains(itemKey))
            {
                aeJobActive = true;
                aeJobStack = itemKey.toStack(1);
                return;
            }
        }
    }

    /**
     * Pass3 (PartA) — primary-output keys of the buffer's patterns, built through
     * the EXACT ICraftingProvider path already exposed to AE2 (getAvailablePatterns()
     * -> DomumPatternDetails.getPrimaryOutput()): no hand-rolled re-decoding. Called
     * lazily by refreshAeJobActive() and memoised until the buffer changes.
     */
    private Set<AEItemKey> buildPatternOutputKeys()
    {
        Set<AEItemKey> keys = new HashSet<>();
        for (IPatternDetails details : getAvailablePatterns())
        {
            if (details.getPrimaryOutput().what() instanceof AEItemKey itemKey)
                keys.add(itemKey);
        }
        return keys;
    }

    /**
     * Picks a random craft from the queue and exposes its targetStack (defensive copy)
     * for the client-side 3D render. Uses level.getRandom(): the BlockEntity has no own
     * random field.
     */
    private void updateRenderStack()
    {
        if (craftQueue.isEmpty() || level == null)
        {
            this.renderStack = ItemStack.EMPTY;
            return;
        }

        // Snapshot the concurrent queue for indexing.
        List<PendingDomumCraft> temp = new ArrayList<>(craftQueue);
        if (temp.isEmpty())
        {
            this.renderStack = ItemStack.EMPTY;
            return;
        }

        int idx = level.getRandom().nextInt(temp.size());
        this.renderStack = temp.get(idx).targetStack().copy();
    }

    /**
     * Retries injecting already-produced but blocked outputs into the ME. Called each
     * tick. While the ME refuses, the items stay pending (persisted in NBT) — never lost.
     */
    private void flushPendingOutputs(Level level)
    {
        if (pendingOutputs.isEmpty()) return;

        IGridNode node = gridNode.getNode();
        if (node == null || !node.isActive()) return; // network down: keep everything

        var inventory = node.getGrid().getStorageService().getInventory();
        IActionSource src = IActionSource.ofMachine(this);

        boolean changed = false;
        Iterator<ItemStack> it = pendingOutputs.iterator();
        while (it.hasNext())
        {
            ItemStack out = it.next();
            if (out.isEmpty()) { it.remove(); changed = true; continue; }

            AEItemKey key = AEItemKey.of(out);
            if (key == null)
            {
                // Should never happen for a real Domum block.
                ColonyLink.LOGGER.error("[DomumPattern] Pending output has no AE key, dropping 1 entry: {}",
                        out.getDisplayName().getString());
                it.remove();
                changed = true;
                continue;
            }

            long inserted = inventory.insert(key, out.getCount(), Actionable.MODULATE, src);
            if (inserted >= out.getCount())
            {
                it.remove();
                changed = true;
            }
            else
            {
                if (inserted > 0)
                {
                    out.shrink((int) inserted);
                    changed = true;
                }
                // ME full for the rest: stop, retry next tick.
                break;
            }
        }

        if (changed) setChanged();
    }

    /**
     * Runs at most one queued Domum craft. Called each tick from the server thread (one
     * craft per tick).
     *
     * v1.4.9: no new craft runs while previous outputs await injection (avoids pile-up),
     * and any output remainder goes to pendingOutputs (persisted + retried) instead of
     * being voided.
     */
    private void processOneCraft(Level level)
    {
        if (craftQueue.isEmpty()) return;
        // Let the ME drain first: do not pile up outputs.
        if (!pendingOutputs.isEmpty()) return;

        PendingDomumCraft pending = craftQueue.poll();
        if (pending == null) return;
        setChanged();

        ItemStack targetStack = pending.targetStack();
        List<ItemStack> materials = pending.materials();

        if (targetStack.isEmpty() || !DomumCraftHandler.isDomumItem(targetStack))
        {
            ColonyLink.LOGGER.warn("[DomumPattern] processOneCraft: target is not a Domum item, skipping.");
            return;
        }

        // Pass2 (Fix 2) — this craft is effectively being processed: arm the
        // visual linger and show WHAT is being cut right now. Placed after the
        // validity guards (error skips must not light the block), before the
        // build/inject so even 1-tick crafts are observable client-side.
        this.workingLinger = WORKING_LINGER_TICKS;
        this.renderStack = targetStack.copy();
        this.renderStackDirty = true;

        // Materials were already extracted from the ME by AE2 before pushPattern().
        ItemStack result = buildDomumResult(targetStack, materials, level.registryAccess());

        if (result.isEmpty())
        {
            // Pathological case: pattern validated but rebuild impossible. v1.6.2 — zero
            // voiding: AE2 already pulled the materials before pushPattern. Rather than lose
            // them, return them TO THE ME via pendingOutputs (reinjected each tick by
            // flushPendingOutputs, NBT-persisted). No double-counting: extracted only once.
            ColonyLink.LOGGER.error("[DomumPattern] Failed to build Domum result for: {} — returning {} raw material stack(s) to the ME network (zero voiding)",
                    targetStack.getDisplayName().getString(), materials.size());
            for (ItemStack mat : materials)
                if (!mat.isEmpty()) pendingOutputs.add(mat.copy());
            setChanged();
            return;
        }

        // Apply the recipe count (e.g. shingles=4, panels=4, framed panes=6)
        result.setCount(pending.outputCount());

        // Domum crafts always go into the ME. Any remainder is kept.
        ItemStack leftover = injectIntoME(result, level);
        if (!leftover.isEmpty())
            pendingOutputs.add(leftover);

        setChanged();
    }

    /**
     * Builds the resulting Domum ItemStack from the supplied materials. Mirrors
     * DomumCraftHandler.handleDomumCraft() but without ME extraction (already done by AE2).
     */
    private ItemStack buildDomumResult(ItemStack targetStack, List<ItemStack> materials,
                                       HolderLookup.Provider provider)
    {
        if (!(targetStack.getItem() instanceof BlockItem bi)) return ItemStack.EMPTY;
        Block block = bi.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock texturedBlock)) return ItemStack.EMPTY;

        MaterialTextureData textureData = MaterialTextureData.readFromItemStack(targetStack);
        Map<ResourceLocation, Block> components = textureData.getTexturedComponents();

        // Rebuild MaterialTextureData from the known components.
        MaterialTextureData.Builder builder = MaterialTextureData.builder();
        for (IMateriallyTexturedBlockComponent component : texturedBlock.getComponents())
        {
            Block materialBlock = components.get(component.getId());
            if (materialBlock != null)
                builder.setComponent(component.getId(), materialBlock);
            else if (!component.isOptional())
            {
                ColonyLink.LOGGER.warn("[DomumPattern] Required component missing: {}", component.getId());
                return ItemStack.EMPTY;
            }
        }

        // Copy the whole ItemStack (preserves BlockItemStateProperties + all DataComponents)
        ItemStack result = targetStack.copy();
        builder.writeToItemStack(result);

        return result;
    }

    /**
     * Tries to reinject an ItemStack into the Redirector's ME network.
     *
     * v1.4.9: returns the uninserted REMAINDER (instead of voiding).
     *   - network unavailable → returns the whole stack (to keep/retry)
     *   - partial insert      → returns what was not accepted
     *   - full insert         → returns ItemStack.EMPTY
     */
    private ItemStack injectIntoME(ItemStack stack, Level level)
    {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        IGridNode node = gridNode.getNode();
        if (node == null || !node.isActive()) return stack.copy();

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) return ItemStack.EMPTY; // not insertable (never for a real item)

        IActionSource src = IActionSource.ofMachine(this);

        long inserted = node.getGrid().getStorageService()
                .getInventory()
                .insert(key, stack.getCount(), Actionable.MODULATE, src);

        long remainder = stack.getCount() - inserted;
        if (remainder <= 0) return ItemStack.EMPTY;
        return stack.copyWithCount((int) remainder);
    }

    /**
     * Notifies AE2 that the pattern list changed (buffer modified). AE2 rescans
     * getAvailablePatterns() on the next cycle.
     */
    public void notifyPatternChange()
    {
        // Pass3 — the memoised output-key list follows the buffer content. All
        // buffer mutations funnel through onContentsChanged() -> here, so this is
        // the single invalidation point (client-side calls just clear an unused cache).
        patternOutputKeys = null;
        if (level != null && !level.isClientSide())
            ICraftingProvider.requestUpdate(gridNode);
    }

    // Lifecycle

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (level != null && !level.isClientSide())
        {
            gridNode.create(level, worldPosition);
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
            // 1.7.0-beta.4 — index by stable UUID; persist a freshly generated one.
            RedirectorRegistry.register(redirectorId, this);
            if (redirectorIdUnsaved) setChanged();
        }
    }

    /**
     * 1.7.0-beta.4 — Clearable: block movers (Sable SubLevelAssemblyHelper.moveBlocks,
     * Create) save the NBT, then call Clearable.tryClear on the old block entity before
     * removing the block, so its contents exist only in the copy. Also used by vanilla
     * /setblock, like any container.
     */
    @Override
    public void clearContent()
    {
        for (int slot = 0; slot < buffer.getSlots(); slot++)
            buffer.setStackInSlot(slot, ItemStack.EMPTY);
        warehouseCardSlot.setStackInSlot(0, ItemStack.EMPTY);
        // 1.7.0-beta.5 (audit #13/#46) — the Clearable contract covers EVERYTHING the
        // block entity holds: the materials AE2 already pulled for the queued crafts and
        // the outputs waiting to be re-injected live here too (persisted in the NBT the
        // mover copied). Leaving them behind would let onRemove() drop a second copy.
        craftQueue.clear();
        pendingOutputs.clear();
    }

    /**
     * 1.7.0-beta.5 (audit #12/#13) — everything this block entity holds OUTSIDE its item
     * handlers, which a normal break would otherwise void:
     *   - craftQueue : the materials AE2 already extracted from the network for each
     *                  queued Domum craft (pushPattern accepted them, they exist nowhere else);
     *   - pendingOutputs : outputs already produced (or raw materials of a failed build)
     *                  waiting for the ME to accept them back.
     * The stacks are handed over (copies) and both collections are emptied, so a later
     * save of this block entity cannot duplicate them. The target reference stack of a
     * queued craft is a pattern template, not a physical item, and is not included.
     * Server thread only.
     */
    public List<ItemStack> drainUnstoredContents()
    {
        List<ItemStack> out = new ArrayList<>();
        for (PendingDomumCraft p : craftQueue)
            for (ItemStack mat : p.materials())
                if (!mat.isEmpty()) out.add(mat.copy());
        for (ItemStack o : pendingOutputs)
            if (!o.isEmpty()) out.add(o.copy());
        craftQueue.clear();
        pendingOutputs.clear();
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        gridNode.destroy();
        if (level != null && !level.isClientSide()) RedirectorRegistry.unregister(redirectorId, this);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        gridNode.destroy();
        if (level != null && !level.isClientSide()) RedirectorRegistry.unregister(redirectorId, this);
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir)
    {
        // Pass3 (PartB) — defence in depth with setExposedOnSides(): the top
        // face is pure glass, never hand the node out for UP.
        return dir == Direction.UP ? null : gridNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir)
    {
        // Pass4 (PartC) — without this override the IInWorldGridNodeHost default
        // is AECableType.GLASS (IInWorldGridNodeHost.java:19-21), which made adjacent
        // smart cables degrade their connection arm to a thin glass joint. Standard
        // AE2 machines return SMART (AENetworkedBlockEntity, PatternProviderBlockEntity).
        // UP is pure glass with no node exposed (passe 3): NONE = isValid() false,
        // no connection arm at all.
        return dir == Direction.UP ? AECableType.NONE : AECableType.SMART;
    }

    private boolean firstTickDone = false;

    private void onFirstTick(Level level, BlockPos pos)
    {
        if (firstTickDone) return;
        firstTickDone = true;
        for (Direction dir : Direction.values())
            level.updateNeighborsAt(pos.relative(dir), level.getBlockState(pos.relative(dir)).getBlock());
        // Initial pattern announcement to the network.
        notifyPatternChange();
    }

    private void onGridStateChanged()
    {
        if (level == null) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    // Warehouse helpers

    public boolean hasWarehouseCard()
    { return !warehouseCardSlot.getStackInSlot(0).isEmpty(); }

    public boolean isWarehousePriority()
    {
        if (level != null && !level.isClientSide()) return warehousePriority;
        return warehousePriorityClientCache;
    }

    public void toggleWarehousePriority()
    {
        warehousePriority = !warehousePriority;
        markDirtyAndUpdate();
    }

    public boolean isAe2Active()
    {
        if (level != null && !level.isClientSide())
            return gridNode.getNode() != null && gridNode.getNode().isActive();
        return ae2ActiveClientCache;
    }

    public boolean isAdjacentToController() { return isAe2Active(); }

    // MineColonies building handlers

    public List<IItemHandler> getBuildingHandlers()
    {
        List<IItemHandler> handlers = new ArrayList<>();
        if (targetInventoryPos == null || level == null) return handlers;

        try
        {
            var colony = com.minecolonies.api.colony.IColonyManager.getInstance()
                    .getClosestColony(level, targetInventoryPos);
            if (colony == null) return handlers;

            for (var b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (!b.getPosition().equals(targetInventoryPos)) continue;
                for (BlockPos containerPos : b.getContainers())
                {
                    IItemHandler h = level.getCapability(
                            Capabilities.ItemHandler.BLOCK, containerPos, null);
                    if (h != null) handlers.add(h);
                }
                break;
            }
        }
        catch (Exception ignored) {}

        return handlers;
    }

    public boolean isTargetInventoryFull()
    {
        if (targetInventoryPos == null || level == null) return false;
        List<IItemHandler> handlers = getBuildingHandlers();
        if (handlers.isEmpty()) return false;
        for (IItemHandler handler : handlers)
            if (hasSpace(handler)) return false;
        return true;
    }

    private static boolean hasSpace(IItemHandler handler)
    {
        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.isEmpty()) return true;
            if (inSlot.getCount() < inSlot.getMaxStackSize()) return true;
        }
        return false;
    }

    public boolean insertItem(ItemStack stack)
    {
        if (targetInventoryPos == null || level == null) return false;

        List<IItemHandler> handlers = getBuildingHandlers();
        if (handlers.isEmpty())
        {
            IItemHandler direct = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, targetInventoryPos, null);
            if (direct == null) return false;
            handlers = List.of(direct);
        }

        ItemStack remainder = stack.copy();
        for (IItemHandler handler : handlers)
        {
            for (int i = 0; i < handler.getSlots() && !remainder.isEmpty(); i++)
                remainder = handler.insertItem(i, remainder, false);
            if (remainder.isEmpty()) return true;
        }

        if (!remainder.isEmpty()) { setState(RedirectorState.STANDBY); return false; }
        return true;
    }

    // State

    public void updateState()
    {
        if (!isAdjacentToController())
            setState(RedirectorState.NO_CONTROLLER);
        else if (targetInventoryPos == null)
            setState(RedirectorState.NOT_LINKED);
        else if (isTargetInventoryFull())
            setState(RedirectorState.STANDBY);
        else
            setState(RedirectorState.LINKED);

        if ((linkedBuilderName == null || linkedBuilderName.equals("N/A"))
                && linkedBuilderPos != null && level != null && !level.isClientSide())
        {
            try
            {
                var colony = com.minecolonies.api.colony.IColonyManager.getInstance()
                        .getClosestColony(level, linkedBuilderPos);
                if (colony != null)
                {
                    for (var b : colony.getServerBuildingManager().getBuildings().values())
                    {
                        if (b.getPosition().equals(linkedBuilderPos)
                                && b instanceof com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder bb
                                && !bb.getAllAssignedCitizen().isEmpty())
                        {
                            linkedBuilderName = bb.getAllAssignedCitizen().iterator().next().getName();
                            setChanged();
                            break;
                        }
                    }
                }
            }
            catch (Exception ignored) {}
        }
    }

    // Getters / Setters

    public BlockPos getTargetInventoryPos() { return targetInventoryPos; }
    public void setTargetInventoryPos(BlockPos pos) { this.targetInventoryPos = pos; markDirtyAndUpdate(); }

    public BlockPos getLinkedBuilderPos() { return linkedBuilderPos; }
    public void setLinkedBuilderPos(BlockPos pos) { this.linkedBuilderPos = pos; markDirtyAndUpdate(); }

    public String getLinkedBuilderName() { return linkedBuilderName; }
    public void setLinkedBuilderName(String name)
    { this.linkedBuilderName = (name != null && !name.isBlank()) ? name : "N/A"; markDirtyAndUpdate(); }

    public RedirectorState getState() { return state; }

    public void setState(RedirectorState state)
    {
        // Pass2 — no-op on unchanged state. updateState() is invoked from
        // repeated paths (Jade server data ~4/s while the block is looked at,
        // terminal GUI sync ticker) and, now that getUpdatePacket() is live,
        // every markDirtyAndUpdate() ships a real BE packet: only sync actual
        // transitions.
        if (this.state == state) return;
        this.state = state;
        markDirtyAndUpdate();
    }

    public boolean isLinked() { return targetInventoryPos != null; }

    public IManagedGridNode getManagedGridNode() { return gridNode; }

    // Render accessors (read by the BlockEntityRenderer)

    /** @return true if a craft is running, outputs await injection, or the 5-tick visual
     *  linger is still running (pass 2). */
    public boolean isWorking() { return this.working; }

    /** @return the current ItemStack to render in 3D (server copy, EMPTY if idle). */
    public ItemStack getRenderStack() { return this.renderStack; }

    /** @return the shuffle progress (0→5) for the item's X slide. */
    public int getShuffleProgress() { return this.shuffleProgress; }

    private void markDirtyAndUpdate()
    {
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // Client sync

    /**
     * Pass2 (Fix 1) — without this override, {@code level.sendBlockUpdated()}
     * never ships BE data to clients mid-game: the default implementation returns
     * {@code null}, so every sendBlockUpdated() in this class was a data no-op and
     * renderStack/working only reached clients on chunk (re)load. The packet
     * serialises via {@link #getUpdateTag}: a handful of primitives plus ONE
     * ItemStack — well under a kilobyte per send.
     */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Pass2 (Fix 1) — client-side receipt of the packet above. Routes through
     * {@link #handleUpdateTag} (same path as the chunk-load sync — simple
     * idempotent reads) instead of the NeoForge default, which calls
     * {@code loadWithComponents()} and would skip the client mirror fields
     * (working / renderStack / ae2Active / state caches).
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.Provider lookupProvider)
    {
        CompoundTag tag = pkt.getTag();
        if (tag != null && !tag.isEmpty())
            handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider)
    {
        CompoundTag tag = super.getUpdateTag(provider);
        boolean ae2Active = gridNode.getNode() != null && gridNode.getNode().isActive();
        tag.putBoolean("ae2_active", ae2Active);
        tag.putString("state", state.name());
        tag.putBoolean("has_warehouse_card", hasWarehouseCard());
        tag.putBoolean("warehouse_priority", warehousePriority);
        if (targetInventoryPos != null)
        {
            tag.putInt("target_x", targetInventoryPos.getX());
            tag.putInt("target_y", targetInventoryPos.getY());
            tag.putInt("target_z", targetInventoryPos.getZ());
        }
        if (linkedBuilderPos != null)
        {
            tag.putInt("builder_x", linkedBuilderPos.getX());
            tag.putInt("builder_y", linkedBuilderPos.getY());
            tag.putInt("builder_z", linkedBuilderPos.getZ());
        }
        tag.putString("linked_builder_name", linkedBuilderName);

        // Animated display state
        tag.putBoolean("working", this.working);
        tag.put("renderStack", this.renderStack.saveOptional(provider));
        tag.putInt("shuffleProgress", this.shuffleProgress);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("ae2_active"))    ae2ActiveClientCache = tag.getBoolean("ae2_active");
        if (tag.contains("state"))
        {
            try { state = RedirectorState.valueOf(tag.getString("state")); }
            catch (Exception ignored) {}
        }
        if (tag.contains("warehouse_priority")) warehousePriorityClientCache = tag.getBoolean("warehouse_priority");
        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
        if (tag.contains("linked_builder_name")) linkedBuilderName = tag.getString("linked_builder_name");

        // Animated display state
        this.working = tag.getBoolean("working");
        this.renderStack = tag.contains("renderStack")
                ? ItemStack.parseOptional(provider, tag.getCompound("renderStack"))
                : ItemStack.EMPTY;
        this.shuffleProgress = tag.getInt("shuffleProgress");
    }

    // NBT

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);
        gridNode.saveToNBT(tag);
        tag.putUUID("RedirectorId", redirectorId); // 1.7.0-beta.4
        redirectorIdUnsaved = false;
        tag.put("buffer", buffer.serializeNBT(provider));
        tag.put("warehouse_card_slot", warehouseCardSlot.serializeNBT(provider));
        tag.putBoolean("warehouse_priority", warehousePriority);

        // v1.4.9 — persist the craft queue and pending outputs. Essential: these
        // ItemStacks are materials already pulled from the AE2 network; losing them on
        // chunk unload = voiding.
        ListTag queueTag = new ListTag();
        for (PendingDomumCraft p : craftQueue)
            queueTag.add(savePending(p, provider));
        tag.put("craft_queue", queueTag);

        ListTag pendingTag = new ListTag();
        // Defensive copy (the ticker also runs on the server thread).
        for (ItemStack out : new ArrayList<>(pendingOutputs))
            if (!out.isEmpty()) pendingTag.add(out.saveOptional(provider));
        tag.put("pending_outputs", pendingTag);

        if (targetInventoryPos != null)
        {
            tag.putInt("target_x", targetInventoryPos.getX());
            tag.putInt("target_y", targetInventoryPos.getY());
            tag.putInt("target_z", targetInventoryPos.getZ());
        }
        if (linkedBuilderPos != null)
        {
            tag.putInt("builder_x", linkedBuilderPos.getX());
            tag.putInt("builder_y", linkedBuilderPos.getY());
            tag.putInt("builder_z", linkedBuilderPos.getZ());
        }
        tag.putString("linked_builder_name", linkedBuilderName);
        tag.putString("state", state.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);
        gridNode.loadFromNBT(tag);
        // 1.7.0-beta.4 — absent on Redirectors saved before beta.4: keep the generated one.
        if (tag.hasUUID("RedirectorId"))
        {
            UUID loaded = tag.getUUID("RedirectorId");
            // A live block entity receiving NBT (level already set): this is how Sable's
            // moveBlocks fills the copy, AFTER its onLoad registered the throwaway UUID.
            // Swap the registration to the loaded UUID. Chunk loading from disk runs with
            // level == null and registers in onLoad instead.
            boolean live = level != null && !level.isClientSide() && !isRemoved();
            if (live && !loaded.equals(redirectorId)) RedirectorRegistry.unregister(redirectorId, this);
            redirectorId = loaded;
            redirectorIdUnsaved = false;
            if (live) RedirectorRegistry.register(redirectorId, this);
        }
        if (tag.contains("buffer"))          buffer.deserializeNBT(provider, tag.getCompound("buffer"));
        if (tag.contains("warehouse_card_slot")) warehouseCardSlot.deserializeNBT(provider, tag.getCompound("warehouse_card_slot"));
        if (tag.contains("warehouse_priority")) warehousePriority = tag.getBoolean("warehouse_priority");

        // v1.4.9 — restore the craft queue and pending outputs.
        craftQueue.clear();
        if (tag.contains("craft_queue"))
        {
            ListTag queueTag = tag.getList("craft_queue", Tag.TAG_COMPOUND);
            for (int i = 0; i < queueTag.size(); i++)
            {
                PendingDomumCraft p = loadPending(queueTag.getCompound(i), provider);
                if (p != null && !p.targetStack().isEmpty())
                    craftQueue.add(p);
            }
        }

        pendingOutputs.clear();
        if (tag.contains("pending_outputs"))
        {
            ListTag pendingTag = tag.getList("pending_outputs", Tag.TAG_COMPOUND);
            for (int i = 0; i < pendingTag.size(); i++)
            {
                ItemStack out = ItemStack.parseOptional(provider, pendingTag.getCompound(i));
                if (!out.isEmpty()) pendingOutputs.add(out);
            }
        }

        if (tag.contains("target_x"))
            targetInventoryPos = new BlockPos(tag.getInt("target_x"), tag.getInt("target_y"), tag.getInt("target_z"));
        if (tag.contains("builder_x"))
            linkedBuilderPos = new BlockPos(tag.getInt("builder_x"), tag.getInt("builder_y"), tag.getInt("builder_z"));
        if (tag.contains("linked_builder_name")) linkedBuilderName = tag.getString("linked_builder_name");
        if (tag.contains("state"))
        {
            try { state = RedirectorState.valueOf(tag.getString("state")); }
            catch (Exception e) { state = RedirectorState.NO_CONTROLLER; }
        }
    }

    // NBT helpers (PendingDomumCraft)

    private static CompoundTag savePending(PendingDomumCraft p, HolderLookup.Provider provider)
    {
        CompoundTag c = new CompoundTag();
        c.put("target", p.targetStack().saveOptional(provider));
        ListTag mats = new ListTag();
        for (ItemStack m : p.materials())
            if (!m.isEmpty()) mats.add(m.saveOptional(provider));
        c.put("materials", mats);
        c.putInt("count", p.outputCount());
        return c;
    }

    @Nullable
    private static PendingDomumCraft loadPending(CompoundTag c, HolderLookup.Provider provider)
    {
        ItemStack target = ItemStack.parseOptional(provider, c.getCompound("target"));
        if (target.isEmpty()) return null;

        List<ItemStack> mats = new ArrayList<>();
        ListTag list = c.getList("materials", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            ItemStack m = ItemStack.parseOptional(provider, list.getCompound(i));
            if (!m.isEmpty()) mats.add(m);
        }

        int count = Math.max(1, c.getInt("count"));
        return new PendingDomumCraft(target, mats, count);
    }

    // PendingDomumCraft record

    /**
     * A Domum craft awaiting execution. Created in pushPattern(), consumed in
     * processOneCraft(). Persisted in NBT (v1.4.9).
     *
     * @param targetStack  the target Domum ItemStack to produce
     * @param materials    the materials already extracted from the ME by AE2
     * @param outputCount  the number of items produced by the pattern (e.g. shingles=4)
     */
    private record PendingDomumCraft(
            ItemStack targetStack,
            List<ItemStack> materials,
            int outputCount
    ) {}
}