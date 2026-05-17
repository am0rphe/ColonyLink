/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  me.shedaniel.rei.api.client.gui.widgets.Label
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.gui.widgets.Widgets
 *  me.shedaniel.rei.api.client.registry.display.DisplayCategory
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.integration.modules.rei;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.rei.BackgroundRenderer;
import appeng.integration.modules.rei.EntropyRecipeDisplay;
import appeng.integration.modules.rei.ReiPlugin;
import appeng.recipes.entropy.EntropyMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class EntropyRecipeCategory
implements DisplayCategory<EntropyRecipeDisplay> {
    private static final int PADDING = 5;
    private static final int BODY_TEXT_COLOR = 0x7E7E7E;
    static final CategoryIdentifier<EntropyRecipeDisplay> ID = CategoryIdentifier.of((ResourceLocation)AppEng.makeId("ae2.entropy_manipulator"));

    public CategoryIdentifier<? extends EntropyRecipeDisplay> getCategoryIdentifier() {
        return ID;
    }

    public Renderer getIcon() {
        return (graphics, bounds, mouseX, mouseY, delta) -> graphics.blit(AppEng.makeId("textures/item/entropy_manipulator.png"), bounds.getX(), bounds.getY(), 0, 0.0f, 0.0f, 16, 16, 16, 16);
    }

    public Component getTitle() {
        return AEItems.ENTROPY_MANIPULATOR.asItem().getDescription();
    }

    public List<Widget> setupDisplay(EntropyRecipeDisplay recipe, Rectangle bounds) {
        EntropyMode mode = recipe.getRecipe().getMode();
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new BackgroundRenderer(this.getDisplayWidth(recipe), this.getDisplayHeight())));
        int centerX = bounds.getCenterX();
        int y = bounds.getY() + 5;
        MutableComponent labelText = switch (mode) {
            default -> throw new MatchException(null, null);
            case EntropyMode.HEAT -> ItemModText.ENTROPY_MANIPULATOR_HEAT.text(1600);
            case EntropyMode.COOL -> ItemModText.ENTROPY_MANIPULATOR_COOL.text(1600);
        };
        MutableComponent interaction = switch (mode) {
            default -> throw new MatchException(null, null);
            case EntropyMode.HEAT -> ItemModText.RIGHT_CLICK.text();
            case EntropyMode.COOL -> ItemModText.SHIFT_RIGHT_CLICK.text();
        };
        Label modeLabel = Widgets.createLabel((Point)new Point(centerX + 4, y + 2), (Component)labelText).color(0x7E7E7E).noShadow().centered();
        int modeLabelX = modeLabel.getBounds().x;
        widgets.add((Widget)modeLabel);
        Widget modeIcon = switch (mode) {
            default -> throw new MatchException(null, null);
            case EntropyMode.HEAT -> Widgets.createTexturedWidget((ResourceLocation)ReiPlugin.TEXTURE, (int)(modeLabelX - 9), (int)(y + 3), (float)0.0f, (float)68.0f, (int)6, (int)6);
            case EntropyMode.COOL -> Widgets.createTexturedWidget((ResourceLocation)ReiPlugin.TEXTURE, (int)(modeLabelX - 9), (int)(y + 3), (float)6.0f, (float)68.0f, (int)6, (int)6);
        };
        widgets.add(modeIcon);
        widgets.add((Widget)Widgets.createArrow((Point)new Point(centerX - 12, y + 14)));
        widgets.add((Widget)Widgets.createLabel((Point)new Point(centerX, y + 38), (Component)interaction).color(0x7E7E7E).noShadow().centered());
        widgets.add((Widget)Widgets.createSlot((Point)new Point(centerX - 34, y + 15)).entries((Collection)recipe.getInput()).markInput());
        int x = centerX + 20;
        for (EntryIngredient entries : recipe.getConsumed()) {
            widgets.add((Widget)Widgets.createSlot((Point)new Point(x, y + 15)).entries((Collection)entries));
            x += 18;
        }
        for (EntryIngredient entries : recipe.getOutputEntries()) {
            widgets.add((Widget)Widgets.createSlot((Point)new Point(x, y + 15)).entries((Collection)entries).markOutput());
            x += 18;
        }
        return widgets;
    }

    public int getDisplayHeight() {
        return 60;
    }

    public int getDisplayWidth(EntropyRecipeDisplay display) {
        return 140;
    }
}

