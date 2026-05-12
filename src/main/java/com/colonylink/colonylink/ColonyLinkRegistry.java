package com.colonylink.colonylink;

import appeng.api.AECapabilities;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ColonyLinkRegistry
{
    public static final DeferredRegister.Blocks BLOCKS     = DeferredRegister.createBlocks(ColonyLink.MODID);
    public static final DeferredRegister.Items  BLOCK_ITEMS = DeferredRegister.createItems(ColonyLink.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, ColonyLink.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            net.minecraft.core.registries.Registries.MENU, ColonyLink.MODID);

    // ── AE2 Redirector ────────────────────────────────────────────────────────

    public static final DeferredBlock<ColonyLinkRedirectorBlock> REDIRECTOR_BLOCK =
            BLOCKS.register("colony_link_redirector", ColonyLinkRedirectorBlock::new);

    public static final DeferredHolder<Item, ColonyLinkRedirectorItem> REDIRECTOR_BLOCK_ITEM =
            BLOCK_ITEMS.register("colony_link_redirector", ColonyLinkRedirectorItem::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColonyLinkRedirectorBlockEntity>>
            REDIRECTOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("colony_link_redirector",
            () -> BlockEntityType.Builder.of(ColonyLinkRedirectorBlockEntity::new,
                    REDIRECTOR_BLOCK.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ColonyLinkRedirectorMenu>>
            REDIRECTOR_MENU_TYPE = MENUS.register("colony_link_redirector",
            () -> IMenuTypeExtension.create(ColonyLinkRedirectorMenu::new));

    // ── RS2 Redirector ────────────────────────────────────────────────────────

    public static final DeferredBlock<ColonyLinkRedirectorBlockRS> REDIRECTOR_BLOCK_RS =
            BLOCKS.register("colony_link_redirector_rs", ColonyLinkRedirectorBlockRS::new);

    public static final DeferredHolder<Item, ColonyLinkRedirectorItemRS> REDIRECTOR_BLOCK_ITEM_RS =
            BLOCK_ITEMS.register("colony_link_redirector_rs", ColonyLinkRedirectorItemRS::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColonyLinkRedirectorBlockEntityRS>>
            REDIRECTOR_BLOCK_ENTITY_RS = BLOCK_ENTITIES.register("colony_link_redirector_rs",
            () -> BlockEntityType.Builder.of(ColonyLinkRedirectorBlockEntityRS::new,
                    REDIRECTOR_BLOCK_RS.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ColonyLinkRedirectorMenuRS>>
            REDIRECTOR_MENU_TYPE_RS = MENUS.register("colony_link_redirector_rs",
            () -> IMenuTypeExtension.create(ColonyLinkRedirectorMenuRS::new));

    // ── Capabilities ──────────────────────────────────────────────────────────

    /**
     * Enregistre les capabilities :
     *
     * 1. AE2 IN_WORLD_GRID_NODE_HOST sur le Redirector AE2.
     * 2. IEnergyStorage (forge:energy) sur la wand AE2 et la wand RS2.
     * 3. NetworkNodeContainerProvider sur le Redirector RS2.
     *
     * Le point 3 est la pièce manquante pour la connexion câble RS2 :
     * quand un câble RS2 appelle neighborChanged, il interroge la capability
     * NetworkNodeContainerProvider sur les BE adjacents pour découvrir les
     * nœuds réseau. Sans cet enregistrement, notre BE est invisible pour RS2
     * même si AbstractBaseNetworkNodeContainerBlockEntity expose getContainerProvider().
     * NeoForge exige un enregistrement explicite via RegisterCapabilitiesEvent.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        // ── AE2 grid node — Redirector AE2 block entity ───────────────────────
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                REDIRECTOR_BLOCK_ENTITY.get(),
                (be, context) -> be);

        // ── RS2 NetworkNodeContainerProvider — Redirector RS2 block entity ────
        // Sans cet enregistrement, les câbles RS2 adjacents ne voient pas notre
        // nœud réseau et ne peuvent pas établir de connexion.
        event.registerBlockEntity(
                RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
                REDIRECTOR_BLOCK_ENTITY_RS.get(),
                (be, context) -> be.getContainerProvider());

        // ── IEnergyStorage — wand AE2 ─────────────────────────────────────────
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new WandEnergyStorage(stack),
                ColonyLink.COLONY_LINK_WAND.get());

        // ── IEnergyStorage — wand RS2 ─────────────────────────────────────────
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new WandEnergyStorage(stack),
                ColonyLink.COLONY_LINK_WAND_RS.get());
    }
}