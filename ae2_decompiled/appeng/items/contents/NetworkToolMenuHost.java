/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.contents;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.items.contents.StackDependentSupplier;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.SupplierInternalInventory;
import com.google.common.primitives.Ints;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NetworkToolMenuHost<T extends NetworkToolItem>
extends ItemMenuHost<T> {
    @Nullable
    private final IInWorldGridNodeHost host;
    private final SupplierInternalInventory<InternalInventory> supplierInv;

    public NetworkToolMenuHost(T item, Player player, ItemMenuHostLocator locator, @Nullable IInWorldGridNodeHost host) {
        super(item, player, locator);
        this.host = host;
        this.supplierInv = new SupplierInternalInventory<InternalInventory>(new StackDependentSupplier<InternalInventory>(this::getItemStack, NetworkToolItem::getInventory));
    }

    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        if (what instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)what;
            ItemStack stack = itemKey.toStack(Ints.saturatedCast((long)amount));
            ItemStack overflow = this.getInventory().addItems(stack, mode.isSimulate());
            return stack.getCount() - overflow.getCount();
        }
        return 0L;
    }

    @Nullable
    public IInWorldGridNodeHost getGridHost() {
        return this.host;
    }

    public InternalInventory getInventory() {
        return this.supplierInv;
    }
}

