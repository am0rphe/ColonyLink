/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.JadeIds
 *  snownee.jade.api.config.IPluginConfig
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.integration.modules.jade.BaseProvider;
import appeng.integration.modules.jade.ContextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

class NameProviderAdapter<T>
extends BaseProvider
implements IBlockComponentProvider {
    private final NameProvider<? super T> provider;
    private final Class<T> objectClass;

    public NameProviderAdapter(ResourceLocation id, int priority, NameProvider<? super T> provider, Class<T> objectClass) {
        super(id, priority);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        TooltipContext context;
        T object = this.objectClass.cast(accessor.getBlockEntity());
        Component name = this.provider.getName(object, context = ContextHelper.getContext(accessor));
        if (name != null) {
            tooltip.remove(JadeIds.CORE_OBJECT_NAME);
            tooltip.add(0, (Component)name.copy().withStyle(style -> {
                if (style.getColor() == null) {
                    return style.withColor(ChatFormatting.WHITE);
                }
                return style;
            }), JadeIds.CORE_OBJECT_NAME);
        }
    }
}

