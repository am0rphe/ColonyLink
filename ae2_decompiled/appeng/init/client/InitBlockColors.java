/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.color.block.BlockColor
 *  net.minecraft.client.color.block.BlockColors
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.init.client;

import appeng.api.util.AEColor;
import appeng.block.networking.CableBusColor;
import appeng.client.render.ColorableBlockEntityBlockColor;
import appeng.client.render.StaticBlockColor;
import appeng.core.definitions.AEBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public final class InitBlockColors {
    private InitBlockColors() {
    }

    public static void init(BlockColors blockColors) {
        blockColors.register((BlockColor)new StaticBlockColor(AEColor.TRANSPARENT), new Block[]{AEBlocks.WIRELESS_ACCESS_POINT.block()});
        blockColors.register((BlockColor)new CableBusColor(), new Block[]{AEBlocks.CABLE_BUS.block()});
        blockColors.register((BlockColor)ColorableBlockEntityBlockColor.INSTANCE, new Block[]{AEBlocks.ME_CHEST.block()});
    }
}

