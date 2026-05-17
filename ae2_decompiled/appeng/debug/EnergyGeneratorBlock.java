/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.level.ItemLike
 */
package appeng.debug;

import appeng.block.AEBaseEntityBlock;
import appeng.core.AEConfig;
import appeng.debug.EnergyGeneratorBlockEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;

public class EnergyGeneratorBlock
extends AEBaseEntityBlock<EnergyGeneratorBlockEntity> {
    public EnergyGeneratorBlock() {
        super(EnergyGeneratorBlock.metalProps());
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (AEConfig.instance().isDebugToolsEnabled()) {
            output.accept((ItemLike)this);
        }
    }
}

