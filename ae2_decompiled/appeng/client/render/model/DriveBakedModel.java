/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Transformation
 *  javax.annotation.Nullable
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.ChunkRenderTypeSet
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.joml.Vector3f
 */
package appeng.client.render.model;

import appeng.client.render.DelegateBakedModel;
import appeng.client.render.model.DriveModelData;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Vector3f;

public class DriveBakedModel
extends DelegateBakedModel {
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.CUTOUT});
    private final Map<Item, BakedModel> cellModels;
    private final BakedModel defaultCellModel;
    private final RenderContext.QuadTransform[] slotTransforms;

    public DriveBakedModel(Transformation rotation, BakedModel bakedBase, Map<Item, BakedModel> cellModels, BakedModel defaultCell) {
        super(bakedBase);
        this.defaultCellModel = defaultCell;
        this.slotTransforms = this.buildSlotTransforms(rotation);
        this.cellModels = cellModels;
    }

    public static void getSlotOrigin(int row, int col, Vector3f translation) {
        float xOffset = (float)(9 - col * 8) / 16.0f;
        float yOffset = (float)(13 - row * 3) / 16.0f;
        float zOffset = 0.0625f;
        translation.set(xOffset, yOffset, zOffset);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        ArrayList<BakedQuad> result = new ArrayList<BakedQuad>(super.getQuads(state, side, rand, extraData, renderType));
        Item[] cells = (Item[])extraData.get(DriveModelData.STATE);
        if (cells != null) {
            for (int row = 0; row < 5; ++row) {
                for (int col = 0; col < 2; ++col) {
                    int slot = DriveBakedModel.getSlotIndex(row, col);
                    Item cell = slot < cells.length ? cells[slot] : null;
                    BakedModel cellChassisModel = this.getCellChassisModel(cell);
                    MutableQuadView quadView = MutableQuadView.getInstance();
                    for (BakedQuad quad : cellChassisModel.getQuads(state, side, rand, ModelData.EMPTY, renderType)) {
                        quadView.fromVanilla(quad, side);
                        this.slotTransforms[slot].transform(quadView);
                        result.add(quadView.toBlockBakedQuad());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    public BakedModel getCellChassisModel(Item cell) {
        if (cell == null) {
            return this.cellModels.get(Items.AIR);
        }
        BakedModel model = this.cellModels.get(cell);
        return model != null ? model : this.defaultCellModel;
    }

    private RenderContext.QuadTransform[] buildSlotTransforms(Transformation rotation) {
        RenderContext.QuadTransform[] result = new RenderContext.QuadTransform[10];
        for (int row = 0; row < 5; ++row) {
            for (int col = 0; col < 2; ++col) {
                Vector3f translation = new Vector3f();
                DriveBakedModel.getSlotOrigin(row, col, translation);
                rotation.getLeftRotation().transform(translation);
                result[DriveBakedModel.getSlotIndex((int)row, (int)col)] = new QuadTranslator(translation.x(), translation.y(), translation.z());
            }
        }
        return result;
    }

    private static int getSlotIndex(int row, int col) {
        return row * 2 + col;
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }

    private static class QuadTranslator
    implements RenderContext.QuadTransform {
        private final float x;
        private final float y;
        private final float z;

        public QuadTranslator(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            Vector3f target = new Vector3f();
            for (int i = 0; i < 4; ++i) {
                quad.copyPos(i, target);
                target.add(this.x, this.y, this.z);
                quad.pos(i, target);
            }
            return true;
        }
    }
}

