/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.joml.Quaternionf
 */
package appeng.client.render.tesr;

import appeng.api.inventories.InternalInventory;
import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

public final class InscriberTESR
implements BlockEntityRenderer<InscriberBlockEntity> {
    private static final float ITEM_RENDER_SCALE = 0.8333333f;
    private static final Material TEXTURE_INSIDE = new Material(InventoryMenu.BLOCK_ATLAS, AppEng.makeId("block/inscriber_inside"));

    public InscriberTESR(BlockEntityRendererProvider.Context context) {
    }

    public void render(InscriberBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        boolean renderPresses;
        float relativeProgress;
        float progress;
        long currentTime;
        ms.pushPose();
        ms.translate(0.5f, 0.5f, 0.5f);
        BlockOrientation orientation = BlockOrientation.get(blockEntity);
        ms.mulPose(orientation.getQuaternion());
        ms.translate(-0.5f, -0.5f, -0.5f);
        long absoluteProgress = 0L;
        if (blockEntity.isSmash() && (absoluteProgress = (currentTime = System.currentTimeMillis()) - blockEntity.getClientStart()) > 800L) {
            blockEntity.setSmash(false);
            if (blockEntity.isRepeatSmash()) {
                blockEntity.setSmash(true);
            }
        }
        progress = (progress = (relativeProgress = (float)(absoluteProgress % 800L) / 400.0f)) > 1.0f ? 1.0f - InscriberTESR.easeDecompressMotion(progress - 1.0f) : InscriberTESR.easeCompressMotion(progress);
        float press = 0.2f;
        press -= progress / 5.0f;
        float middle = 0.5f;
        float TwoPx = 0.125f;
        float base = 0.4f;
        TextureAtlasSprite tas = TEXTURE_INSIDE.sprite();
        VertexConsumer buffer = buffers.getBuffer(RenderType.solid());
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, (middle += 0.02f) + press, 0.125f, 0.875f, 0.125f, combinedOverlay, combinedLight, Direction.DOWN);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle + press, 0.125f, 0.125f, 0.125f, combinedOverlay, combinedLight, Direction.DOWN);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle + press, 0.875f, 0.125f, 0.875f, combinedOverlay, combinedLight, Direction.DOWN);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle + press, 0.875f, 0.875f, 0.875f, combinedOverlay, combinedLight, Direction.DOWN);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle + 0.4f, 0.125f, 0.125f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle + 0.4f, 0.125f, 0.875f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle + press, 0.125f, 0.875f, 0.125f, combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle + press, 0.125f, 0.125f, 0.125f, combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle + 0.4f, 0.875f, 0.125f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle + press, 0.875f, 0.125f, 0.125f, combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle + press, 0.875f, 0.875f, 0.125f, combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle + 0.4f, 0.875f, 0.875f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, (middle -= 0.04f) - press, 0.125f, 0.875f, 0.125f, combinedOverlay, combinedLight, Direction.UP);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle - press, 0.125f, 0.125f, 0.125f, combinedOverlay, combinedLight, Direction.UP);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle - press, 0.875f, 0.125f, 0.875f, combinedOverlay, combinedLight, Direction.UP);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle - press, 0.875f, 0.875f, 0.875f, combinedOverlay, combinedLight, Direction.UP);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle - 0.4f, 0.125f, 0.125f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle - 0.4f, 0.125f, 0.875f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle - press, 0.125f, 0.875f, 0.125f, combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle - press, 0.125f, 0.125f, 0.125f, combinedOverlay, combinedLight, Direction.NORTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle - press, 0.875f, 0.875f, 0.125f, combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.125f, middle - 0.4f, 0.875f, 0.875f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle - 0.4f, 0.875f, 0.125f, 0.125f - (press - 0.4f), combinedOverlay, combinedLight, Direction.SOUTH);
        InscriberTESR.addVertex(buffer, ms, tas, 0.875f, middle - press, 0.875f, 0.125f, 0.125f, combinedOverlay, combinedLight, Direction.SOUTH);
        InternalInventory inv = blockEntity.getInternalInventory();
        int items = 0;
        if (!inv.getStackInSlot(0).isEmpty()) {
            ++items;
        }
        if (!inv.getStackInSlot(1).isEmpty()) {
            ++items;
        }
        if (!inv.getStackInSlot(2).isEmpty()) {
            ++items;
        }
        if (relativeProgress > 1.0f || items == 0) {
            InscriberRecipe ir;
            renderPresses = false;
            ItemStack is = inv.getStackInSlot(3);
            if (is.isEmpty() && (ir = blockEntity.getTask()) != null) {
                renderPresses = ir.getProcessType() == InscriberProcessType.INSCRIBE;
                is = ir.getResultItem().copy();
            }
            this.renderItem(ms, is, 0.0f, buffers, combinedLight, combinedOverlay, blockEntity.getLevel());
        } else {
            renderPresses = true;
            this.renderItem(ms, inv.getStackInSlot(2), 0.0f, buffers, combinedLight, combinedOverlay, blockEntity.getLevel());
        }
        if (renderPresses) {
            this.renderItem(ms, inv.getStackInSlot(0), press, buffers, combinedLight, combinedOverlay, blockEntity.getLevel());
            this.renderItem(ms, inv.getStackInSlot(1), -press, buffers, combinedLight, combinedOverlay, blockEntity.getLevel());
        }
        ms.popPose();
    }

    private static void addVertex(VertexConsumer vb, PoseStack ms, TextureAtlasSprite sprite, float x, float y, float z, float texU, float texV, int overlayUV, int lightmapUV, Direction front) {
        vb.addVertex(ms.last().pose(), x, y, z);
        vb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        vb.setUv(sprite.getU(texU), sprite.getV(texV));
        vb.setOverlay(overlayUV);
        vb.setLight(lightmapUV);
        vb.setNormal(ms.last(), (float)front.getStepX(), (float)front.getStepY(), (float)front.getStepZ());
    }

    private void renderItem(PoseStack ms, ItemStack stack, float o, MultiBufferSource buffers, int combinedLight, int combinedOverlay, Level level) {
        if (!stack.isEmpty()) {
            ms.pushPose();
            ms.translate(0.5f, 0.5f + o, 0.5f);
            ms.mulPose(new Quaternionf().rotationX(1.5707964f));
            ms.scale(0.8333333f, 0.8333333f, 0.8333333f);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel model = itemRenderer.getItemModelShaper().getItemModel(stack);
            List quads = model.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, null);
            if (quads != null && !quads.isEmpty()) {
                ms.scale(0.5f, 0.5f, 0.5f);
            }
            RenderSystem.applyModelViewMatrix();
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, ms, buffers, level, 0);
            ms.popPose();
        }
    }

    private static float easeCompressMotion(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return (float)(1.0 + (double)c3 * Math.pow(x - 1.0f, 3.0) + (double)c1 * Math.pow(x - 1.0f, 2.0));
    }

    private static float easeDecompressMotion(float x) {
        return x * x * x * x * x;
    }
}

