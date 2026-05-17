/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.FluidState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins;

import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemEntity.class})
public abstract class ItemEntityMixin
extends Entity {
    private int ae2_transformTime = 0;
    private int ae2_delay = 0;

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Inject(at={@At(value="HEAD")}, method={"hurt"}, cancellable=true)
    void handleExplosion(DamageSource src, float dmg, CallbackInfoReturnable<Boolean> ci) {
        ItemEntity self;
        if (!this.level().isClientSide && src.is(DamageTypeTags.IS_EXPLOSION) && !this.isRemoved() && TransformLogic.canTransformInExplosion(self = (ItemEntity)this) && TransformLogic.tryTransform(self, TransformCircumstance::isExplosion)) {
            ci.setReturnValue((Object)false);
            ci.cancel();
        }
    }

    @Inject(at={@At(value="RETURN")}, method={"tick"})
    void handleEntityTransform(CallbackInfo ci) {
        boolean isValidFluid;
        if (this.isRemoved()) {
            return;
        }
        ItemEntity self = (ItemEntity)this;
        if (!TransformLogic.canTransformInAnyFluid(self)) {
            return;
        }
        int j = Mth.floor((double)this.getX());
        int i = Mth.floor((double)((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0));
        int k = Mth.floor((double)this.getZ());
        FluidState state = this.level().getFluidState(new BlockPos(j, i, k));
        boolean bl = isValidFluid = !state.isEmpty() && TransformLogic.canTransformInFluid(self, state);
        if (this.level().isClientSide()) {
            if (isValidFluid && this.ae2_delay++ > 30 && AEConfig.instance().isEnableEffects()) {
                AppEng.instance().spawnEffect(EffectType.Lightning, this.level(), this.getX(), this.getY(), this.getZ(), null);
                this.ae2_delay = 0;
            }
        } else if (isValidFluid) {
            ++this.ae2_transformTime;
            if (this.ae2_transformTime > 60 && !TransformLogic.tryTransform(self, c -> c.isFluid(state))) {
                this.ae2_transformTime = 0;
            }
        } else {
            this.ae2_transformTime = 0;
        }
    }
}

