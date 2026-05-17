/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.HopperBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.AttachFace
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.server.testworld;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.server.testworld.BlockEntityCustomizer;
import appeng.server.testworld.BuildAction;
import appeng.server.testworld.CableBuilder;
import appeng.server.testworld.DriveBuilder;
import appeng.server.testworld.PartCustomizer;
import appeng.server.testworld.PlaceBlockState;
import appeng.server.testworld.PlaceFacade;
import appeng.server.testworld.PlacePart;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.PostGridInitAction;
import appeng.server.testworld.SpawnEntityAction;
import appeng.server.testworld.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;

public interface PlotBuilder {
    public void addBuildAction(BuildAction var1);

    public void addPostBuildAction(PostBuildAction var1);

    public void addPostInitAction(PostBuildAction var1);

    public BoundingBox bb(String var1);

    public static String posToBb(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    default public CableBuilder cable(BlockPos pos) {
        return this.cable(PlotBuilder.posToBb(pos));
    }

    default public CableBuilder cable(String bb) {
        return this.cable(bb, AEParts.SMART_CABLE, AEColor.TRANSPARENT);
    }

    default public CableBuilder cable(String bb, ColoredItemDefinition<? extends IPartItem<?>> definition) {
        return this.cable(bb, definition, AEColor.TRANSPARENT);
    }

    default public CableBuilder cable(String bb, ColoredItemDefinition<? extends IPartItem<?>> definition, AEColor color) {
        return this.cable(bb, definition.item(color));
    }

    default public CableBuilder cable(String bb, IPartItem<?> what) {
        this.addBuildAction(new PlacePart(this.bb(bb), what, null));
        return new CableBuilder(this, bb);
    }

    default public CableBuilder denseCable(BlockPos pos) {
        return this.denseCable(PlotBuilder.posToBb(pos));
    }

    default public CableBuilder denseCable(String bb) {
        return this.cable(bb, (IPartItem)AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT));
    }

    default public void part(String bb, Direction side, ItemDefinition<? extends PartItem<?>> part) {
        this.addBuildAction(new PlacePart(this.bb(bb), (IPartItem)part.asItem(), side));
    }

    default public void part(BlockPos pos, Direction side, ItemDefinition<? extends PartItem<?>> part) {
        this.part(PlotBuilder.posToBb(pos), side, part);
    }

    default public <T extends IPart> void part(BlockPos pos, Direction side, ItemDefinition<? extends PartItem<T>> part, Consumer<T> partCustomizer) {
        this.part(PlotBuilder.posToBb(pos), side, part, partCustomizer);
    }

    default public <T extends IPart> void part(String bb, Direction side, ItemDefinition<? extends PartItem<T>> part, Consumer<T> partCustomizer) {
        this.addBuildAction(new PlacePart(this.bb(bb), (IPartItem)part.get(), side));
        this.addBuildAction(new PartCustomizer<T>(this.bb(bb), side, part, partCustomizer));
    }

    default public <T extends IPart> void facade(String bb, Direction side, ItemLike item) {
        this.addBuildAction(new PlaceFacade(this.bb(bb), item.asItem().getDefaultInstance(), side));
    }

    default public void creativeEnergyCell(BlockPos pos) {
        this.creativeEnergyCell(PlotBuilder.posToBb(pos));
    }

    default public void creativeEnergyCell(String bb) {
        this.block(bb, AEBlocks.CREATIVE_ENERGY_CELL);
    }

    default public BlockPos leverOn(BlockPos pos, Direction side) {
        BlockPos leverPos = pos.relative(side);
        AttachFace face = AttachFace.WALL;
        if (side == Direction.UP) {
            face = AttachFace.CEILING;
            side = Direction.EAST;
        } else if (side == Direction.DOWN) {
            face = AttachFace.FLOOR;
            side = Direction.EAST;
        }
        BlockState state = (BlockState)((BlockState)Blocks.LEVER.defaultBlockState().setValue((Property)LeverBlock.FACE, (Comparable)face)).setValue((Property)LeverBlock.FACING, (Comparable)side);
        this.blockState(leverPos, state);
        return leverPos;
    }

