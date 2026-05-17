package com.colonylink.colonylink;

import appeng.api.AECapabilities;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ColonyLinkRegistry
{
    public static final DeferredRegister.Blocks BLOCKS      = DeferredRegister.createBlocks(ColonyLink.MODID);
    public static final DeferredRegister.Items  BLOCK_ITEMS = DeferredRegister.createItems(ColonyLink.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, ColonyLink.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            net.minecraft.core.registries.Registries.MENU, ColonyLink.MODID);

    // ── AE2 Redirector (bloc existant) ────────────────────────────────────────

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

    // ── Warehouse Link Terminal Part ──────────────────────────────────────────
    //
    // No Block or BlockEntity — the terminal is a Part placed on a cable bus.
    // Only an Item (implementing IPartItem) and a MenuType are needed.

    /** The Part item — placed on cable buses like the ME Crafting Terminal. */
    public static final DeferredHolder<Item, WarehouseLinkTerminalItem>
            WAREHOUSE_LINK_TERMINAL_ITEM = BLOCK_ITEMS.register("warehouse_link_terminal",
            WarehouseLinkTerminalItem::new);

    /** Menu type for the terminal GUI. */
    public static final DeferredHolder<MenuType<?>, MenuType<WarehouseLinkTerminalMenu>>
            WAREHOUSE_LINK_TERMINAL_MENU_TYPE = MENUS.register("warehouse_link_terminal",
            () -> IMenuTypeExtension.create(WarehouseLinkTerminalMenu::new));

    // ── Capabilities ──────────────────────────────────────────────────────────

    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        // AE2 grid node host — Redirector bloc
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                REDIRECTOR_BLOCK_ENTITY.get(),
                (be, context) -> be);

        // IEnergyStorage — wand AE2
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new WandEnergyStorage(stack),
                ColonyLink.COLONY_LINK_WAND.get());
    }
}