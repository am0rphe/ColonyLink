/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.parts.misc;

import appeng.api.networking.GridFlags;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.ToggleBusPart;
import net.minecraft.resources.ResourceLocation;

public class InvertedToggleBusPart
extends ToggleBusPart {
    @PartModels
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("part/inverted_toggle_bus_base");
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_STATUS_OFF);
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_STATUS_ON);
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_STATUS_HAS_CHANNEL);

    public InvertedToggleBusPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.getOuterNode().setIdlePowerUsage(0.0);
        this.getMainNode().setFlags(new GridFlags[0]);
        this.getOuterNode().setFlags(new GridFlags[0]);
    }

    @Override
    protected boolean isEnabled() {
        return !super.isEnabled();
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isEnabled() && this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        if (this.isEnabled() && this.isPowered()) {
            return MODELS_ON;
        }
        return MODELS_OFF;
    }
}

