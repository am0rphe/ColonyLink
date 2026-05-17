/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcjty.theoneprobe.api.CompoundText
 *  mcjty.theoneprobe.api.IBlockDisplayOverride
 *  mcjty.theoneprobe.api.IProbeHitData
 *  mcjty.theoneprobe.api.IProbeInfo
 *  mcjty.theoneprobe.api.IProbeInfoProvider
 *  mcjty.theoneprobe.api.ProbeMode
 *  mcjty.theoneprobe.api.TextStyleClass
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.integration.modules.theoneprobe;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.AppEng;
import appeng.integration.modules.igtooltip.TooltipProviders;
import appeng.integration.modules.theoneprobe.TopTooltipBuilder;
import appeng.util.Platform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityInfoProvider
implements IProbeInfoProvider,
IBlockDisplayOverride {
    private final List<ServerDataCollector> dataCollectors = new ArrayList<ServerDataCollector>();
    private final List<BodyCustomizer<?>> bodyCustomizers = new ArrayList();
    private final List<NameCustomizer<?>> nameCustomizers = new ArrayList();
    private final List<ModNameCustomizer<?>> modNameCustomizers = new ArrayList();
    private final List<IconCustomizer<?>> iconCustomizers = new ArrayList();

    public BlockEntityInfoProvider() {
        TooltipProviders.loadCommon(new CommonRegistration(){

            @Override
            public <T extends BlockEntity> void addBlockEntityData(ResourceLocation id, Class<T> blockEntityClass, ServerDataProvider<? super T> provider) {
                BlockEntityInfoProvider.this.dataCollectors.add((blockEntity, player, serverData) -> {
                    if (blockEntityClass.isInstance(blockEntity)) {
                        BlockEntity obj = (BlockEntity)blockEntityClass.cast(blockEntity);
                        provider.provideServerData((Player)player, obj, serverData);
                    }
                });
            }
        });
        TooltipProviders.loadClient(new ClientRegistration(){

            @Override
            public <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, BodyProvider<? super T> provider, int priority) {
                BlockEntityInfoProvider.this.bodyCustomizers.add(new BodyCustomizer<T>(blockEntityClass, provider, priority));
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, IconProvider<? super T> provider, int priority) {
                BlockEntityInfoProvider.this.iconCustomizers.add(new IconCustomizer<T>(blockEntityClass, provider, priority));
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, NameProvider<? super T> provider, int priority) {
                BlockEntityInfoProvider.this.nameCustomizers.add(new NameCustomizer<T>(blockEntityClass, provider, priority));
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, ModNameProvider<? super T> provider, int priority) {
                BlockEntityInfoProvider.this.modNameCustomizers.add(new ModNameCustomizer<T>(blockEntityClass, provider, priority));
            }
        });
        this.nameCustomizers.sort(Comparator.comparingInt(NameCustomizer::priority));
        this.iconCustomizers.sort(Comparator.comparingInt(IconCustomizer::priority));
        this.modNameCustomizers.sort(Comparator.comparingInt(ModNameCustomizer::priority));
        this.bodyCustomizers.sort(Comparator.comparingInt(BodyCustomizer::priority));
    }

    public ResourceLocation getID() {
        return AppEng.makeId("block-entity");
    }

    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
        BlockEntity blockEntity = level.getBlockEntity(data.getPos());
        if (blockEntity != null) {
            CompoundTag serverData = this.getServerData(player, blockEntity);
            TooltipContext context = BlockEntityInfoProvider.getContext(player, data, serverData);
            TopTooltipBuilder tooltipBuilder = new TopTooltipBuilder(probeInfo);
            for (BodyCustomizer<?> customizer : this.bodyCustomizers) {
                customizer.buildTooltip(blockEntity, context, tooltipBuilder);
            }
        }
    }

    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData probeHitData) {
        Record customizer;
        BlockEntity blockEntity = level.getBlockEntity(probeHitData.getPos());
        if (blockEntity == null) {
            return false;
        }
        CompoundTag serverData = this.getServerData(player, blockEntity);
        TooltipContext context = BlockEntityInfoProvider.getContext(player, probeHitData, serverData);
        Component name = null;
        String modName = null;
        ItemStack icon = null;
        Iterator<Record> iterator = this.nameCustomizers.iterator();
        while (iterator.hasNext() && (name = ((NameCustomizer)(customizer = iterator.next())).getName(blockEntity, context)) == null) {
        }
        iterator = this.modNameCustomizers.iterator();
        while (iterator.hasNext() && (modName = ((ModNameCustomizer)(customizer = (ModNameCustomizer)iterator.next())).getModName(blockEntity, context)) == null) {
        }
        iterator = this.iconCustomizers.iterator();
        while (iterator.hasNext() && (icon = ((IconCustomizer)(customizer = (IconCustomizer)iterator.next())).getIcon(blockEntity, context)) == null) {
        }
        if (name != null || modName != null || icon != null) {
            ItemStack pickBlock = probeHitData.getPickBlock();
            if (name == null) {
                name = pickBlock.getHoverName();
            }
            if (icon == null) {
                icon = pickBlock;
            }
            if (modName == null) {
                modName = Platform.getModName(BuiltInRegistries.ITEM.getKey((Object)pickBlock.getItem()).getNamespace());
            }
            probeInfo.horizontal().item(icon).vertical().text(name).text(CompoundText.create().style(TextStyleClass.MODNAME).text(modName));
            return true;
        }
        return false;
    }

    private CompoundTag getServerData(Player player, BlockEntity blockEntity) {
        CompoundTag serverData = new CompoundTag();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            for (ServerDataCollector dataCollector : this.dataCollectors) {
                dataCollector.collect(blockEntity, serverPlayer, serverData);
            }
        }
        return serverData;
    }

    private static TooltipContext getContext(Player player, IProbeHitData data, CompoundTag serverData) {
        return new TooltipContext(serverData, data.getHitVec(), player);
    }

    record BodyCustomizer<T>(Class<T> beClass, BodyProvider<? super T> provider, int priority) {
        public void buildTooltip(BlockEntity blockEntity, TooltipContext context, TooltipBuilder tooltipBuilder) {
            if (this.beClass.isInstance(blockEntity)) {
                this.provider.buildTooltip(this.beClass.cast(blockEntity), context, tooltipBuilder);
            }
        }
    }

    record NameCustomizer<T>(Class<T> beClass, NameProvider<? super T> provider, int priority) {
        public Component getName(BlockEntity blockEntity, TooltipContext context) {
            if (this.beClass.isInstance(blockEntity)) {
                return this.provider.getName(this.beClass.cast(blockEntity), context);
            }
            return null;
        }
    }

    record ModNameCustomizer<T>(Class<T> beClass, ModNameProvider<? super T> provider, int priority) {
        public String getModName(BlockEntity blockEntity, TooltipContext context) {
            if (this.beClass.isInstance(blockEntity)) {
                return this.provider.getModName(this.beClass.cast(blockEntity), context);
            }
            return null;
        }
    }

    record IconCustomizer<T>(Class<T> beClass, IconProvider<? super T> provider, int priority) {
        public ItemStack getIcon(BlockEntity blockEntity, TooltipContext context) {
            if (this.beClass.isInstance(blockEntity)) {
                return this.provider.getIcon(this.beClass.cast(blockEntity), context);
            }
            return null;
        }
    }

    @FunctionalInterface
    private static interface ServerDataCollector {
        public void collect(BlockEntity var1, ServerPlayer var2, CompoundTag var3);
    }
}

