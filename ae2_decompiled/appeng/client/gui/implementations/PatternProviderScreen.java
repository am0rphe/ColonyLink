/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Inventory
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.client.gui.implementations;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.PatternProviderLockReason;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.GuiText;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class PatternProviderScreen<C extends PatternProviderMenu>
extends AEBaseScreen<C> {
    private final SettingToggleButton<YesNo> blockingModeButton = new ServerSettingToggleButton<YesNo>(Settings.BLOCKING_MODE, YesNo.NO);
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;
    private final ToggleButton showInPatternAccessTerminalButton;
    private final PatternProviderLockReason lockReason;

    public PatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(this.blockingModeButton);
        this.lockCraftingModeButton = new ServerSettingToggleButton<LockCraftingMode>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.addToLeftToolbar(this.lockCraftingModeButton);
        this.widgets.addOpenPriorityButton();
        this.showInPatternAccessTerminalButton = new ToggleButton(Icon.PATTERN_ACCESS_SHOW, Icon.PATTERN_ACCESS_HIDE, (Component)GuiText.PatternAccessTerminal.text(), (Component)GuiText.PatternAccessTerminalHint.text(), btn -> this.selectNextPatternProviderMode());
        this.addToLeftToolbar(this.showInPatternAccessTerminalButton);
        this.lockReason = new PatternProviderLockReason(this);
        this.widgets.add("lockReason", this.lockReason);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.lockReason.setVisible(((PatternProviderMenu)this.menu).getLockCraftingMode() != LockCraftingMode.NONE);
        this.blockingModeButton.set(((PatternProviderMenu)this.menu).getBlockingMode());
        this.lockCraftingModeButton.set(((PatternProviderMenu)this.menu).getLockCraftingMode());
        this.showInPatternAccessTerminalButton.setState(((PatternProviderMenu)this.menu).getShowInAccessTerminal() == YesNo.YES);
    }

    private void selectNextPatternProviderMode() {
        boolean backwards = this.isHandlingRightClick();
        ConfigButtonPacket message = new ConfigButtonPacket(Settings.PATTERN_ACCESS_TERMINAL, backwards);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

