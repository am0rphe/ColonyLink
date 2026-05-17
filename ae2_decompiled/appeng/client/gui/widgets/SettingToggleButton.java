/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.api.config.AccessRestriction;
import appeng.api.config.CondenserOutput;
import appeng.api.config.CpuSelectionMode;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.InscriberInputCapacity;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerUnit;
import appeng.api.config.RedstoneMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.StorageFilter;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.definitions.AEParts;
import appeng.core.localization.ButtonToolTips;
import appeng.util.EnumCycler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class SettingToggleButton<T extends Enum<T>>
extends IconButton {
    private static Map<EnumPair<?>, ButtonAppearance> appearances;
    private final Setting<T> buttonSetting;
    private final IHandler<SettingToggleButton<T>> onPress;
    private final EnumSet<T> validValues;
    private T currentValue;

    public SettingToggleButton(Setting<T> setting, T val, IHandler<SettingToggleButton<T>> onPress) {
        this((Setting<Enum>)setting, (Enum)val, t -> true, (IHandler<SettingToggleButton<Enum>>)onPress);
    }

    public SettingToggleButton(Setting<T> setting, T val, Predicate<T> isValidValue, IHandler<SettingToggleButton<T>> onPress) {
        super(SettingToggleButton::onPress);
        this.onPress = onPress;
        EnumSet<Object> validValues = EnumSet.allOf(((Enum)val).getDeclaringClass());
        validValues.removeIf(isValidValue.negate());
        validValues.removeIf(s -> !setting.getValues().contains(s));
        this.validValues = validValues;
        this.buttonSetting = setting;
        this.currentValue = val;
        if (appearances == null) {
            appearances = new HashMap();
            SettingToggleButton.registerApp(Icon.CONDENSER_OUTPUT_TRASH, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH, ButtonToolTips.CondenserOutput, ButtonToolTips.Trash);
            SettingToggleButton.registerApp(Icon.CONDENSER_OUTPUT_MATTER_BALL, Settings.CONDENSER_OUTPUT, CondenserOutput.MATTER_BALLS, ButtonToolTips.CondenserOutput, new Component[]{ButtonToolTips.MatterBalls.text(CondenserOutput.MATTER_BALLS.requiredPower)});
            SettingToggleButton.registerApp(Icon.CONDENSER_OUTPUT_SINGULARITY, Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY, ButtonToolTips.CondenserOutput, new Component[]{ButtonToolTips.Singularity.text(CondenserOutput.SINGULARITY.requiredPower)});
            SettingToggleButton.registerApp(Icon.ACCESS_READ, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode, ButtonToolTips.Read);
            SettingToggleButton.registerApp(Icon.ACCESS_WRITE, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode, ButtonToolTips.Write);
            SettingToggleButton.registerApp(Icon.ACCESS_READ_WRITE, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode, ButtonToolTips.ReadWrite);
            SettingToggleButton.registerApp(Icon.POWER_UNIT_AE, Settings.POWER_UNITS, PowerUnit.AE, ButtonToolTips.PowerUnits, PowerUnit.AE.textComponent());
            SettingToggleButton.registerApp(Icon.POWER_UNIT_RF, Settings.POWER_UNITS, PowerUnit.FE, ButtonToolTips.PowerUnits, PowerUnit.FE.textComponent());
            SettingToggleButton.registerApp(Icon.REDSTONE_IGNORE, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE, ButtonToolTips.RedstoneMode, ButtonToolTips.AlwaysActive);
            SettingToggleButton.registerApp(Icon.REDSTONE_LOW, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveWithoutSignal);
            SettingToggleButton.registerApp(Icon.REDSTONE_HIGH, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveWithSignal);
            SettingToggleButton.registerApp(Icon.REDSTONE_PULSE, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveOnPulse);
            SettingToggleButton.registerApp(Icon.REDSTONE_BELOW, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.EmitLevelsBelow);
            SettingToggleButton.registerApp(Icon.REDSTONE_ABOVE_EQUAL, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.EmitLevelAbove);
            SettingToggleButton.registerApp(Icon.ARROW_LEFT, Settings.OPERATION_MODE, OperationMode.FILL, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToStorageCell);
            SettingToggleButton.registerApp(Icon.ARROW_RIGHT, Settings.OPERATION_MODE, OperationMode.EMPTY, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToNetwork);
            SettingToggleButton.registerApp(Icon.ARROW_LEFT, Settings.IO_DIRECTION, RelativeDirection.LEFT, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToStorageCell);
            SettingToggleButton.registerApp(Icon.ARROW_RIGHT, Settings.IO_DIRECTION, RelativeDirection.RIGHT, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToNetwork);
            SettingToggleButton.registerApp(Icon.ARROW_UP, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder, ButtonToolTips.Ascending);
            SettingToggleButton.registerApp(Icon.ARROW_DOWN, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder, ButtonToolTips.Descending);
            SettingToggleButton.registerApp(Icon.TERMINAL_STYLE_SMALL, Settings.TERMINAL_STYLE, TerminalStyle.SMALL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Small);
            SettingToggleButton.registerApp(Icon.TERMINAL_STYLE_MEDIUM, Settings.TERMINAL_STYLE, TerminalStyle.MEDIUM, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Medium);
            SettingToggleButton.registerApp(Icon.TERMINAL_STYLE_TALL, Settings.TERMINAL_STYLE, TerminalStyle.TALL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Tall);
            SettingToggleButton.registerApp(Icon.TERMINAL_STYLE_FULL, Settings.TERMINAL_STYLE, TerminalStyle.FULL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Full);
            SettingToggleButton.registerApp(Icon.SORT_BY_NAME, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy, ButtonToolTips.ItemName);
            SettingToggleButton.registerApp(Icon.SORT_BY_AMOUNT, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy, ButtonToolTips.NumberOfItems);
            SettingToggleButton.registerApp(Icon.SORT_BY_MOD, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod);
            SettingToggleButton.registerApp(Icon.VIEW_MODE_STORED, Settings.VIEW_MODE, ViewItems.STORED, ButtonToolTips.View, ButtonToolTips.StoredItems);
            SettingToggleButton.registerApp(Icon.VIEW_MODE_ALL, Settings.VIEW_MODE, ViewItems.ALL, ButtonToolTips.View, ButtonToolTips.StoredCraftable);
            SettingToggleButton.registerApp(Icon.VIEW_MODE_CRAFTING, Settings.VIEW_MODE, ViewItems.CRAFTABLE, ButtonToolTips.View, ButtonToolTips.Craftable);
            SettingToggleButton.registerApp(Icon.FUZZY_PERCENT_25, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_25);
            SettingToggleButton.registerApp(Icon.FUZZY_PERCENT_50, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_50);
            SettingToggleButton.registerApp(Icon.FUZZY_PERCENT_75, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_75);
            SettingToggleButton.registerApp(Icon.FUZZY_PERCENT_99, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_99);
            SettingToggleButton.registerApp(Icon.FUZZY_IGNORE, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode, ButtonToolTips.FZIgnoreAll);
            SettingToggleButton.registerApp(Icon.FULLNESS_EMPTY, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenEmpty);
            SettingToggleButton.registerApp(Icon.FULLNESS_HALF, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenWorkIsDone);
            SettingToggleButton.registerApp(Icon.FULLNESS_FULL, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenFull);
            SettingToggleButton.registerApp(Icon.BLOCKING_MODE_YES, Settings.BLOCKING_MODE, YesNo.YES, ButtonToolTips.InterfaceBlockingMode, ButtonToolTips.Blocking);
            SettingToggleButton.registerApp(Icon.BLOCKING_MODE_NO, Settings.BLOCKING_MODE, YesNo.NO, ButtonToolTips.InterfaceBlockingMode, ButtonToolTips.NonBlocking);
            SettingToggleButton.registerApp(Icon.VIEW_MODE_CRAFTING, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft, ButtonToolTips.CraftOnly);
            SettingToggleButton.registerApp(Icon.VIEW_MODE_ALL, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft, ButtonToolTips.CraftEither);
            SettingToggleButton.registerApp(Icon.CRAFT_HAMMER, Settings.CRAFT_VIA_REDSTONE, YesNo.YES, ButtonToolTips.EmitterMode, ButtonToolTips.CraftViaRedstone);
            SettingToggleButton.registerApp(Icon.ACCESS_READ, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, ButtonToolTips.EmitterMode, ButtonToolTips.EmitWhenCrafting);
            SettingToggleButton.registerApp(Icon.STORAGE_FILTER_EXTRACTABLE_ONLY, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY, ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsNo);
            SettingToggleButton.registerApp(Icon.STORAGE_FILTER_EXTRACTABLE_NONE, Settings.STORAGE_FILTER, StorageFilter.NONE, ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsYes);
            SettingToggleButton.registerApp(Icon.PLACEMENT_BLOCK, Settings.PLACE_BLOCK, YesNo.YES, ButtonToolTips.BlockPlacement, ButtonToolTips.BlockPlacementYes);
            SettingToggleButton.registerApp(Icon.PLACEMENT_ITEM, Settings.PLACE_BLOCK, YesNo.NO, ButtonToolTips.BlockPlacement, ButtonToolTips.BlockPlacementNo);
            SettingToggleButton.registerApp(Icon.SCHEDULING_DEFAULT, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT, ButtonToolTips.SchedulingMode, ButtonToolTips.SchedulingModeDefault);
            SettingToggleButton.registerApp(Icon.SCHEDULING_ROUND_ROBIN, Settings.SCHEDULING_MODE, SchedulingMode.ROUNDROBIN, ButtonToolTips.SchedulingMode, ButtonToolTips.SchedulingModeRoundRobin);
            SettingToggleButton.registerApp(Icon.SCHEDULING_RANDOM, Settings.SCHEDULING_MODE, SchedulingMode.RANDOM, ButtonToolTips.SchedulingMode, ButtonToolTips.SchedulingModeRandom);
            SettingToggleButton.registerApp(Icon.OVERLAY_OFF, Settings.OVERLAY_MODE, YesNo.NO, ButtonToolTips.OverlayMode, ButtonToolTips.OverlayModeNo);
            SettingToggleButton.registerApp(Icon.OVERLAY_ON, Settings.OVERLAY_MODE, YesNo.YES, ButtonToolTips.OverlayMode, ButtonToolTips.OverlayModeYes);
            SettingToggleButton.registerApp(Icon.FILTER_ON_EXTRACT_ENABLED, Settings.FILTER_ON_EXTRACT, YesNo.YES, ButtonToolTips.FilterOnExtract, ButtonToolTips.FilterOnExtractEnabled);
            SettingToggleButton.registerApp(Icon.FILTER_ON_EXTRACT_DISABLED, Settings.FILTER_ON_EXTRACT, YesNo.NO, ButtonToolTips.FilterOnExtract, ButtonToolTips.FilterOnExtractDisabled);
            SettingToggleButton.registerApp(Icon.CRAFT_HAMMER, Settings.CPU_SELECTION_MODE, CpuSelectionMode.ANY, ButtonToolTips.CpuSelectionMode, ButtonToolTips.CpuSelectionModeAny);
            SettingToggleButton.registerApp(AEParts.TERMINAL, Settings.CPU_SELECTION_MODE, CpuSelectionMode.PLAYER_ONLY, ButtonToolTips.CpuSelectionMode, new Component[]{ButtonToolTips.CpuSelectionModePlayersOnly.text()});
            SettingToggleButton.registerApp(AEParts.EXPORT_BUS, Settings.CPU_SELECTION_MODE, CpuSelectionMode.MACHINE_ONLY, ButtonToolTips.CpuSelectionMode, new Component[]{ButtonToolTips.CpuSelectionModeAutomationOnly.text()});
            SettingToggleButton.registerApp(Icon.PATTERN_TERMINAL_ALL, Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.ALL, ButtonToolTips.InterfaceTerminalDisplayMode, ButtonToolTips.ShowAllProviders);
            SettingToggleButton.registerApp(Icon.PATTERN_TERMINAL_VISIBLE, Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE, ButtonToolTips.InterfaceTerminalDisplayMode, ButtonToolTips.ShowVisibleProviders);
            SettingToggleButton.registerApp(Icon.PATTERN_TERMINAL_NOT_FULL, Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.NOT_FULL, ButtonToolTips.InterfaceTerminalDisplayMode, ButtonToolTips.ShowNonFullProviders);
            SettingToggleButton.registerApp(Icon.UNLOCKED, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE, ButtonToolTips.LockCraftingMode, ButtonToolTips.LockCraftingModeNone);
            SettingToggleButton.registerApp(Icon.REDSTONE_ON, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_WHILE_HIGH, ButtonToolTips.LockCraftingMode, ButtonToolTips.LockCraftingWhileRedstoneHigh);
            SettingToggleButton.registerApp(Icon.REDSTONE_OFF, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_WHILE_LOW, ButtonToolTips.LockCraftingMode, ButtonToolTips.LockCraftingWhileRedstoneLow);
            SettingToggleButton.registerApp(Icon.REDSTONE_PULSE, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_PULSE, ButtonToolTips.LockCraftingMode, ButtonToolTips.LockCraftingUntilRedstonePulse);
            SettingToggleButton.registerApp(Icon.ENTER, Settings.LOCK_CRAFTING_MODE, LockCraftingMode.LOCK_UNTIL_RESULT, ButtonToolTips.LockCraftingMode, ButtonToolTips.LockCraftingUntilResultReturned);
            SettingToggleButton.registerApp(Icon.INSCRIBER_SEPARATE_SIDES, Settings.INSCRIBER_SEPARATE_SIDES, YesNo.YES, ButtonToolTips.InscriberSideness, ButtonToolTips.InscriberSidenessSeparate);
            SettingToggleButton.registerApp(Icon.INSCRIBER_COMBINED_SIDES, Settings.INSCRIBER_SEPARATE_SIDES, YesNo.NO, ButtonToolTips.InscriberSideness, ButtonToolTips.InscriberSidenessCombined);
            SettingToggleButton.registerApp(Icon.AUTO_EXPORT_ON, Settings.AUTO_EXPORT, YesNo.YES, ButtonToolTips.AutoExport, ButtonToolTips.AutoExportOn);
            SettingToggleButton.registerApp(Icon.AUTO_EXPORT_OFF, Settings.AUTO_EXPORT, YesNo.NO, ButtonToolTips.AutoExport, ButtonToolTips.AutoExportOff);
            SettingToggleButton.registerApp(Icon.INSCRIBER_BUFFER_64, Settings.INSCRIBER_INPUT_CAPACITY, InscriberInputCapacity.SIXTY_FOUR, ButtonToolTips.InscriberBufferSize, ButtonToolTips.InscriberBufferHigh);
            SettingToggleButton.registerApp(Icon.INSCRIBER_BUFFER_4, Settings.INSCRIBER_INPUT_CAPACITY, InscriberInputCapacity.FOUR, ButtonToolTips.InscriberBufferSize, ButtonToolTips.InscriberBufferLow);
            SettingToggleButton.registerApp(Icon.INSCRIBER_BUFFER_1, Settings.INSCRIBER_INPUT_CAPACITY, InscriberInputCapacity.ONE, ButtonToolTips.InscriberBufferSize, ButtonToolTips.InscriberBufferVeryLow);
        }
    }

    private static void onPress(Button btn) {
        if (btn instanceof SettingToggleButton) {
            ((SettingToggleButton)btn).triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = false;
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AEBaseScreen) {
            backwards = ((AEBaseScreen)currentScreen).isHandlingRightClick();
        }
        this.onPress.handle(this, backwards);
    }

    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val, ButtonToolTips title, Component ... tooltipLines) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)title.text());
        Collections.addAll(lines, tooltipLines);
        appearances.put(new EnumPair<T>(setting, val), new ButtonAppearance(icon, null, lines));
    }

    private static <T extends Enum<T>> void registerApp(ItemLike item, Setting<T> setting, T val, ButtonToolTips title, Component ... tooltipLines) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)title.text());
        Collections.addAll(lines, tooltipLines);
        appearances.put(new EnumPair<T>(setting, val), new ButtonAppearance(null, item.asItem(), lines));
    }

    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val, ButtonToolTips title, ButtonToolTips hint) {
        SettingToggleButton.registerApp(icon, setting, val, title, new Component[]{hint.text()});
    }

    @Nullable
    private ButtonAppearance getApperance() {
        if (this.buttonSetting != null && this.currentValue != null) {
            return appearances.get(new EnumPair<T>(this.buttonSetting, this.currentValue));
        }
        return null;
    }

    @Override
    protected Icon getIcon() {
        ButtonAppearance app = this.getApperance();
        if (app != null && app.icon != null) {
            return app.icon;
        }
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    @Override
    protected Item getItemOverlay() {
        ButtonAppearance app = this.getApperance();
        if (app != null && app.item != null) {
            return app.item;
        }
        return null;
    }

    public Setting<T> getSetting() {
        return this.buttonSetting;
    }

    public T getCurrentValue() {
        return this.currentValue;
    }

    public void set(T e) {
        if (this.currentValue != e) {
            this.currentValue = e;
        }
    }

    public T getNextValue(boolean backwards) {
        return EnumCycler.rotateEnum(this.currentValue, backwards, this.validValues);
    }

    @Override
    public List<Component> getTooltipMessage() {
        if (this.buttonSetting == null || this.currentValue == null) {
            return Collections.emptyList();
        }
        ButtonAppearance buttonAppearance = appearances.get(new EnumPair<T>(this.buttonSetting, this.currentValue));
        if (buttonAppearance == null) {
            return Collections.singletonList(ButtonToolTips.NoSuchMessage.text());
        }
        return buttonAppearance.tooltipLines;
    }

    @FunctionalInterface
    public static interface IHandler<T extends SettingToggleButton<?>> {
        public void handle(T var1, boolean var2);
    }

    private static final class EnumPair<T extends Enum<T>> {
        final Setting<T> setting;
        final T value;

        public EnumPair(Setting<T> setting, T value) {
            this.setting = setting;
            this.value = value;
        }

        public int hashCode() {
            return this.setting.hashCode() ^ ((Enum)this.value).hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            EnumPair other = (EnumPair)obj;
            return other.setting == this.setting && other.value == this.value;
        }
    }

    private record ButtonAppearance(@Nullable Icon icon, @Nullable Item item, List<Component> tooltipLines) {
    }
}

