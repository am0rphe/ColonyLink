/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.Mth
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.implementations;

import appeng.api.client.AEKeyRendering;
import appeng.api.config.LockCraftingMode;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class PatternProviderLockReason
implements ICompositeWidget {
    protected boolean visible = false;
    protected int x;
    protected int y;
    private final PatternProviderScreen<?> screen;

    public PatternProviderLockReason(PatternProviderScreen<?> screen) {
        this.screen = screen;
    }

    @Override
    public void setPosition(Point position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(this.x, this.y, 126, 16);
    }

    @Override
    public final boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        MutableComponent lockStatusText;
        Icon icon;
        PatternProviderMenu menu = (PatternProviderMenu)this.screen.getMenu();
        if (menu.getCraftingLockedReason() == LockCraftingMode.NONE) {
            icon = Icon.UNLOCKED;
            lockStatusText = GuiText.CraftingLockIsUnlocked.text().setStyle(Style.EMPTY.withColor(Mth.color((float)0.49019608f, (float)0.6627451f, (float)0.8235294f)));
        } else {
            icon = Icon.LOCKED;
            lockStatusText = GuiText.CraftingLockIsLocked.text().setStyle(Style.EMPTY.withColor(Mth.color((float)0.75686276f, (float)0.25882354f, (float)0.29411766f)));
        }
        icon.getBlitter().dest(this.x, this.y).blit(guiGraphics);
        guiGraphics.drawString(Minecraft.getInstance().font, (Component)lockStatusText, this.x + 15, this.y + 5, -1, false);
    }

    @Override
    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        PatternProviderMenu menu = (PatternProviderMenu)this.screen.getMenu();
        MutableComponent tooltip = switch (menu.getCraftingLockedReason()) {
            default -> throw new MatchException(null, null);
            case LockCraftingMode.NONE -> null;
            case LockCraftingMode.LOCK_UNTIL_PULSE -> InGameTooltip.CraftingLockedUntilPulse.text();
            case LockCraftingMode.LOCK_WHILE_HIGH -> InGameTooltip.CraftingLockedByRedstoneSignal.text();
            case LockCraftingMode.LOCK_WHILE_LOW -> InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
            case LockCraftingMode.LOCK_UNTIL_RESULT -> {
                MutableComponent stackAmount;
                MutableComponent stackName;
                GenericStack stack = menu.getUnlockStack();
                if (stack != null) {
                    stackName = AEKeyRendering.getDisplayName(stack.what());
                    stackAmount = Component.literal((String)stack.what().formatAmount(stack.amount(), AmountFormat.FULL));
                } else {
                    stackName = Component.literal((String)"ERROR");
                    stackAmount = Component.literal((String)"ERROR");
                }
                yield InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount);
            }
        };
        return tooltip != null ? new Tooltip(new Component[]{tooltip}) : null;
    }
}

