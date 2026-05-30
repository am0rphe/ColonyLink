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

    // ── Items ─────────────────────────────────────────────────────────────────

    public static final DeferredItem<Item> COLONY_LINK_WAND = ITEMS.register("colony_link_wand",
            () -> new ColonyLinkWand(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WAREHOUSE_LINK_CARD = ITEMS.register("warehouse_link_card",
            () -> new WarehouseLinkCard());

    public static final DeferredItem<Item> COLONY_LINK_PACKAGE = ITEMS.register("colonylink_package",
            () -> new ColonyLinkPackage());

    // ── Creative tab ──────────────────────────────────────────────────────────

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
                                // v1.4.2 — DomumPatternItem dans l'onglet créatif
                                output.accept(ColonyLinkRegistry.DOMUM_PATTERN_ITEM.get());
                            }).build());

    // ── AE2 linkable handler ──────────────────────────────────────────────────

    public static final ColonyLinkWandLinkableHandler LINKABLE_HANDLER =
            new ColonyLinkWandLinkableHandler();

    // ── Constructor ───────────────────────────────────────────────────────────

    public ColonyLink(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(this::registerScreens);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            modEventBus.addListener(ColonyLinkClient::onRegisterAdditionalModels);
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

        ColonyLinkRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        PartModels.registerModels(
                PartModelsHelper.createModels(WarehouseLinkTerminalPart.class));

        NeoForge.EVENT_BUS.register(ColonyLinkServerTicker.class);
        NeoForge.EVENT_BUS.register(ColonyLinkCommand.class);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            NeoForge.EVENT_BUS.addListener(ColonyLinkClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ColonyLink::onRightClickBlock);

        modContainer.registerConfig(ModConfig.Type.COMMON, ColonyLinkConfig.SPEC,
                "colonylink-common.toml");
    }

    // ── Screens ───────────────────────────────────────────────────────────────

    @OnlyIn(Dist.CLIENT)
    private void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE.get(),
                ColonyLinkRedirectorScreen::new);
        event.register(ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_MENU_TYPE.get(),
                WarehouseLinkTerminalScreen::new);
    }

    // ── Common setup ──────────────────────────────────────────────────────────

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            GridLinkables.register(COLONY_LINK_WAND.get(), LINKABLE_HANDLER);
            LOGGER.info("ColonyLink v1.4.7 loaded.");
        });
    }

    // ── Events: Redirector block right-click ──────────────────────────────────

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
                player.sendSystemMessage(Component.literal("§aColony Link Redirector removed!"));
            }
            else
            {
                redirector.updateState();
                player.sendSystemMessage(Component.literal(
                        "§7[Redirector] AE2: " + (redirector.isAe2Active() ? "§aLinked" : "§cUnlinked")));
                switch (redirector.getState())
                {
                    case NOT_LINKED -> player.sendSystemMessage(
                            Component.literal("§e[Redirector] No builder linked."));
                    case STANDBY -> player.sendSystemMessage(
                            Component.literal("§6[Redirector] Builder inventory is full."));
                    case LINKED ->
                    {
                        player.sendSystemMessage(Component.literal("§a[Redirector] Operational!"));
                        // v1.4.2 — Affiche le nombre de patterns Domum chargés
                        int patternCount = 0;
                        for (int slot = 0; slot < redirector.buffer.getSlots(); slot++)
                            if (!redirector.buffer.getStackInSlot(slot).isEmpty()) patternCount++;
                        if (patternCount > 0)
                            player.sendSystemMessage(Component.literal(
                                    "§b[Redirector] §f" + patternCount + " Domum pattern(s) loaded."));
                        if (redirector.getTargetInventoryPos() != null)
                            player.sendSystemMessage(Component.literal(
                                    "§7Target: " + redirector.getTargetInventoryPos().toShortString()));
                        if (redirector.getLinkedBuilderPos() != null)
                            player.sendSystemMessage(Component.literal(
                                    "§7Builder: " + redirector.getLinkedBuilderPos().toShortString()));
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

    // ── Payloads ──────────────────────────────────────────────────────────────

    private void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

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

        // ── v1.4.2 — Domum Pattern encoding ──────────────────────────────────
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