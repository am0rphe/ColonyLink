/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.render.crafting;

import net.minecraft.world.item.ItemStack;

public class AssemblerAnimationStatus {
    private final ItemStack is;
    private final byte speed;
    private final int ticksRequired;
    private float accumulatedTicks;
    private float ticksUntilParticles;

    public AssemblerAnimationStatus(byte speed, ItemStack is) {
        this.speed = speed;
        this.is = is;
        this.ticksRequired = (int)Math.ceil(Math.max(1.0f, 100.0f / (float)speed)) + 2;
    }

    public ItemStack getIs() {
        return this.is;
    }

    public byte getSpeed() {
        return this.speed;
    }

    public float getAccumulatedTicks() {
        return this.accumulatedTicks;
    }

    public void setAccumulatedTicks(float accumulatedTicks) {
        this.accumulatedTicks = accumulatedTicks;
    }

    public float getTicksUntilParticles() {
        return this.ticksUntilParticles;
    }

    public void setTicksUntilParticles(float ticksUntilParticles) {
        this.ticksUntilParticles = ticksUntilParticles;
    }

    public boolean isExpired() {
        return this.accumulatedTicks > (float)this.ticksRequired;
    }
}

