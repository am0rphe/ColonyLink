/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 */
package appeng.client.render.effects;

import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.LightningArcParticleData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class ParticleTypes {
    public static final SimpleParticleType CRAFTING = new SimpleParticleType(false);
    public static final ParticleType<EnergyParticleData> ENERGY = new ParticleType<EnergyParticleData>(false){

        public MapCodec<EnergyParticleData> codec() {
            return EnergyParticleData.CODEC;
        }

        public StreamCodec<? super RegistryFriendlyByteBuf, EnergyParticleData> streamCodec() {
            return EnergyParticleData.STREAM_CODEC;
        }
    };
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = new ParticleType<LightningArcParticleData>(false){

        public MapCodec<LightningArcParticleData> codec() {
            return LightningArcParticleData.CODEC;
        }

        public StreamCodec<? super RegistryFriendlyByteBuf, LightningArcParticleData> streamCodec() {
            return LightningArcParticleData.STREAM_CODEC;
        }
    };
    public static final SimpleParticleType LIGHTNING = new SimpleParticleType(false);
    public static final SimpleParticleType MATTER_CANNON = new SimpleParticleType(false);
    public static final SimpleParticleType VIBRANT = new SimpleParticleType(false);

    private ParticleTypes() {
    }
}

