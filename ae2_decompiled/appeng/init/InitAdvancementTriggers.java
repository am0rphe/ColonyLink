/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.CriterionTrigger
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.init;

import appeng.core.AppEng;
import appeng.core.stats.AdvancementTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public final class InitAdvancementTriggers {
    public static void init(Registry<CriterionTrigger<?>> registry) {
        Registry.register(registry, (ResourceLocation)AppEng.makeId("network_apprentice"), (Object)AdvancementTriggers.NETWORK_APPRENTICE);
        Registry.register(registry, (ResourceLocation)AppEng.makeId("network_engineer"), (Object)AdvancementTriggers.NETWORK_ENGINEER);
        Registry.register(registry, (ResourceLocation)AppEng.makeId("network_admin"), (Object)AdvancementTriggers.NETWORK_ADMIN);
        Registry.register(registry, (ResourceLocation)AppEng.makeId("spatial_explorer"), (Object)AdvancementTriggers.SPATIAL_EXPLORER);
        Registry.register(registry, (ResourceLocation)AppEng.makeId("recursive_networking"), (Object)AdvancementTriggers.RECURSIVE);
    }
}

