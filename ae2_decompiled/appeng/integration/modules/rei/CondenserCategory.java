/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  me.shedaniel.rei.api.client.gui.widgets.Slot
 *  me.shedaniel.rei.api.client.gui.widgets.Tooltip
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.gui.widgets.Widgets
 *  me.shedaniel.rei.api.client.registry.display.DisplayCategory
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.rei;

import appeng.api.config.CondenserOutput;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.integration.modules.rei.BackgroundRenderer;
import appeng.integration.modules.rei.CondenserOutputDisplay;
import appeng.integration.modules.rei.ProgressBarRenderer;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

class CondenserCategory
implements DisplayCategory<CondenserOutputDisplay> {
    private static final int PADDING = 7;
    private static final int WIDTH = 96;
    private static final int HEIGHT = 48;
    public static final CategoryIdentifier<CondenserOutputDisplay> ID = CategoryIdentifier.of((ResourceLocation)AppEng.makeId("condenser"));

    CondenserCategory() {
    }

    public Renderer getIcon() {
        return EntryStacks.of((ItemStack)AEBlocks.CONDENSER.stack());
    }

    public Component getTitle() {
        return Component.translatable((String)"gui.ae2.Condenser");
    }

    public CategoryIdentifier<? extends CondenserOutputDisplay> getCategoryIdentifier() {
        return ID;
    }

    public List<Widget> setupDisplay(CondenserOutputDisplay recipeDisplay, Rectangle bounds) {
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new BackgroundRenderer(this.getDisplayWidth(recipeDisplay), this.getDisplayHeight())));
        Point origin = new Point(bounds.x + 7, bounds.y + 7);
        ResourceLocation location = AppEng.makeId("textures/guis/condenser.png");
        widgets.add(Widgets.createTexturedWidget((ResourceLocation)location, (int)origin.x, (int)origin.y, (float)48.0f, (float)25.0f, (int)96, (int)48));
        ResourceLocation statesLocation = AppEng.makeId("textures/guis/states.png");
        widgets.add(Widgets.createTexturedWidget((ResourceLocation)statesLocation, (int)(origin.x + 4), (int)(origin.y + 28), (float)241.0f, (float)81.0f, (int)14, (int)14));
        widgets.add(Widgets.createTexturedWidget((ResourceLocation)statesLocation, (int)(origin.x + 80), (int)(origin.y + 28), (float)240.0f, (float)240.0f, (int)16, (int)16));
        widgets.add((Widget)Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new ProgressBarRenderer(location, origin.x + 72, origin.y, 6, 18, 176, 0)));
        if (recipeDisplay.getType() == CondenserOutput.MATTER_BALLS) {
            widgets.add(Widgets.createTexturedWidget((ResourceLocation)statesLocation, (int)(origin.x + 80), (int)(origin.y + 28), (float)16.0f, (float)112.0f, (int)14, (int)14));
        } else if (recipeDisplay.getType() == CondenserOutput.SINGULARITY) {
            widgets.add(Widgets.createTexturedWidget((ResourceLocation)statesLocation, (int)(origin.x + 80), (int)(origin.y + 28), (float)32.0f, (float)112.0f, (int)14, (int)14));
        }
        widgets.add(Widgets.createDrawableWidget((guiGraphics, mouseX, mouseY, delta) -> {
            Rectangle rect = new Rectangle(origin.x + 80, origin.y + 28, 16, 16);
            if (rect.contains(mouseX, mouseY)) {
                Tooltip.create((Collection)this.getTooltip(recipeDisplay.getType()).stream().map(Component::literal).collect(Collectors.toList())).queue();
            }
        }));
        Slot outputSlot = Widgets.createSlot((Point)new Point(origin.x + 57, origin.y + 27)).disableBackground().markOutput().entries((Collection)recipeDisplay.getOutputEntries().get(0));
        widgets.add((Widget)outputSlot);
        Slot storageCellSlot = Widgets.createSlot((Point)new Point(origin.x + 53, origin.y + 1)).disableBackground().markInput().entries(recipeDisplay.getViableStorageComponents());
        widgets.add((Widget)storageCellSlot);
        return widgets;
    }

    public int getDisplayWidth(CondenserOutputDisplay display) {
        return 110;
    }

    public int getDisplayHeight() {
        return 62;
    }

    private List<String> getTooltip(CondenserOutput type) {
        String key;
        switch (type) {
            case MATTER_BALLS: {
                key = "gui.tooltips.ae2.MatterBalls";
                break;
            }
            case SINGULARITY: {
                key = "gui.tooltips.ae2.Singularity";
                break;
            }
            default: {
                return Collections.emptyList();
            }
        }
        return Splitter.on((String)"\n").splitToList((CharSequence)Component.translatable((String)key, (Object[])new Object[]{type.requiredPower}).getString());
    }
}

