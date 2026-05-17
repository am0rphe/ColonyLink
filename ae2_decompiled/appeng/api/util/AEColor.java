/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.util.StringRepresentable
 *  net.minecraft.world.item.DyeColor
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 */
package appeng.api.util;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum AEColor implements StringRepresentable
{
    WHITE("White", "gui.ae2.White", "white", DyeColor.WHITE, 0xB4B4B4, 0xE0E0E0, 0xF9F9F9, 0),
    LIGHT_GRAY("Light Gray", "gui.ae2.LightGray", "light_gray", DyeColor.LIGHT_GRAY, 0x7E7E7E, 10526624, 0xC4C4C4, 0),
    GRAY("Gray", "gui.ae2.Gray", "gray", DyeColor.GRAY, 0x4F4F4F, 0x6C6B6C, 0x949294, 0),
    BLACK("Black", "gui.ae2.Black", "black", DyeColor.BLACK, 0x131313, 0x272727, 0x3B3B3B, 0xFFFFFF),
    LIME("Lime", "gui.ae2.Lime", "lime", DyeColor.LIME, 5161038, 7397977, 11794541, 0),
    YELLOW("Yellow", "gui.ae2.Yellow", "yellow", DyeColor.YELLOW, 16764736, 16769881, 16056192, 0),
    ORANGE("Orange", "gui.ae2.Orange", "orange", DyeColor.ORANGE, 14252079, 15508028, 15907401, 0),
    BROWN("Brown", "gui.ae2.Brown", "brown", DyeColor.BROWN, 7227922, 8281110, 9334298, 0),
    RED("Red", "gui.ae2.Red", "red", DyeColor.RED, 11149611, 14106178, 15758949, 0),
    PINK("Pink", "gui.ae2.Pink", "pink", DyeColor.PINK, 14184106, 0xFF99BB, 16501461, 0),
    MAGENTA("Magenta", "gui.ae2.Magenta", "magenta", DyeColor.MAGENTA, 12669321, 13988252, 15113919, 0),
    PURPLE("Purple", "gui.ae2.Purple", "purple", DyeColor.PURPLE, 7232696, 9526733, 11562973, 0),
    BLUE("Blue", "gui.ae2.Blue", "blue", DyeColor.BLUE, 3375088, 3708159, 4243967, 0),
    LIGHT_BLUE("Light Blue", "gui.ae2.LightBlue", "light_blue", DyeColor.LIGHT_BLUE, 6928895, 7394047, 8452095, 0),
    CYAN("Cyan", "gui.ae2.Cyan", "cyan", DyeColor.CYAN, 2273454, 3132599, 6678729, 0),
    GREEN("Green", "gui.ae2.Green", "green", DyeColor.GREEN, 498539, 1554541, 3332176, 0),
    TRANSPARENT("Fluix", "gui.ae2.Fluix", "fluix", null, 5916574, 9526733, 14853091, 0);

    public static final Codec<AEColor> CODEC;
    public static final StreamCodec<FriendlyByteBuf, AEColor> STREAM_CODEC;
    public static final List<AEColor> VALID_COLORS;
    public static final int TINTINDEX_DARK = 1;
    public static final int TINTINDEX_MEDIUM = 2;
    public static final int TINTINDEX_BRIGHT = 3;
    public static final int TINTINDEX_MEDIUM_BRIGHT = 4;
    public final String translationKey;
    public final int blackVariant;
    public final int mediumVariant;
    public final int whiteVariant;
    public final DyeColor dye;
    public final String registryPrefix;
    public final String englishName;
    public final int contrastTextColor;

    private AEColor(String englishName, String translationKey, String registryPrefix, DyeColor dye, int blackHex, int medHex, int whiteHex, int contrastTextColor) {
        this.englishName = englishName;
        this.translationKey = translationKey;
        this.registryPrefix = registryPrefix;
        this.blackVariant = blackHex;
        this.mediumVariant = medHex;
        this.whiteVariant = whiteHex;
        this.contrastTextColor = contrastTextColor;
        this.dye = dye;
    }

    public static AEColor fromDye(DyeColor vanillaDye) {
        for (AEColor value : AEColor.values()) {
            if (value.dye != vanillaDye) continue;
            return value;
        }
        throw new IllegalArgumentException("Unknown Vanilla dye: " + String.valueOf(vanillaDye));
    }

    public int getVariantByTintIndex(int tintIndex) {
        switch (tintIndex) {
            case 0: {
                return -1;
            }
            case 1: {
                return this.blackVariant;
            }
            case 2: {
                return this.mediumVariant;
            }
            case 3: {
                return this.whiteVariant;
            }
            case 4: {
                int light = this.whiteVariant;
                int dark = this.mediumVariant;
                return ((light >> 16 & 0xFF) + (dark >> 16 & 0xFF)) / 2 << 16 | ((light >> 8 & 0xFF) + (dark >> 8 & 0xFF)) / 2 << 8 | ((light & 0xFF) + (dark & 0xFF)) / 2;
            }
        }
        return -1;
    }

    public String getEnglishName() {
        return this.englishName;
    }

    public String toString() {
        return this.translationKey;
    }

    public String getSerializedName() {
        return this.registryPrefix;
    }

    static {
        CODEC = StringRepresentable.fromEnum(AEColor::values);
        STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(AEColor.class);
        VALID_COLORS = Arrays.asList(WHITE, LIGHT_GRAY, GRAY, BLACK, LIME, YELLOW, ORANGE, BROWN, RED, PINK, MAGENTA, PURPLE, BLUE, LIGHT_BLUE, CYAN, GREEN);
    }
}

