/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.neoforged.neoforge.capabilities.BlockCapability
 */
package appeng.parts.p2p;

import appeng.api.parts.IPartItem;
import appeng.parts.PartAdjacentApi;
import appeng.parts.p2p.P2PTunnelPart;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

public abstract class CapabilityP2PTunnelPart<P extends CapabilityP2PTunnelPart<P, T>, T>
extends P2PTunnelPart<P> {
    private final PartAdjacentApi<T> adjacentCapability;
    private int accessDepth = 0;
    private final CapabilityGuard capabilityGuard = new CapabilityGuard();
    private final EmptyCapabilityGuard emptyCapabilityGuard = new EmptyCapabilityGuard();
    protected T inputHandler;
    protected T outputHandler;
    protected T emptyHandler;

    public CapabilityP2PTunnelPart(IPartItem<?> partItem, BlockCapability<T, Direction> capability) {
        super(partItem);
        this.adjacentCapability = new PartAdjacentApi<T>(this, capability, this::forwardCapabilityInvalidation);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    public T getExposedApi() {
        if (this.isOutput()) {
            return this.outputHandler;
        }
        return this.inputHandler;
    }

    protected final CapabilityGuard getAdjacentCapability() {
        ++this.accessDepth;
        return this.capabilityGuard;
    }

    protected final CapabilityGuard getInputCapability() {
        CapabilityP2PTunnelPart input = (CapabilityP2PTunnelPart)this.getInput();
        return input == null ? this.emptyCapabilityGuard : input.getAdjacentCapability();
    }

    protected void forwardCapabilityInvalidation() {
        if (this.isOutput()) {
            CapabilityP2PTunnelPart input = (CapabilityP2PTunnelPart)this.getInput();
            if (input != null) {
                input.getBlockEntity().invalidateCapabilities();
            }
        } else {
            for (CapabilityP2PTunnelPart output : this.getOutputs()) {
                output.getBlockEntity().invalidateCapabilities();
            }
        }
    }

    @Override
    public void onTunnelNetworkChange() {
        this.getBlockEntity().invalidateCapabilities();
    }

    protected class CapabilityGuard
    implements AutoCloseable {
        protected CapabilityGuard() {
        }

        public T get() {
            if (CapabilityP2PTunnelPart.this.accessDepth == 0) {
                throw new IllegalStateException("get was called after closing the wrapper");
            }
            if (CapabilityP2PTunnelPart.this.accessDepth == 1) {
                if (CapabilityP2PTunnelPart.this.isActive()) {
                    return Objects.requireNonNullElse(CapabilityP2PTunnelPart.this.adjacentCapability.find(), CapabilityP2PTunnelPart.this.emptyHandler);
                }
                return CapabilityP2PTunnelPart.this.emptyHandler;
            }
            return CapabilityP2PTunnelPart.this.emptyHandler;
        }

        @Override
        public void close() {
            if (--CapabilityP2PTunnelPart.this.accessDepth < 0) {
                throw new IllegalStateException("Close has been called multiple times");
            }
        }
    }

    protected class EmptyCapabilityGuard
    extends CapabilityGuard
    implements AutoCloseable {
        protected EmptyCapabilityGuard() {
        }

        @Override
        public void close() {
        }

        @Override
        public T get() {
            return CapabilityP2PTunnelPart.this.emptyHandler;
        }
    }
}

