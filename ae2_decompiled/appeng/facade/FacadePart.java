/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 */
package appeng.facade;

import appeng.api.ids.AEComponents;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.util.InteractionUtil;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class FacadePart
implements IFacadePart {
    private final Direction side;
    private BlockState facade;

    public FacadePart(BlockState facade, Direction side) {
        this.side = Objects.requireNonNull(side, "side");
        this.facade = Objects.requireNonNull(facade, "facade");
    }

    @Override
    public ItemStack getItemStack() {
        return AEItems.FACADE.get().createFacadeForItemUnchecked(this.getTextureItem());
    }

    @Override
    public void getBoxes(IPartCollisionHelper ch, boolean itemEntity) {
        if (itemEntity) {
            ch.addBox(0.0, 0.0, 15.0, 16.0, 16.0, 15.9);
        } else {
            ch.addBox(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
        }
    }

    @Override
    public Direction getSide() {
        return this.side;
    }

    @Override
    public Item getItem() {
        return this.facade.getBlock().asItem();
    }

    @Override
    public ItemStack getTextureItem() {
        return new ItemStack((ItemLike)this.getItem());
    }

    @Override
    public BlockState getBlockState() {
        return this.facade;
    }

    private void setBlockState(BlockState blockState) {
        this.facade = blockState;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (!InteractionUtil.canWrenchRotate(heldItem)) {
            return false;
        }
        return this.handleInteraction(player, true, heldItem);
    }

    @Override
    public boolean onClicked(Player player, Vec3 pos) {
        ItemStack heldItem = player.getMainHandItem();
        if (!InteractionUtil.canWrenchRotate(heldItem)) {
            return false;
        }
        return this.handleInteraction(player, false, heldItem);
    }

    private boolean handleInteraction(Player player, boolean shouldCycleState, ItemStack heldItem) {
        Holder holder = this.getBlockState().getBlockHolder();
        StateDefinition statedefinition = ((Block)holder.value()).getStateDefinition();
        Collection properties = statedefinition.getProperties();
        if (properties.isEmpty()) {
            return false;
        }
        Property firstProperty = (Property)properties.iterator().next();
        String cyclePropertyName = (String)heldItem.getOrDefault(AEComponents.FACADE_CYCLE_PROPERTY, (Object)firstProperty.getName());
        Property property = statedefinition.getProperty(cyclePropertyName);
        if (property == null) {
            property = firstProperty;
        }
        if (shouldCycleState) {
            BlockState newState = (BlockState)this.getBlockState().cycle(property);
            this.setBlockState(newState);
            Comparable defaultValue = this.getBlockState().getBlock().defaultBlockState().getValue(property);
            if (Objects.equals(newState.getValue(property), defaultValue)) {
                FacadePart.message(player, (Component)PlayerMessages.FacadePropertyWrapped.text(property.getName()));
            }
        } else {
            if ((property = (Property)Util.findNextInIterable((Iterable)properties, (Object)property)) == firstProperty) {
                heldItem.remove(AEComponents.FACADE_CYCLE_PROPERTY);
            } else {
                heldItem.set(AEComponents.FACADE_CYCLE_PROPERTY, (Object)property.getName());
            }
            FacadePart.message(player, (Component)PlayerMessages.FacadePropertySelected.text(property.getName()));
        }
        return true;
    }

    private static void message(Player player, Component messageComponent) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            serverPlayer.sendSystemMessage(messageComponent, true);
        }
    }
}

