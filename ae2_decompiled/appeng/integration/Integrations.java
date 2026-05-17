/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.event.lifecycle.InterModEnqueueEvent
 */
package appeng.integration;

import appeng.integration.modules.theoneprobe.TOP;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

public class Integrations {
    public static void enqueueIMC(InterModEnqueueEvent event) {
        TOP.enqueueIMC(event);
    }
}

