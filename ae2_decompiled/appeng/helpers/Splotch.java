/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.phys.Vec3
 */
package appeng.helpers;

import appeng.api.util.AEColor;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class Splotch {
    private final Direction side;
    private final boolean lumen;
    private final AEColor color;
    private final int pos;

    public Splotch(AEColor col, boolean lit, Direction side, Vec3 position) {
        double y;
        double x;
        this.color = col;
        this.lumen = lit;
        if (side == Direction.SOUTH || side == Direction.NORTH) {
            x = position.x;
            y = position.y;
        } else {
            x = side == Direction.UP || side == Direction.DOWN ? position.x : position.y;
            y = position.z;
        }
        int a = (int)(x * 15.0);
        int b = (int)(y * 15.0);
        this.pos = a | b << 4;
        this.side = side;
    }

    public Splotch(FriendlyByteBuf data) {
        this.pos = data.readByte();
        byte val = data.readByte();
        this.side = Direction.values()[val & 7];
        this.color = AEColor.values()[val >> 3 & 0xF];
        this.lumen = (val >> 7 & 1) > 0;
    }

    public void writeToStream(FriendlyByteBuf stream) {
        stream.writeByte(this.pos);
        int val = this.getSide().ordinal() | this.getColor().ordinal() << 3 | (this.isLumen() ? 128 : 0);
        stream.writeByte(val);
    }

    public float x() {
        return (float)(this.pos & 0xF) / 15.0f;
    }

    public float y() {
        return (float)(this.pos >> 4 & 0xF) / 15.0f;
    }

    public int getSeed() {
        int val = this.getSide().ordinal() | this.getColor().ordinal() << 3 | (this.isLumen() ? 128 : 0);
        return Math.abs(this.pos + val);
    }

    public Direction getSide() {
        return this.side;
    }

    public AEColor getColor() {
        return this.color;
    }

    public boolean isLumen() {
        return this.lumen;
    }
}

