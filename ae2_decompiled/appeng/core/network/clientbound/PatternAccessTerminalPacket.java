/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record PatternAccessTerminalPacket(boolean fullUpdate, long inventoryId, int inventorySize, long sortBy, PatternContainerGroup group, Int2ObjectMap<ItemStack> slots) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternAccessTerminalPacket> STREAM_CODEC = StreamCodec.ofMember(PatternAccessTerminalPacket::write, PatternAccessTerminalPacket::decode);
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ItemStack>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(Int2ObjectOpenHashMap::new, (StreamCodec)ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue), (StreamCodec)ItemStack.OPTIONAL_STREAM_CODEC, (int)128);
    public static final CustomPacketPayload.Type<PatternAccessTerminalPacket> TYPE = CustomAppEngPayload.createType("pattern_access_terminal");

    public CustomPacketPayload.Type<PatternAccessTerminalPacket> type() {
        return TYPE;
    }

    public static PatternAccessTerminalPacket decode(RegistryFriendlyByteBuf stream) {
        long inventoryId = stream.readVarLong();
        boolean fullUpdate = stream.readBoolean();
        int inventorySize = 0;
        long sortBy = 0L;
        PatternContainerGroup group = null;
        if (fullUpdate) {
            inventorySize = stream.readVarInt();
            sortBy = stream.readVarLong();
            group = PatternContainerGroup.readFromPacket(stream);
        }
        Int2ObjectMap slots = (Int2ObjectMap)SLOTS_STREAM_CODEC.decode((Object)stream);
        return new PatternAccessTerminalPacket(fullUpdate, inventoryId, inventorySize, sortBy, group, (Int2ObjectMap<ItemStack>)slots);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarLong(this.inventoryId);
        data.writeBoolean(this.fullUpdate);
        if (this.fullUpdate) {
            data.writeVarInt(this.inventorySize);
            data.writeVarLong(this.sortBy);
            this.group.writeToPacket(data);
        }
        SLOTS_STREAM_CODEC.encode((Object)data, this.slots);
    }

    public static PatternAccessTerminalPacket fullUpdate(long inventoryId, int inventorySize, long sortBy, PatternContainerGroup group, Int2ObjectMap<ItemStack> slots) {
        return new PatternAccessTerminalPacket(true, inventoryId, inventorySize, sortBy, group, slots);
    }

    public static PatternAccessTerminalPacket incrementalUpdate(long inventoryId, Int2ObjectMap<ItemStack> slots) {
        return new PatternAccessTerminalPacket(false, inventoryId, 0, 0L, null, slots);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof PatternAccessTermScreen) {
            PatternAccessTermScreen patternAccessTerminal = (PatternAccessTermScreen)screen;
            if (this.fullUpdate) {
                patternAccessTerminal.postFullUpdate(this.inventoryId, this.sortBy, this.group, this.inventorySize, this.slots);
            } else {
                patternAccessTerminal.postIncrementalUpdate(this.inventoryId, this.slots);
            }
        }
    }
}

