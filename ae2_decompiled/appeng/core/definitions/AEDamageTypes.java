/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.worldgen.BootstrapContext
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageType
 */
package appeng.core.definitions;

import appeng.core.AppEng;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class AEDamageTypes {
    public static final ResourceKey<DamageType> MATTER_CANNON = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)AppEng.makeId("matter_cannon"));

    public static void init(BootstrapContext<DamageType> context) {
        context.register(MATTER_CANNON, (Object)new DamageType("matter_cannon", 0.1f));
    }
}

