/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.crafting;

import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.core.network.serverbound.ConfirmAutoCraftPacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import java.util.Objects;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class CraftAmountMenu
extends AEBaseMenu
implements ISubMenu {
    public static final MenuType<CraftAmountMenu> TYPE = MenuTypeBuilder.create(CraftAmountMenu::new, ISubMenuHost.class).build("craftamount");
    private final AppEngSlot craftingItem;
    private AEKey whatToCraft;
    private final ISubMenuHost host;

    public CraftAmountMenu(int id, Inventory ip, ISubMenuHost host) {
        super(TYPE, id, ip, host);
        this.host = host;
        this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.craftingItem.setHideAmount(true);
        this.addSlot(this.craftingItem, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public ISubMenuHost getHost() {
        return this.host;
    }

    public static void open(ServerPlayer player, MenuHostLocator locator, AEKey whatToCraft, int initialAmount) {
        MenuOpener.open(TYPE, (Player)player, locator);
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof CraftAmountMenu) {
            CraftAmountMenu cca = (CraftAmountMenu)abstractContainerMenu;
            cca.setWhatToCraft(whatToCraft, initialAmount);
            cca.broadcastChanges();
        }
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level();
    }

    private void setWhatToCraft(AEKey whatToCraft, int initialAmount) {
        this.whatToCraft = Objects.requireNonNull(whatToCraft, "whatToCraft");
        this.craftingItem.set(GenericStack.wrapInItemStack(whatToCraft, initialAmount));
    }

    public void confirm(int amount, boolean craftMissingAmount, boolean autoStart) {
        MenuHostLocator locator;
        IGridNode node;
        IActionHost host;
        if (!this.isServerSide()) {
            ConfirmAutoCraftPacket message = new ConfirmAutoCraftPacket(amount, craftMissingAmount, autoStart);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (this.whatToCraft == null) {
            return;
        }
        if (craftMissingAmount && (host = this.getActionHost()) != null && (node = host.getActionableNode()) != null) {
            IStorageService storage = node.getGrid().getStorageService();
            int existingAmount = (int)Math.min(storage.getCachedInventory().get(this.whatToCraft), Integer.MAX_VALUE);
            amount = existingAmount > amount ? 0 : (amount -= existingAmount);
        }
        if ((locator = this.getLocator()) != null) {
            Player player = this.getPlayer();
            if (amount > 0) {
                MenuOpener.open(CraftConfirmMenu.TYPE, player, locator);
                AbstractContainerMenu abstractContainerMenu = player.containerMenu;
                if (abstractContainerMenu instanceof CraftConfirmMenu) {
                    CraftConfirmMenu ccc = (CraftConfirmMenu)abstractContainerMenu;
                    ccc.setAutoStart(autoStart);
                    ccc.planJob(this.whatToCraft, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
                    this.broadcastChanges();
                }
            } else {
                this.host.returnToMainMenu(player, this);
            }
        }
    }

    @Nullable
    public GenericStack getWhatToCraft() {
        return GenericStack.unwrapItemStack(this.craftingItem.getItem());
    }
}

