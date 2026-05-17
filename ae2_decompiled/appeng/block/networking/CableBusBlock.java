/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleEngine
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.pathfinder.PathComputationType
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.networking;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusBreakingParticle;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.FacadeRenderState;
import appeng.integration.abstraction.IAEFacade;
import appeng.parts.CableBusContainer;
import appeng.parts.ICableBusContainer;
import appeng.parts.NullCableBusContainer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class CableBusBlock
extends AEBaseEntityBlock<CableBusBlockEntity>
implements IAEFacade,
SimpleWaterloggedBlock {
    private static final ICableBusContainer NULL_CABLE_BUS = new NullCableBusContainer();
    private static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create((String)"light_level", (int)0, (int)15);
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static ThreadLocal<Direction> RENDERING_FACADE_DIRECTION = new ThreadLocal();

    public CableBusBlock() {
        super(CableBusBlock.glassProps().noOcclusion().noLootTable().dynamicShape().forceSolidOn().lightLevel(state -> (Integer)state.getValue((Property)LIGHT_LEVEL)));
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)LIGHT_LEVEL, (Comparable)Integer.valueOf(0))).setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(false)));
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Object object = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (object instanceof CableBusBlockEntity) {
            CableBusBlockEntity bus = (CableBusBlockEntity)object;
            ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
            bus.getCableBus().addPartDrops(drops);
            return drops;
        }
        return List.of();
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        this.cb((BlockGetter)level, pos).animateTick(level, pos, rand);
    }

    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.cb(level, pos).isProvidingWeakPower(side.getOpposite());
    }

    public boolean isSignalSource(BlockState state) {
        return true;
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
        this.cb((BlockGetter)level, pos).onEntityCollision(entityIn);
    }

    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.cb(level, pos).isProvidingStrongPower(side.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{LIGHT_LEVEL, WATERLOGGED});
    }

    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return this.cb((BlockGetter)level, pos).isLadder(entity);
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return super.canBeReplaced(state, useContext) && this.cb((BlockGetter)useContext.getLevel(), useContext.getClickedPos()).isEmpty();
    }

    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction side) {
        if (side == null) {
            return false;
        }
        return this.cb(level, pos).canConnectRedstone(side.getOpposite());
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        Vec3 v3 = target.getLocation().subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        SelectedPart sp = this.cb((BlockGetter)level, pos).selectPartLocal(v3);
        if (sp.part != null) {
            return new ItemStack(sp.part.getPartItem());
        }
        if (sp.facade != null) {
            return sp.facade.getItemStack();
        }
        return ItemStack.EMPTY;
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            this.cb((BlockGetter)level, pos).onNeighborChanged((BlockGetter)level, pos, fromPos);
        }
    }

    private ICableBusContainer cb(BlockGetter level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        CableBusContainer out = null;
        if (te instanceof CableBusBlockEntity) {
            out = ((CableBusBlockEntity)te).getCableBus();
        }
        return out == null ? NULL_CABLE_BUS : out;
    }

    @Nullable
    private IFacadeContainer fc(BlockGetter level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        IFacadeContainer out = null;
        if (te instanceof CableBusBlockEntity) {
            out = ((CableBusBlockEntity)te).getCableBus().getFacadeContainer();
        }
        return out;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Vec3 hitVec = hit.getLocation();
        Vec3 hitInBlock = new Vec3(hitVec.x - (double)pos.getX(), hitVec.y - (double)pos.getY(), hitVec.z - (double)pos.getZ());
        return this.cb((BlockGetter)level, pos).useItemOn(heldItem, player, hand, hitInBlock) ? ItemInteractionResult.sidedSuccess((boolean)level.isClientSide()) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Vec3 hitVec = hitResult.getLocation();
        Vec3 hitInBlock = new Vec3(hitVec.x - (double)pos.getX(), hitVec.y - (double)pos.getY(), hitVec.z - (double)pos.getZ());
        return this.cb((BlockGetter)level, pos).useWithoutItem(player, hitInBlock) ? InteractionResult.sidedSuccess((boolean)level.isClientSide()) : InteractionResult.PASS;
    }

    public boolean recolorBlock(BlockGetter level, BlockPos pos, Direction side, DyeColor color, Player who) {
        try {
            return this.cb(level, pos).recolourBlock(side, AEColor.fromDye(color), who);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    }

    @Override
    public BlockState getFacadeState(BlockGetter level, BlockPos pos, Direction side) {
        IFacadePart facade;
        IFacadeContainer container;
        if (side != null && (container = this.fc(level, pos)) != null && (facade = container.getFacade(side)) != null) {
            return facade.getBlockState();
        }
        return level.getBlockState(pos);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        CableBusBlockEntity te = (CableBusBlockEntity)this.getBlockEntity(level, pos);
        if (te == null) {
            return Shapes.empty();
        }
        return te.getCableBus().getShape();
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        CableBusBlockEntity te = (CableBusBlockEntity)this.getBlockEntity(level, pos);
        if (te == null) {
            return Shapes.empty();
        }
        return te.getCableBus().getCollisionShape(context);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, CableBusBlockEntity be) {
        if (currentState.getBlock() != this) {
            return currentState;
        }
        int lightLevel = be.getCableBus().getLightValue();
        return (BlockState)super.updateBlockStateFromBlockEntity(currentState, be).setValue((Property)LIGHT_LEVEL, (Comparable)Integer.valueOf(lightLevel));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(Level level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return (BlockState)this.defaultBlockState().setValue((Property)WATERLOGGED, (Comparable)Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    public FluidState getFluidState(BlockState blockState) {
        return (Boolean)blockState.getValue((Property)WATERLOGGED) != false ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (((Boolean)blockState.getValue((Property)WATERLOGGED)).booleanValue()) {
            level.scheduleTick(currentPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)level));
        }
        this.cb((BlockGetter)level, currentPos).onUpdateShape(level, currentPos, facing);
        return super.updateShape(blockState, facing, facingState, level, currentPos, facingPos);
    }

    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        this.cb((BlockGetter)level, pos).onNeighborChanged((BlockGetter)level, pos, neighbor);
    }

    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions(){

            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine effectRenderer) {
                if (level.getRandom().nextBoolean()) {
                    return true;
                }
                if (target.getType() != HitResult.Type.BLOCK) {
                    return false;
                }
                BlockPos blockPos = BlockPos.containing((double)target.getLocation().x, (double)target.getLocation().y, (double)target.getLocation().z);
                ICableBusContainer cb = CableBusBlock.this.cb((BlockGetter)level, blockPos);
                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(CableBusBlock.this.defaultBlockState());
                if (!(model instanceof CableBusBakedModel)) {
                    return true;
                }
                CableBusBakedModel cableBusModel = (CableBusBakedModel)model;
                CableBusRenderState renderState = cb.getRenderState();
                List<TextureAtlasSprite> textures = cableBusModel.getParticleTextures(renderState);
                if (!textures.isEmpty()) {
                    TextureAtlasSprite texture = (TextureAtlasSprite)Util.getRandom(textures, (RandomSource)level.getRandom());
                    double x = target.getLocation().x;
                    double y = target.getLocation().y;
                    double z = target.getLocation().z;
                    effectRenderer.add(new CableBusBreakingParticle((ClientLevel)level, x, y, z, texture).scale(0.8f));
                }
                return true;
            }

            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine effectRenderer) {
                ICableBusContainer cb = CableBusBlock.this.cb((BlockGetter)level, pos);
                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(CableBusBlock.this.defaultBlockState());
                if (!(model instanceof CableBusBakedModel)) {
                    return true;
                }
                CableBusBakedModel cableBusModel = (CableBusBakedModel)model;
                CableBusRenderState renderState = cb.getRenderState();
                List<TextureAtlasSprite> textures = cableBusModel.getParticleTextures(renderState);
                if (!textures.isEmpty()) {
                    for (int j = 0; j < 4; ++j) {
                        for (int k = 0; k < 4; ++k) {
                            for (int l = 0; l < 4; ++l) {
                                TextureAtlasSprite texture = (TextureAtlasSprite)Util.getRandom(textures, (RandomSource)level.getRandom());
                                double x = (double)pos.getX() + ((double)j + 0.5) / 4.0;
                                double y = (double)pos.getY() + ((double)k + 0.5) / 4.0;
                                double z = (double)pos.getZ() + ((double)l + 0.5) / 4.0;
                                CableBusBreakingParticle effect = new CableBusBreakingParticle((ClientLevel)level, x, y, z, x - (double)pos.getX() - 0.5, y - (double)pos.getY() - 0.5, z - (double)pos.getZ() - 0.5, texture);
                                effectRenderer.add((Particle)effect);
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        ModelData modelData;
        if (renderView instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)renderView;
            BlockEntity be = renderView.getBlockEntity(pos);
            modelData = be != null ? be.getModelData() : ModelData.EMPTY;
        } else {
            modelData = renderView.getModelData(pos);
        }
        CableBusRenderState cableBusRenderState = (CableBusRenderState)modelData.get(CableBusRenderState.PROPERTY);
        if (cableBusRenderState != null) {
            FacadeRenderState facadeState;
            Direction renderingFacadeDir = RENDERING_FACADE_DIRECTION.get();
            EnumMap<Direction, FacadeRenderState> facades = cableBusRenderState.getFacades();
            if (side.getOpposite() != renderingFacadeDir && (facadeState = facades.get(side)) != null) {
                return facadeState.getSourceBlock();
            }
            if (renderingFacadeDir != null && facades.containsKey(renderingFacadeDir)) {
                return facades.get(renderingFacadeDir).getSourceBlock();
            }
        }
        return state;
    }
}

