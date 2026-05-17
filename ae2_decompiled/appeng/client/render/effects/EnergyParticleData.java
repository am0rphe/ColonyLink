/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package appeng.client.render.effects;

import appeng.client.render.effects.ParticleTypes;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EnergyParticleData(boolean forItem, Direction direction) implements ParticleOptions
{
    public static final MapCodec<EnergyParticleData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)Codec.BOOL.fieldOf("forItem").forGetter(EnergyParticleData::forItem), (App)Direction.CODEC.fieldOf("direction").forGetter(EnergyParticleData::direction)).apply((Applicative)builder, EnergyParticleData::new));
    public static final StreamCodec<FriendlyByteBuf, EnergyParticleData> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.BOOL, EnergyParticleData::forItem, (StreamCodec)Direction.STREAM_CODEC, EnergyParticleData::direction, EnergyParticleData::new);
    public static final EnergyParticleData FOR_BLOCK = new EnergyParticleData(false, Direction.UP);

    public ParticleType<?> getType() {
        return ParticleTypes.ENERGY;
    }
}

