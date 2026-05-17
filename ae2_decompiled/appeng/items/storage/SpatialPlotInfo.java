/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package appeng.items.storage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SpatialPlotInfo(int id, BlockPos size) {
    public static final Codec<SpatialPlotInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)Codec.INT.fieldOf("id").forGetter(SpatialPlotInfo::id), (App)BlockPos.CODEC.fieldOf("size").forGetter(SpatialPlotInfo::size)).apply((Applicative)builder, SpatialPlotInfo::new));
    public static final StreamCodec<FriendlyByteBuf, SpatialPlotInfo> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, SpatialPlotInfo::id, (StreamCodec)BlockPos.STREAM_CODEC, SpatialPlotInfo::size, SpatialPlotInfo::new);
}

