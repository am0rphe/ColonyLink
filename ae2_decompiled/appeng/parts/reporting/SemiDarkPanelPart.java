/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.parts.reporting;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractPanelPart;
import net.minecraft.resources.ResourceLocation;

public class SemiDarkPanelPart
extends AbstractPanelPart {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppEng.makeId("part/monitor_medium_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppEng.makeId("part/monitor_medium_on");
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON);

    public SemiDarkPanelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected int getBrightnessColor() {
        int light = this.getColor().whiteVariant;
        int dark = this.getColor().mediumVariant;
        return ((light >> 16 & 0xFF) + (dark >> 16 & 0xFF)) / 2 << 16 | ((light >> 8 & 0xFF) + (dark >> 8 & 0xFF)) / 2 << 8 | ((light & 0xFF) + (dark & 0xFF)) / 2;
    }

    @Override
    public IPartModel getStaticModels() {
        return this.isPowered() ? MODELS_ON : MODELS_OFF;
    }
}

