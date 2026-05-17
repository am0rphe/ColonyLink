/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.Guide
 *  guideme.internal.siteexport.SiteExporter
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  org.jetbrains.annotations.Nullable
 */
package appeng.siteexport;

import appeng.client.guidebook.ConfigValueTagExtension;
import appeng.core.definitions.AEBlocks;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;
import guideme.Guide;
import guideme.internal.siteexport.SiteExporter;
import java.lang.runtime.SwitchBootstraps;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class AESiteExporter
extends SiteExporter {
    public AESiteExporter(Minecraft client, Path outputFolder, Guide guide) {
        super(client, outputFolder, guide);
        this.referenceItem((ItemLike)Items.FURNACE);
        this.referenceItem(AEBlocks.INSCRIBER);
        this.referenceFluid((Fluid)Fluids.WATER);
        this.referenceFluid((Fluid)Fluids.LAVA);
        this.referenceItem((ItemLike)Items.TNT);
        this.referenceItem((ItemLike)Blocks.SMITHING_TABLE);
    }

    protected Map<String, Object> getModData() {
        return Map.of("defaultConfigValues", ConfigValueTagExtension.CONFIG_VALUES.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (String)((Supplier)entry.getValue()).get())));
    }

    @Nullable
    protected Map<String, Object> getCustomRecipeFields(ResourceLocation id, Recipe<?> recipe) {
        Recipe<?> recipe2 = recipe;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{InscriberRecipe.class, TransformRecipe.class, EntropyRecipe.class, MatterCannonAmmo.class, ChargerRecipe.class}, recipe2, n)) {
            case 0 -> {
                InscriberRecipe inscriberRecipe = (InscriberRecipe)recipe2;
                yield this.addRecipe(inscriberRecipe);
            }
            case 1 -> {
                TransformRecipe transformRecipe = (TransformRecipe)recipe2;
                yield this.addRecipe(transformRecipe);
            }
            case 2 -> {
                EntropyRecipe entropyRecipe = (EntropyRecipe)recipe2;
                yield this.addRecipe(entropyRecipe);
            }
            case 3 -> {
                MatterCannonAmmo ammoRecipe = (MatterCannonAmmo)recipe2;
                yield this.addRecipe(ammoRecipe);
            }
            case 4 -> {
                ChargerRecipe chargerRecipe = (ChargerRecipe)recipe2;
                yield this.addRecipe(chargerRecipe);
            }
            default -> null;
        };
    }

    private Map<String, Object> addRecipe(InscriberRecipe recipe) {
        ItemStack resultItem = recipe.getResultItem();
        return Map.of("top", recipe.getTopOptional(), "middle", recipe.getMiddleInput(), "bottom", recipe.getBottomOptional(), "resultItem", resultItem.getItem(), "resultCount", resultItem.getCount(), "consumesTopAndBottom", recipe.getProcessType() == InscriberProcessType.PRESS);
    }

    private Map<String, Object> addRecipe(TransformRecipe recipe) {
        HashMap<String, Object> circumstanceJson = new HashMap<String, Object>();
        TransformCircumstance circumstance = recipe.circumstance;
        if (circumstance.isExplosion()) {
            circumstanceJson.put("type", "explosion");
        } else if (circumstance.isFluid()) {
            circumstanceJson.put("type", "fluid");
            if (recipe.circumstance.isFluidTag((TagKey<Fluid>)FluidTags.WATER)) {
                circumstanceJson.put("fluids", List.of(Fluids.WATER));
            } else {
                circumstanceJson.put("fluids", circumstance.getFluidsForRendering());
            }
        } else {
            throw new IllegalStateException("Unknown circumstance: " + String.valueOf(circumstance.toJson()));
        }
        return Map.of("resultItem", recipe.getResultItem(null), "ingredients", recipe.getIngredients(), "circumstance", circumstanceJson);
    }

    private Map<String, Object> addRecipe(EntropyRecipe recipe) {
        return Map.of("mode", recipe.getMode().name().toLowerCase(Locale.ROOT));
    }

    private Map<String, Object> addRecipe(MatterCannonAmmo recipe) {
        return Map.of("ammo", recipe.getAmmo(), "damage", MatterCannonItem.getDamageFromPenetration(recipe.getWeight()));
    }

    private Map<String, Object> addRecipe(ChargerRecipe recipe) {
        return Map.of("resultItem", recipe.getResultItem(), "ingredient", recipe.getIngredient());
    }
}

