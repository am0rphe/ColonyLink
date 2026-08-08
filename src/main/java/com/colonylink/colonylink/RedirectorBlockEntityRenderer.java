package com.colonylink.colonylink;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders the Redirector's current craft item inside the glass cage, sliding
 * along the saw blade plane (local Z=8) on a short loop at a perfectly fixed
 * height — as if being fed through the spinning blade.
 *
 * <p>Animation clock: driven by the client's {@code gameTime + partialTick}, NOT by
 * {@link ColonyLinkRedirectorBlockEntity#getShuffleProgress()}. The server only
 * syncs the block entity to clients when the render stack is (re)picked — roughly
 * every 5 ticks — so {@code shuffleProgress} on the client is effectively pinned
 * near 0 and cannot drive a smooth slide. A client-side game-time loop is smooth
 * (interpolated by {@code partialTick}), pauses with the game, and needs no extra
 * per-tick network sync. {@code getRenderStack()} still supplies WHICH item shows;
 * a new pick simply swaps the sprite mid-conveyor.
 *
 * <p>Server safety: {@code @OnlyIn(Dist.CLIENT)} — this class is only referenced
 * from {@link ColonyLinkClient} (also client-only) and registered client-side.
 */
@OnlyIn(Dist.CLIENT)
public class RedirectorBlockEntityRenderer implements BlockEntityRenderer<ColonyLinkRedirectorBlockEntity>
{
    /** Item ride height in blocks — CONSTANT (bobbing removed in passe 4): the deck
     *  (cage floor) sits at Y=11px, the cage interior spans Y=11..15.5px; 12px keeps
     *  the item skimming the deck through the blade. */
    private static final double BASE_Y = 12.0 / 16.0;
    /** Horizontal slide half-range in pixels (item travels -3px .. +3px). */
    private static final float SLIDE_HALF_PX = 3.0f;
    /** Slide loop length in ticks (matches the server shuffle period). */
    private static final float SLIDE_TICKS = 5.0f;
    /** Uniform scale applied to the displayed item (1.0 = GROUND default). Tune to taste. */
    private static final float ITEM_SCALE = 0.75f;

    private final ItemRenderer itemRenderer;

    public RedirectorBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ColonyLinkRedirectorBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay)
    {
        ItemStack stack = blockEntity.getRenderStack();
        if (stack.isEmpty()) return;

        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        // Client animation clock: smooth, pauses with the game, no extra sync needed.
        float time = (level != null ? (float) (level.getGameTime() % 100000L) : 0.0f) + partialTick;

        // Horizontal slide: 0..1 looping every SLIDE_TICKS, mapped to -3px .. +3px.
        float slide = (time % SLIDE_TICKS) / SLIDE_TICKS;
        float xOffsetPx = -SLIDE_HALF_PX + slide * (SLIDE_HALF_PX * 2.0f);

        Direction facing = blockEntity.getBlockState().getValue(ColonyLinkRedirectorBlock.FACING);

        poseStack.pushPose();
        // Rotate around the block centre so the item slides along the model's local
        // X axis — i.e. inside the saw blade plane (local Z=8) — for every FACING.
        // 180 - toYRot() maps NORTH->0, EAST->-90, SOUTH->180, WEST->90, matching
        // the blockstate y-rotations (north=0, east=90, south=180, west=270).
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - facing.toYRot()));
        // Local offsets after rotation; pixel offsets converted to block units (/16).
        // Y is constant — only the X slide animates.
        poseStack.translate(xOffsetPx / 16.0, BASE_Y, 0.0);
        // Uniform shrink around the display point set by the translate above.
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        this.itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                level,
                pos.hashCode());
        poseStack.popPose();
    }

    @Override
    public int getViewDistance()
    {
        return 64;
    }
}
