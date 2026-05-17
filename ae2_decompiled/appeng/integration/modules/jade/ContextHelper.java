/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.BlockHitResult
 *  snownee.jade.api.BlockAccessor
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.TooltipContext;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;

class ContextHelper {
    private ContextHelper() {
    }

    public static TooltipContext getContext(BlockAccessor accessor) {
        return new TooltipContext(accessor.getServerData(), ((BlockHitResult)accessor.getHitResult()).getLocation(), accessor.getPlayer());
    }
}

