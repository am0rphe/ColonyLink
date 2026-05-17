/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.util.StringRepresentable
 */
package appeng.recipes.entropy;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EntropyMode implements StringRepresentable
{
    HEAT("heat"),
    COOL("cool");

    public static final Codec<EntropyMode> CODEC;
    private final String serializedName;

    private EntropyMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    static {
        CODEC = StringRepresentable.fromEnum(EntropyMode::values);
    }
}

