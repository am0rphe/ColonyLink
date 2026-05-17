/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.CreativeModeTab
 */
package appeng.api.ids;

import appeng.core.AppEng;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public final class AECreativeTabIds {
    public static final ResourceKey<CreativeModeTab> MAIN = AECreativeTabIds.create("main");
    public static final ResourceKey<CreativeModeTab> FACADES = AECreativeTabIds.create("facades");

    private AECreativeTabIds() {
    }

    private static ResourceKey<CreativeModeTab> create(String path) {
        return ResourceKey.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (ResourceLocation)AppEng.makeId(path));
    }
}

