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

public class PanelPart
extends AbstractPanelPart {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppEng.makeId("part/monitor_bright_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppEng.makeId("part/monitor_bright_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON);

    public PanelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected int getBrightnessColor() {
        return this.getColor().whiteVariant;
    }

    @Override
    public IPartModel getStaticModels() {
        return this.isPowered() ? MODELS_ON : MODELS_OFF;
    }
}

