/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 */
package appeng.api.implementations.items;

import appeng.api.util.AEColor;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MemoryCardColors(AEColor top1, AEColor top2, AEColor top3, AEColor top4, AEColor bottom1, AEColor bottom2, AEColor bottom3, AEColor bottom4) {
    public static final Codec<MemoryCardColors> CODEC = AEColor.CODEC.listOf().xmap(aeColors -> new MemoryCardColors(0 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(0))) : AEColor.TRANSPARENT, 1 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(1))) : AEColor.TRANSPARENT, 2 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(2))) : AEColor.TRANSPARENT, 3 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(3))) : AEColor.TRANSPARENT, 4 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(4))) : AEColor.TRANSPARENT, 5 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(5))) : AEColor.TRANSPARENT, 6 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(6))) : AEColor.TRANSPARENT, 7 < aeColors.size() ? (AEColor)((Object)((Object)aeColors.get(7))) : AEColor.TRANSPARENT), colors -> List.of(colors.top1(), colors.top2(), colors.top3(), colors.top4(), colors.bottom1(), colors.bottom2(), colors.bottom3(), colors.bottom4()));
    public static StreamCodec<FriendlyByteBuf, MemoryCardColors> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, MemoryCardColors>(){

        public MemoryCardColors decode(FriendlyByteBuf buffer) {
            return new MemoryCardColors((AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class), (AEColor)buffer.readEnum(AEColor.class));
        }

        public void encode(FriendlyByteBuf buffer, MemoryCardColors colors) {
            buffer.writeEnum((Enum)colors.top1);
            buffer.writeEnum((Enum)colors.top2);
            buffer.writeEnum((Enum)colors.top3);
            buffer.writeEnum((Enum)colors.top4);
            buffer.writeEnum((Enum)colors.bottom1);
            buffer.writeEnum((Enum)colors.bottom2);
            buffer.writeEnum((Enum)colors.bottom3);
            buffer.writeEnum((Enum)colors.bottom4);
        }
    };
    public static final MemoryCardColors DEFAULT = new MemoryCardColors(AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT);

    public AEColor get(int x, int y) {
        int index = x + y * 4;
        return switch (index) {
            case 0 -> this.top1;
            case 1 -> this.top2;
            case 2 -> this.top3;
            case 3 -> this.top4;
            case 4 -> this.bottom1;
            case 5 -> this.bottom2;
            case 6 -> this.bottom3;
            case 7 -> this.bottom4;
            default -> AEColor.TRANSPARENT;
        };
    }
}

