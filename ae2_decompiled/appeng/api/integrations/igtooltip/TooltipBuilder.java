/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package appeng.api.integrations.igtooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface TooltipBuilder {
    public void addLine(Component var1);

    public void addLine(Component var1, ResourceLocation var2);
}

