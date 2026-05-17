/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.util.StringRepresentable
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 */
package appeng.api.config;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum FuzzyMode implements StringRepresentable
{
    IGNORE_ALL(-1.0f),
    PERCENT_99(0.0f),
    PERCENT_75(25.0f),
    PERCENT_50(50.0f),
    PERCENT_25(75.0f);

    public static final Codec<FuzzyMode> CODEC;
    public static final StreamCodec<FriendlyByteBuf, FuzzyMode> STREAM_CODEC;
    public final float breakPoint;
    public final float percentage;

    private FuzzyMode(float p) {
        this.percentage = p;
        this.breakPoint = p / 100.0f;
    }

    public int calculateBreakPoint(int maxDamage) {
        return (int)(this.percentage * (float)maxDamage / 100.0f);
    }

    public String getSerializedName() {
        return this.name();
    }

    static {
        CODEC = StringRepresentable.fromEnum(FuzzyMode::values);
        STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(FuzzyMode.class);
    }
}

