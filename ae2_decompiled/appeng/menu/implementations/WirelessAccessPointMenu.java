/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package appeng.menu.implementations;

import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class WirelessAccessPointMenu
extends AEBaseMenu
implements InternalInventoryHost {
    public static final MenuType<WirelessAccessPointMenu> TYPE = MenuTypeBuilder.create(WirelessAccessPointMenu::new, WirelessAccessPointBlockEntity.class).build("wireless_access_point");
    private final WirelessAccessPointBlockEntity accessPoint;
    private final RestrictedInputSlot boosterSlot;
    private final RestrictedInputSlot linkableIn;
    private final OutputSlot linkableOut;
    @GuiSync(value=1)
    public long range = 0L;
    @GuiSync(value=2)
    public long drain = 0L;

    public WirelessAccessPointMenu(int id, Inventory ip, WirelessAccessPointBlockEntity host) {
        super(TYPE, id, ip, host);
        this.accessPoint = host;
        this.boosterSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.RANGE_BOOSTER, host.getInternalInventory(), 0);
        this.addSlot(this.boosterSlot, SlotSemantics.STORAGE);
        this.boosterSlot.setEmptyTooltip(() -> Tooltips.slotTooltip(ButtonToolTips.PlaceWirelessBooster.text()));
        AppEngInternalInventory gridLinkingInv = new AppEngInternalInventory(this, 2);
        this.linkableIn = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.GRID_LINKABLE_ITEM, gridLinkingInv, 0);
        this.addSlot(this.linkableIn, SlotSemantics.MACHINE_INPUT);
        this.linkableIn.setEmptyTooltip(() -> Tooltips.slotTooltip(ButtonToolTips.LinkWirelessTerminal.text()));
        this.linkableOut = new OutputSlot(gridLinkingInv, 1, null);
        this.addSlot(this.linkableOut, SlotSemantics.MACHINE_OUTPUT);
        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        int boosters = this.boosterSlot.getItem().isEmpty() ? 0 : this.boosterSlot.getItem().getCount();
        this.setRange((long)(10.0 * AEConfig.instance().wireless_getMaxRange(boosters)));
        this.setDrain((long)(100.0 * AEConfig.instance().wireless_getPowerDrain(boosters)));
        super.broadcastChanges();
    }

    public long getRange() {
        return this.range;
    }

    private void setRange(long range) {
        this.range = range;
    }

    public long getDrain() {
        return this.drain;
    }

    private void setDrain(long drain) {
        this.drain = drain;
    }

    public void removed(Player player) {
        super.removed(player);
        if (this.linkableIn.hasItem()) {
            player.drop(this.linkableIn.getItem(), false);
        }
        if (this.linkableOut.hasItem()) {
            player.drop(this.linkableOut.getItem(), false);
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        ItemStack term;
        IGridLinkableHandler handler;
        if (!this.linkableOut.hasItem() && this.linkableIn.hasItem() && (handler = GridLinkables.get((ItemLike)(term = this.linkableIn.getItem().copy()).getItem())) != null && handler.canLink(term)) {
            handler.link(term, this.accessPoint.getGlobalPos());
            this.linkableIn.set(ItemStack.EMPTY);
            this.linkableOut.set(term);
        }
    }
}

