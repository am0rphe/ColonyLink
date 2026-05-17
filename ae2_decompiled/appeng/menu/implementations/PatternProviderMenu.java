/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigMenuInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class PatternProviderMenu
extends AEBaseMenu {
    public static final MenuType<PatternProviderMenu> TYPE = MenuTypeBuilder.create(PatternProviderMenu::new, PatternProviderLogicHost.class).build("pattern_provider");
    protected final PatternProviderLogic logic;
    @GuiSync(value=3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(value=4)
    public YesNo showInAccessTerminal = YesNo.YES;
    @GuiSync(value=5)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(value=6)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;
    @GuiSync(value=7)
    public GenericStack unlockStack = null;

    public PatternProviderMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory, PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);
        this.createPlayerInventorySlots(playerInventory);
        this.logic = host.getLogic();
        InternalInventory patternInv = this.logic.getPatternInv();
        for (int x = 0; x < patternInv.size(); ++x) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, patternInv, x), SlotSemantics.ENCODED_PATTERN);
        }
        ConfigMenuInventory returnInv = this.logic.getReturnInv().createMenuWrapper();
        for (int i = 0; i < PatternProviderReturnInventory.NUMBER_OF_SLOTS; ++i) {
            if (i >= returnInv.size()) continue;
            this.addSlot(new AppEngSlot(returnInv, i), SlotSemantics.STORAGE);
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            this.blockingMode = this.logic.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            this.showInAccessTerminal = this.logic.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL);
            this.lockCraftingMode = this.logic.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
            this.craftingLockedReason = this.logic.getCraftingLockedReason();
            this.unlockStack = this.logic.getUnlockStack();
        }
        super.broadcastChanges();
    }

    public GenericStackInv getReturnInv() {
        return this.logic.getReturnInv();
    }

    public YesNo getBlockingMode() {
        return this.blockingMode;
    }

    public LockCraftingMode getLockCraftingMode() {
        return this.lockCraftingMode;
    }

    public LockCraftingMode getCraftingLockedReason() {
        return this.craftingLockedReason;
    }

    public GenericStack getUnlockStack() {
        return this.unlockStack;
    }

    public YesNo getShowInAccessTerminal() {
        return this.showInAccessTerminal;
    }
}

