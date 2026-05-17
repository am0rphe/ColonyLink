/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.Unpooled
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.blockentity.misc;

import appeng.api.util.AEColor;
import appeng.block.paint.PaintSplotches;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.Splotch;
import appeng.items.misc.PaintBallItem;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class PaintSplotchesBlockEntity
extends AEBaseBlockEntity {
    public static final ModelProperty<PaintSplotches> SPLOTCHES = new ModelProperty();
    private List<Splotch> dots = null;

    public PaintSplotchesBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        FriendlyByteBuf myDat = new FriendlyByteBuf(Unpooled.buffer());
        this.writeBuffer(myDat);
        if (myDat.hasArray()) {
            data.putByteArray("dots", myDat.array());
        }
    }

    private void writeBuffer(FriendlyByteBuf out) {
        if (this.dots == null) {
            out.writeByte(0);
            return;
        }
        out.writeByte(this.dots.size());
        for (Splotch s : this.dots) {
            s.writeToStream(out);
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if (data.contains("dots")) {
            this.readBuffer(new FriendlyByteBuf(Unpooled.copiedBuffer((byte[])data.getByteArray("dots"))));
        }
    }

    private void readBuffer(FriendlyByteBuf in) {
        int howMany = in.readByte();
        if (howMany == 0) {
            this.dots = null;
            return;
        }
        this.dots = new ArrayList<Splotch>(howMany);
        for (int x = 0; x < howMany; ++x) {
            this.dots.add(new Splotch(in));
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        this.writeBuffer((FriendlyByteBuf)data);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        super.readFromStream(data);
        this.readBuffer((FriendlyByteBuf)data);
        return true;
    }

    public void neighborChanged() {
        if (this.dots == null) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (this.isSideValid(side)) continue;
            this.removeSide(side);
        }
        this.updateData();
    }

    public boolean isSideValid(Direction side) {
        BlockPos p = this.worldPosition.relative(side);
        BlockState blk = this.level.getBlockState(p);
        return blk.isFaceSturdy((BlockGetter)this.level, p, side.getOpposite());
    }

    private void removeSide(Direction side) {
        this.dots.removeIf(s -> s.getSide() == side);
        this.markForUpdate();
        this.saveChanges();
    }

    private void updateData() {
        if (this.dots.isEmpty()) {
            this.dots = null;
        }
        if (this.dots == null) {
            this.level.removeBlock(this.worldPosition, false);
        } else {
            int lumenCount = 0;
            for (Splotch dot : this.dots) {
                if (dot.isLumen() && ++lumenCount >= 2) break;
            }
            this.level.setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)PaintSplotchesBlock.LIGHT_LEVEL, (Comparable)Integer.valueOf(lumenCount)));
        }
    }

    public void cleanSide(Direction side) {
        if (this.dots == null) {
            return;
        }
        this.removeSide(side);
        this.updateData();
    }

    public void addBlot(ItemStack type, Direction side, Vec3 hitVec) {
        PaintBallItem paintBallItem = (PaintBallItem)type.getItem();
        this.addBlot(paintBallItem.getColor(), paintBallItem.isLumen(), side, hitVec);
    }

    public void addBlot(AEColor color, boolean lit, Direction side, Vec3 hitVec) {
        BlockPos p = this.worldPosition.relative(side);
        BlockState blk = this.level.getBlockState(p);
        if (blk.isFaceSturdy((BlockGetter)this.level, p, side.getOpposite())) {
            if (this.dots == null) {
                this.dots = new ArrayList<Splotch>();
            }
            if (this.dots.size() > 20) {
                this.dots.remove(0);
            }
            this.dots.add(new Splotch(color, lit, side, hitVec));
            this.updateData();
            this.markForUpdate();
            this.saveChanges();
        }
    }

    public Collection<Splotch> getDots() {
        if (this.dots == null) {
            return Collections.emptyList();
        }
        return this.dots;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(SPLOTCHES, (Object)new PaintSplotches(this.getDots())).build();
    }
}

