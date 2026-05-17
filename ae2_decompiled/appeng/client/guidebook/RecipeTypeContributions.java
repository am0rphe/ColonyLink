/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.compiler.tags.RecipeTypeMappingSupplier
 *  guideme.compiler.tags.RecipeTypeMappingSupplier$RecipeTypeMappings
 *  guideme.document.LytRect
 *  guideme.document.block.LytBlock
 *  guideme.document.block.LytSlotGrid
 *  guideme.document.block.recipes.LytStandardRecipeBox
 *  guideme.document.block.recipes.LytStandardRecipeBox$Builder
 *  guideme.layout.LayoutContext
 *  guideme.render.RenderContext
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package appeng.client.guidebook;

import appeng.client.guidebook.LytInscriberRecipe;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.GuiText;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import appeng.util.Platform;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlotGrid;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class RecipeTypeContributions
implements RecipeTypeMappingSupplier {
    public void collect(RecipeTypeMappingSupplier.RecipeTypeMappings mappings) {
        mappings.add(AERecipeTypes.INSCRIBER, RecipeTypeContributions::inscribing);
        mappings.add(AERecipeTypes.CHARGER, RecipeTypeContributions::charging);
        mappings.add(AERecipeTypes.TRANSFORM, RecipeTypeContributions::transform);
    }

    private static LytStandardRecipeBox<ChargerRecipe> charging(RecipeHolder<ChargerRecipe> holder) {
        return LytStandardRecipeBox.builder().icon(AEBlocks.CHARGER).title(AEBlocks.CHARGER.asItem().getDescription().getString()).input(((ChargerRecipe)holder.value()).getIngredient()).outputFromResultOf(holder).build(holder);
    }

    private static LytStandardRecipeBox<InscriberRecipe> inscribing(RecipeHolder<InscriberRecipe> holder) {
        return LytStandardRecipeBox.builder().icon(AEBlocks.INSCRIBER).title(AEBlocks.INSCRIBER.asItem().getDescription().getString()).customBody((LytBlock)new LytInscriberRecipe((InscriberRecipe)holder.value())).build(holder);
    }

    private static LytStandardRecipeBox<TransformRecipe> transform(RecipeHolder<TransformRecipe> holder) {
        TransformRecipe recipe = (TransformRecipe)holder.value();
        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder().input(LytSlotGrid.column(recipe.getIngredients(), (boolean)true)).output(LytSlotGrid.column(List.of(Ingredient.of((ItemStack[])new ItemStack[]{recipe.getResultItem()})), (boolean)true));
        if (recipe.circumstance.isExplosion()) {
            builder.icon((ItemLike)Blocks.TNT);
            builder.title(GuiText.TransformTypeExplode.text().getString());
        } else if (recipe.circumstance.isFluid()) {
            Fluid fluid = Fluids.EMPTY;
            if (recipe.circumstance.isFluidTag((TagKey<Fluid>)FluidTags.WATER)) {
                fluid = Fluids.WATER;
            } else {
                List<Fluid> fluidsForRendering = recipe.circumstance.getFluidsForRendering();
                if (!fluidsForRendering.isEmpty()) {
                    long cycle = System.currentTimeMillis() / 1500L;
                    fluid = fluidsForRendering.get((int)(cycle % (long)fluidsForRendering.size()));
                }
            }
            builder.icon((LytBlock)new FluidIcon(fluid));
            builder.title(GuiText.TransformTypeThrowInFluid.text(Platform.getFluidDisplayName(fluid)).getString());
        }
        return builder.build(holder);
    }

    static class FluidIcon
    extends LytBlock {
        private final Fluid fluid;

        public FluidIcon(Fluid fluid) {
            this.fluid = fluid;
        }

        protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
            return new LytRect(x, y, 8, 8);
        }

        protected void onLayoutMoved(int deltaX, int deltaY) {
        }

        public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        }

        public void render(RenderContext context) {
            context.renderFluid(this.fluid, this.bounds.x(), this.bounds.y(), 0, this.bounds.width(), this.bounds.height());
        }
    }
}

