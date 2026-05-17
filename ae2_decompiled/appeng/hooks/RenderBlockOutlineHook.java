/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$DepthTestStateShard
 *  net.minecraft.client.renderer.RenderStateShard$LineStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.client.event.RenderHighlightEvent$Block
 *  net.neoforged.neoforge.common.NeoForge
 */
package appeng.hooks;

import appeng.api.implementations.items.IFacadeItem;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.core.AEConfig;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import appeng.parts.BusCollisionHelper;
import appeng.parts.PartPlacement;
import appeng.parts.misc.CableAnchorPart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

public class RenderBlockOutlineHook {
    public static final RenderType LINES_BEHIND_BLOCK = RenderType.create((String)"lines_behind_block", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.LINES, (int)256, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setDepthTestState(new RenderStateShard.DepthTestStateShard(">", 516)).setOutputState(RenderStateShard.ITEM_ENTITY_TARGET).setWriteMaskState(RenderStateShard.COLOR_WRITE).setCullState(RenderStateShard.NO_CULL).createCompositeState(false));

    private RenderBlockOutlineHook() {
    }

    public static void install() {
        NeoForge.EVENT_BUS.addListener(RenderBlockOutlineHook::handleEvent);
    }

    private static void handleEvent(RenderHighlightEvent.Block evt) {
        ClientLevel level = Minecraft.getInstance().level;
        PoseStack poseStack = evt.getPoseStack();
        MultiBufferSource buffers = evt.getMultiBufferSource();
        Camera camera = evt.getCamera();
        if (level == null || buffers == null) {
            return;
        }
        BlockHitResult blockHitResult = evt.getTarget();
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (RenderBlockOutlineHook.replaceBlockOutline(level, poseStack, buffers, camera, blockHitResult)) {
            evt.setCanceled(true);
        }
    }

    private static boolean replaceBlockOutline(ClientLevel level, PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockHitResult hitResult) {
        BlockPos pos;
        BlockEntity blockEntity;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        if (AEConfig.instance().isPlacementPreviewEnabled()) {
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            RenderBlockOutlineHook.showPartPlacementPreview((Player)player, poseStack, buffers, camera, hitResult, itemInHand, true);
            RenderBlockOutlineHook.showPartPlacementPreview((Player)player, poseStack, buffers, camera, hitResult, itemInHand, false);
        }
        if ((blockEntity = level.getBlockEntity(pos = hitResult.getBlockPos())) instanceof IPartHost) {
            IPartHost partHost = (IPartHost)blockEntity;
            if (AEConfig.instance().isPlacementPreviewEnabled()) {
                ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                RenderBlockOutlineHook.showFacadePlacementPreview(poseStack, buffers, camera, hitResult, partHost, itemInHand, true);
                RenderBlockOutlineHook.showFacadePlacementPreview(poseStack, buffers, camera, hitResult, partHost, itemInHand, false);
            }
            SelectedPart selectedPart = partHost.selectPartWorld(hitResult.getLocation());
            if (selectedPart.facade != null) {
                RenderBlockOutlineHook.renderFacade(poseStack, buffers, camera, pos, selectedPart.facade, selectedPart.side, false, false);
                return true;
            }
            if (selectedPart.part != null) {
                RenderBlockOutlineHook.renderPart(poseStack, buffers, camera, pos, selectedPart.part, selectedPart.side, false, false);
                return true;
            }
        }
        return false;
    }

    private static boolean showFacadePlacementPreview(PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockHitResult blockHitResult, IPartHost partHost, ItemStack itemInHand, boolean insideBlock) {
        Direction side;
        IFacadeItem facadeItem;
        IFacadePart facade;
        BlockPos pos = blockHitResult.getBlockPos();
        Item item = itemInHand.getItem();
        if (item instanceof IFacadeItem && (facade = (facadeItem = (IFacadeItem)item).createPartFromItemStack(itemInHand, side = blockHitResult.getDirection())) != null && FacadeItem.canPlaceFacade(partHost, facade)) {
            if (partHost.getPart(side) == null) {
                CableAnchorPart cableAnchor = AEParts.CABLE_ANCHOR.get().createPart();
                RenderBlockOutlineHook.renderPart(poseStack, buffers, camera, pos, cableAnchor, side, true, insideBlock);
            }
            RenderBlockOutlineHook.renderFacade(poseStack, buffers, camera, pos, facade, side, true, insideBlock);
            return true;
        }
        return false;
    }

    private static void showPartPlacementPreview(Player player, PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockHitResult blockHitResult, ItemStack itemInHand, boolean insideBlock) {
        Item item = itemInHand.getItem();
        if (item instanceof IPartItem) {
            IPartItem partItem = (IPartItem)item;
            PartPlacement.Placement placement = PartPlacement.getPartPlacement(player, player.level(), itemInHand, blockHitResult.getBlockPos(), blockHitResult.getDirection(), blockHitResult.getLocation());
            if (placement != null) {
                Object part = partItem.createPart();
                RenderBlockOutlineHook.renderPart(poseStack, buffers, camera, placement.pos(), part, placement.side(), true, insideBlock);
            }
        }
    }

    private static void renderPart(PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockPos pos, IPart part, Direction side, boolean preview, boolean insideBlock) {
        ArrayList<AABB> boxes = new ArrayList<AABB>();
        BusCollisionHelper helper = new BusCollisionHelper(boxes, side, true);
        part.getBoxes(helper);
        RenderBlockOutlineHook.renderBoxes(poseStack, buffers, camera, pos, boxes, preview, insideBlock);
    }

    private static void renderFacade(PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockPos pos, IFacadePart facade, Direction side, boolean preview, boolean insideBlock) {
        ArrayList<AABB> boxes = new ArrayList<AABB>();
        BusCollisionHelper helper = new BusCollisionHelper(boxes, side, true);
        facade.getBoxes(helper, false);
        RenderBlockOutlineHook.renderBoxes(poseStack, buffers, camera, pos, boxes, preview, insideBlock);
    }

    private static void renderBoxes(PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockPos pos, List<AABB> boxes, boolean preview, boolean insideBlock) {
        RenderType renderType = insideBlock ? LINES_BEHIND_BLOCK : RenderType.lines();
        VertexConsumer buffer = buffers.getBuffer(renderType);
        float alpha = insideBlock ? 0.2f : (preview ? 0.6f : 0.4f);
        for (AABB box : boxes) {
            VoxelShape shape = Shapes.create((AABB)box);
            LevelRenderer.renderShape((PoseStack)poseStack, (VertexConsumer)buffer, (VoxelShape)shape, (double)((double)pos.getX() - camera.getPosition().x), (double)((double)pos.getY() - camera.getPosition().y), (double)((double)pos.getZ() - camera.getPosition().z), (float)(preview ? 1.0f : 0.0f), (float)(preview ? 1.0f : 0.0f), (float)(preview ? 1.0f : 0.0f), (float)alpha);
        }
    }
}

