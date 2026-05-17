/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.JadeIds
 *  snownee.jade.api.config.IPluginConfig
 *  snownee.jade.api.config.IWailaConfig
 *  snownee.jade.api.ui.IElement
 *  snownee.jade.api.ui.IElement$Align
 *  snownee.jade.api.ui.IElementHelper
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.integration.modules.jade.BaseProvider;
import appeng.integration.modules.jade.ContextHelper;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

class ModNameProviderAdapter<T>
extends BaseProvider
implements IBlockComponentProvider {
    private final ModNameProvider<? super T> provider;
    private final Class<T> objectClass;

    public ModNameProviderAdapter(ResourceLocation id, ModNameProvider<? super T> provider, Class<T> objectClass) {
        super(id, Integer.MAX_VALUE);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        TooltipContext context;
        T object = this.objectClass.cast(accessor.getBlockEntity());
        String modName = this.provider.getModName(object, context = ContextHelper.getContext(accessor));
        if (modName != null) {
            for (int i = 0; i < tooltip.size(); ++i) {
                for (IElement.Align align : IElement.Align.values()) {
                    List line = tooltip.get(i, align);
                    for (int j = 0; j < line.size(); ++j) {
                        IElement el = (IElement)line.get(j);
                        if (!JadeIds.CORE_MOD_NAME.equals((Object)el.getTag())) continue;
                        line.set(j, IElementHelper.get().text((Component)Component.literal((String)modName).withStyle(IWailaConfig.get().getFormatting().getItemModNameStyle())));
                    }
                }
            }
        }
    }
}

