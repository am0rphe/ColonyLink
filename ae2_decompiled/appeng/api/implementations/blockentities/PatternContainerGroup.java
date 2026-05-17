/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentSerialization
 *  net.minecraft.world.Nameable
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.blockentities;

import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEItemKey;
import appeng.core.localization.GuiText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public record PatternContainerGroup(@Nullable AEItemKey icon, Component name, List<Component> tooltip) {
    private static final PatternContainerGroup NOTHING = new PatternContainerGroup(AEItemKey.of((ItemLike)Items.AIR), (Component)GuiText.Nothing.text(), List.of());

    public static PatternContainerGroup nothing() {
        return NOTHING;
    }

    public void writeToPacket(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(this.icon != null);
        if (this.icon != null) {
            this.icon.writeToPacket(buffer);
        }
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode((Object)buffer, (Object)this.name);
        buffer.writeVarInt(this.tooltip.size());
        for (Component component : this.tooltip) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode((Object)buffer, (Object)component);
        }
    }

    public static PatternContainerGroup readFromPacket(RegistryFriendlyByteBuf buffer) {
        AEItemKey icon = buffer.readBoolean() ? AEItemKey.fromPacket(buffer) : null;
        Component name = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode((Object)buffer);
        int lineCount = buffer.readVarInt();
        ArrayList<Component> lines = new ArrayList<Component>(lineCount);
        for (int i = 0; i < lineCount; ++i) {
            lines.add((Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode((Object)buffer));
        }
        return new PatternContainerGroup(icon, name, lines);
    }

    @Nullable
    public static PatternContainerGroup fromMachine(Level level, BlockPos pos, Direction side) {
        Component name;
        AEItemKey icon;
        IFluidHandler fluidHandler;
        ICraftingMachine craftingMachine = ICraftingMachine.of(level, pos, side);
        if (craftingMachine != null) {
            return craftingMachine.getCraftingMachineInfo();
        }
        BlockEntity target = level.getBlockEntity(pos);
        if (target == null) {
            return null;
        }
        IItemHandler itemHandler = (IItemHandler)level.getCapability(Capabilities.ItemHandler.BLOCK, pos, target.getBlockState(), target, (Object)side);
        if (!(itemHandler != null && itemHandler.getSlots() > 0 || (fluidHandler = (IFluidHandler)level.getCapability(Capabilities.FluidHandler.BLOCK, pos, target.getBlockState(), target, (Object)side)) != null && fluidHandler.getTanks() != 0)) {
            return null;
        }
        List<Component> tooltip = List.of();
        if (target instanceof IPartHost) {
            IPartHost partHost = (IPartHost)target;
            IPart part = partHost.getPart(side);
            if (part == null) {
                return null;
            }
            icon = AEItemKey.of(part.getPartItem());
            if (part instanceof Nameable) {
                Nameable nameable = (Nameable)part;
                name = nameable.getDisplayName();
            } else {
                name = icon.getDisplayName();
            }
        } else {
            Nameable nameable;
            Block targetBlock = target.getBlockState().getBlock();
            ItemStack targetItem = new ItemStack((ItemLike)targetBlock);
            icon = AEItemKey.of(targetItem);
            if (target instanceof Nameable && (nameable = (Nameable)target).hasCustomName()) {
                name = nameable.getCustomName();
            } else {
                if (targetItem.isEmpty()) {
                    return null;
                }
                name = targetItem.getHoverName();
            }
        }
        return new PatternContainerGroup(icon, name, tooltip);
    }
}

