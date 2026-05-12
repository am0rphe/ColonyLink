package com.colonylink.colonylink;

import appeng.api.features.GridLinkables;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.neoforged.fml.ModList;
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

    // ── Items wand ────────────────────────────────────────────────────────────

    /** Wand AE2 — toujours enregistrée (AE2 est required dans neoforge.mods.toml) */
    public static final DeferredItem<Item> COLONY_LINK_WAND = ITEMS.register("colony_link_wand",
            () -> new ColonyLinkWand(new Item.Properties().stacksTo(1)));

    /** Wand RS2 — toujours enregistrée, affichée uniquement si RS2 est installé */
    public static final DeferredItem<Item> COLONY_LINK_WAND_RS = ITEMS.register("colony_link_wand_rs",
            () -> new ColonyLinkWandRS(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WAREHOUSE_LINK_CARD = ITEMS.register("warehouse_link_card",
            () -> new WarehouseLinkCard());

    // ── Creative tab ──────────────────────────────────────────────────────────

    /**
     * Creative tab ColonyLink.
     *
     * Logique d'affichage conditionnel :
     * - AE2 seul     → wand AE2 + redirector AE2
     * - RS2 seul     → wand RS2 + redirector RS2
     * - AE2 + RS2    → les deux wands + les deux redirectors
     *
     * L'icône du tab est la wand AE2 si disponible, sinon RS2.
     * La WarehouseLinkCard est toujours affichée (compatible les deux).
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COLONY_LINK_TAB =
            CREATIVE_MODE_TABS.register("colony_link_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.colonylink"))
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> {
                                // Icône = wand AE2 si AE2 présent, sinon wand RS2
                                if (isAe2Loaded()) return COLONY_LINK_WAND.get().getDefaultInstance();
                                return COLONY_LINK_WAND_RS.get().getDefaultInstance();
                            })
                            .displayItems((parameters, output) -> {
                                boolean ae2  = isAe2Loaded();
                                boolean rs2  = isRs2Loaded();

                                if (ae2)
                                {
                                    output.accept(COLONY_LINK_WAND.get());
                                    output.accept(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM.get());
                                }
                                if (rs2)
                                {
                                    output.accept(COLONY_LINK_WAND_RS.get());
                                    output.accept(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM_RS.get());
                                }
                                output.accept(WAREHOUSE_LINK_CARD.get());
                            }).build());

    // ── Handler AE2 ───────────────────────────────────────────────────────────

    public static final ColonyLinkWandLinkableHandler LINKABLE_HANDLER =
            new ColonyLinkWandLinkableHandler();

    // ── Constructeur ──────────────────────────────────────────────────────────

    public ColonyLink(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(ColonyLinkRegistry::registerCapabilities);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        ColonyLinkRegistry.BLOCKS.register(modEventBus);
        ColonyLinkRegistry.BLOCK_ITEMS.register(modEventBus);
        ColonyLinkRegistry.BLOCK_ENTITIES.register(modEventBus);
        ColonyLinkRegistry.MENUS.register(modEventBus);

        ColonyLinkRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        NeoForge.EVENT_BUS.register(ColonyLinkServerTicker.class);
        NeoForge.EVENT_BUS.addListener(ColonyLink::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ColonyLink::onRightClickBlockRS);

        modContainer.registerConfig(ModConfig.Type.COMMON, ColonyLinkConfig.SPEC,
                "colonylink-common.toml");
    }

    // ── Détection mods ────────────────────────────────────────────────────────

    public static boolean isAe2Loaded()
    { return ModList.get().isLoaded("ae2"); }

    public static boolean isRs2Loaded()
    { return ModList.get().isLoaded("refinedstorage"); }

    // ── Screens ───────────────────────────────────────────────────────────────

    @OnlyIn(Dist.CLIENT)
    private void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE.get(), ColonyLinkRedirectorScreen::new);
        event.register(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE_RS.get(), ColonyLinkRedirectorScreenRS::new);
    }

    // ── Events AE2 ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        BlockPos pos = event.getPos();

        // Gère uniquement le Redirector AE2
        var be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return;

        var wrenchTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
        if (heldItem.is(wrenchTag))
        {
            event.setCanceled(true);
            if (player.isShiftKeyDown())
            {
                var blockState = event.getLevel().getBlockState(pos);
                Block.dropResources(blockState, event.getLevel(), pos, be, player, heldItem);
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
                            Component.literal("§6[Redirector] STANDBY - Target inventory is full!"));
                    case LINKED ->
                    {
                        player.sendSystemMessage(Component.literal("§a[Redirector] LINKED and operational!"));
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

    // ── Events RS2 ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRightClickBlockRS(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        BlockPos pos = event.getPos();

        // Gère uniquement le Redirector RS2
        var be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntityRS redirector)) return;

        var wrenchTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
        if (heldItem.is(wrenchTag))
        {
            event.setCanceled(true);
            if (player.isShiftKeyDown())
            {
                var blockState = event.getLevel().getBlockState(pos);
                Block.dropResources(blockState, event.getLevel(), pos, be, player, heldItem);
                event.getLevel().removeBlock(pos, false);
                player.sendSystemMessage(Component.literal("§aColony Link Redirector RS removed!"));
            }
            else
            {
                redirector.updateState();
                player.sendSystemMessage(Component.literal(
                        "§7[Redirector RS] RS2: " + (redirector.isRs2Active() ? "§aLinked" : "§cUnlinked")));
                switch (redirector.getState())
                {
                    case NOT_LINKED -> player.sendSystemMessage(
                            Component.literal("§e[Redirector RS] No builder linked."));
                    case STANDBY -> player.sendSystemMessage(
                            Component.literal("§6[Redirector RS] STANDBY - Target inventory is full!"));
                    case LINKED ->
                    {
                        player.sendSystemMessage(Component.literal("§a[Redirector RS] LINKED and operational!"));
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

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            // Enregistrement AE2 GridLinkables pour la wand AE2
            if (isAe2Loaded())
                GridLinkables.register(COLONY_LINK_WAND.get(), LINKABLE_HANDLER);

            LOGGER.info("ColonyLink loaded! (v1.1.4 — AE2: {}, RS2: {})",
                    isAe2Loaded(), isRs2Loaded());
        });
    }

    // ── Payloads ──────────────────────────────────────────────────────────────

    private void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

        // Packets partagés AE2 + RS2
        registrar.playToClient(ColonyLinkPacket.TYPE, ColonyLinkPacket.STREAM_CODEC, ColonyLinkPacket::handle);
        registrar.playToClient(WarehouseResultPacket.TYPE, WarehouseResultPacket.STREAM_CODEC, WarehouseResultPacket::handle);
        registrar.playToServer(CraftRequestPacket.TYPE, CraftRequestPacket.STREAM_CODEC, CraftRequestPacket::handle);
        registrar.playToServer(GuiStatePacket.TYPE, GuiStatePacket.STREAM_CODEC, GuiStatePacket::handle);
        registrar.playToServer(SendToBuilderPacket.TYPE, SendToBuilderPacket.STREAM_CODEC, SendToBuilderPacket::handle);
        registrar.playToServer(CraftAllRequestPacket.TYPE, CraftAllRequestPacket.STREAM_CODEC, CraftAllRequestPacket::handle);
        registrar.playToServer(RestartBuilderPacket.TYPE, RestartBuilderPacket.STREAM_CODEC, RestartBuilderPacket::handle);
        registrar.playToServer(WarehouseCheckPacket.TYPE, WarehouseCheckPacket.STREAM_CODEC, WarehouseCheckPacket::handle);
        registrar.playToServer(WarehousePriorityPacket.TYPE, WarehousePriorityPacket.STREAM_CODEC, WarehousePriorityPacket::handle);
        registrar.playToServer(WarehouseCraftPacket.TYPE, WarehouseCraftPacket.STREAM_CODEC, WarehouseCraftPacket::handle);
        registrar.playToServer(RemoveBuilderPacket.TYPE, RemoveBuilderPacket.STREAM_CODEC, RemoveBuilderPacket::handle);

        // Packets RS2
        registrar.playToServer(GuiStatePacketRS.TYPE, GuiStatePacketRS.STREAM_CODEC, GuiStatePacketRS::handle);
        registrar.playToServer(SendToBuilderPacketRS.TYPE, SendToBuilderPacketRS.STREAM_CODEC, SendToBuilderPacketRS::handle);
        registrar.playToServer(CraftRequestPacketRS.TYPE, CraftRequestPacketRS.STREAM_CODEC, CraftRequestPacketRS::handle);
        registrar.playToServer(RemoveBuilderPacketRS.TYPE, RemoveBuilderPacketRS.STREAM_CODEC, RemoveBuilderPacketRS::handle);
    }
}
