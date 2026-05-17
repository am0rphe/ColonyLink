/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.InterModComms
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.event.lifecycle.InterModEnqueueEvent
 */
package appeng.integration.modules.theoneprobe;

import appeng.integration.modules.theoneprobe.TheOneProbeModule;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

public class TOP {
    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo((String)"theoneprobe", (String)"getTheOneProbe", TheOneProbeModule::new);
        }
    }
}

