/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.color.block.BlockColors
 *  net.minecraft.client.color.item.ItemColors
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.BlockModelRotation
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.cablebus;

import appeng.api.parts.PartHelper;
import appeng.api.util.AEAxisAlignedBB;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.FacadeBlockAccess;
import appeng.client.render.cablebus.FacadeRenderState;
import appeng.client.render.cablebus.QuadRotator;
import appeng.parts.misc.CableAnchorPart;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadClamper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadCornerKicker;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadTinter;
import appeng.thirdparty.fabric.Mesh;
import appeng.thirdparty.fabric.MeshBuilder;
import appeng.thirdparty.fabric.MeshBuilderImpl;
import appeng.thirdparty.fabric.ModelHelper;
import appeng.thirdparty.fabric.QuadEmitter;
import appeng.thirdparty.fabric.RenderContext;
import appeng.thirdparty.fabric.Renderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class FacadeBuilder {
    private final Renderer renderer = Renderer.getInstance();
    public static final double THIN_THICKNESS = 0.0605;
    public static final AABB[] THIN_FACADE_BOXES = new AABB[]{new AABB(0.0, 0.0, 0.0, 1.0, 0.0605, 1.0), new AABB(0.0, 0.9395, 0.0, 1.0, 1.0, 1.0), new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 0.0605), new AABB(0.0, 0.0, 0.9395, 1.0, 1.0, 1.0), new AABB(0.0, 0.0, 0.0, 0.0605, 1.0, 1.0), new AABB(0.9395, 0.0, 0.0, 1.0, 1.0, 1.0)};
    private final Map<Direction, Mesh> transparentFacadeQuads;
    private final Map<Direction, Mesh> cableAnchorStilts;

    public FacadeBuilder(ModelBaker modelLoader, @Nullable BakedModel transparentFacadeModel) {
        this.cableAnchorStilts = this.buildCableAnchorStems(modelLoader);
        this.transparentFacadeQuads = new EnumMap<Direction, Mesh>(Direction.class);
        if (transparentFacadeModel != null) {
            List partQuads = transparentFacadeModel.getQuads(null, null, RandomSource.create());
            for (Direction facing : Direction.values()) {
                MeshBuilderImpl meshBuilder = new MeshBuilderImpl();
                QuadEmitter emitter = meshBuilder.getEmitter();
                RenderContext.QuadTransform rotator = QuadRotator.get(facing, 0);
                for (BakedQuad quad : partQuads) {
                    emitter.fromVanilla(quad.getVertices(), 0);
                    emitter.cullFace(null);
                    emitter.nominalFace(quad.getDirection());
                    emitter.shade(quad.isShade());
                    emitter.ambientOcclusion(quad.hasAmbientOcclusion());
                    if (!rotator.transform(emitter)) continue;
                    emitter.emit();
                }
                this.transparentFacadeQuads.put(facing, meshBuilder.build());
            }
        } else {
            for (Direction facing : Direction.values()) {
                this.transparentFacadeQuads.put(facing, new MeshBuilderImpl().build());
            }
        }
    }

    private Map<Direction, Mesh> buildCableAnchorStems(ModelBaker modelLoader) {
        EnumMap<Direction, Mesh> stems = new EnumMap<Direction, Mesh>(Direction.class);
        ArrayList<BakedModel> cableAnchorParts = new ArrayList<BakedModel>();
        for (ResourceLocation model : CableAnchorPart.FACADE_MODELS.getModels()) {
            BakedModel cableAnchor = modelLoader.bake(model, (ModelState)BlockModelRotation.X0_Y0);
            cableAnchorParts.add(cableAnchor);
        }
        for (Direction side : Direction.values()) {
            RenderContext.QuadTransform rotator = QuadRotator.get(side, 0);
            MeshBuilder meshBuilder = this.renderer.meshBuilder();
            QuadEmitter emitter = meshBuilder.getEmitter();
            for (BakedModel model : cableAnchorParts) {
                for (int cullFaceIdx = 0; cullFaceIdx <= 6; ++cullFaceIdx) {
                    Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
                    List quads = model.getQuads(null, cullFace, RandomSource.create());
                    for (BakedQuad quad : quads) {
                        emitter.fromVanilla(quad.getVertices(), 0);
                        emitter.cullFace(cullFace);
                        emitter.nominalFace(quad.getDirection());
                        emitter.shade(quad.isShade());
                        emitter.ambientOcclusion(quad.hasAmbientOcclusion());
                        if (!rotator.transform(emitter)) continue;
                        emitter.emit();
                    }
                }
            }
            stems.put(side, meshBuilder.build());
        }
        return stems;
    }

    public Mesh getFacadeMesh(CableBusRenderState renderState, Supplier<RandomSource> rand, BlockAndTintGetter level, EnumMap<Direction, ModelData> facadeModelData, @Nullable RenderType renderType) {
        boolean transparent = PartHelper.getCableRenderMode().transparentFacades;
        EnumMap<Direction, FacadeRenderState> facadeStates = renderState.getFacades();
        List<AABB> partBoxes = renderState.getBoundingBoxes();
        Set<Direction> sidesWithParts = renderState.getAttachments().keySet();
        BlockPos pos = renderState.getPos();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        MeshBuilder meshBuilder = this.renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        for (Map.Entry entry : facadeStates.entrySet()) {
            AABB fullBounds;
            boolean renderStilt;
            Direction side = (Direction)entry.getKey();
            int sideIndex = side.ordinal();
            FacadeRenderState facadeRenderState = (FacadeRenderState)entry.getValue();
            boolean bl = renderStilt = !sidesWithParts.contains(side);
            if (renderStilt) {
                this.cableAnchorStilts.get(side).forEach(quad -> {
                    quad.copyTo(emitter);
                    emitter.emit();
                });
            }
            if (transparent) {
                this.transparentFacadeQuads.get(side).forEach(quad -> {
                    quad.copyTo(emitter);
                    emitter.emit();
                });
                continue;
            }
            BlockState blockState = facadeRenderState.getSourceBlock();
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            BakedModel model = dispatcher.getBlockModel(blockState);
            ModelData modelData = Objects.requireNonNullElse(facadeModelData.get(side), ModelData.EMPTY);
            if (renderType != null && !model.getRenderTypes(blockState, rand.get(), modelData).contains(renderType)) continue;
            AABB facadeBox = fullBounds = THIN_FACADE_BOXES[sideIndex];
            if (facadeRenderState.isTransparent()) {
                double offset = 0.0605;
                AEAxisAlignedBB tmpBB = null;
                block9: for (Direction face : Direction.values()) {
                    FacadeRenderState otherState;
                    if (face.getAxis() == side.getAxis() || (otherState = (FacadeRenderState)facadeStates.get(face)) == null || otherState.isTransparent()) continue;
                    if (tmpBB == null) {
                        tmpBB = AEAxisAlignedBB.fromBounds(facadeBox);
                    }
                    switch (face) {
                        case DOWN: {
                            tmpBB.minY += offset;
                            continue block9;
                        }
                        case UP: {
                            tmpBB.maxY -= offset;
                            continue block9;
                        }
                        case NORTH: {
                            tmpBB.minZ += offset;
                            continue block9;
                        }
                        case SOUTH: {
                            tmpBB.maxZ -= offset;
                            continue block9;
                        }
                        case WEST: {
                            tmpBB.minX += offset;
                            continue block9;
                        }
                        case EAST: {
                            tmpBB.maxX -= offset;
                            continue block9;
                        }
                        default: {
                            throw new RuntimeException("Switch falloff. " + String.valueOf(face));
                        }
                    }
                }
                if (tmpBB != null) {
                    facadeBox = tmpBB.getBoundingBox();
                }
            }
            int facadeMask = 0;
            for (Map.Entry ent : facadeStates.entrySet()) {
                FacadeRenderState otherState;
                Direction s = (Direction)ent.getKey();
                if (s.getAxis() == side.getAxis() || (otherState = (FacadeRenderState)ent.getValue()).isTransparent()) continue;
                facadeMask |= 1 << s.ordinal();
            }
            AEAxisAlignedBB cutOutBox = FacadeBuilder.getCutOutBox(facadeBox, partBoxes);
            List<AABB> holeStrips = FacadeBuilder.getBoxes(facadeBox, cutOutBox, side.getAxis());
            FacadeBlockAccess facadeAccess = new FacadeBlockAccess(level, pos, side, blockState);
            QuadFaceStripper faceStripper = new QuadFaceStripper(fullBounds, facadeMask);
            QuadCornerKicker kicker = new QuadCornerKicker();
            kicker.setSide(sideIndex);
            kicker.setFacadeMask(facadeMask);
            kicker.setBox(fullBounds);
            kicker.setThickness(0.0605);
            QuadReInterpolator interpolator = new QuadReInterpolator();
            for (int cullFaceIdx = 0; cullFaceIdx <= 6; ++cullFaceIdx) {
                BlockPos adjPos;
                BlockState adjState;
                Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
                List quads = model.getQuads(blockState, cullFace, rand.get(), modelData, renderType);
                if (cullFace != null && blockState.skipRendering(adjState = level.getBlockState(adjPos = pos.relative(cullFace)).getAppearance(level, adjPos, cullFace.getOpposite(), blockState, pos), cullFace)) continue;
                for (BakedQuad quad2 : quads) {
                    QuadTinter quadTinter = null;
                    if (quad2.getTintIndex() != -1) {
                        quadTinter = new QuadTinter(blockColors.getColor(blockState, (BlockAndTintGetter)facadeAccess, pos, quad2.getTintIndex()));
                    }
                    for (AABB box : holeStrips) {
                        emitter.fromVanilla(quad2.getVertices(), 0);
                        emitter.cullFace((Direction)(cullFace == side ? side : null));
                        emitter.nominalFace(quad2.getDirection());
                        emitter.shade(quad2.isShade());
                        emitter.ambientOcclusion(quad2.hasAmbientOcclusion());
                        interpolator.setInputQuad(emitter);
                        QuadClamper clamper = new QuadClamper(box);
                        if (!clamper.transform(emitter) || !faceStripper.transform(emitter) || !kicker.transform(emitter)) continue;
                        interpolator.transform(emitter);
                        if (quadTinter != null) {
                            quadTinter.transform(emitter);
                        }
                        emitter.emit();
                    }
                }
            }
        }
        return meshBuilder.build();
    }

    public Mesh buildFacadeItemQuads(ItemStack textureItem, Direction side) {
        MeshBuilder meshBuilder = this.renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(textureItem, null, null, 0);
        QuadReInterpolator interpolator = new QuadReInterpolator();
        ItemColors itemColors = Minecraft.getInstance().getItemColors();
        QuadClamper clamper = new QuadClamper(THIN_FACADE_BOXES[side.ordinal()]);
        for (int cullFaceIdx = 0; cullFaceIdx <= 6; ++cullFaceIdx) {
            Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
            List quads = model.getQuads(null, cullFace, RandomSource.create());
            for (BakedQuad quad : quads) {
                QuadTinter quadTinter = null;
                if (quad.getTintIndex() != -1) {
                    quadTinter = new QuadTinter(itemColors.getColor(textureItem, quad.getTintIndex()));
                }
                emitter.fromVanilla(quad.getVertices(), 0);
                emitter.cullFace(cullFace);
                emitter.nominalFace(quad.getDirection());
                emitter.shade(quad.isShade());
                emitter.ambientOcclusion(quad.hasAmbientOcclusion());
                interpolator.setInputQuad(emitter);
                if (!clamper.transform(emitter)) continue;
                interpolator.transform(emitter);
                if (quadTinter != null) {
                    quadTinter.transform(emitter);
                }
                emitter.emit();
            }
        }
        return meshBuilder.build();
    }

    @Nullable
    private static AEAxisAlignedBB getCutOutBox(AABB facadeBox, List<AABB> partBoxes) {
        AEAxisAlignedBB b = null;
        for (AABB bb : partBoxes) {
            if (!bb.intersects(facadeBox)) continue;
            if (b == null) {
                b = AEAxisAlignedBB.fromBounds(bb);
                continue;
            }
            b.maxX = Math.max(b.maxX, bb.maxX);
            b.maxY = Math.max(b.maxY, bb.maxY);
            b.maxZ = Math.max(b.maxZ, bb.maxZ);
            b.minX = Math.min(b.minX, bb.minX);
            b.minY = Math.min(b.minY, bb.minY);
            b.minZ = Math.min(b.minZ, bb.minZ);
        }
        return b;
    }

    private static List<AABB> getBoxes(AABB fb, AEAxisAlignedBB hole, Direction.Axis axis) {
        if (hole == null) {
            return Collections.singletonList(fb);
        }
        ArrayList<AABB> boxes = new ArrayList<AABB>();
        switch (axis) {
            case Y: {
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(hole.minX, fb.minY, fb.minZ, hole.maxX, fb.maxY, hole.minZ));
                boxes.add(new AABB(hole.minX, fb.minY, hole.maxZ, hole.maxX, fb.maxY, fb.maxZ));
                break;
            }
            case Z: {
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.minY, fb.minZ, hole.minX, hole.maxY, fb.maxZ));
                boxes.add(new AABB(hole.maxX, hole.minY, fb.minZ, fb.maxX, hole.maxY, fb.maxZ));
                break;
            }
            case X: {
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.minY, fb.minZ, fb.maxX, hole.maxY, hole.minZ));
                boxes.add(new AABB(fb.minX, hole.minY, hole.maxZ, fb.maxX, hole.maxY, fb.maxZ));
                break;
            }
            default: {
                throw new RuntimeException("switch falloff. " + String.valueOf(axis));
            }
        }
        return boxes;
    }
}

