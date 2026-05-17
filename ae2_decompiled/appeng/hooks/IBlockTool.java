/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.item.context.UseOnContext
 */
package appeng.hooks;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public interface IBlockTool {
    public InteractionResult useOn(UseOnContext var1);
}

