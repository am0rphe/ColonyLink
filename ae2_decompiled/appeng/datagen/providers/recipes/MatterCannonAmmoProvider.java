/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MatterCannonAmmoProvider
extends AE2RecipeProvider {
    public MatterCannonAmmoProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        MatterCannonAmmoProvider.tag(consumer, "nuggets/meatraw", "c:nuggets/meatraw", 32.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/meatcooked", "c:nuggets/meatcooked", 32.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/meat", "c:nuggets/meat", 32.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/chicken", "c:nuggets/chicken", 32.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/beef", "c:nuggets/beef", 32.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/sheep", "c:nuggets/sheep", 32.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/fish", "c:nuggets/fish", 32.0f);
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/nuggets/iron"), ConventionTags.IRON_NUGGET, 55.845f);
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/nuggets/gold"), ConventionTags.GOLD_NUGGET, 196.96655f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/lithium", "c:nuggets/lithium", 6.941f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/beryllium", "c:nuggets/beryllium", 9.0122f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/boron", "c:nuggets/boron", 10.811f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/carbon", "c:nuggets/carbon", 12.0107f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/coal", "c:nuggets/coal", 12.0107f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/charcoal", "c:nuggets/charcoal", 12.0107f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/sodium", "c:nuggets/sodium", 22.9897f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/magnesium", "c:nuggets/magnesium", 24.305f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/aluminum", "c:nuggets/aluminum", 26.9815f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/silicon", "c:nuggets/silicon", 28.0855f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/phosphorus", "c:nuggets/phosphorus", 30.9738f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/sulfur", "c:nuggets/sulfur", 32.065f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/potassium", "c:nuggets/potassium", 39.0983f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/calcium", "c:nuggets/calcium", 40.078f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/scandium", "c:nuggets/scandium", 44.9559f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/titanium", "c:nuggets/titanium", 47.867f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/vanadium", "c:nuggets/vanadium", 50.9415f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/manganese", "c:nuggets/manganese", 54.938f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/nickel", "c:nuggets/nickel", 58.6934f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/cobalt", "c:nuggets/cobalt", 58.9332f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/copper", "c:nuggets/copper", 63.546f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/zinc", "c:nuggets/zinc", 65.39f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/gallium", "c:nuggets/gallium", 69.723f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/germanium", "c:nuggets/germanium", 72.64f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/bromine", "c:nuggets/bromine", 79.904f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/krypton", "c:nuggets/krypton", 83.8f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/rubidium", "c:nuggets/rubidium", 85.4678f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/strontium", "c:nuggets/strontium", 87.62f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/yttrium", "c:nuggets/yttrium", 88.9059f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/zirconium", "c:nuggets/zirconium", 91.224f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/niobium", "c:nuggets/niobium", 92.9064f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/technetium", "c:nuggets/technetium", 98.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/ruthenium", "c:nuggets/ruthenium", 101.07f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/rhodium", "c:nuggets/rhodium", 102.9055f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/palladium", "c:nuggets/palladium", 106.42f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/silver", "c:nuggets/silver", 107.8682f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/cadmium", "c:nuggets/cadmium", 112.411f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/indium", "c:nuggets/indium", 114.818f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/tin", "c:nuggets/tin", 118.71f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/antimony", "c:nuggets/antimony", 121.76f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/iodine", "c:nuggets/iodine", 126.9045f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/tellurium", "c:nuggets/tellurium", 127.6f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/xenon", "c:nuggets/xenon", 131.293f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/cesium", "c:nuggets/cesium", 132.9055f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/barium", "c:nuggets/barium", 137.327f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/lanthanum", "c:nuggets/lanthanum", 138.9055f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/cerium", "c:nuggets/cerium", 140.116f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/tantalum", "c:nuggets/tantalum", 180.9479f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/tungsten", "c:nuggets/tungsten", 183.84f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/osmium", "c:nuggets/osmium", 190.23f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/iridium", "c:nuggets/iridium", 192.217f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/platinum", "c:nuggets/platinum", 195.078f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/lead", "c:nuggets/lead", 207.2f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/bismuth", "c:nuggets/bismuth", 208.9804f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/uranium", "c:nuggets/uranium", 238.0289f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/plutonium", "c:nuggets/plutonium", 244.0f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/invar", "c:nuggets/invar", 56.794468f);
        MatterCannonAmmoProvider.tag(consumer, "nuggets/electrum", "c:nuggets/electrum", 152.41737f);
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/matter_ball"), AEItems.MATTER_BALL, 32.0f);
    }

    private static void tag(RecipeOutput consumer, String recipeId, String tagId, float weight) {
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/" + recipeId), (TagKey<Item>)TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.parse((String)tagId)), weight);
    }
}

