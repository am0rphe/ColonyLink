/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 */
package appeng.api.integrations.igtooltip;

import appeng.api.integrations.igtooltip.BaseClassRegistration;
import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.OverrideOnly
public interface TooltipProvider {
    public static final int DEFAULT_PRIORITY = 1000;
    public static final int DEBUG_PRIORITY = 5000;

    default public void registerCommon(CommonRegistration registration) {
    }

    default public void registerClient(ClientRegistration registration) {
    }

    default public void registerBlockEntityBaseClasses(BaseClassRegistration registration) {
    }
}

