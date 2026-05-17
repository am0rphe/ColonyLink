/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.parts.p2p;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.parts.PartModel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class P2PModels {
    public static final ResourceLocation MODEL_STATUS_OFF = AppEng.makeId("part/p2p/p2p_tunnel_status_off");
    public static final ResourceLocation MODEL_STATUS_ON = AppEng.makeId("part/p2p/p2p_tunnel_status_on");
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = AppEng.makeId("part/p2p/p2p_tunnel_status_has_channel");
    public static final ResourceLocation MODEL_FREQUENCY = AppEng.makeId("part/p2p/p2p_tunnel_frequency");
    private final IPartModel modelsOff;
    private final IPartModel modelsOn;
    private final IPartModel modelsHasChannel;

    public P2PModels(ResourceLocation frontModel) {
        this.modelsOff = new PartModel(MODEL_STATUS_OFF, MODEL_FREQUENCY, frontModel);
        this.modelsOn = new PartModel(MODEL_STATUS_ON, MODEL_FREQUENCY, frontModel);
        this.modelsHasChannel = new PartModel(MODEL_STATUS_HAS_CHANNEL, MODEL_FREQUENCY, frontModel);
    }

    public IPartModel getModel(boolean hasPower, boolean hasChannel) {
        if (hasPower && hasChannel) {
            return this.modelsHasChannel;
        }
        if (hasPower) {
            return this.modelsOn;
        }
        return this.modelsOff;
    }

    public List<IPartModel> getModels() {
        ArrayList<IPartModel> result = new ArrayList<IPartModel>();
        result.add(this.modelsOff);
        result.add(this.modelsOn);
        result.add(this.modelsHasChannel);
        return result;
    }
}

