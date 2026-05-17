/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.crafting.UnsuitableCpus;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.core.AELog;
import appeng.core.network.clientbound.CraftConfirmPlanPacket;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.helpers.ICraftingGridMenu;
import appeng.me.helpers.PlayerSource;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.PacketWritable;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftingCPUCycler;
import appeng.menu.me.crafting.CraftingCPURecord;
import appeng.menu.me.crafting.CraftingPlanSummary;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CraftConfirmMenu
extends AEBaseMenu
implements ISubMenu {
    private static final String ACTION_BACK = "back";
    private static final String ACTION_CYCLE_CPU = "cycleCpu";
    private static final String ACTION_START_JOB = "startJob";
    private static final String ACTION_REPLAN = "replan";
    private static final SyncableSubmitResult NO_ERROR = new SyncableSubmitResult((ICraftingSubmitResult)null);
    public static final MenuType<CraftConfirmMenu> TYPE = MenuTypeBuilder.create(CraftConfirmMenu::new, ISubMenuHost.class).build("craftconfirm");
    private final CraftingCPUCycler cpuCycler;
    private ICraftingCPU selectedCpu;
    private AEKey whatToCraft;
    private int amount;
    private Future<ICraftingPlan> job;
    private ICraftingPlan result;
    @GuiSync(value=3)
    public boolean autoStart = false;
    @GuiSync(value=6)
    public boolean noCPU = true;
    @GuiSync(value=1)
    public long cpuBytesAvail;
    @GuiSync(value=2)
    public int cpuCoProcessors;
    @GuiSync(value=7)
    public Component cpuName;
    @GuiSync(value=8)
    public SyncableSubmitResult submitError = NO_ERROR;
    private CraftingPlanSummary plan;
    private final ISubMenuHost host;
    @Nullable
    private List<ICraftingGridMenu.AutoCraftEntry> autoCraftingQueue;
    private List<Integer> requestedSlots;

    public CraftConfirmMenu(int id, Inventory ip, ISubMenuHost te) {
        super(TYPE, id, ip, te);
        this.host = te;
        this.cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
        this.cpuCycler.setAllowNoSelection(true);
        this.registerClientAction(ACTION_BACK, this::goBack);
        this.registerClientAction(ACTION_CYCLE_CPU, Boolean.class, this::cycleSelectedCPU);
        this.registerClientAction(ACTION_START_JOB, this::startJob);
        this.registerClientAction(ACTION_REPLAN, this::replan);
    }

    public static void openWithCraftingList(@Nullable IActionHost terminal, ServerPlayer player, @Nullable MenuHostLocator locator, List<ICraftingGridMenu.AutoCraftEntry> stacksToCraft) {
        if (terminal == null || locator == null || stacksToCraft.isEmpty()) {
            return;
        }
        ICraftingGridMenu.AutoCraftEntry firstToCraft = stacksToCraft.get(0);
        List<ICraftingGridMenu.AutoCraftEntry> subsequentCrafts = stacksToCraft.subList(1, stacksToCraft.size());
        try {
            MenuOpener.open(TYPE, (Player)player, locator);
            AbstractContainerMenu abstractContainerMenu = player.containerMenu;
            if (abstractContainerMenu instanceof CraftConfirmMenu) {
                CraftConfirmMenu ccc = (CraftConfirmMenu)abstractContainerMenu;
                if (!ccc.planJob(firstToCraft.what(), firstToCraft.slots().size(), CalculationStrategy.CRAFT_LESS)) {
                    ccc.setValidMenu(false);
                    return;
                }
                ccc.autoCraftingQueue = subsequentCrafts;
                ccc.requestedSlots = firstToCraft.slots();
                ccc.broadcastChanges();
            }
        }
        catch (Throwable e) {
            AELog.info(e);
        }
    }

    public boolean planJob(AEKey what, int amount, CalculationStrategy strategy) {
        if (this.job != null) {
            this.job.cancel(true);
        }
        this.result = null;
        this.clearError();
        this.whatToCraft = what;
        this.amount = amount;
        Player player = this.getPlayer();
        IGrid grid = this.getGrid();
        if (grid == null) {
            return false;
        }
        ICraftingService cg = grid.getCraftingService();
        this.job = cg.beginCraftingCalculation(player.level(), this::getActionSrc, what, amount, strategy);
        return true;
    }

    public void cycleSelectedCPU(boolean next) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CYCLE_CPU, next);
        } else {
            this.cpuCycler.cycleCpu(next);
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.isClientSide()) {
            return;
        }
        IGrid grid = this.getGrid();
        if (grid == null) {
            this.setValidMenu(false);
            return;
        }
        this.cpuCycler.detectAndSendChanges(grid);
        super.broadcastChanges();
        if (this.job != null && this.job.isDone()) {
            try {
                this.result = this.job.get();
                if (!this.result.simulation() && this.isAutoStart()) {
                    this.startJob();
                    return;
                }
                this.plan = CraftingPlanSummary.fromJob(this.getGrid(), this.getActionSrc(), this.result);
                this.sendPacketToClient(new CraftConfirmPlanPacket(this.plan));
            }
            catch (Throwable e) {
                this.getPlayerInventory().player.sendSystemMessage((Component)Component.literal((String)("Error: " + String.valueOf(e))));
                AELog.warn("Failed to start crafting job.", e);
                this.setValidMenu(false);
                this.result = null;
            }
            this.job = null;
        }
    }

    private IGrid getGrid() {
        IActionHost h = (IActionHost)this.getTarget();
        IGridNode a = h.getActionableNode();
        return a != null ? a.getGrid() : null;
    }

    private boolean cpuMatches(ICraftingCPU c) {
        if (this.plan == null) {
            return true;
        }
        return c.getAvailableStorage() >= this.plan.getUsedBytes() && !c.isBusy();
    }

    public void startJob() {
        this.clearError();
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_START_JOB);
            return;
        }
        if (this.result != null && !this.result.simulation()) {
            ICraftingService cc = this.getGrid().getCraftingService();
            ICraftingSubmitResult submitResult = cc.submitJob(this.result, null, this.selectedCpu, true, this.getActionSrc());
            this.setAutoStart(false);
            if (submitResult.successful()) {
                if (this.autoCraftingQueue != null && !this.autoCraftingQueue.isEmpty()) {
                    CraftConfirmMenu.openWithCraftingList(this.getActionHost(), (ServerPlayer)this.getPlayer(), this.getLocator(), this.autoCraftingQueue);
                } else {
                    this.host.returnToMainMenu(this.getPlayer(), this);
                }
            } else {
                AELog.info("Couldn't submit crafting job for %dx%s: %s [Detail: %s]", new Object[]{this.result.finalOutput().amount(), this.result.finalOutput().what(), submitResult.errorCode(), submitResult.errorDetail()});
                this.submitError = new SyncableSubmitResult(submitResult);
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost)this.getTarget());
    }

    public void removed(Player player) {
        super.removed(player);
        if (this.job != null) {
            this.job.cancel(true);
            this.job = null;
        }
    }

    private void onCPUSelectionChanged(CraftingCPURecord cpuRecord, boolean cpusAvailable) {
        boolean bl = this.noCPU = !cpusAvailable;
        if (cpuRecord == null) {
            this.cpuBytesAvail = 0L;
            this.cpuCoProcessors = 0;
            this.cpuName = null;
            this.selectedCpu = null;
        } else {
            this.cpuBytesAvail = cpuRecord.getSize();
            this.cpuCoProcessors = cpuRecord.getProcessors();
            this.cpuName = cpuRecord.getName();
            this.selectedCpu = cpuRecord.getCpu();
        }
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level();
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public long getCpuAvailableBytes() {
        return this.cpuBytesAvail;
    }

    public int getCpuCoProcessors() {
        return this.cpuCoProcessors;
    }

    public Component getName() {
        return this.cpuName;
    }

    public boolean hasNoCPU() {
        return this.noCPU;
    }

    public void setJob(Future<ICraftingPlan> job) {
        this.job = job;
    }

    @Nullable
    public CraftingPlanSummary getPlan() {
        return this.plan;
    }

    public void setPlan(CraftingPlanSummary plan) {
        this.plan = plan;
    }

    public void goBack() {
        this.clearError();
        Player player = this.getPlayerInventory().player;
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (this.autoCraftingQueue != null && !this.autoCraftingQueue.isEmpty()) {
                CraftConfirmMenu.openWithCraftingList(this.getActionHost(), (ServerPlayer)this.getPlayer(), this.getLocator(), this.autoCraftingQueue);
            } else if (this.whatToCraft != null) {
                CraftAmountMenu.open(serverPlayer, this.getLocator(), this.whatToCraft, this.amount);
            } else {
                this.host.returnToMainMenu(this.getPlayer(), this);
            }
        } else {
            this.sendClientAction(ACTION_BACK);
        }
    }

    @Override
    public ISubMenuHost getHost() {
        return this.host;
    }

    public void replan() {
        this.clearError();
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_REPLAN);
            return;
        }
        if (this.whatToCraft != null) {
            if (!this.planJob(this.whatToCraft, this.amount, CalculationStrategy.CRAFT_LESS)) {
                this.goBack();
            }
        } else {
            this.goBack();
        }
    }

    public void clearError() {
        this.submitError = NO_ERROR;
    }

    public record SyncableSubmitResult(@Nullable ICraftingSubmitResult result) implements PacketWritable
    {
        public SyncableSubmitResult(RegistryFriendlyByteBuf data) {
            this(SyncableSubmitResult.readFromPacket(data));
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        private static ICraftingSubmitResult readFromPacket(RegistryFriendlyByteBuf data) {
            if (!data.readBoolean()) {
                return null;
            }
            if (data.readBoolean()) {
                return CraftingSubmitResult.successful(null);
            }
            CraftingSubmitErrorCode errorCode = (CraftingSubmitErrorCode)data.readEnum(CraftingSubmitErrorCode.class);
            return switch (errorCode) {
                case CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND -> {
                    UnsuitableCpus unsuitableCpus = new UnsuitableCpus(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    yield CraftingSubmitResult.noSuitableCpu(unsuitableCpus);
                }
                case CraftingSubmitErrorCode.MISSING_INGREDIENT -> {
                    GenericStack missingIngredient = GenericStack.readBuffer(data);
                    yield CraftingSubmitResult.missingIngredient(missingIngredient);
                }
                default -> CraftingSubmitResult.simpleError(errorCode);
            };
        }

        @Override
        public void writeToPacket(RegistryFriendlyByteBuf data) {
            if (this.result == null) {
                data.writeBoolean(false);
                return;
            }
            data.writeBoolean(true);
            data.writeBoolean(this.result.successful());
            if (!this.result.successful()) {
                CraftingSubmitErrorCode errorCode = Objects.requireNonNull(this.result.errorCode());
                data.writeEnum((Enum)errorCode);
                switch (errorCode) {
                    case NO_SUITABLE_CPU_FOUND: {
                        UnsuitableCpus unsuitableCpus = Objects.requireNonNull((UnsuitableCpus)this.result.errorDetail());
                        data.writeInt(unsuitableCpus.offline());
                        data.writeInt(unsuitableCpus.busy());
                        data.writeInt(unsuitableCpus.tooSmall());
                        data.writeInt(unsuitableCpus.excluded());
                        break;
                    }
                    case MISSING_INGREDIENT: {
                        GenericStack missingIngredient = Objects.requireNonNull((GenericStack)this.result.errorDetail());
                        GenericStack.writeBuffer(missingIngredient, data);
                    }
                }
            }
        }
    }
}