    default public BlockPos buttonOn(BlockPos pos, Direction side) {
        BlockPos leverPos = pos.relative(side);
        AttachFace face = AttachFace.WALL;
        if (side == Direction.UP) {
            face = AttachFace.FLOOR;
            side = Direction.NORTH;
        } else if (side == Direction.DOWN) {
            face = AttachFace.CEILING;
            side = Direction.NORTH;
        }
        BlockState state = (BlockState)((BlockState)Blocks.POLISHED_BLACKSTONE_BUTTON.defaultBlockState().setValue((Property)ButtonBlock.FACE, (Comparable)face)).setValue((Property)ButtonBlock.FACING, (Comparable)side);
        this.blockState(leverPos, state);
        return leverPos;
    }

    default public void block(BlockPos pos, BlockDefinition<?> block) {
        this.block(PlotBuilder.posToBb(pos), block);
    }

    default public void block(String bb, BlockDefinition<?> block) {
        this.blockState(bb, block.block().defaultBlockState());
    }

    default public <T extends AEBaseBlockEntity> void blockEntity(BlockPos pos, BlockDefinition<? extends AEBaseEntityBlock<T>> block, Consumer<T> postProcessor) {
        this.blockEntity(PlotBuilder.posToBb(pos), block, postProcessor);
    }

    default public <T extends AEBaseBlockEntity> void blockEntity(String bb, BlockDefinition<? extends AEBaseEntityBlock<T>> block, Consumer<T> postProcessor) {
        this.blockState(bb, block.block().defaultBlockState());
        BlockEntityType<T> type = block.block().getBlockEntityType();
        this.addBuildAction(new BlockEntityCustomizer<T>(this.bb(bb), type, postProcessor));
    }

    default public void chest(BlockPos pos, ItemStack ... stacks) {
        this.chest(PlotBuilder.posToBb(pos), stacks);
    }

    default public void chest(String bb, ItemStack ... stacks) {
        this.block(bb, Blocks.CHEST);
        this.customizeBlockEntity(bb, BlockEntityType.CHEST, (T chest) -> {
            for (int i = 0; i < stacks.length; ++i) {
                chest.setItem(i, stacks[i]);
            }
        });
    }

    default public void filledHopper(String bb, Direction direction, ItemLike item) {
        ItemStack stack = new ItemStack(item);
        stack.setCount(stack.getMaxStackSize());
        this.filledHopper(bb, direction, stack);
    }

    default public void filledHopper(String bb, Direction direction, ItemStack stack) {
        this.blockState(bb, (BlockState)Blocks.HOPPER.defaultBlockState().setValue((Property)HopperBlock.FACING, (Comparable)direction));
        this.customizeBlockEntity(bb, BlockEntityType.HOPPER, (T hopper) -> {
            for (int i = 0; i < hopper.getContainerSize(); ++i) {
                hopper.setItem(i, stack.copy());
            }
        });
    }

    default public void hopper(BlockPos pos, Direction direction) {
        this.hopper(PlotBuilder.posToBb(pos), direction, new ItemStack[0]);
    }

    default public void hopper(BlockPos pos, Direction direction, ItemStack ... stacks) {
        this.hopper(PlotBuilder.posToBb(pos), direction, stacks);
    }

