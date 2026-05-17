/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.dispenser.DispenseItemBehavior
 *  net.minecraft.world.level.block.DispenserBlock
 */
package appeng.init;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.hooks.BlockToolDispenseItemBehavior;
import appeng.hooks.MatterCannonDispenseItemBehavior;
import appeng.hooks.TinyTNTDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.block.DispenserBlock;

public final class InitDispenserBehavior {
    private InitDispenserBehavior() {
    }

    public static void init() {
        DispenserBlock.registerBehavior(AEBlocks.TINY_TNT, (DispenseItemBehavior)new TinyTNTDispenseItemBehavior());
        DispenserBlock.registerBehavior(AEItems.ENTROPY_MANIPULATOR, (DispenseItemBehavior)new BlockToolDispenseItemBehavior());
        DispenserBlock.registerBehavior(AEItems.MATTER_CANNON, (DispenseItemBehavior)new MatterCannonDispenseItemBehavior());
        DispenserBlock.registerBehavior(AEItems.COLOR_APPLICATOR, (DispenseItemBehavior)new BlockToolDispenseItemBehavior());
    }
}

