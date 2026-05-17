/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.StonecutterRecipe
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.me.items;

import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.Nullable;

public final class StonecuttingEncodingPanel
extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 140, 124, 66);
    private static final Blitter BG_SLOT = BG.copy().src(124, 140, 20, 22);
    private static final Blitter BG_SLOT_SELECTED = BG.copy().src(124, 162, 20, 22);
    private static final Blitter BG_SLOT_HOVER = BG.copy().src(124, 184, 20, 22);
    private static final int COLS = 4;
    private static final int ROWS = 2;
    private final Scrollbar scrollbar;

    public StonecuttingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
        this.scrollbar = widgets.addScrollBar("stonecuttingPatternModeScrollbar", Scrollbar.SMALL);
        this.scrollbar.setRange(0, 0, 4);
        this.scrollbar.setCaptureMouseWheel(false);
    }

    @Override
    public void updateBeforeRender() {
        int totalRows = (this.menu.getStonecuttingRecipes().size() + 4 - 1) / 4;
        this.scrollbar.setRange(0, totalRows - 2, 2);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 8, bounds.getY() + bounds.getHeight() - 165).blit(guiGraphics);
        this.drawRecipes(guiGraphics, bounds, mouse);
    }

    private RegistryAccess getRegistryAccess() {
        return Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
    }

    private void drawRecipes(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        List<RecipeHolder<StonecutterRecipe>> recipes = this.menu.getStonecuttingRecipes();
        int startIndex = this.scrollbar.getCurrentScroll() * 4;
        int endIndex = startIndex + 8;
        ResourceLocation selectedRecipe = this.menu.getStonecuttingRecipeId();
        for (int i = startIndex; i < endIndex && i < recipes.size(); ++i) {
            Rect2i slotBounds = this.getRecipeBounds(i - startIndex);
            RecipeHolder<StonecutterRecipe> recipe = recipes.get(i);
            boolean selected = selectedRecipe != null && selectedRecipe.equals((Object)recipe.id());
            Blitter blitter = BG_SLOT;
            if (selected) {
                blitter = BG_SLOT_SELECTED;
            } else if (mouse.isIn(slotBounds)) {
                blitter = BG_SLOT_HOVER;
            }
            int renderX = bounds.getX() + slotBounds.getX();
            int renderY = bounds.getY() + slotBounds.getY();
            blitter.dest(renderX, renderY).blit(guiGraphics);
            ItemStack resultItem = ((StonecutterRecipe)recipe.value()).getResultItem((HolderLookup.Provider)this.getRegistryAccess());
            if (selected || mouse.isIn(slotBounds)) {
                guiGraphics.renderItem(resultItem, renderX + 2, renderY + 3);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, resultItem, renderX + 2, renderY + 3);
                continue;
            }
            guiGraphics.renderItem(resultItem, renderX + 2, renderY + 2);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, resultItem, renderX + 2, renderY + 2);
        }
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        RecipeHolder<StonecutterRecipe> recipe = this.getRecipeAt(mousePos);
        if (recipe != null) {
            this.menu.setStonecuttingRecipeId(recipe.id());
            Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.UI_STONECUTTER_SELECT_RECIPE, (float)1.0f));
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        RecipeHolder<StonecutterRecipe> recipe = this.getRecipeAt(new Point(mouseX, mouseY));
        if (recipe != null) {
            List<Component> lines = this.screen.getTooltipFromContainerItem(((StonecutterRecipe)recipe.value()).getResultItem((HolderLookup.Provider)this.getRegistryAccess()));
            return new Tooltip(lines);
        }
        return null;
    }

    @Nullable
    private RecipeHolder<StonecutterRecipe> getRecipeAt(Point point) {
        List<RecipeHolder<StonecutterRecipe>> recipes = this.menu.getStonecuttingRecipes();
        if (!recipes.isEmpty()) {
            int startIndex = this.scrollbar.getCurrentScroll() * 4;
            int endIndex = startIndex + 8;
            for (int i = startIndex; i < endIndex && i < recipes.size(); ++i) {
                Rect2i slotBounds = this.getRecipeBounds(i - startIndex);
                if (!point.isIn(slotBounds)) continue;
                return recipes.get(i);
            }
        }
        return null;
    }

    private Rect2i getRecipeBounds(int index) {
        int col = index % 4;
        int row = index / 4;
        int slotX = this.x + 26 + col * BG_SLOT.getSrcWidth();
        int slotY = this.y + 12 + row * BG_SLOT.getSrcHeight();
        return new Rect2i(slotX, slotY, BG_SLOT.getSrcWidth(), BG_SLOT.getSrcHeight());
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        return this.scrollbar.onMouseWheel(mousePos, delta);
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_STONECUTTING;
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.StonecuttingPattern.text();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.scrollbar.setVisible(visible);
        this.screen.setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, !visible);
    }
}

