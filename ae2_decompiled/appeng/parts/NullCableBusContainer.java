/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.Vec3
 */
package appeng.parts;

import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.parts.ICableBusContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class NullCableBusContainer
implements ICableBusContainer {
    @Override
    public int isProvidingStrongPower(Direction opposite) {
        return 0;
    }

    @Override
    public int isProvidingWeakPower(Direction opposite) {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(Direction opposite) {
        return false;
    }

    @Override
    public void onEntityCollision(Entity e) {
    }

    @Override
    public boolean useItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 localPos) {
        return false;
    }

    @Override
    public boolean useWithoutItem(Player player, Vec3 localPos) {
        return false;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
    }

    @Override
    public void onUpdateShape(LevelAccessor level, BlockPos pos, Direction side) {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public SelectedPart selectPartLocal(Vec3 v3) {
        return new SelectedPart();
    }

    @Override
    public boolean recolourBlock(Direction side, AEColor colour, Player who) {
        return false;
    }

    @Override
    public boolean isLadder(LivingEntity entity) {
        return false;
    }

    @Override
    public void animateTick(Level level, BlockPos pos, RandomSource r) {
    }

    @Override
    public int getLightValue() {
        return 0;
    }

    @Override
    public CableBusRenderState getRenderState() {
        return new CableBusRenderState();
    }
}

