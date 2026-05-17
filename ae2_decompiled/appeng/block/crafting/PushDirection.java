/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.Direction
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.util.StringRepresentable
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Nullable;

public enum PushDirection implements StringRepresentable
{
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    ALL;

    public static final Codec<PushDirection> CODEC;
    public static final StreamCodec<FriendlyByteBuf, PushDirection> STREAM_CODEC;
    @Nullable
    private final Direction direction;

    private PushDirection(Direction direction) {
        this.direction = direction;
    }

    private PushDirection() {
        this.direction = null;
    }

    @Nullable
    public Direction getDirection() {
        return this.direction;
    }

    public String getSerializedName() {
        return this.direction != null ? this.direction.getSerializedName() : "all";
    }

    public static PushDirection fromDirection(@Nullable Direction direction) {
        return direction != null ? PushDirection.values()[direction.ordinal()] : ALL;
    }

    static {
        CODEC = StringRepresentable.fromEnum(PushDirection::values);
        STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(PushDirection.class);
    }
}

