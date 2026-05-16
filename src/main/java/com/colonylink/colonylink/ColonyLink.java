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

    /** Wand AE2 — required */
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
                                output.accept(WAREHOUSE_LINK_CARD.get());
                                output.accept(COLONY_LINK_PACKAGE.get());
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

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient())
            NeoForge.EVENT_BUS.addListener(ColonyLinkHudRenderer::onRenderGuiPostStatic);
        NeoForge.EVENT_BUS.addListener(ColonyLink::onRightClickBlock);

        modContainer.registerConfig(ModConfig.Type.COMMON, ColonyLinkConfig.SPEC,
                "colonylink-common.toml");
    }

    // ── Screens ───────────────────────────────────────────────────────────────

    @OnlyIn(Dist.CLIENT)
    private void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE.get(), ColonyLinkRedirectorScreen::new);
    }

    // ── Events AE2 ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        BlockPos pos = event.getPos();

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
                            Component.literal("§6[Redirector] Builder inventory is full — waiting for space."));
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

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            GridLinkables.register(COLONY_LINK_WAND.get(), LINKABLE_HANDLER);
            LOGGER.info("ColonyLink loaded! (v1.2.2 — AE2 only, Package token)");
        });
    }

    // ── Payloads ──────────────────────────────────────────────────────────────

    private void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

        // S→C
        registrar.playToClient(ColonyLinkPacket.TYPE, ColonyLinkPacket.STREAM_CODEC, ColonyLinkPacket::handle);
        registrar.playToClient(TabCountsPacket.TYPE, TabCountsPacket.STREAM_CODEC, TabCountsPacket::handle);
        registrar.playToClient(WarehouseResultPacket.TYPE, WarehouseResultPacket.STREAM_CODEC, WarehouseResultPacket::handle);
        registrar.playToClient(CitizensPacket.TYPE, CitizensPacket.STREAM_CODEC, CitizensPacket::handle);

        // C→S
        registrar.playToServer(GuiStatePacket.TYPE, GuiStatePacket.STREAM_CODEC, GuiStatePacket::handle);
        registrar.playToServer(CraftRequestPacket.TYPE, CraftRequestPacket.STREAM_CODEC, CraftRequestPacket::handle);
        registrar.playToServer(CraftAllRequestPacket.TYPE, CraftAllRequestPacket.STREAM_CODEC, CraftAllRequestPacket::handle);
        registrar.playToServer(SendToBuilderPacket.TYPE, SendToBuilderPacket.STREAM_CODEC, SendToBuilderPacket::handle);
        registrar.playToServer(SendToWarehousePacket.TYPE, SendToWarehousePacket.STREAM_CODEC, SendToWarehousePacket::handle);
        registrar.playToServer(RestartBuilderPacket.TYPE, RestartBuilderPacket.STREAM_CODEC, RestartBuilderPacket::handle);
        registrar.playToServer(RemoveBuilderPacket.TYPE, RemoveBuilderPacket.STREAM_CODEC, RemoveBuilderPacket::handle);
        registrar.playToServer(WarehouseCheckPacket.TYPE, WarehouseCheckPacket.STREAM_CODEC, WarehouseCheckPacket::handle);
        registrar.playToServer(WarehousePriorityPacket.TYPE, WarehousePriorityPacket.STREAM_CODEC, WarehousePriorityPacket::handle);
        registrar.playToServer(WarehouseCraftPacket.TYPE, WarehouseCraftPacket.STREAM_CODEC, WarehouseCraftPacket::handle);
        registrar.playToServer(CitizensRequestPacket.TYPE, CitizensRequestPacket.STREAM_CODEC, CitizensRequestPacket::handle);

        // Packets Citizens Package token
        registrar.playToClient(PackageTokenSyncPacket.TYPE, PackageTokenSyncPacket.STREAM_CODEC, PackageTokenSyncPacket::handle);
        registrar.playToServer(PackageTokenPacket.TYPE, PackageTokenPacket.STREAM_CODEC, PackageTokenPacket::handle);
        registrar.playToServer(PackageLoadPacket.TYPE, PackageLoadPacket.STREAM_CODEC, PackageLoadPacket::handle);
    }
}