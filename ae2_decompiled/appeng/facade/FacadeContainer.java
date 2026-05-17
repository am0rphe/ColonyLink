/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.IdMap
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  org.apache.commons.lang3.StringUtils
 */
package appeng.facade;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.facade.FacadePart;
import appeng.parts.CableBusStorage;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IdMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;

public class FacadeContainer
implements IFacadeContainer {
    private static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC = ByteBufCodecs.idMapper((IdMap)Block.BLOCK_STATE_REGISTRY);
    private static final String[] NBT_KEY_NAMES = (String[])Arrays.stream(Direction.values()).map(d -> "facade" + StringUtils.capitalize((String)d.getSerializedName())).toArray(String[]::new);
    private final CableBusStorage storage;
    private final Consumer<Direction> changeCallback;

    public FacadeContainer(CableBusStorage cbs, Consumer<Direction> changeCallback) {
        this.storage = cbs;
        this.changeCallback = changeCallback;
    }

    @Override
    public boolean canAddFacade(IFacadePart a) {
        return this.getFacade(a.getSide()) == null;
    }

    @Override
    public boolean addFacade(IFacadePart a) {
        if (this.canAddFacade(a)) {
            this.storage.setFacade(a.getSide(), a);
            this.notifyChange(a.getSide());
            return true;
        }
        return false;
    }

    @Override
    public void removeFacade(IPartHost host, Direction side) {
        if (side != null && this.storage.getFacade(side) != null) {
            this.storage.removeFacade(side);
            this.notifyChange(side);
            if (host != null) {
                host.markForUpdate();
            }
        }
    }

    @Override
    public IFacadePart getFacade(Direction side) {
        return this.storage.getFacade(side);
    }

    @Override
    public void readFromNBT(CompoundTag c, HolderLookup.Provider registries) {
        for (Direction side : Direction.values()) {
            this.storage.removeFacade(side);
            Tag tag = c.get(NBT_KEY_NAMES[side.ordinal()]);
            Optional result = BlockState.CODEC.decode((DynamicOps)NbtOps.INSTANCE, (Object)tag).result();
            if (!result.isPresent()) continue;
            BlockState blockState = (BlockState)((Pair)result.get()).getFirst();
            this.storage.setFacade(side, new FacadePart(blockState, side));
        }
    }

    @Override
    public void writeToNBT(CompoundTag c, HolderLookup.Provider registries) {
        for (Direction side : Direction.values()) {
            if (this.storage.getFacade(side) == null) continue;
            Tag data = (Tag)BlockState.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.storage.getFacade(side).getBlockState()).getOrThrow();
            c.put(NBT_KEY_NAMES[side.ordinal()], data);
        }
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf out) {
        byte facadeSides = out.readByte();
        boolean changed = false;
        for (Direction side : Direction.values()) {
            int ix = 1 << side.ordinal();
            if ((facadeSides & ix) == ix) {
                BlockState facade = (BlockState)BLOCK_STATE_STREAM_CODEC.decode((Object)out);
                changed = changed || this.storage.getFacade(side) == null;
                this.storage.setFacade(side, new FacadePart(facade, side));
                continue;
            }
            changed = changed || this.storage.getFacade(side) != null;
            this.storage.removeFacade(side);
        }
        return changed;
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf out) {
        int facadeSides = 0;
        for (Direction side : Direction.values()) {
            if (this.getFacade(side) == null) continue;
            facadeSides |= 1 << side.ordinal();
        }
        out.writeByte((byte)facadeSides);
        for (Direction side : Direction.values()) {
            IFacadePart part = this.getFacade(side);
            if (part == null) continue;
            BLOCK_STATE_STREAM_CODEC.encode((Object)out, (Object)part.getBlockState());
        }
    }

    @Override
    public boolean isEmpty() {
        for (Direction side : Direction.values()) {
            if (this.storage.getFacade(side) == null) continue;
            return false;
        }
        return true;
    }

    private void notifyChange(Direction side) {
        this.changeCallback.accept(side);
    }
}

