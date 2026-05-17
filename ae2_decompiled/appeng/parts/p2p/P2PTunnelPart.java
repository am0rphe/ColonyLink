/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.p2p;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.P2PTunnelFrequencyModelData;
import appeng.core.AEConfig;
import appeng.items.tools.MemoryCardItem;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public abstract class P2PTunnelPart<T extends P2PTunnelPart<T>>
extends AEBasePart {
    private boolean output;
    private short freq;

    public P2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(this.getPowerDrainPerTick());
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    protected float getPowerDrainPerTick() {
        return 1.0f;
    }

    @Nullable
    public T getInput() {
        if (this.getFrequency() == 0) {
            return null;
        }
        IGrid grid = this.getMainNode().getGrid();
        if (grid != null) {
            P2PTunnelPart tunnel = P2PService.get(grid).getInput(this.getFrequency());
            if (this.getClass().isInstance(tunnel)) {
                return (T)tunnel;
            }
        }
        return null;
    }

    public List<T> getOutputs() {
        return this.getOutputStream().toList();
    }

    public Stream<T> getOutputStream() {
        IGrid grid;
        if (this.getMainNode().isOnline() && (grid = this.getMainNode().getGrid()) != null) {
            return P2PService.get(grid).getOutputs(this.getFrequency(), this.getClass());
        }
        return Stream.empty();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0);
        bch.addBox(3.0, 3.0, 13.0, 13.0, 13.0, 14.0);
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.setOutput(data.getBoolean("output"));
        this.freq = data.getShort("freq");
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putBoolean("output", this.isOutput());
        data.putShort("freq", this.getFrequency());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        short oldf = this.freq;
        this.freq = data.readShort();
        return c || oldf != this.freq;
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeShort((int)this.getFrequency());
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1.0f;
    }

    @Override
    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        Item item;
        ItemStack newType = P2PTunnelAttunement.getTunnelPartByTriggerItem(heldItem);
        if (!newType.isEmpty() && newType.getItem() != this.getPartItem() && (item = newType.getItem()) instanceof IPartItem) {
            IPartItem partItem = (IPartItem)item;
            boolean oldOutput = this.isOutput();
            short myFreq = this.getFrequency();
            Object tunnel = this.getHost().replacePart(partItem, this.getSide(), player, hand);
            if (!this.isClientSide() && tunnel instanceof P2PTunnelPart) {
                P2PTunnelPart newTunnel = (P2PTunnelPart)tunnel;
                newTunnel.setOutput(oldOutput);
                newTunnel.onTunnelNetworkChange();
                newTunnel.getMainNode().ifPresent(grid -> P2PService.get(grid).updateFreq(newTunnel, myFreq));
            }
            Platform.notifyBlocksOfNeighbors(this.getLevel(), this.getBlockEntity().getBlockPos());
            return true;
        }
        if (this.isClientSide() || hand == InteractionHand.OFF_HAND) {
            return false;
        }
        Item oldOutput = heldItem.getItem();
        if (oldOutput instanceof IMemoryCard) {
            IPartItem partItem;
            IMemoryCard mc = (IMemoryCard)oldOutput;
            if (InteractionUtil.isInAlternateUseMode(player)) {
                Short storedFrequency = (Short)heldItem.get(AEComponents.EXPORTED_P2P_FREQUENCY);
                short newFreq = this.getFrequency();
                boolean wasOutput = this.isOutput();
                this.setOutput(false);
                boolean needsNewFrequency = wasOutput || newFreq == 0;
                IGrid grid2 = this.getMainNode().getGrid();
                if (grid2 != null) {
                    P2PService p2p = P2PService.get(grid2);
                    if (needsNewFrequency) {
                        newFreq = p2p.newFrequency();
                    }
                    p2p.updateFreq(this, newFreq);
                }
                this.onTunnelConfigChange();
                MemoryCardItem.clearCard(heldItem);
                heldItem.set(AEComponents.EXPORTED_SETTINGS_SOURCE, (Object)this.getPartItem().asItem().getDescription());
                heldItem.applyComponents(this.exportSettings(SettingsFrom.MEMORY_CARD));
                if (needsNewFrequency) {
                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_RESET);
                } else {
                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                }
                return true;
            }
            Item p2pTunnelItem = (Item)heldItem.get(AEComponents.EXPORTED_P2P_TYPE);
            if (p2pTunnelItem instanceof IPartItem && P2PTunnelPart.class.isAssignableFrom((partItem = (IPartItem)p2pTunnelItem).getPartClass())) {
                P2PTunnelPart<T> newBus = this;
                if (newBus.getPartItem() != partItem) {
                    newBus = this.getHost().replacePart(partItem, this.getSide(), player, hand);
                }
                if (newBus instanceof P2PTunnelPart) {
                    P2PTunnelPart newTunnel = newBus;
                    newTunnel.importSettings(SettingsFrom.MEMORY_CARD, heldItem.getComponents(), player);
                }
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                return true;
            }
            mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            return false;
        }
        return false;
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        Short frequency = (Short)input.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        if (frequency != null && frequency != this.freq) {
            this.setOutput(true);
            IGrid grid = this.getMainNode().getGrid();
            if (grid != null) {
                P2PService.get(grid).updateFreq(this, frequency);
            } else {
                this.setFrequency(frequency);
                this.onTunnelNetworkChange();
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        super.exportSettings(mode, builder);
        if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_P2P_TYPE, (Object)this.getPartItem().asItem());
            if (this.freq != 0) {
                builder.set(AEComponents.EXPORTED_P2P_FREQUENCY, (Object)this.freq);
                AEColor[] colors = Platform.p2p().toColors(this.freq);
                builder.set(AEComponents.MEMORY_CARD_COLORS, (Object)new MemoryCardColors(colors[0], colors[0], colors[1], colors[1], colors[2], colors[2], colors[3], colors[3]));
            }
        }
    }

    public void onTunnelConfigChange() {
    }

    public void onTunnelNetworkChange() {
    }

    protected void deductEnergyCost(double energyTransported, PowerUnit typeTransported) {
        double costFactor = AEConfig.instance().getP2PTunnelEnergyTax();
        if (costFactor <= 0.0) {
            return;
        }
        this.getMainNode().ifPresent(grid -> {
            double tax = typeTransported.convertTo(PowerUnit.AE, energyTransported * costFactor);
            grid.getEnergyService().extractAEPower(tax, Actionable.MODULATE, PowerMultiplier.CONFIG);
        });
    }

    protected void deductTransportCost(long amountTransported, AEKeyType typeTransported) {
        double costFactor = AEConfig.instance().getP2PTunnelTransportTax();
        if (costFactor <= 0.0) {
            return;
        }
        this.getMainNode().ifPresent(grid -> {
            double operations = (double)amountTransported / (double)typeTransported.getAmountPerOperation();
            double tax = operations * costFactor;
            grid.getEnergyService().extractAEPower(tax, Actionable.MODULATE, PowerMultiplier.CONFIG);
        });
    }

    @Deprecated(forRemoval=true, since="1.21.1")
    protected void queueTunnelDrain(PowerUnit unit, double f) {
        double ae_to_tax = unit.convertTo(PowerUnit.AE, f * 0.05);
        this.getMainNode().ifPresent(grid -> grid.getEnergyService().extractAEPower(ae_to_tax, Actionable.MODULATE, PowerMultiplier.CONFIG));
    }

    public short getFrequency() {
        return this.freq;
    }

    public void setFrequency(short freq) {
        short oldf = this.freq;
        this.freq = freq;
        if (oldf != this.freq) {
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        }
    }

    public boolean isOutput() {
        return this.output;
    }

    void setOutput(boolean output) {
        this.output = output;
        this.getHost().markForSave();
    }

    @Override
    public ModelData getModelData() {
        long ret = Short.toUnsignedLong(this.getFrequency());
        if (this.isActive() && this.isPowered()) {
            ret |= 0x10000L;
        }
        return ModelData.builder().with(P2PTunnelFrequencyModelData.FREQUENCY, (Object)ret).build();
    }
}

