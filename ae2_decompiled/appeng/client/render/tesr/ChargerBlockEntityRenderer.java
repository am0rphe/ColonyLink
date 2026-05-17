/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Transformation
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
 *  net.minecraft.world.item.ItemStack
 *  org.apache.commons.lang3.tuple.ImmutablePair
 *  org.apache.commons.lang3.tuple.Pair
 *  org.joml.Vector3f
 */
package appeng.client.render.tesr;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.client.render.renderable.ItemRenderable;
import appeng.client.render.tesr.ModularTESR;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

public final class ChargerBlockEntityRenderer {
    public static BlockEntityRendererProvider<ChargerBlockEntity> FACTORY = context -> new ModularTESR(new ItemRenderable<ChargerBlockEntity>(ChargerBlockEntityRenderer::getRenderedItem));

    private ChargerBlockEntityRenderer() {
    }

    private static Pair<ItemStack, Transformation> getRenderedItem(ChargerBlockEntity blockEntity) {
        double time = (double)System.currentTimeMillis() / 1000.0;
        float yOffset = (float)Math.sin(time) * 0.02f;
        Transformation transform = new Transformation(new Vector3f(0.5f, 0.35f + yOffset, 0.5f), null, null, null);
        return new ImmutablePair((Object)blockEntity.getInternalInventory().getStackInSlot(0), (Object)transform);
    }
}

