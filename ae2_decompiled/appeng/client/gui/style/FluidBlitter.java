/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
 *  net.neoforged.neoforge.fluids.FluidStack
 */
package appeng.client.gui.style;

import appeng.api.stacks.AEFluidKey;
import appeng.client.gui.style.Blitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidBlitter {
    private FluidBlitter() {
    }

    public static Blitter create(AEFluidKey fluidKey) {
        return FluidBlitter.create(fluidKey.toStack(1));
    }

    public static Blitter create(FluidStack stack) {
        if (stack.isEmpty() && stack.getFluid() != Fluids.EMPTY) {
            stack = new FluidStack(stack.getFluidHolder(), 1, stack.getComponentsPatch());
        }
        Fluid fluid = stack.getFluid();
        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of((Fluid)fluid);
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture(stack));
        return Blitter.sprite(sprite).colorRgb(attributes.getTintColor(stack)).blending(false);
    }
}

