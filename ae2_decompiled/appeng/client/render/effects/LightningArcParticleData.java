/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.util.ExtraCodecs
 *  org.joml.Vector3f
 */
package appeng.client.render.effects;

import appeng.client.render.effects.ParticleTypes;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public record LightningArcParticleData(Vector3f target) implements ParticleOptions
{
    public static final MapCodec<LightningArcParticleData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)ExtraCodecs.VECTOR3F.fieldOf("target").forGetter(LightningArcParticleData::target)).apply((Applicative)builder, LightningArcParticleData::new));
    public static final StreamCodec<ByteBuf, LightningArcParticleData> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VECTOR3F, d -> d.target, LightningArcParticleData::new);

    public ParticleType<?> getType() {
        return ParticleTypes.LIGHTNING_ARC;
    }
}

