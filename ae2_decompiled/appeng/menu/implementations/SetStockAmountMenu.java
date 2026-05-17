/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.implementations;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.primitives.Ints;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SetStockAmountMenu
extends AEBaseMenu
implements ISubMenu {
    public static final MenuType<SetStockAmountMenu> TYPE = MenuTypeBuilder.create(SetStockAmountMenu::new, InterfaceLogicHost.class).build("set_stock_amount");
    public static final String ACTION_SET_STOCK_AMOUNT = "setStockAmount";
    private final Slot stockedItem;
    private AEKey whatToStock;
    @GuiSync(value=1)
    private int initialAmount = -1;
    @GuiSync(value=2)
    private int maxAmount = -1;
    private int slot;
    private final InterfaceLogicHost host;

    public SetStockAmountMenu(int id, Inventory ip, InterfaceLogicHost host) {
        super(TYPE, id, ip, host);
        this.registerClientAction(ACTION_SET_STOCK_AMOUNT, Integer.class, this::confirm);
        this.host = host;
        this.stockedItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.addSlot(this.stockedItem, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public InterfaceLogicHost getHost() {
        return this.host;
    }

    public static void open(ServerPlayer player, MenuHostLocator locator, int slot, AEKey whatToStock, int initialAmount) {
        MenuOpener.open(TYPE, (Player)player, locator);
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof SetStockAmountMenu) {
            SetStockAmountMenu cca = (SetStockAmountMenu)abstractContainerMenu;
            cca.setWhatToStock(slot, whatToStock, initialAmount);
            cca.broadcastChanges();
        }
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level();
    }

    private void setWhatToStock(int slot, AEKey whatToStock, int initialAmount) {
        this.slot = slot;
        this.whatToStock = Objects.requireNonNull(whatToStock, "whatToStock");
        this.initialAmount = initialAmount;
        this.maxAmount = Ints.saturatedCast((long)this.host.getConfig().getMaxAmount(whatToStock));
        this.stockedItem.set(whatToStock.wrapForDisplayOrFilter());
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public void confirm(int amount) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_STOCK_AMOUNT, amount);
            return;
        }
        GenericStackInv config = this.host.getConfig();
        if (!Objects.equals(config.getKey(this.slot), this.whatToStock)) {
            this.host.returnToMainMenu(this.getPlayer(), this);
            return;
        }
        if ((amount = (int)Math.min((long)amount, config.getMaxAmount(this.whatToStock))) <= 0) {
            config.setStack(this.slot, null);
        } else {
            config.setStack(this.slot, new GenericStack(this.whatToStock, amount));
        }
        this.host.returnToMainMenu(this.getPlayer(), this);
    }

    public int getInitialAmount() {
        return this.initialAmount;
    }

    @Nullable
    public AEKey getWhatToStock() {
        GenericStack stack = GenericStack.fromItemStack(this.stockedItem.getItem());
        return stack != null ? stack.what() : null;
    }
}

