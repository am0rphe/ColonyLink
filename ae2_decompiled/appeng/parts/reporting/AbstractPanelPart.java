/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.parts.reporting;

import appeng.api.parts.IPartItem;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.reporting.AbstractReportingPart;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPanelPart
extends AbstractReportingPart {
    @PartModels
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("part/monitor_base");

    public AbstractPanelPart(IPartItem<?> partItem) {
        super(partItem, false);
    }

    @Override
    public boolean isLightSource() {
        return true;
    }

    protected abstract int getBrightnessColor();
}

