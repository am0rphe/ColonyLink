/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.parts;

import appeng.api.parts.IPartCollisionHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IFacadePart {
    public ItemStack getItemStack();

    public void getBoxes(IPartCollisionHelper var1, boolean var2);

    public Direction getSide();

    public Item getItem();

    public ItemStack getTextureItem();

    public BlockState getBlockState();

    public boolean onUseItemOn(ItemStack var1, Player var2, InteractionHand var3, Vec3 var4);

    public boolean onClicked(Player var1, Vec3 var2);
}

