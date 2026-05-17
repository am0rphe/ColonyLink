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
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.parts;

import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ICableBusContainer {
    public int isProvidingStrongPower(Direction var1);

    public int isProvidingWeakPower(Direction var1);

    public boolean canConnectRedstone(Direction var1);

    public void onEntityCollision(Entity var1);

    public boolean useItemOn(ItemStack var1, Player var2, InteractionHand var3, Vec3 var4);

    public boolean useWithoutItem(Player var1, Vec3 var2);

    public void onNeighborChanged(BlockGetter var1, BlockPos var2, BlockPos var3);

    public void onUpdateShape(LevelAccessor var1, BlockPos var2, Direction var3);

    public boolean isEmpty();

    public SelectedPart selectPartLocal(Vec3 var1);

    public boolean recolourBlock(Direction var1, AEColor var2, Player var3);

    public boolean isLadder(LivingEntity var1);

    @OnlyIn(value=Dist.CLIENT)
    public void animateTick(Level var1, BlockPos var2, RandomSource var3);

    public int getLightValue();

    public CableBusRenderState getRenderState();
}

