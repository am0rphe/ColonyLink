/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.block.networking;

import appeng.api.ids.AEComponents;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class EnergyCellBlock
extends AEBaseEntityBlock<EnergyCellBlockEntity> {
    public static final int MAX_FULLNESS = 4;
    public static final IntegerProperty ENERGY_STORAGE = IntegerProperty.create((String)"fullness", (int)0, (int)4);
    private final double maxPower;
    private final double chargeRate;
    private final int priority;

    public EnergyCellBlock(double maxPower, double chargeRate, int priority) {
        super(EnergyCellBlock.glassProps());
        this.maxPower = maxPower;
        this.chargeRate = chargeRate;
        this.priority = priority;
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);
        ItemStack charged = new ItemStack((ItemLike)this, 1);
        charged.set(AEComponents.STORED_ENERGY, (Object)this.getMaxPower());
        output.accept(charged);
    }

    public double getMaxPower() {
        return this.maxPower;
    }

    public double getChargeRate() {
        return this.chargeRate;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{ENERGY_STORAGE});
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        EnergyCellBlockEntity cell = (EnergyCellBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (cell != null) {
            double currentPower = cell.getAECurrentPower();
            double maxPower = cell.getAEMaxPower();
            double fillFactor = currentPower / maxPower;
            return Mth.floor((double)(fillFactor * 14.0)) + (currentPower > 0.0 ? 1 : 0);
        }
        return 0;
    }
}

