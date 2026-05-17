/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.init.client;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AppEng;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;

public final class InitParticleTypes {
    private InitParticleTypes() {
    }

    public static void init(Registry<ParticleType<?>> registry) {
        InitParticleTypes.register(registry, ParticleTypes.CRAFTING, "crafting_fx");
        InitParticleTypes.register(registry, ParticleTypes.ENERGY, "energy_fx");
        InitParticleTypes.register(registry, ParticleTypes.LIGHTNING_ARC, "lightning_arc_fx");
        InitParticleTypes.register(registry, ParticleTypes.LIGHTNING, "lightning_fx");
        InitParticleTypes.register(registry, ParticleTypes.MATTER_CANNON, "matter_cannon_fx");
        InitParticleTypes.register(registry, ParticleTypes.VIBRANT, "vibrant_fx");
    }

    private static void register(Registry<ParticleType<?>> registry, ParticleType<?> type, String name) {
        Registry.register(registry, (ResourceLocation)AppEng.makeId(name), type);
    }
}

