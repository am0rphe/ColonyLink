/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.ItemStackLinkedSet
 *  net.minecraft.world.item.Items
 */
package appeng.core;

import appeng.api.ids.AECreativeTabIds;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.parts.FacadeItem;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;

public final class FacadeCreativeTab {
    private static CreativeModeTab group;

    public static void init(Registry<CreativeModeTab> registry) {
        group = CreativeModeTab.builder().title((Component)GuiText.CreativeTabFacades.text()).withTabsBefore(new ResourceKey[]{AECreativeTabIds.MAIN}).icon(() -> {
            if (group == null) {
                return ItemStack.EMPTY;
            }
            Collection items = group.getDisplayItems();
            return items.stream().findFirst().orElse(Items.CAKE.getDefaultInstance());
        }).displayItems(FacadeCreativeTab::buildDisplayItems).build();
        Registry.register(registry, AECreativeTabIds.FACADES, (Object)group);
    }

    public static Collection<ItemStack> getDisplayItems() {
        return group == null ? Set.of() : group.getDisplayItems();
    }

    private static void buildDisplayItems(CreativeModeTab.ItemDisplayParameters displayParameters, CreativeModeTab.Output output) {
        Set facades = ItemStackLinkedSet.createTypeAndComponentsSet();
        FacadeItem itemFacade = AEItems.FACADE.get();
        try {
            for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                if (tab == group) continue;
                for (ItemStack displayItem : tab.getDisplayItems()) {
                    ItemStack facade = itemFacade.createFacadeForItem(displayItem, false);
                    if (facade.isEmpty()) continue;
                    facades.add(facade);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        output.acceptAll((Collection)facades);
    }
}

