/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.EmiExclusionArea
 *  dev.emi.emi.api.widget.Bounds
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.Rect2i
 */
package appeng.integration.modules.emi;

import appeng.client.gui.AEBaseScreen;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

class EmiAeBaseScreenExclusionZones
implements EmiExclusionArea<Screen> {
    EmiAeBaseScreenExclusionZones() {
    }

    public void addExclusionArea(Screen screen, Consumer<Bounds> consumer) {
        if (!(screen instanceof AEBaseScreen)) {
            return;
        }
        AEBaseScreen aeScreen = (AEBaseScreen)screen;
        for (Rect2i zone : aeScreen.getExclusionZones()) {
            consumer.accept(new Bounds(zone.getX(), zone.getY(), zone.getWidth(), zone.getHeight()));
        }
    }
}

