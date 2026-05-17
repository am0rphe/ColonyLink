/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.ChunkRenderTypeSet
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.cablebus;

import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.networking.CableBusBlock;
import appeng.client.render.cablebus.CableBuilder;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.CableCoreType;
import appeng.client.render.cablebus.FacadeBuilder;
import appeng.client.render.cablebus.FacadeRenderState;
import appeng.client.render.cablebus.QuadRotator;
import appeng.client.render.model.AEModelData;
import appeng.thirdparty.fabric.MeshBuilderImpl;
import appeng.thirdparty.fabric.QuadEmitter;
import appeng.thirdparty.fabric.RenderContext;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CableBusBakedModel
implements IDynamicBakedModel {
    private static final int CACHE_QUAD_COUNT = 5000;
    private static final Direction[] SPIN_TO_DIRECTION = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST, Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST, Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH, Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH};
    private static final ModelProperty<FacadeModelData> FACADE_DATA = new ModelProperty();
    private final LoadingCache<CableBusRenderState, List<BakedQuad>> cableModelCache;
    private final CableBuilder cableBuilder;
    private final FacadeBuilder facadeBuilder;
    private final Map<ResourceLocation, BakedModel> partModels;
    private final TextureAtlasSprite particleTexture;

    CableBusBakedModel(CableBuilder cableBuilder, FacadeBuilder facadeBuilder, Map<ResourceLocation, BakedModel> partModels, TextureAtlasSprite particleTexture) {
        this.cableBuilder = cableBuilder;
        this.facadeBuilder = facadeBuilder;
        this.partModels = partModels;
        this.particleTexture = particleTexture;
        this.cableModelCache = CacheBuilder.newBuilder().maximumWeight(5000L).weigher((key, value) -> value.size()).build((CacheLoader)new CacheLoader<CableBusRenderState, List<BakedQuad>>(){

            public List<BakedQuad> load(CableBusRenderState renderState) {
                ArrayList<BakedQuad> model = new ArrayList<BakedQuad>();
                CableBusBakedModel.this.addCableQuads(renderState, model);
                return model;
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NotNull
    public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data) {
        CableBusRenderState renderState = (CableBusRenderState)data.get(CableBusRenderState.PROPERTY);
        if (renderState == null || renderState.getFacades().isEmpty()) {
            return data;
        }
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        EnumMap<Direction, ModelData> facadeModelData = new EnumMap<Direction, ModelData>(Direction.class);
        for (Map.Entry<Direction, FacadeRenderState> entry : renderState.getFacades().entrySet()) {
            Direction side = entry.getKey();
            CableBusBlock.RENDERING_FACADE_DIRECTION.set(side);
            try {
                BlockState blockState = entry.getValue().getSourceBlock();
                BakedModel model = dispatcher.getBlockModel(blockState);
                facadeModelData.put(side, model.getModelData(level, pos, blockState, data));
            }
            finally {
                CableBusBlock.RENDERING_FACADE_DIRECTION.set(null);
            }
        }
        return data.derive().with(FACADE_DATA, (Object)new FacadeModelData(facadeModelData, level)).build();
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, RenderType renderType) {
        FacadeModelData facadeData;
        CableBusRenderState renderState = (CableBusRenderState)data.get(CableBusRenderState.PROPERTY);
        if (renderState == null || side != null) {
            return Collections.emptyList();
        }
        ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>();
        if (renderType == null || renderType == RenderType.cutout()) {
            List cableModel = (List)this.cableModelCache.getUnchecked((Object)renderState);
            quads.addAll(cableModel);
            MeshBuilderImpl meshBuilder = new MeshBuilderImpl();
            QuadEmitter emitter = meshBuilder.getEmitter();
            for (Direction facing : Direction.values()) {
                IPartModel partModel = renderState.getAttachments().get(facing);
                if (partModel == null) continue;
                ModelData partModelData = renderState.getPartModelData().get(facing);
                if (partModelData == null) {
                    partModelData = ModelData.EMPTY;
                }
                for (ResourceLocation model : partModel.getModels()) {
                    BakedModel bakedModel = this.partModels.get(model);
                    if (bakedModel == null) {
                        throw new IllegalStateException("Trying to use an unregistered part model: " + String.valueOf(model));
                    }
                    List partQuads = bakedModel.getQuads(state, null, rand, partModelData, renderType);
                    int spin = CableBusBakedModel.getPartSpin(partModelData);
                    RenderContext.QuadTransform rotator = QuadRotator.get(facing, spin);
                    for (BakedQuad partQuad : partQuads) {
                        emitter.fromVanilla(partQuad, null);
                        rotator.transform(emitter);
                        quads.add(emitter.toBakedQuad(partQuad.getSprite()));
                    }
                }
            }
        }
        if ((facadeData = (FacadeModelData)data.get(FACADE_DATA)) != null) {
            this.facadeBuilder.getFacadeMesh(renderState, () -> rand, facadeData.level, facadeData.facadeData, renderType).forEach(qv -> quads.add(qv.toBlockBakedQuad()));
        }
        return quads;
    }

    private static boolean isStraightLine(AECableType cableType, EnumMap<Direction, AECableType> sides) {
        Iterator<Map.Entry<Direction, AECableType>> it = sides.entrySet().iterator();
        if (!it.hasNext()) {
            return false;
        }
        Map.Entry<Direction, AECableType> nextConnection = it.next();
        Direction firstSide = nextConnection.getKey();
        AECableType firstType = nextConnection.getValue();
        if (!it.hasNext()) {
            return false;
        }
        if (firstSide.getOpposite() != it.next().getKey()) {
            return false;
        }
        if (it.hasNext()) {
            return false;
        }
        AECableType secondType = sides.get(firstSide.getOpposite());
        return firstType == secondType && cableType == firstType && cableType == secondType;
    }

    private static int getPartSpin(ModelData partModelData) {
        Byte spin = (Byte)partModelData.get(AEModelData.SPIN);
        if (spin != null) {
            return spin.byteValue();
        }
        return 0;
    }

    private void addCableQuads(CableBusRenderState renderState, List<BakedQuad> quadsOut) {
        boolean noAttachments;
        AECableType cableType = renderState.getCableType();
        if (cableType == AECableType.NONE) {
            return;
        }
        AEColor cableColor = renderState.getCableColor();
        EnumMap<Direction, AECableType> connectionTypes = renderState.getConnectionTypes();
        boolean bl = noAttachments = !renderState.getAttachments().values().stream().anyMatch(IPartModel::requireCableConnection);
        if (noAttachments && CableBusBakedModel.isStraightLine(cableType, connectionTypes)) {
            Direction facing = connectionTypes.keySet().iterator().next();
            switch (cableType) {
                case GLASS: {
                    this.cableBuilder.addStraightGlassConnection(facing, cableColor, quadsOut);
                    break;
                }
                case COVERED: {
                    this.cableBuilder.addStraightCoveredConnection(facing, cableColor, quadsOut);
                    break;
                }
                case SMART: {
                    this.cableBuilder.addStraightSmartConnection(facing, cableColor, renderState.getChannelsOnSide().get(facing), quadsOut);
                    break;
                }
                case DENSE_COVERED: {
                    this.cableBuilder.addStraightDenseCoveredConnection(facing, cableColor, quadsOut);
                    break;
                }
                case DENSE_SMART: {
                    this.cableBuilder.addStraightDenseSmartConnection(facing, cableColor, renderState.getChannelsOnSide().get(facing), quadsOut);
                    break;
                }
            }
            return;
        }
        this.cableBuilder.addCableCore(renderState.getCoreType(), cableColor, quadsOut);
        EnumMap<Direction, Integer> attachmentConnections = renderState.getAttachmentConnections();
        for (Direction direction : attachmentConnections.keySet()) {
            int distance = attachmentConnections.get(direction);
            int channels = renderState.getChannelsOnSide().get(direction);
            switch (cableType) {
                case GLASS: {
                    this.cableBuilder.addConstrainedGlassConnection(direction, cableColor, distance, quadsOut);
                    break;
                }
                case COVERED: {
                    this.cableBuilder.addConstrainedCoveredConnection(direction, cableColor, distance, quadsOut);
                    break;
                }
                case SMART: {
                    this.cableBuilder.addConstrainedSmartConnection(direction, cableColor, distance, channels, quadsOut);
                    break;
                }
                case DENSE_COVERED: 
                case DENSE_SMART: {
                    break;
                }
            }
        }
        for (Map.Entry entry : connectionTypes.entrySet()) {
            Direction facing = (Direction)entry.getKey();
            AECableType connectionType = (AECableType)((Object)entry.getValue());
            boolean cableBusAdjacent = renderState.getCableBusAdjacent().contains(facing);
            int channels = renderState.getChannelsOnSide().get(facing);
            switch (cableType) {
                case GLASS: {
                    this.cableBuilder.addGlassConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
                    break;
                }
                case COVERED: {
                    this.cableBuilder.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
                    break;
                }
                case SMART: {
                    this.cableBuilder.addSmartConnection(facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut);
                    break;
                }
                case DENSE_COVERED: {
                    this.cableBuilder.addDenseCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
                    break;
                }
                case DENSE_SMART: {
                    this.cableBuilder.addDenseSmartConnection(facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut);
                    break;
                }
            }
        }
    }

    public List<TextureAtlasSprite> getParticleTextures(CableBusRenderState renderState) {
        CableCoreType coreType = CableCoreType.fromCableType(renderState.getCableType());
        AEColor cableColor = renderState.getCableColor();
        ArrayList<TextureAtlasSprite> result = new ArrayList<TextureAtlasSprite>();
        if (coreType != null) {
            result.add(this.cableBuilder.getCoreTexture(coreType, cableColor));
        }
        for (Direction side : renderState.getAttachments().keySet()) {
            IPartModel partModel = renderState.getAttachments().get(side);
            for (ResourceLocation model : partModel.getModels()) {
                BakedModel bakedModel = this.partModels.get(model);
                if (bakedModel == null) {
                    throw new IllegalStateException("Trying to use an unregistered part model: " + String.valueOf(model));
                }
                TextureAtlasSprite particleTexture = bakedModel.getParticleIcon();
                if (this.isMissingTexture(particleTexture)) continue;
                result.add(particleTexture);
            }
        }
        return result;
    }

    private boolean isMissingTexture(TextureAtlasSprite particleTexture) {
        return particleTexture.contents().name().equals((Object)MissingTextureAtlasSprite.getLocation());
    }

    public boolean useAmbientOcclusion() {
        return true;
    }

    public boolean isGui3d() {
        return false;
    }

    public boolean usesBlockLight() {
        return false;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.particleTexture;
    }

    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.all();
    }

    private record FacadeModelData(EnumMap<Direction, ModelData> facadeData, BlockAndTintGetter level) {
    }
}

