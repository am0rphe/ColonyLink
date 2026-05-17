/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 */
package appeng.sounds;

import appeng.core.AppEng;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class AppEngSounds {
    public static final ResourceLocation GUIDE_CLICK_ID = AppEng.makeId("guide.click");
    public static SoundEvent GUIDE_CLICK_EVENT = SoundEvent.createVariableRangeEvent((ResourceLocation)GUIDE_CLICK_ID);

    public static void register(Registry<SoundEvent> registry) {
        Registry.register(registry, (ResourceLocation)GUIDE_CLICK_ID, (Object)GUIDE_CLICK_EVENT);
    }
}

