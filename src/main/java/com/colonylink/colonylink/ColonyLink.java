package com.colonylink.colonylink;

import appeng.api.features.GridLinkables;
import appeng.items.tools.NetworkToolItem;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> COLONY_LINK_WAND = ITEMS.register("colony_link_wand",
            () -> new ColonyLinkWand(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WAREHOUSE_LINK_CARD = ITEMS.register("warehouse_link_card",
            () -> new WarehouseLinkCard());

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
                            }).build());

    public static final ColonyLinkWandLinkableHandler LINKABLE_HANDLER =
            new ColonyLinkWandLinkableHandler();

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
    }

    @OnlyIn(Dist.CLIENT)
    private void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ColonyLinkRegistry.REDIRECTOR_MENU_TYPE.get(), ColonyLinkRedirectorScreen::new);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        BlockPos pos = event.getPos();

        var be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return;

        // Wrench AE2 sneak + clic = casse instantané (Network Tool + Certus + Fluix wrench)
        var wrenchTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ae2", "tools/wrench"));
        boolean isWrench = heldItem.is(wrenchTag) || heldItem.getItem() instanceof NetworkToolItem;
        if (isWrench && player.isShiftKeyDown())
        {
            var blockState = event.getLevel().getBlockState(pos);
            Block.dropResources(blockState, event.getLevel(), pos, be, player, heldItem);
            event.getLevel().removeBlock(pos, false);
            player.sendSystemMessage(Component.literal("§aColony Link Redirector removed!"));
            event.setCanceled(true);
            return;
        }

        // Wand + sneak → délégué à ColonyLinkWand.useOn()
        if (heldItem.getItem() instanceof ColonyLinkWand && player.isShiftKeyDown())
            return;

        // Main vide + clic droit = ouvre le GUI buffer
        if (heldItem.isEmpty())
        {
            player.openMenu(redirector, buf -> buf.writeBlockPos(pos));
            event.setCanceled(true);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            GridLinkables.register(COLONY_LINK_WAND.get(), LINKABLE_HANDLER);
            LOGGER.info("ColonyLink loaded successfully!");
        });
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                ColonyLinkPacket.TYPE,
                ColonyLinkPacket.STREAM_CODEC,
                ColonyLinkPacket::handle
        );

        registrar.playToClient(
                WarehouseResultPacket.TYPE,
                WarehouseResultPacket.STREAM_CODEC,
                WarehouseResultPacket::handle
        );

        registrar.playToServer(
                CraftRequestPacket.TYPE,
                CraftRequestPacket.STREAM_CODEC,
                CraftRequestPacket::handle
        );

        registrar.playToServer(
                GuiStatePacket.TYPE,
                GuiStatePacket.STREAM_CODEC,
                GuiStatePacket::handle
        );

        registrar.playToServer(
                SendToBuilderPacket.TYPE,
                SendToBuilderPacket.STREAM_CODEC,
                SendToBuilderPacket::handle
        );

        registrar.playToServer(
                CraftAllRequestPacket.TYPE,
                CraftAllRequestPacket.STREAM_CODEC,
                CraftAllRequestPacket::handle
        );

        registrar.playToServer(
                RestartBuilderPacket.TYPE,
                RestartBuilderPacket.STREAM_CODEC,
                RestartBuilderPacket::handle
        );

        registrar.playToServer(
                WarehouseCheckPacket.TYPE,
                WarehouseCheckPacket.STREAM_CODEC,
                WarehouseCheckPacket::handle
        );

        registrar.playToServer(
                WarehousePriorityPacket.TYPE,
                WarehousePriorityPacket.STREAM_CODEC,
                WarehousePriorityPacket::handle
        );

        registrar.playToServer(
                WarehouseCraftPacket.TYPE,
                WarehouseCraftPacket.STREAM_CODEC,
                WarehouseCraftPacket::handle
        );
    }
}