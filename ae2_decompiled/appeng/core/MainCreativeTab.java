/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  net.minecraft.core.Registry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
 */
package appeng.core;

import appeng.api.ids.AECreativeTabIds;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class MainCreativeTab {
    private static final Multimap<ResourceKey<CreativeModeTab>, ItemDefinition<?>> externalItemDefs = HashMultimap.create();
    private static final List<ItemDefinition<?>> itemDefs = new ArrayList();

    public static void init(Registry<CreativeModeTab> registry) {
        CreativeModeTab tab = CreativeModeTab.builder().title((Component)GuiText.CreativeTab.text()).icon(() -> AEBlocks.CONTROLLER.stack(1)).displayItems(MainCreativeTab::buildDisplayItems).build();
        Registry.register(registry, AECreativeTabIds.MAIN, (Object)tab);
    }

    public static void initExternal(BuildCreativeModeTabContentsEvent contents) {
        for (ItemDefinition itemDefinition : externalItemDefs.get((Object)contents.getTabKey())) {
            contents.accept((ItemLike)itemDefinition);
        }
    }

    public static void add(ItemDefinition<?> itemDef) {
        itemDefs.add(itemDef);
    }

    public static void addExternal(ResourceKey<CreativeModeTab> tab, ItemDefinition<?> itemDef) {
        externalItemDefs.put(tab, itemDef);
    }

    private static void buildDisplayItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        for (ItemDefinition<?> itemDef : itemDefs) {
            AEBaseBlockItem baseItem;
            Block block;
            Object item = itemDef.asItem();
            if (item instanceof AEBaseBlockItem && (block = (baseItem = (AEBaseBlockItem)((Object)item)).getBlock()) instanceof AEBaseBlock) {
                AEBaseBlock baseBlock = (AEBaseBlock)block;
                baseBlock.addToMainCreativeTab(itemDisplayParameters, output);
                continue;
            }
            if (item instanceof AEBaseItem) {
                AEBaseItem baseItem2 = (AEBaseItem)((Object)item);
                baseItem2.addToMainCreativeTab(itemDisplayParameters, output);
                continue;
            }
            output.accept(itemDef);
        }
    }
}

