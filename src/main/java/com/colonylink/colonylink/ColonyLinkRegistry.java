package com.colonylink.colonylink;

import appeng.api.AECapabilities;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ColonyLinkRegistry
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ColonyLink.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(ColonyLink.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, ColonyLink.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            net.minecraft.core.registries.Registries.MENU, ColonyLink.MODID);

    public static final DeferredBlock<ColonyLinkRedirectorBlock> REDIRECTOR_BLOCK = BLOCKS.register(
            "colony_link_redirector",
            ColonyLinkRedirectorBlock::new
    );

    public static final DeferredHolder<Item, ColonyLinkRedirectorItem> REDIRECTOR_BLOCK_ITEM = BLOCK_ITEMS.register(
            "colony_link_redirector",
            ColonyLinkRedirectorItem::new
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColonyLinkRedirectorBlockEntity>> REDIRECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("colony_link_redirector",
                    () -> BlockEntityType.Builder.of(ColonyLinkRedirectorBlockEntity::new, REDIRECTOR_BLOCK.get()).build(null)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<ColonyLinkRedirectorMenu>> REDIRECTOR_MENU_TYPE =
            MENUS.register("colony_link_redirector",
                    () -> IMenuTypeExtension.create(ColonyLinkRedirectorMenu::new)
            );

    /**
     * Enregistre la capability AE2 IN_WORLD_GRID_NODE_HOST pour le redirector.
     *
     * CRITIQUE : AE2 ne détecte pas les blocs IInWorldGridNodeHost par instanceof.
     * Il utilise exclusivement le système de capabilities NeoForge via
     * AECapabilities.IN_WORLD_GRID_NODE_HOST. Sans cet enregistrement,
     * les câbles AE2 adjacents ne voient jamais le nœud du redirector
     * et ne forment aucune connexion — quelle que soit la topologie du réseau.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                REDIRECTOR_BLOCK_ENTITY.get(),
                (be, context) -> be
        );
    }
}