    default public void hopper(BlockPos pos, Direction direction, ItemLike ... items) {
        this.hopper(pos, direction, (ItemStack[])Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    default public void hopper(String bb, Direction direction, ItemStack ... stacks) {
        this.blockState(bb, (BlockState)Blocks.HOPPER.defaultBlockState().setValue((Property)HopperBlock.FACING, (Comparable)direction));
        this.customizeBlockEntity(bb, BlockEntityType.HOPPER, (T hopper) -> {
            for (int i = 0; i < stacks.length; ++i) {
                hopper.setItem(i, stacks[i]);
            }
        });
    }

    default public <T extends BlockEntity> void customizeBlockEntity(String bb, BlockEntityType<T> type, Consumer<T> consumer) {
        this.addBuildAction(new BlockEntityCustomizer<T>(this.bb(bb), type, consumer));
    }

    default public <T extends BlockEntity> void customizeBlockEntity(BlockPos pos, BlockEntityType<T> type, Consumer<T> consumer) {
        this.customizeBlockEntity(PlotBuilder.posToBb(pos), type, consumer);
    }

    default public void block(BlockPos pos, Block block) {
        this.block(PlotBuilder.posToBb(pos), block);
    }

    default public void block(String bb, Block block) {
        this.blockState(bb, block.defaultBlockState());
    }

    default public void fluid(String bb, Fluid fluid) {
        this.blockState(bb, fluid.defaultFluidState().createLegacyBlock());
    }

    default public void blockState(BlockPos pos, BlockState blockState) {
        this.blockState(PlotBuilder.posToBb(pos), blockState);
    }

    default public void blockState(String bb, BlockState blockState) {
        this.addBuildAction(new PlaceBlockState(this.bb(bb), blockState));
    }

    public PlotBuilder transform(Function<BoundingBox, BoundingBox> var1);

    default public PlotBuilder offset(int x, int y, int z) {
        return this.transform(bb -> bb.moved(x, y, z));
    }

    default public PlotBuilder offset(BlockPos pos) {
        return this.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    default public void afterGridInitAt(List<BlockPos> positions, BiConsumer<IGrid, IGridNode> consumer) {
        this.addBuildAction(new PostGridInitAction(positions, consumer, true));
    }

    default public void afterGridInitAt(BlockPos pos, BiConsumer<IGrid, IGridNode> consumer) {
        this.afterGridInitAt(List.of(pos), consumer);
    }

    default public void afterGridExistsAt(List<BlockPos> positions, BiConsumer<IGrid, IGridNode> consumer) {
        this.addBuildAction(new PostGridInitAction(positions, consumer, false));
    }

    default public void afterGridExistsAt(BlockPos pos, BiConsumer<IGrid, IGridNode> consumer) {
        this.afterGridExistsAt(List.of(pos), consumer);
    }

    default public void storageDrive(BlockPos pos) {
        this.storageDrive(pos, Direction.NORTH);
    }

    default public void storageDrive(BlockPos pos, Direction facing) {
        this.blockEntity(PlotBuilder.posToBb(pos), AEBlocks.DRIVE, (T drive) -> {
            BlockOrientation.get(facing).setOn((BlockEntity)drive);
            InternalInventory cells = drive.getInternalInventory();
            cells.addItems(AEItems.ITEM_CELL_64K.stack());
            cells.addItems(AEItems.FLUID_CELL_64K.stack());
        });
    }

    default public DriveBuilder drive(BlockPos pos) {
        ArrayList<ItemStack> cells = new ArrayList<ItemStack>(10);
        this.blockEntity(PlotBuilder.posToBb(pos), AEBlocks.DRIVE, (T drive) -> {
            InternalInventory cellInv = drive.getInternalInventory();
            for (ItemStack cell : cells) {
                cellInv.addItems(cell);
            }
        });
        return new DriveBuilder(cells);
    }

    public Test test(Consumer<PlotTestHelper> var1);

    default public void fencedEntity(BlockPos pos, EntityType<?> entity) {
        this.fencedEntity(pos, entity, e -> {});
    }

    default public void entity(BlockPos pos, EntityType<?> entity) {
        this.entity(pos, entity, e -> {});
    }

    default public void entity(BlockPos pos, EntityType<?> entity, Consumer<Entity> postProcessor) {
        this.addBuildAction(new SpawnEntityAction(this.bb(PlotBuilder.posToBb(pos)), entity, postProcessor));
    }

    default public void fencedEntity(BlockPos pos, EntityType<?> entity, Consumer<Entity> postProcessor) {
        PlotBuilder subPlot = this.offset(pos.getX(), pos.getY(), pos.getZ());
        subPlot.block("[-1,1] -1 [-1,1]", Blocks.STONE);
        subPlot.block("[-1,1] 0 [-1,1]", Blocks.STONE_BRICK_WALL);
        subPlot.block("0 0 0", Blocks.AIR);
        this.addBuildAction(new SpawnEntityAction(this.bb(PlotBuilder.posToBb(pos)), entity, postProcessor));
    }

    @FunctionalInterface
    public static interface PostBuildAction {
        public void postBuild(ServerLevel var1, Player var2, BlockPos var3);
    }
}

