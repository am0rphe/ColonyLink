/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.LinkedHashMultimap
 *  com.google.common.collect.Multimap
 *  net.minecraft.nbt.CompoundTag
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.service;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.events.GridPowerStatusChange;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.AELog;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.P2PTunnelPart;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class P2PService
implements IGridService,
IGridServiceProvider {
    private final IGrid myGrid;
    private final HashMap<Short, P2PTunnelPart<?>> inputs = new HashMap();
    private final Multimap<Short, P2PTunnelPart<?>> outputs = LinkedHashMultimap.create();
    private final Random frequencyGenerator;

    public static P2PService get(IGrid grid) {
        return grid.getService(P2PService.class);
    }

    public P2PService(IGrid g) {
        this.myGrid = g;
        this.frequencyGenerator = new Random(g.hashCode());
    }

    public void wakeInputTunnels() {
        ITickManager tm = this.myGrid.getTickManager();
        for (P2PTunnelPart<?> tunnel : this.inputs.values()) {
            if (!(tunnel instanceof MEP2PTunnelPart)) continue;
            tm.wakeDevice(tunnel.getGridNode());
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        Object object = node.getOwner();
        if (object instanceof P2PTunnelPart) {
            P2PTunnelPart tunnel = (P2PTunnelPart)object;
            if (tunnel instanceof MEP2PTunnelPart && !node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                return;
            }
            if (tunnel.isOutput()) {
                this.outputs.remove((Object)tunnel.getFrequency(), (Object)tunnel);
            } else {
                this.inputs.remove(tunnel.getFrequency());
            }
            this.updateTunnel(tunnel.getFrequency(), !tunnel.isOutput(), false);
        }
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag savedData) {
        Object object = node.getOwner();
        if (object instanceof P2PTunnelPart) {
            P2PTunnelPart tunnel = (P2PTunnelPart)object;
            if (tunnel instanceof MEP2PTunnelPart && !node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                return;
            }
            if (tunnel.isOutput()) {
                this.outputs.put((Object)tunnel.getFrequency(), (Object)tunnel);
            } else {
                this.inputs.put(tunnel.getFrequency(), tunnel);
            }
            this.updateTunnel(tunnel.getFrequency(), !tunnel.isOutput(), false);
        }
    }

    private void updateTunnel(short freq, boolean updateOutputs, boolean configChange) {
        P2PTunnelPart<?> in;
        if (updateOutputs) {
            for (P2PTunnelPart p : this.outputs.get((Object)freq)) {
                if (configChange) {
                    p.onTunnelConfigChange();
                }
                p.onTunnelNetworkChange();
            }
        }
        if (!updateOutputs && (in = this.inputs.get(freq)) != null) {
            if (configChange) {
                in.onTunnelConfigChange();
            }
            in.onTunnelNetworkChange();
        }
    }

    public void updateFreq(P2PTunnelPart t, short newFrequency) {
        if (this.outputs.containsValue((Object)t)) {
            this.outputs.remove((Object)t.getFrequency(), (Object)t);
        }
        if (this.inputs.containsValue(t)) {
            this.inputs.remove(t.getFrequency());
        }
        short oldFrequency = t.getFrequency();
        t.setFrequency(newFrequency);
        if (t.isOutput()) {
            this.outputs.put((Object)t.getFrequency(), (Object)t);
        } else {
            this.inputs.put(t.getFrequency(), t);
        }
        if (oldFrequency != newFrequency) {
            this.updateTunnel(oldFrequency, true, true);
            this.updateTunnel(oldFrequency, false, true);
        }
        this.updateTunnel(newFrequency, true, true);
        this.updateTunnel(newFrequency, false, true);
    }

    public short newFrequency() {
        short newFrequency;
        int cycles = 0;
        do {
            newFrequency = (short)this.frequencyGenerator.nextInt(65536);
            ++cycles;
        } while (newFrequency == 0 || this.inputs.containsKey(newFrequency));
        if (cycles > 25) {
            AELog.debug("Generating a new P2P frequency '%1$d' took %2$d cycles", newFrequency, cycles);
        }
        return newFrequency;
    }

    public <T extends P2PTunnelPart<T>> Stream<T> getOutputs(short freq, Class<T> c) {
        P2PTunnelPart<?> input = this.inputs.get(freq);
        if (!c.isInstance(input)) {
            return Stream.empty();
        }
        return this.outputs.get((Object)freq).stream().filter(c::isInstance).map(c::cast);
    }

    public P2PTunnelPart getInput(short freq) {
        return this.inputs.get(freq);
    }

    static {
        GridHelper.addGridServiceEventHandler(GridBootingStatusChange.class, P2PService.class, (service, evt) -> {
            if (!evt.isBooting()) {
                service.wakeInputTunnels();
            }
        });
        GridHelper.addGridServiceEventHandler(GridPowerStatusChange.class, P2PService.class, (service, evt) -> service.wakeInputTunnels());
    }
}

