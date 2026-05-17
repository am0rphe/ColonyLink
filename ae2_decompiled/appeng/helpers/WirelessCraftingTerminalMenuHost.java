/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers;

import appeng.api.ids.AEComponents;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.contents.StackDependentSupplier;
import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.SupplierInternalInventory;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

public class WirelessCraftingTerminalMenuHost<T extends WirelessCraftingTerminalItem>
extends WirelessTerminalMenuHost<T>
implements ISegmentedInventory {
    private final SupplierInternalInventory<InternalInventory> craftingGrid = new SupplierInternalInventory<InternalInventory>(new StackDependentSupplier<InternalInventory>(this::getItemStack, stack -> WirelessCraftingTerminalMenuHost.createCraftingInv(player, stack)));

    public WirelessCraftingTerminalMenuHost(T item, Player player, ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
    }

    @Override
    @Nullable
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)CraftingTerminalPart.INV_CRAFTING)) {
            return this.craftingGrid;
        }
        return null;
    }

    private static InternalInventory createCraftingInv(final Player player, final ItemStack stack) {
        AppEngInternalInventory craftingGrid = new AppEngInternalInventory(new InternalInventoryHost(){

            @Override
            public void saveChangedInventory(AppEngInternalInventory inv) {
                stack.set(AEComponents.CRAFTING_INV, (Object)inv.toItemContainerContents());
            }

            @Override
            public boolean isClientSide() {
                return player.level().isClientSide();
            }
        }, 9);
        craftingGrid.fromItemContainerContents((ItemContainerContents)stack.getOrDefault(AEComponents.CRAFTING_INV, (Object)ItemContainerContents.EMPTY));
        return craftingGrid;
    }
}

