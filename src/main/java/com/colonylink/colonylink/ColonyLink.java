package com.colonylink.colonylink;

import appeng.api.features.GridLinkables;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartModelsHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(ColonyLink.MODID)
public class ColonyLink
{
    public static final String MODID = "colonylink";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);

    // Items

    public static final DeferredItem<Item> COLONY_LINK_WAND = ITEMS.register("colony_link_wand",
            () -> new ColonyLinkWand(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WAREHOUSE_LINK_CARD = ITEMS.register("warehouse_link_card",
            () -> new WarehouseLinkCard());

    public static final DeferredItem<Item> COLONY_LINK_PACKAGE = ITEMS.register("colonylink_package",
            () -> new ColonyLinkPackage());

    // v1.8.0 — Skill Tomes: one item per MineColonies Skill (11 total).
    // Registered by looping over Skill.values() so a skill add/remove upstream
    // simply changes how many tomes exist. Stored by the Skill reference, never
    // an ordinal. Stack size 16 (precious item).

    public static final java.util.Map<com.minecolonies.api.entity.citizen.Skill, DeferredItem<SkillTomeItem>>
            SKILL_TOMES = new java.util.EnumMap<>(com.minecolonies.api.entity.citizen.Skill.class);

    static
    {
        for (final com.minecolonies.api.entity.citizen.Skill skill
                : com.minecolonies.api.entity.citizen.Skill.values())
        {
            final String id = "tome_of_" + skill.name().toLowerCase(java.util.Locale.ROOT);
            SKILL_TOMES.put(skill, ITEMS.register(id,
                    () -> new SkillTomeItem(new Item.Properties().stacksTo(16), skill)));
        }
    }

    // Creative tab

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COLONY_LINK_TAB =
            CREATIVE_MODE_TABS.register("colony_link_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.colonylink"))
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> COLONY_LINK_WAND.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(COLONY_LINK_WAND.get());
                                output.accept(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM.get());
                                output.accept(ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_ITEM.get());
                                output.accept(WAREHOUSE_LINK_CARD.get());
                                output.accept(COLONY_LINK_PACKAGE.get());
                                // v1.4.2 — DomumPatternItem in the creative tab
                                output.accept(ColonyLinkRegistry.DOMUM_PATTERN_ITEM.get());
                                // v1.8.0 — Skill Tomes, in a stable (enum) order.
                                // Omitted when the server disabled the feature; the tab
                                // is rebuilt on world join once the server config is synced.
                                if (ColonyLinkConfig.safeGet(ColonyLinkConfig.ENABLE_SKILL_BOOKS, true))
                                    for (final com.minecolonies.api.entity.citizen.Skill skill
                                            : com.minecolonies.api.entity.citizen.Skill.values())
                                        output.accept(SKILL_TOMES.get(skill).get());
                            }).build());

    // AE2 linkable handler

    public static final ColonyLinkWandLinkableHandler LINKABLE_HANDLER =
            new ColonyLinkWandLinkableHandler();

    // Constructor

    public ColonyLink(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(this::registerScreens);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(ColonyLinkClient::onRegisterAdditionalModels);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(ColonyLinkClient::onRegisterBlockEntityRenderers);
        modEventBus.addListener(ColonyLinkRegistry::registerCapabilities);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(ColonyLinkClient::onRegisterItemDecorations);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(ColonyLinkClient::onRegisterItemColors);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(ColonyLinkClient::onRegisterKeyMappings);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        ColonyLinkRegistry.BLOCKS.register(modEventBus);
        ColonyLinkRegistry.BLOCK_ITEMS.register(modEventBus);
        ColonyLinkRegistry.BLOCK_ENTITIES.register(modEventBus);
        ColonyLinkRegistry.MENUS.register(modEventBus);
        ColonyLinkRegistry.DATA_COMPONENTS.register(modEventBus);

        ColonyLinkRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        AddPackageLootModifier.LOOT_MODIFIERS.register(modEventBus);

        PartModels.registerModels(
                PartModelsHelper.createModels(WarehouseLinkTerminalPart.class));

        NeoForge.EVENT_BUS.register(ColonyLinkServerTicker.class);
        NeoForge.EVENT_BUS.register(ColonyLinkCommand.class);

        // v1.8.0 — Skill Tome right-click interaction (common, server-safe)
        NeoForge.EVENT_BUS.register(SkillTomeHandler.class);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            NeoForge.EVENT_BUS.addListener(ColonyLinkClient::onKeyInput);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            NeoForge.EVENT_BUS.addListener(ColonyLinkClient::onRecipesUpdated);
        NeoForge.EVENT_BUS.addListener(ColonyLink::onRightClickBlock);

        // v1.6.0 — SERVER config: synced to clients on login, lockable by modpacks.
        // NeoForge derives the filename automatically: config/colonylink-server.toml
        // (a per-world override in <world>/serverconfig/ wins if that file exists).
        modContainer.registerConfig(ModConfig.Type.SERVER, ColonyLinkConfig.SPEC);
    }

    // Screens

    @OnlyIn(Dist.CLIENT)
    private void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE.get(),
                ColonyLinkRedirectorScreen::new);
        event.register(ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_MENU_TYPE.get(),
                WarehouseLinkTerminalScreen::new);
    }

    // Common setup

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            GridLinkables.register(COLONY_LINK_WAND.get(), LINKABLE_HANDLER);
            LOGGER.info("ColonyLink loaded.");
        });
    }

    // Events: Redirector block right-click

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player    = event.getEntity();
        ItemStack held   = event.getItemStack();
        BlockPos pos     = event.getPos();

        var be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return;

        var wrenchTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));

        if (held.is(wrenchTag))
        {
            event.setCanceled(true);
            if (player.isShiftKeyDown())
            {
                var blockState = event.getLevel().getBlockState(pos);
                Block.dropResources(blockState, event.getLevel(), pos, be, player, held);
                event.getLevel().removeBlock(pos, false);
                player.sendSystemMessage(Component.translatable("colonylink.redir_block.removed"));
            }
            else
            {
                redirector.updateState();
                player.sendSystemMessage(Component.translatable(redirector.isAe2Active() ? "colonylink.redir_block.ae2_linked" : "colonylink.redir_block.ae2_unlinked"));
                switch (redirector.getState())
                {
                    case NOT_LINKED -> player.sendSystemMessage(
                            Component.translatable("colonylink.redir_block.no_builder"));
                    case STANDBY -> player.sendSystemMessage(
                            Component.translatable("colonylink.redir_block.inv_full"));
                    case LINKED ->
                    {
                        player.sendSystemMessage(Component.translatable("colonylink.redir_block.operational"));
                        // v1.4.2 — show the number of loaded Domum patterns
                        int patternCount = 0;
                        for (int slot = 0; slot < redirector.buffer.getSlots(); slot++)
                            if (!redirector.buffer.getStackInSlot(slot).isEmpty()) patternCount++;
                        if (patternCount > 0)
                            player.sendSystemMessage(Component.translatable("colonylink.redir_block.patterns", patternCount));
                        if (redirector.getTargetInventoryPos() != null)
                            player.sendSystemMessage(Component.translatable("colonylink.redir_block.target", redirector.getTargetInventoryPos().toShortString()));
                        if (redirector.getLinkedBuilderPos() != null)
                            player.sendSystemMessage(Component.translatable("colonylink.redir_block.builder", redirector.getLinkedBuilderPos().toShortString()));
                    }
                    default -> {}
                }
            }
            return;
        }

        if (!player.isShiftKeyDown())
        {
            player.openMenu(redirector, buf -> buf.writeBlockPos(pos));
            event.setCanceled(true);
        }
    }

    // Payloads

    private void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        // v1.6.0 — bumped to "2": SendToWarehousePacket and PackageTokenPacket changed
        // shape and ResourceStatus gained an ordinal; mismatched client/server versions
        // must fail the handshake cleanly instead of corrupting codecs.
        // 1.7.0-beta.3 — bumped to "3": CitizensPacket entries gained the request UUID,
        // colony state, Warehouse stock and mark; PackageTokenPacket carries a request UUID.
        // v1.8.0 — bumped to "4": added Stats tab packets (StatsRequestPacket / StatsPacket).
        PayloadRegistrar registrar = event.registrar("4");

        registrar.playToClient(ColonyLinkPacket.TYPE, ColonyLinkPacket.STREAM_CODEC,
                (p, c) -> ClientPacketHandler.handleColonyLink(p, c));
        registrar.playToClient(TabCountsPacket.TYPE, TabCountsPacket.STREAM_CODEC,
                (p, c) -> ClientPacketHandler.handleTabCounts(p, c));
        registrar.playToClient(WarehouseResultPacket.TYPE, WarehouseResultPacket.STREAM_CODEC,
                (p, c) -> ClientPacketHandler.handleWarehouseResult(p, c));
        registrar.playToClient(CitizensPacket.TYPE, CitizensPacket.STREAM_CODEC,
                (p, c) -> ClientPacketHandler.handleCitizens(p, c));
        registrar.playToClient(PackageTokenSyncPacket.TYPE, PackageTokenSyncPacket.STREAM_CODEC,
                (p, c) -> ClientPacketHandler.handlePackageTokenSync(p, c));

        registrar.playToServer(GuiStatePacket.TYPE, GuiStatePacket.STREAM_CODEC,
                GuiStatePacket::handle);
        registrar.playToServer(CraftRequestPacket.TYPE, CraftRequestPacket.STREAM_CODEC,
                CraftRequestPacket::handle);
        registrar.playToServer(CraftAllRequestPacket.TYPE, CraftAllRequestPacket.STREAM_CODEC,
                CraftAllRequestPacket::handle);
        registrar.playToServer(SendToBuilderPacket.TYPE, SendToBuilderPacket.STREAM_CODEC,
                SendToBuilderPacket::handle);
        registrar.playToServer(SendToWarehousePacket.TYPE, SendToWarehousePacket.STREAM_CODEC,
                SendToWarehousePacket::handle);
        registrar.playToServer(RestartBuilderPacket.TYPE, RestartBuilderPacket.STREAM_CODEC,
                RestartBuilderPacket::handle);
        registrar.playToServer(CancelRequestPacket.TYPE, CancelRequestPacket.STREAM_CODEC,
                CancelRequestPacket::handle);
        registrar.playToServer(LocateBuilderPacket.TYPE, LocateBuilderPacket.STREAM_CODEC,
                LocateBuilderPacket::handle);
        registrar.playToServer(RemoveBuilderPacket.TYPE, RemoveBuilderPacket.STREAM_CODEC,
                RemoveBuilderPacket::handle);
        registrar.playToServer(WarehouseCheckPacket.TYPE, WarehouseCheckPacket.STREAM_CODEC,
                WarehouseCheckPacket::handle);
        registrar.playToServer(WarehousePriorityPacket.TYPE, WarehousePriorityPacket.STREAM_CODEC,
                WarehousePriorityPacket::handle);
        registrar.playToServer(WarehouseCraftPacket.TYPE, WarehouseCraftPacket.STREAM_CODEC,
                WarehouseCraftPacket::handle);
        registrar.playToServer(CitizensRequestPacket.TYPE, CitizensRequestPacket.STREAM_CODEC,
                CitizensRequestPacket::handle);
        // v1.8.0 — Stats tab
        registrar.playToServer(StatsRequestPacket.TYPE, StatsRequestPacket.STREAM_CODEC,
                StatsRequestPacket::handle);
        registrar.playToClient(StatsPacket.TYPE, StatsPacket.STREAM_CODEC,
                (p, c) -> ClientPacketHandler.handleStats(p, c));
        registrar.playToServer(PackageTokenPacket.TYPE, PackageTokenPacket.STREAM_CODEC,
                PackageTokenPacket::handle);
        registrar.playToServer(PackageLoadPacket.TYPE, PackageLoadPacket.STREAM_CODEC,
                PackageLoadPacket::handle);

        registrar.playToServer(OpenWandGuiPacket.TYPE, OpenWandGuiPacket.STREAM_CODEC,
                OpenWandGuiPacket::handle);

        registrar.playToClient(WarehouseTerminalSyncPacket.TYPE,
                WarehouseTerminalSyncPacket.STREAM_CODEC,
                (p, c) -> TerminalClientPacketHandler.handleWarehouseSync(p, c));
        registrar.playToClient(TerminalMeSyncPacket.TYPE,
                TerminalMeSyncPacket.STREAM_CODEC,
                (p, c) -> TerminalClientPacketHandler.handleMeSync(p, c));

        registrar.playToServer(TerminalGuiStatePacket.TYPE,
                TerminalGuiStatePacket.STREAM_CODEC, TerminalGuiStatePacket::handle);
        registrar.playToServer(TerminalTransferPacket.TYPE,
                TerminalTransferPacket.STREAM_CODEC, TerminalTransferPacket::handle);
        registrar.playToServer(TerminalCraftPacket.TYPE,
                TerminalCraftPacket.STREAM_CODEC, TerminalCraftPacket::handle);

        // v1.4.2 — Domum Pattern encoding
        registrar.playToServer(DomumEncodePatternPacket.TYPE,
                DomumEncodePatternPacket.STREAM_CODEC,
                DomumEncodePatternPacket::handle);

        registrar.playToServer(DomumQueuePacket.TYPE,
                DomumQueuePacket.STREAM_CODEC,
                DomumQueuePacket::handle);

        registrar.playToClient(DomumQueueSyncPacket.TYPE,
                DomumQueueSyncPacket.STREAM_CODEC,
                (p, c) -> TerminalClientPacketHandler.handleDomumQueueSync(p, c));
    }
}