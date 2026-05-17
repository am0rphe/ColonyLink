/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.JadeIds
 *  snownee.jade.api.config.IPluginConfig
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.integration.modules.jade.BaseProvider;
import appeng.integration.modules.jade.ContextHelper;
import appeng.integration.modules.jade.JadeTooltipBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

class BodyProviderAdapter<T extends BlockEntity>
extends BaseProvider
implements IBlockComponentProvider {
    private final BodyProvider<? super T> provider;
    private final Class<T> objectClass;

    public BodyProviderAdapter(ResourceLocation id, int priority, BodyProvider<? super T> provider, Class<T> objectClass) {
        super(id, priority);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.remove(JadeIds.UNIVERSAL_ENERGY_STORAGE_DETAILED);
        tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED);
        TooltipContext context = ContextHelper.getContext(accessor);
        JadeTooltipBuilder tooltipBuilder = new JadeTooltipBuilder(tooltip);
        BlockEntity obj = (BlockEntity)this.objectClass.cast(accessor.getBlockEntity());
        this.provider.buildTooltip(obj, context, tooltipBuilder);
    }
}

