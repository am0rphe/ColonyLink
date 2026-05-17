/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.util.helpers;

import appeng.api.util.AEColor;
import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class P2PHelper {
    private static final String[] HEX_DIGITS = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public AEColor[] toColors(short frequency) {
        AEColor[] colors = new AEColor[4];
        for (int i = 0; i < 4; ++i) {
            int nibble = P2PHelper.getFrequencyNibble(frequency, i);
            colors[i] = AEColor.values()[nibble];
        }
        return colors;
    }

    private static int getFrequencyNibble(short frequency, int i) {
        return frequency >> 4 * (3 - i) & 0xF;
    }

    public short fromColors(AEColor[] colors) {
        Preconditions.checkArgument((colors.length == 4 ? 1 : 0) != 0);
        int t = 0;
        for (int i = 0; i < 4; ++i) {
            int code = colors[3 - i].ordinal() << 4 * i;
            t |= code;
        }
        return (short)(t & 0xFFFF);
    }

    public String toHexDigit(AEColor color) {
        return String.format("%01X", color.ordinal());
    }

    public String toHexString(short frequency) {
        return String.format("%04X", frequency);
    }

    public MutableComponent toColoredHexString(short frequency) {
        MutableComponent parent = Component.empty();
        for (int i = 0; i < 4; ++i) {
            int nibble = P2PHelper.getFrequencyNibble(frequency, i);
            parent.append((Component)Component.literal((String)HEX_DIGITS[nibble]).withColor(AEColor.values()[nibble].whiteVariant));
        }
        return parent;
    }
}

