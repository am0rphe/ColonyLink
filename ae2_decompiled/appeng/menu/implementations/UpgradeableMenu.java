/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 */
package appeng.menu.implementations;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.IOptionalSlotHost;
import appeng.menu.slot.OptionalFakeSlot;
import appeng.util.ConfigMenuInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

public abstract class UpgradeableMenu<T extends IUpgradeableObject>
extends AEBaseMenu
implements IOptionalSlotHost {
    private final T host;
    @GuiSync(value=0)
    public RedstoneMode rsMode = RedstoneMode.IGNORE;
    @GuiSync(value=1)
    public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;
    @GuiSync(value=5)
    public YesNo cMode = YesNo.NO;
    @GuiSync(value=6)
    public SchedulingMode schedulingMode = SchedulingMode.DEFAULT;
    private final ToolboxMenu toolbox;

    public UpgradeableMenu(MenuType<?> menuType, int id, Inventory ip, T host) {
        super(menuType, id, ip, host);
        this.host = host;
        this.toolbox = new ToolboxMenu(this);
        this.setupInventorySlots();
        this.setupUpgrades();
        this.setupConfig();
        this.createPlayerInventorySlots(ip);
    }

    @ApiStatus.OverrideOnly
    protected void setupInventorySlots() {
    }

    @ApiStatus.OverrideOnly
    protected void setupConfig() {
    }

    @ApiStatus.OverrideOnly
    protected void setupUpgrades() {
        this.setupUpgrades(this.getHost().getUpgrades());
    }

    protected final void addExpandableConfigSlots(GenericStackInv config, int rows, int cols, int optionalRows) {
        ConfigMenuInventory inv = config.createMenuWrapper();
        for (int y = 0; y < rows + optionalRows; ++y) {
            for (int x = 0; x < cols; ++x) {
                int invIdx = y * cols + x;
                if (y < rows) {
                    this.addSlot(new FakeSlot(inv, invIdx), SlotSemantics.CONFIG);
                    continue;
                }
                this.addSlot(new OptionalFakeSlot(inv, this, invIdx, y - rows), SlotSemantics.CONFIG);
            }
        }
    }

    public ToolboxMenu getToolbox() {
        return this.toolbox;
    }

    @Override
    public void broadcastChanges() {
        T t;
        if (this.isServerSide() && (t = this.getHost()) instanceof IConfigurableObject) {
            IConfigurableObject configurableObject = (IConfigurableObject)t;
            this.loadSettingsFromHost(configurableObject.getConfigManager());
        }
        this.toolbox.tick();
        for (Object o : this.slots) {
            OptionalFakeSlot fs;
            if (!(o instanceof OptionalFakeSlot) || (fs = (OptionalFakeSlot)o).isSlotEnabled() || fs.getDisplayStack().isEmpty()) continue;
            fs.clearStack();
        }
        this.standardDetectAndSendChanges();
    }

    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_CONTROLLED));
        if (cm.hasSetting(Settings.CRAFT_ONLY)) {
            this.setCraftingMode(cm.getSetting(Settings.CRAFT_ONLY));
        }
        if (cm.hasSetting(Settings.SCHEDULING_MODE)) {
            this.setSchedulingMode(cm.getSetting(Settings.SCHEDULING_MODE));
        }
    }

    protected void standardDetectAndSendChanges() {
        super.broadcastChanges();
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        int capacityUpgrades = this.getHost().getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);
        return idx == 1 && capacityUpgrades >= 1 || idx == 2 && capacityUpgrades >= 2;
    }

    public FuzzyMode getFuzzyMode() {
        return this.fzMode;
    }

    public void setFuzzyMode(FuzzyMode fzMode) {
        this.fzMode = fzMode;
    }

    public YesNo getCraftingMode() {
        return this.cMode;
    }

    public void setCraftingMode(YesNo cMode) {
        this.cMode = cMode;
    }

    public RedstoneMode getRedStoneMode() {
        return this.rsMode;
    }

    public void setRedStoneMode(RedstoneMode rsMode) {
        this.rsMode = rsMode;
    }

    public SchedulingMode getSchedulingMode() {
        return this.schedulingMode;
    }

    private void setSchedulingMode(SchedulingMode schedulingMode) {
        this.schedulingMode = schedulingMode;
    }

    public final T getHost() {
        return this.host;
    }

    public final IUpgradeInventory getUpgrades() {
        return this.getHost().getUpgrades();
    }

    public final boolean hasUpgrade(ItemLike upgradeCard) {
        return this.getUpgrades().isInstalled(upgradeCard);
    }
}

