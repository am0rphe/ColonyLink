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

public abstract class AbstractDisplayPart
extends AbstractReportingPart {
    @PartModels
    protected static final ResourceLocation MODEL_BASE = AppEng.makeId("part/display_base");
    @PartModels
    protected static final ResourceLocation MODEL_STATUS_OFF = AppEng.makeId("part/display_status_off");
    @PartModels
    protected static final ResourceLocation MODEL_STATUS_ON = AppEng.makeId("part/display_status_on");
    @PartModels
    protected static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = AppEng.makeId("part/display_status_has_channel");

    public AbstractDisplayPart(IPartItem<?> partItem, boolean requireChannel) {
        super(partItem, requireChannel);
    }

    @Override
    public boolean isLightSource() {
        return false;
    }
}

