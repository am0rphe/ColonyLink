/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.me.crafting;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.network.clientbound.CraftingStatusPacket;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.crafting.CraftingStatus;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class CraftingCPUMenu
extends AEBaseMenu {
    private static final String ACTION_CANCEL_CRAFTING = "cancelCrafting";
    private static final String ACTION_TOGGLE_SCHEDULING = "toggleScheduling";
    public static final MenuType<CraftingCPUMenu> TYPE = MenuTypeBuilder.create(CraftingCPUMenu::new, CraftingBlockEntity.class).withMenuTitle(craftingBlockEntity -> {
        CraftingCPUCluster cluster = craftingBlockEntity.getCluster();
        if (cluster != null && cluster.getName() != null) {
            return cluster.getName();
        }
        return Component.empty();
    }).build("craftingcpu");
    private final IncrementalUpdateHelper incrementalUpdateHelper = new IncrementalUpdateHelper();
    private final IGrid grid;
    private CraftingCPUCluster cpu = null;
    private final Consumer<AEKey> cpuChangeListener = this.incrementalUpdateHelper::addChange;
    private boolean cachedSuspend;
    @GuiSync(value=0)
    public CpuSelectionMode schedulingMode = CpuSelectionMode.ANY;
    @GuiSync(value=1)
    public boolean cantStoreItems = false;

    public CraftingCPUMenu(MenuType<?> menuType, int id, Inventory ip, Object te) {
        super(menuType, id, ip, te);
        IActionHost host = (IActionHost)(te instanceof IActionHost ? te : null);
        this.grid = host != null && host.getActionableNode() != null ? host.getActionableNode().getGrid() : null;
        if (te instanceof CraftingBlockEntity) {
            this.setCPU(((CraftingBlockEntity)te).getCluster());
        }
        if (this.getGrid() == null && this.isServerSide()) {
            this.setValidMenu(false);
        }
        this.registerClientAction(ACTION_CANCEL_CRAFTING, this::cancelCrafting);
        this.registerClientAction(ACTION_TOGGLE_SCHEDULING, this::toggleScheduling);
    }

    protected void setCPU(ICraftingCPU c) {
        if (c == this.cpu) {
            return;
        }
        if (this.cpu != null) {
            this.cpu.craftingLogic.removeListener(this.cpuChangeListener);
        }
        this.incrementalUpdateHelper.reset();
        this.cachedSuspend = false;
        if (c instanceof CraftingCPUCluster) {
            this.cpu = (CraftingCPUCluster)c;
            KeyCounter allItems = new KeyCounter();
            this.cpu.craftingLogic.getAllItems(allItems);
            for (Object2LongMap.Entry<AEKey> entry : allItems) {
                this.incrementalUpdateHelper.addChange((AEKey)entry.getKey());
            }
            this.cpu.craftingLogic.addListener(this.cpuChangeListener);
        } else {
            this.cpu = null;
            this.sendPacketToClient(new CraftingStatusPacket(this.containerId, CraftingStatus.EMPTY));
        }
    }

    public void cancelCrafting() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CANCEL_CRAFTING);
        } else if (this.cpu != null) {
            this.cpu.cancelJob();
        }
    }

    public void toggleScheduling() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_TOGGLE_SCHEDULING);
        } else if (this.cpu != null) {
            CraftingCpuLogic logic;
            logic.setJobSuspended(!(logic = this.cpu.craftingLogic).isJobSuspended());
        }
    }

    public void removed(Player player) {
        super.removed(player);
        if (this.cpu != null) {
            this.cpu.craftingLogic.removeListener(this.cpuChangeListener);
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide() && this.cpu != null) {
            this.schedulingMode = this.cpu.getSelectionMode();
            this.cantStoreItems = this.cpu.craftingLogic.isCantStoreItems();
            if (this.incrementalUpdateHelper.hasChanges() || this.cachedSuspend != this.cpu.craftingLogic.isJobSuspended()) {
                CraftingStatus status = CraftingStatus.create(this.incrementalUpdateHelper, this.cpu.craftingLogic);
                this.incrementalUpdateHelper.commitChanges();
                this.cachedSuspend = status.isSuspended();
                this.sendPacketToClient(new CraftingStatusPacket(this.containerId, status));
            }
        }
        super.broadcastChanges();
    }

    public CpuSelectionMode getSchedulingMode() {
        return this.schedulingMode;
    }

    public boolean isCantStoreItems() {
        return this.cantStoreItems;
    }

    public boolean allowConfiguration() {
        return true;
    }

    IGrid getGrid() {
        return this.grid;
    }
}

