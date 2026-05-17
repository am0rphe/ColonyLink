/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.parts.automation;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.parts.PartModel;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class PlaneModels {
    public static final ResourceLocation MODEL_CHASSIS_OFF = AppEng.makeId("part/transition_plane_off");
    public static final ResourceLocation MODEL_CHASSIS_ON = AppEng.makeId("part/transition_plane_on");
    public static final ResourceLocation MODEL_CHASSIS_HAS_CHANNEL = AppEng.makeId("part/transition_plane_has_channel");
    private final IPartModel modelOff;
    private final IPartModel modelOn;
    private final IPartModel modelHasChannel;

    public PlaneModels(String planeOffLocation, String planeOnLocation) {
        ResourceLocation planeOff = AppEng.makeId(planeOffLocation);
        ResourceLocation planeOn = AppEng.makeId(planeOnLocation);
        this.modelOff = new PartModel(MODEL_CHASSIS_OFF, planeOff);
        this.modelOn = new PartModel(MODEL_CHASSIS_ON, planeOff);
        this.modelHasChannel = new PartModel(MODEL_CHASSIS_HAS_CHANNEL, planeOn);
    }

    public IPartModel getModel(boolean hasPower, boolean hasChannel) {
        if (hasPower && hasChannel) {
            return this.modelHasChannel;
        }
        if (hasPower) {
            return this.modelOn;
        }
        return this.modelOff;
    }

    public List<IPartModel> getModels() {
        return ImmutableList.of((Object)this.modelOff, (Object)this.modelOn, (Object)this.modelHasChannel);
    }
}

