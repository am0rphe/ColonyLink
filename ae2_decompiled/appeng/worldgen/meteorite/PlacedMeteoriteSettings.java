/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 */
package appeng.worldgen.meteorite;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class PlacedMeteoriteSettings {
    private final BlockPos pos;
    private final float meteoriteRadius;
    private final CraterType craterType;
    private final FalloutMode fallout;
    private final boolean pureCrater;
    private final boolean craterLake;

    public PlacedMeteoriteSettings(BlockPos pos, float meteoriteRadius, CraterType craterType, FalloutMode fallout, boolean pureCrater, boolean craterLake) {
        this.pos = pos;
        this.craterType = craterType;
        this.meteoriteRadius = meteoriteRadius;
        this.fallout = fallout;
        this.pureCrater = pureCrater;
        this.craterLake = craterLake;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public CraterType getCraterType() {
        return this.craterType;
    }

    public float getMeteoriteRadius() {
        return this.meteoriteRadius;
    }

    public FalloutMode getFallout() {
        return this.fallout;
    }

    public boolean shouldPlaceCrater() {
        return this.craterType != CraterType.NONE;
    }

    public boolean isPureCrater() {
        return this.pureCrater;
    }

    public boolean isCraterLake() {
        return this.craterLake;
    }

    public CompoundTag write(CompoundTag tag) {
        tag.putLong("c", this.pos.asLong());
        tag.putFloat("r", this.meteoriteRadius);
        tag.putByte("t", (byte)this.craterType.ordinal());
        tag.putByte("f", (byte)this.fallout.ordinal());
        tag.putBoolean("p", this.pureCrater);
        tag.putBoolean("l", this.craterLake);
        return tag;
    }

    public static PlacedMeteoriteSettings read(CompoundTag tag) {
        BlockPos pos = BlockPos.of((long)tag.getLong("c"));
        float meteoriteRadius = tag.getFloat("r");
        CraterType craterType = CraterType.values()[tag.getByte("t")];
        FalloutMode fallout = FalloutMode.values()[tag.getByte("f")];
        boolean pureCrater = tag.getBoolean("p");
        boolean craterLake = tag.getBoolean("l");
        return new PlacedMeteoriteSettings(pos, meteoriteRadius, craterType, fallout, pureCrater, craterLake);
    }

    public String toString() {
        return "PlacedMeteoriteSettings [pos=" + String.valueOf(this.pos) + ", meteoriteRadius=" + this.meteoriteRadius + ", craterType=" + String.valueOf((Object)this.craterType) + ", fallout=" + String.valueOf((Object)this.fallout) + ", pureCrater=" + this.pureCrater + ", craterLake=" + this.craterLake + "]";
    }
}

