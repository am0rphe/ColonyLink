/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.implementations;

import appeng.api.config.InscriberInputCapacity;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.client.gui.Icon;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.Side;
import appeng.core.localization.Tooltips;
import appeng.items.materials.NamePressItem;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.OutputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InscriberMenu
extends UpgradeableMenu<InscriberBlockEntity>
implements IProgressProvider {
    public static final MenuType<InscriberMenu> TYPE = MenuTypeBuilder.create(InscriberMenu::new, InscriberBlockEntity.class).build("inscriber");
    private final Slot top;
    private final Slot middle;
    private final Slot bottom;
    @GuiSync(value=2)
    public int maxProcessingTime = -1;
    @GuiSync(value=3)
    public int processingTime = -1;
    @GuiSync(value=7)
    public YesNo separateSides = YesNo.NO;
    @GuiSync(value=8)
    public YesNo autoExport = YesNo.NO;
    @GuiSync(value=9)
    public InscriberInputCapacity bufferSize = InscriberInputCapacity.SIXTY_FOUR;

    public InscriberMenu(int id, Inventory ip, InscriberBlockEntity host) {
        super((MenuType<?>)TYPE, id, ip, host);
        InternalInventory inv = host.getInternalInventory();
        AppEngSlot top = new AppEngSlot(inv, 0);
        top.setIcon(Icon.BACKGROUND_PLATE);
        top.setEmptyTooltip(() -> this.separateSides == YesNo.YES ? Tooltips.inputSlot(Side.TOP) : Tooltips.inputSlot(Side.ANY));
        this.top = this.addSlot(top, SlotSemantics.INSCRIBER_PLATE_TOP);
        AppEngSlot bottom = new AppEngSlot(inv, 1);
        bottom.setIcon(Icon.BACKGROUND_PLATE);
        bottom.setEmptyTooltip(() -> this.separateSides == YesNo.YES ? Tooltips.inputSlot(Side.BOTTOM) : Tooltips.inputSlot(Side.ANY));
        this.bottom = this.addSlot(bottom, SlotSemantics.INSCRIBER_PLATE_BOTTOM);
        AppEngSlot middle = new AppEngSlot(inv, 2);
        middle.setIcon(Icon.BACKGROUND_INGOT);
        middle.setEmptyTooltip(() -> this.separateSides == YesNo.YES ? Tooltips.inputSlot(Side.LEFT, Side.RIGHT, Side.BACK, Side.FRONT) : Tooltips.inputSlot(Side.ANY));
        this.middle = this.addSlot(middle, SlotSemantics.MACHINE_INPUT);
        OutputSlot output = new OutputSlot(inv, 3, null);
        output.setEmptyTooltip(() -> this.separateSides == YesNo.YES ? Tooltips.outputSlot(Side.LEFT, Side.RIGHT, Side.BACK, Side.FRONT) : Tooltips.outputSlot(Side.ANY));
        this.addSlot(output, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.separateSides = ((InscriberBlockEntity)this.getHost()).getConfigManager().getSetting(Settings.INSCRIBER_SEPARATE_SIDES);
        this.autoExport = ((InscriberBlockEntity)this.getHost()).getConfigManager().getSetting(Settings.AUTO_EXPORT);
        this.bufferSize = ((InscriberBlockEntity)this.getHost()).getConfigManager().getSetting(Settings.INSCRIBER_INPUT_CAPACITY);
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (this.isServerSide()) {
            this.maxProcessingTime = ((InscriberBlockEntity)this.getHost()).getMaxProcessingTime();
            this.processingTime = ((InscriberBlockEntity)this.getHost()).getProcessingTime();
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public boolean isValidForSlot(Slot s, ItemStack is) {
        ItemStack top = ((InscriberBlockEntity)this.getHost()).getInternalInventory().getStackInSlot(0);
        ItemStack bot = ((InscriberBlockEntity)this.getHost()).getInternalInventory().getStackInSlot(1);
        if (s == this.middle) {
            ItemDefinition<NamePressItem> press = AEItems.NAME_PRESS;
            if (press.is(top) || press.is(bot)) {
                return !press.is(is);
            }
            return InscriberRecipes.findRecipe(((InscriberBlockEntity)this.getHost()).getLevel(), is, top, bot, false) != null;
        }
        if (s == this.top && !bot.isEmpty() || s == this.bottom && !top.isEmpty()) {
            ItemDefinition<NamePressItem> namePress = AEItems.NAME_PRESS;
            ItemStack otherSlot = s == this.top ? this.bottom.getItem() : this.top.getItem();
            if (namePress.is(otherSlot)) {
                return namePress.is(is);
            }
            return InscriberRecipes.isValidOptionalIngredientCombination(((InscriberBlockEntity)this.getHost()).getLevel(), is, otherSlot);
        }
        return true;
    }

    @Override
    public int getCurrentProgress() {
        return this.processingTime;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcessingTime;
    }

    public YesNo getSeparateSides() {
        return this.separateSides;
    }

    public YesNo getAutoExport() {
        return this.autoExport;
    }

    public InscriberInputCapacity getBufferSize() {
        return this.bufferSize;
    }
}

