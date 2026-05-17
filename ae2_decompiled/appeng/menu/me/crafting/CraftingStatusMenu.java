/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentSerialization
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.crafting;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.menu.ISubMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.PacketWritable;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.crafting.CraftingCPUMenu;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class CraftingStatusMenu
extends CraftingCPUMenu
implements ISubMenu {
    private static final CraftingCpuList EMPTY_CPU_LIST = new CraftingCpuList(Collections.emptyList());
    private static final Comparator<CraftingCpuListEntry> CPU_COMPARATOR = Comparator.comparing(e -> e.name() == null).thenComparing(e -> e.name() != null ? e.name().getString() : "").thenComparingInt(CraftingCpuListEntry::serial);
    private static final String ACTION_SELECT_CPU = "selectCpu";
    public static final MenuType<CraftingStatusMenu> TYPE = MenuTypeBuilder.create(CraftingStatusMenu::new, ITerminalHost.class).build("craftingstatus");
    private final WeakHashMap<ICraftingCPU, Integer> cpuSerialMap = new WeakHashMap();
    private int nextCpuSerial = 1;
    private ImmutableSet<ICraftingCPU> lastCpuSet = ImmutableSet.of();
    private int lastUpdate = 0;
    @GuiSync(value=8)
    public CraftingCpuList cpuList = EMPTY_CPU_LIST;
    private final ITerminalHost host;
    @Nullable
    private ICraftingCPU selectedCpu = null;
    @GuiSync(value=9)
    private int selectedCpuSerial = -1;

    public CraftingStatusMenu(int id, Inventory ip, ITerminalHost host) {
        super(TYPE, id, ip, host);
        this.host = host;
        this.registerClientAction(ACTION_SELECT_CPU, Integer.class, this::selectCpu);
    }

    @Override
    public ITerminalHost getHost() {
        return this.host;
    }

    @Override
    protected void setCPU(ICraftingCPU c) {
        super.setCPU(c);
        this.selectedCpuSerial = this.getOrAssignCpuSerial(c);
    }

    @Override
    public void broadcastChanges() {
        IGrid network = this.getGrid();
        if (this.isServerSide() && network != null) {
            if (!this.lastCpuSet.equals(network.getCraftingService().getCpus()) || ++this.lastUpdate >= 20) {
                this.lastCpuSet = network.getCraftingService().getCpus();
                this.cpuList = this.createCpuList();
            }
        } else {
            this.lastUpdate = 20;
            if (!this.lastCpuSet.isEmpty()) {
                this.cpuList = EMPTY_CPU_LIST;
                this.lastCpuSet = ImmutableSet.of();
            }
        }
        if (this.selectedCpuSerial != -1 && this.cpuList.cpus().stream().noneMatch(c -> c.serial() == this.selectedCpuSerial)) {
            this.selectCpu(-1);
        }
        if (this.selectedCpuSerial == -1) {
            for (CraftingCpuListEntry cpu : this.cpuList.cpus()) {
                if (cpu.currentJob() == null) continue;
                this.selectCpu(cpu.serial());
                break;
            }
            if (this.selectedCpuSerial == -1 && !this.cpuList.cpus().isEmpty()) {
                this.selectCpu(this.cpuList.cpus().get(0).serial());
            }
        }
        super.broadcastChanges();
    }

    private CraftingCpuList createCpuList() {
        ArrayList<CraftingCpuListEntry> entries = new ArrayList<CraftingCpuListEntry>(this.lastCpuSet.size());
        for (ICraftingCPU cpu : this.lastCpuSet) {
            int serial = this.getOrAssignCpuSerial(cpu);
            CraftingJobStatus status = cpu.getJobStatus();
            float progress = 0.0f;
            if (status != null && status.totalItems() > 0L) {
                progress = (float)((double)status.progress() / (double)status.totalItems());
            }
            entries.add(new CraftingCpuListEntry(serial, cpu.getAvailableStorage(), cpu.getCoProcessors(), cpu.getName(), cpu.getSelectionMode(), status != null ? status.crafting() : null, progress, status != null ? status.elapsedTimeNanos() : 0L));
        }
        entries.sort(CPU_COMPARATOR);
        return new CraftingCpuList(entries);
    }

    private int getOrAssignCpuSerial(ICraftingCPU cpu) {
        return this.cpuSerialMap.computeIfAbsent(cpu, ignored -> this.nextCpuSerial++);
    }

    @Override
    public boolean allowConfiguration() {
        return false;
    }

    public void selectCpu(int serial) {
        if (this.isClientSide()) {
            this.selectedCpuSerial = serial;
            this.sendClientAction(ACTION_SELECT_CPU, serial);
        } else {
            ICraftingCPU newSelectedCpu = null;
            if (serial != -1) {
                for (ICraftingCPU cpu : this.lastCpuSet) {
                    if (this.cpuSerialMap.getOrDefault(cpu, -1) != serial) continue;
                    newSelectedCpu = cpu;
                    break;
                }
            }
            if (newSelectedCpu != this.selectedCpu) {
                this.setCPU(newSelectedCpu);
            }
        }
    }

    public int getSelectedCpuSerial() {
        return this.selectedCpuSerial;
    }

    public record CraftingCpuList(List<CraftingCpuListEntry> cpus) implements PacketWritable
    {
        public CraftingCpuList(RegistryFriendlyByteBuf data) {
            this(CraftingCpuList.readFromPacket(data));
        }

        private static List<CraftingCpuListEntry> readFromPacket(RegistryFriendlyByteBuf data) {
            int count = data.readInt();
            ArrayList<CraftingCpuListEntry> result = new ArrayList<CraftingCpuListEntry>(count);
            for (int i = 0; i < count; ++i) {
                result.add(CraftingCpuListEntry.readFromPacket(data));
            }
            return result;
        }

        @Override
        public void writeToPacket(RegistryFriendlyByteBuf data) {
            data.writeInt(this.cpus.size());
            for (CraftingCpuListEntry entry : this.cpus) {
                entry.writeToPacket(data);
            }
        }
    }

    public record CraftingCpuListEntry(int serial, long storage, int coProcessors, Component name, CpuSelectionMode mode, GenericStack currentJob, float progress, long elapsedTimeNanos) {
        public static CraftingCpuListEntry readFromPacket(RegistryFriendlyByteBuf data) {
            return new CraftingCpuListEntry(data.readInt(), data.readLong(), data.readInt(), data.readBoolean() ? (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode((Object)data) : null, (CpuSelectionMode)data.readEnum(CpuSelectionMode.class), GenericStack.readBuffer(data), data.readFloat(), data.readVarLong());
        }

        public void writeToPacket(RegistryFriendlyByteBuf data) {
            data.writeInt(this.serial);
            data.writeLong(this.storage);
            data.writeInt(this.coProcessors);
            data.writeBoolean(this.name != null);
            if (this.name != null) {
                ComponentSerialization.TRUSTED_STREAM_CODEC.encode((Object)data, (Object)this.name);
            }
            data.writeEnum((Enum)this.mode);
            GenericStack.writeBuffer(this.currentJob, data);
            data.writeFloat(this.progress);
            data.writeVarLong(this.elapsedTimeNanos);
        }
    }
}

