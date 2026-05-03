package com.colonylink.colonylink;

import appeng.api.features.IGridLinkableHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;

public class ColonyLinkWandLinkableHandler implements IGridLinkableHandler
{
    private static final String NBT_LINKED_X = "linked_x";
    private static final String NBT_LINKED_Y = "linked_y";
    private static final String NBT_LINKED_Z = "linked_z";
    private static final String NBT_LINKED_DIM = "linked_dim";

    @Override
    public boolean canLink(ItemStack stack)
    {
        return stack.getItem() instanceof ColonyLinkWand;
    }

    @Override
    public void link(ItemStack stack, GlobalPos pos)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.putInt(NBT_LINKED_X, pos.pos().getX());
            tag.putInt(NBT_LINKED_Y, pos.pos().getY());
            tag.putInt(NBT_LINKED_Z, pos.pos().getZ());
            tag.putString(NBT_LINKED_DIM, pos.dimension().location().toString());
            return CustomData.of(tag);
        });
    }

    @Override
    public void unlink(ItemStack stack)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.remove(NBT_LINKED_X);
            tag.remove(NBT_LINKED_Y);
            tag.remove(NBT_LINKED_Z);
            tag.remove(NBT_LINKED_DIM);
            return CustomData.of(tag);
        });
    }

    public static boolean isLinked(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains(NBT_LINKED_DIM);
    }

    public static GlobalPos getLinkedPos(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains(NBT_LINKED_DIM)) return null;

        int x = tag.getInt(NBT_LINKED_X);
        int y = tag.getInt(NBT_LINKED_Y);
        int z = tag.getInt(NBT_LINKED_Z);
        String dimStr = tag.getString(NBT_LINKED_DIM);

        ResourceKey<Level> dimKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(dimStr)
        );

        return GlobalPos.of(dimKey, new BlockPos(x, y, z));
    }
}