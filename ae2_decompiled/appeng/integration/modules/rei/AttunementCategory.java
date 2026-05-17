/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  me.shedaniel.math.Point
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  me.shedaniel.rei.api.client.gui.widgets.Widget
 *  me.shedaniel.rei.api.client.gui.widgets.Widgets
 *  me.shedaniel.rei.api.client.registry.display.DisplayCategory
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.integration.modules.rei;

import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.rei.AttunementDisplay;
import appeng.integration.modules.rei.BackgroundRenderer;
import com.google.common.collect.Lists;
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
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AttunementCategory
implements DisplayCategory<AttunementDisplay> {
    public static final CategoryIdentifier<AttunementDisplay> ID = CategoryIdentifier.of((ResourceLocation)AppEng.makeId("attunement"));

    public Renderer getIcon() {
        return EntryStacks.of(AEParts.ME_P2P_TUNNEL);
    }

    public Component getTitle() {
        return ItemModText.P2P_TUNNEL_ATTUNEMENT.text();
    }

    public CategoryIdentifier<? extends AttunementDisplay> getCategoryIdentifier() {
        return ID;
    }

    public List<Widget> setupDisplay(AttunementDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);
        ArrayList widgets = Lists.newArrayList();
        widgets.add(Widgets.wrapRenderer((Rectangle)bounds, (Renderer)new BackgroundRenderer(this.getDisplayWidth((Display)display), this.getDisplayHeight())));
        widgets.add(Widgets.createArrow((Point)new Point(startPoint.x + 27, startPoint.y + 4)));
        widgets.add(Widgets.createResultSlotBackground((Point)new Point(startPoint.x + 61, startPoint.y + 5)));
        widgets.add(Widgets.createSlot((Point)new Point(startPoint.x + 4, startPoint.y + 5)).entries((Collection)display.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createSlot((Point)new Point(startPoint.x + 61, startPoint.y + 5)).entries((Collection)display.getOutputEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }

    public int getDisplayHeight() {
        return 36;
    }
}

