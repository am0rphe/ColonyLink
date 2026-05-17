/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.inventories;

import appeng.api.inventories.InternalInventory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ISegmentedInventory {
    public static final ResourceLocation CONFIG = ResourceLocation.parse((String)"ae2:config");
    public static final ResourceLocation UPGRADES = ResourceLocation.parse((String)"ae2:upgrades");
    public static final ResourceLocation STORAGE = ResourceLocation.parse((String)"ae2:storage");
    public static final ResourceLocation CELLS = ResourceLocation.parse((String)"ae2:cells");

    @Nullable
    public InternalInventory getSubInventory(ResourceLocation var1);
}

