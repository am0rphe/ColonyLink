/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package appeng.api.integrations.igtooltip;

import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.parts.PartTooltipProviders;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class PartTooltips {
    private PartTooltips() {
    }

    public static <T> void addServerData(Class<T> baseClass, ServerDataProvider<? super T> provider) {
        PartTooltips.addServerData(baseClass, provider, 1000);
    }

    public static <T> void addServerData(Class<T> baseClass, ServerDataProvider<? super T> provider, int priority) {
        PartTooltipProviders.addServerData(baseClass, provider, priority);
    }

    public static <T> void addBody(Class<T> baseClass, BodyProvider<? super T> provider) {
        PartTooltips.addBody(baseClass, provider, 1000);
    }

    public static <T> void addBody(Class<T> baseClass, BodyProvider<? super T> provider, int priority) {
        PartTooltipProviders.addBody(baseClass, provider, priority);
    }

    public static <T> void addName(Class<T> baseClass, NameProvider<? super T> provider) {
        PartTooltips.addName(baseClass, provider, 1000);
    }

    public static <T> void addName(Class<T> baseClass, NameProvider<? super T> provider, int priority) {
        PartTooltipProviders.addName(baseClass, provider, priority);
    }

    public static <T> void addIcon(Class<T> baseClass, IconProvider<? super T> provider) {
        PartTooltips.addIcon(baseClass, provider, 1000);
    }

    public static <T> void addIcon(Class<T> baseClass, IconProvider<? super T> provider, int priority) {
        PartTooltipProviders.addIcon(baseClass, provider, priority);
    }
}

