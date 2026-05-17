/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  snownee.jade.api.IWailaClientRegistration
 *  snownee.jade.api.IWailaCommonRegistration
 *  snownee.jade.api.IWailaPlugin
 *  snownee.jade.api.WailaPlugin
 *  snownee.jade.api.ui.IElementHelper
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.TooltipProviders;
import appeng.integration.modules.jade.BodyProviderAdapter;
import appeng.integration.modules.jade.IconProviderAdapter;
import appeng.integration.modules.jade.ModNameProviderAdapter;
import appeng.integration.modules.jade.NameProviderAdapter;
import appeng.integration.modules.jade.ServerDataProviderAdapter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.ui.IElementHelper;

@WailaPlugin
public class JadeModule
implements IWailaPlugin {
    public void register(final IWailaCommonRegistration registration) {
        TooltipProviders.loadCommon(new CommonRegistration(){

            @Override
            public <T extends BlockEntity> void addBlockEntityData(ResourceLocation id, Class<T> blockEntityClass, ServerDataProvider<? super T> provider) {
                ServerDataProviderAdapter<? super T> adapter = new ServerDataProviderAdapter<T>(id, provider, blockEntityClass);
                registration.registerBlockDataProvider(adapter, blockEntityClass);
            }
        });
    }

    public void registerClient(final IWailaClientRegistration registration) {
        TooltipProviders.loadClient(new ClientRegistration(){

            @Override
            public <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, BodyProvider<? super T> provider, int priority) {
                BodyProviderAdapter<? super T> adapter = new BodyProviderAdapter<T>(id, priority, provider, blockEntityClass);
                registration.registerBlockComponent(adapter, blockClass);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, IconProvider<? super T> provider, int priority) {
                IconProviderAdapter<? super T> adapter = new IconProviderAdapter<T>(id, priority, IElementHelper.get(), provider, blockEntityClass);
                registration.registerBlockIcon(adapter, blockClass);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, NameProvider<? super T> provider, int priority) {
                NameProviderAdapter<? super T> adapter = new NameProviderAdapter<T>(id, priority, provider, blockEntityClass);
                registration.registerBlockComponent(adapter, blockClass);
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, ModNameProvider<? super T> provider, int priority) {
                ModNameProviderAdapter<? super T> adapter = new ModNameProviderAdapter<T>(id, provider, blockEntityClass);
                registration.registerBlockComponent(adapter, blockClass);
            }
        });
    }
}

