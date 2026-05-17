/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcp.mobius.waila.api.IBlockAccessor
 *  mcp.mobius.waila.api.IBlockComponentProvider
 *  mcp.mobius.waila.api.IPluginConfig
 *  mcp.mobius.waila.api.IRegistrar
 *  mcp.mobius.waila.api.ITooltip
 *  mcp.mobius.waila.api.ITooltipComponent
 *  mcp.mobius.waila.api.IWailaPlugin
 *  mcp.mobius.waila.api.TooltipPosition
 *  mcp.mobius.waila.api.WailaConstants
 *  mcp.mobius.waila.api.component.ItemComponent
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.wthit;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.TooltipProviders;
import appeng.integration.modules.wthit.WthitTooltipBuilder;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WthitModule
implements IWailaPlugin {
    public void register(final IRegistrar registrar) {
        TooltipProviders.loadCommon(new CommonRegistration(){

            @Override
            public <T extends BlockEntity> void addBlockEntityData(ResourceLocation id, Class<T> blockEntityClass, ServerDataProvider<? super T> provider) {
                registrar.addBlockData((data, accessor, config) -> {
                    BlockEntity obj = (BlockEntity)blockEntityClass.cast(accessor.getTarget());
                    provider.provideServerData((Player)accessor.getPlayer(), obj, data.raw());
                }, blockEntityClass);
            }
        });
        TooltipProviders.loadClient(new ClientRegistration(){

            @Override
            public <T extends BlockEntity> void addBlockEntityBody(final Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, final BodyProvider<? super T> provider, int priority) {
                registrar.addComponent(new IBlockComponentProvider(){

                    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                        BlockEntity be = (BlockEntity)blockEntityClass.cast(accessor.getBlockEntity());
                        TooltipContext context = WthitModule.getContext(accessor);
                        provider.buildTooltip(be, context, new WthitTooltipBuilder(tooltip));
                    }
                }, TooltipPosition.BODY, blockEntityClass, priority);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityIcon(final Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, final IconProvider<? super T> provider, int priority) {
                registrar.addIcon(new IBlockComponentProvider(){

                    @Nullable
                    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
                        TooltipContext context;
                        BlockEntity be = (BlockEntity)blockEntityClass.cast(accessor.getBlockEntity());
                        ItemStack icon = provider.getIcon(be, context = WthitModule.getContext(accessor));
                        return icon != null ? new ItemComponent(icon) : null;
                    }
                }, blockClass, priority);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityName(final Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, final NameProvider<? super T> provider, int priority) {
                registrar.addComponent(new IBlockComponentProvider(){

                    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                        TooltipContext context;
                        BlockEntity obj = (BlockEntity)blockEntityClass.cast(accessor.getBlockEntity());
                        Component name = provider.getName(obj, context = WthitModule.getContext(accessor));
                        if (name != null) {
                            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, (Component)name.copy().withStyle(style -> {
                                if (style.getColor() == null) {
                                    return style.withColor(ChatFormatting.WHITE);
                                }
                                return style;
                            }));
                        }
                    }
                }, TooltipPosition.HEAD, blockEntityClass, priority);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityModName(final Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, final ModNameProvider<? super T> provider, int priority) {
                registrar.addComponent(new IBlockComponentProvider(){

                    public void appendTail(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
                        TooltipContext context;
                        BlockEntity obj = (BlockEntity)blockEntityClass.cast(accessor.getBlockEntity());
                        String modName = provider.getModName(obj, context = WthitModule.getContext(accessor));
                        if (modName != null && tooltip.getLine(WailaConstants.MOD_NAME_TAG) != null) {
                            tooltip.setLine(WailaConstants.MOD_NAME_TAG, (Component)Component.literal((String)modName).withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.ITALIC}));
                        }
                    }
                }, TooltipPosition.TAIL, blockEntityClass, priority);
            }
        });
    }

    private static TooltipContext getContext(IBlockAccessor accessor) {
        return new TooltipContext(accessor.getData().raw(), accessor.getBlockHitResult().getLocation(), accessor.getPlayer());
    }
}

