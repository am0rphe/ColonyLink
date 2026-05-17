/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.gui.widgets.Widgets
 *  me.shedaniel.rei.api.client.registry.display.DisplayCategory
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.util.EntryIngredients
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 */
package appeng.integration.modules.rei;

import appeng.core.definitions.AEBlocks;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.rei.BackgroundRenderer;
import appeng.integration.modules.rei.ChargerDisplay;
import appeng.recipes.handlers.ChargerRecipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ChargerCategory
implements DisplayCategory<ChargerDisplay> {
    private final Renderer icon = EntryStacks.of((ItemStack)AEBlocks.CHARGER.stack());

    public CategoryIdentifier<? extends ChargerDisplay> getCategoryIdentifier() {
        return ChargerDisplay.ID;
    }

    public Component getTitle() {
        return AEBlocks.CHARGER.stack().getHoverName();
    }

    public Renderer getIcon() {
        return this.icon;
    }

    public List<Widget> setupDisplay(ChargerDisplay display, Rectangle bounds) {
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        int x = bounds.x;
        int y = bounds.y;
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new BackgroundRenderer(this.getDisplayWidth(display), this.getDisplayHeight())));
        ChargerRecipe recipe = (ChargerRecipe)display.holder().value();
        widgets.add((Widget)Widgets.createSlot((Point)new Point(x + 31, y + 8)).markInput().backgroundEnabled(true).entries((Collection)EntryIngredients.ofIngredient((Ingredient)recipe.getIngredient())));
        widgets.add((Widget)Widgets.createSlot((Point)new Point(x + 81, y + 8)).markOutput().backgroundEnabled(true).entry(EntryStacks.of((ItemStack)recipe.getResultItem())));
        widgets.add((Widget)Widgets.createSlot((Point)new Point(x + 3, y + 30)).unmarkInputOrOutput().backgroundEnabled(false).entry(EntryStacks.of((ItemStack)AEBlocks.CRANK.stack())));
        widgets.add((Widget)Widgets.createArrow((Point)new Point(x + 52, y + 8)));
        int turns = 10;
        widgets.add((Widget)Widgets.createLabel((Point)new Point(x + 20, y + 35), (Component)ItemModText.CHARGER_REQUIRED_POWER.text(turns, 1600)).color(0x7E7E7E).noShadow().leftAligned());
        return widgets;
    }

    public int getDisplayWidth(ChargerDisplay display) {
        return 130;
    }

    public int getDisplayHeight() {
        return 50;
    }
}

