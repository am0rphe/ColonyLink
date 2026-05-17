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
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.rei;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.integration.modules.rei.BackgroundRenderer;
import appeng.integration.modules.rei.InscriberRecipeDisplay;
import appeng.integration.modules.rei.ProgressBarRenderer;
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
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

class InscriberRecipeCategory
implements DisplayCategory<InscriberRecipeDisplay> {
    private static final int PADDING = 5;
    private static final int SLOT_INPUT_TOP = 0;
    private static final int SLOT_INPUT_MIDDLE = 1;
    private static final int SLOT_INPUT_BOTTOM = 2;
    private static final int WIDTH = 105;
    private static final int HEIGHT = 54;
    static final CategoryIdentifier<InscriberRecipeDisplay> ID = CategoryIdentifier.of((ResourceLocation)AppEng.makeId("ae2.inscriber"));

    InscriberRecipeCategory() {
    }

    public Renderer getIcon() {
        return EntryStacks.of((ItemStack)AEBlocks.INSCRIBER.stack());
    }

    public Component getTitle() {
        return AEBlocks.INSCRIBER.asItem().getDescription();
    }

    public CategoryIdentifier<InscriberRecipeDisplay> getCategoryIdentifier() {
        return ID;
    }

    public List<Widget> setupDisplay(InscriberRecipeDisplay recipeDisplay, Rectangle bounds) {
        ResourceLocation location = AppEng.makeId("textures/guis/inscriber.png");
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new BackgroundRenderer(this.getDisplayWidth(recipeDisplay), this.getDisplayHeight())));
        int innerX = bounds.x + 5;
        int innerY = bounds.y + 5;
        widgets.add(Widgets.createTexturedWidget((ResourceLocation)location, (int)innerX, (int)innerY, (float)36.0f, (float)20.0f, (int)105, (int)54));
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new ProgressBarRenderer(location, innerX + 100, innerY + 19, 6, 18, 177, 0)));
        List<EntryIngredient> ingredients = recipeDisplay.getInputEntries();
        EntryIngredient output = recipeDisplay.getOutputEntries().get(0);
        widgets.add((Widget)Widgets.createSlot((Point)new Point(innerX + 3, innerY + 3)).disableBackground().markInput().entries((Collection)ingredients.get(0)));
        widgets.add((Widget)Widgets.createSlot((Point)new Point(innerX + 27, innerY + 19)).disableBackground().markInput().entries((Collection)ingredients.get(1)));
        widgets.add((Widget)Widgets.createSlot((Point)new Point(innerX + 3, innerY + 35)).disableBackground().markInput().entries((Collection)ingredients.get(2)));
        widgets.add((Widget)Widgets.createSlot((Point)new Point(innerX + 77, innerY + 20)).disableBackground().markOutput().entries((Collection)output));
        return widgets;
    }

    public int getDisplayHeight() {
        return 64;
    }

    public int getDisplayWidth(InscriberRecipeDisplay display) {
        return 115;
    }
}

