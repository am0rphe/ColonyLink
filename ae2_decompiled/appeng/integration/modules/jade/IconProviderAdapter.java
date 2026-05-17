/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IBlockComponentProvider
 *  snownee.jade.api.ITooltip
 *  snownee.jade.api.config.IPluginConfig
 *  snownee.jade.api.ui.IElement
 *  snownee.jade.api.ui.IElementHelper
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.integration.modules.jade.BaseProvider;
import appeng.integration.modules.jade.ContextHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

class IconProviderAdapter<T extends BlockEntity>
extends BaseProvider
implements IBlockComponentProvider {
    private final IElementHelper elementHelper;
    private final IconProvider<? super T> iconProvider;
    private final Class<T> objectClass;

    public IconProviderAdapter(ResourceLocation id, int priority, IElementHelper elementHelper, IconProvider<? super T> iconProvider, Class<T> objectClass) {
        super(id, priority);
        this.elementHelper = elementHelper;
        this.iconProvider = iconProvider;
        this.objectClass = objectClass;
    }

    @Nullable
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        TooltipContext context;
        BlockEntity object = (BlockEntity)this.objectClass.cast(accessor.getBlockEntity());
        ItemStack icon = this.iconProvider.getIcon(object, context = ContextHelper.getContext(accessor));
        return icon != null ? this.elementHelper.item(icon) : null;
    }

    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
    }
}

