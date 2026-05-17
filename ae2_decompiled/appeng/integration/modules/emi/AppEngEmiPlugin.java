/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.EmiApi
 *  dev.emi.emi.api.EmiDragDropHandler
 *  dev.emi.emi.api.EmiEntrypoint
 *  dev.emi.emi.api.EmiExclusionArea
 *  dev.emi.emi.api.EmiPlugin
 *  dev.emi.emi.api.EmiRegistry
 *  dev.emi.emi.api.EmiStackProvider
 *  dev.emi.emi.api.recipe.EmiCraftingRecipe
 *  dev.emi.emi.api.recipe.EmiInfoRecipe
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 */
package appeng.integration.modules.emi;

import appeng.api.config.CondenserOutput;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.Upgrades;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.core.localization.ItemModText;
import appeng.core.localization.LocalizationEnum;
import appeng.integration.abstraction.ItemListMod;
import appeng.integration.modules.emi.EmiAddItemUpgradeRecipe;
import appeng.integration.modules.emi.EmiAeBaseScreenDragDropHandler;
import appeng.integration.modules.emi.EmiAeBaseScreenExclusionZones;
import appeng.integration.modules.emi.EmiAeBaseScreenStackProvider;
import appeng.integration.modules.emi.EmiChargerRecipe;
import appeng.integration.modules.emi.EmiCondenserRecipe;
import appeng.integration.modules.emi.EmiEncodePatternHandler;
import appeng.integration.modules.emi.EmiEntropyRecipe;
import appeng.integration.modules.emi.EmiFacadeGenerator;
import appeng.integration.modules.emi.EmiFluidStackConverter;
import appeng.integration.modules.emi.EmiInscriberRecipe;
import appeng.integration.modules.emi.EmiItemListModAdapter;
import appeng.integration.modules.emi.EmiItemStackConverter;
import appeng.integration.modules.emi.EmiP2PAttunementRecipe;
import appeng.integration.modules.emi.EmiTransformRecipe;
import appeng.integration.modules.emi.EmiUseCraftingRecipeHandler;
import appeng.integration.modules.itemlists.ItemPredicates;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.game.StorageCellUpgradeRecipe;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

@EmiEntrypoint
public class AppEngEmiPlugin
implements EmiPlugin {
    static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    public void register(EmiRegistry registry) {
        ItemListMod.setAdapter(new EmiItemListModAdapter());
        EmiStackConverters.register(new EmiItemStackConverter());
        EmiStackConverters.register(new EmiFluidStackConverter());
        registry.addGenericExclusionArea((EmiExclusionArea)new EmiAeBaseScreenExclusionZones());
        registry.addGenericStackProvider((EmiStackProvider)new EmiAeBaseScreenStackProvider());
        registry.addGenericDragDropHandler((EmiDragDropHandler)new EmiAeBaseScreenDragDropHandler());
        this.registerWorkstations(registry);
        this.registerDescriptions(registry);
        registry.addRecipeHandler(PatternEncodingTermMenu.TYPE, new EmiEncodePatternHandler<PatternEncodingTermMenu>(PatternEncodingTermMenu.class));
        registry.addRecipeHandler(CraftingTermMenu.TYPE, new EmiUseCraftingRecipeHandler<CraftingTermMenu>(CraftingTermMenu.class));
        registry.addRecipeHandler(WirelessCraftingTermMenu.TYPE, new EmiUseCraftingRecipeHandler<WirelessCraftingTermMenu>(WirelessCraftingTermMenu.class));
        registry.addCategory(EmiInscriberRecipe.CATEGORY);
        registry.addWorkstation(EmiInscriberRecipe.CATEGORY, (EmiIngredient)EmiStack.of(AEBlocks.INSCRIBER));
        AppEngEmiPlugin.adaptRecipeType(registry, AERecipeTypes.INSCRIBER, EmiInscriberRecipe::new);
        registry.addCategory(EmiChargerRecipe.CATEGORY);
        registry.addWorkstation(EmiChargerRecipe.CATEGORY, (EmiIngredient)EmiStack.of(AEBlocks.CHARGER));
        registry.addWorkstation(EmiChargerRecipe.CATEGORY, (EmiIngredient)EmiStack.of(AEBlocks.CRANK));
        AppEngEmiPlugin.adaptRecipeType(registry, AERecipeTypes.CHARGER, EmiChargerRecipe::new);
        AppEngEmiPlugin.adaptSpecialRecipes(registry, StorageCellUpgradeRecipe.class, this::convertStorageCellUpgradeRecipe);
        registry.addCategory(EmiP2PAttunementRecipe.CATEGORY);
        registry.addDeferredRecipes(this::registerP2PAttunements);
        registry.addCategory(EmiCondenserRecipe.CATEGORY);
        registry.addWorkstation(EmiCondenserRecipe.CATEGORY, (EmiIngredient)EmiStack.of(AEBlocks.CONDENSER));
        registry.addRecipe((EmiRecipe)new EmiCondenserRecipe(CondenserOutput.MATTER_BALLS));
        registry.addRecipe((EmiRecipe)new EmiCondenserRecipe(CondenserOutput.SINGULARITY));
        registry.addCategory(EmiEntropyRecipe.CATEGORY);
        registry.addWorkstation(EmiEntropyRecipe.CATEGORY, (EmiIngredient)EmiStack.of(AEItems.ENTROPY_MANIPULATOR));
        AppEngEmiPlugin.adaptRecipeType(registry, AERecipeTypes.ENTROPY, EmiEntropyRecipe::new);
        registry.addCategory(EmiTransformRecipe.CATEGORY);
        AppEngEmiPlugin.adaptRecipeType(registry, AERecipeTypes.TRANSFORM, EmiTransformRecipe::new);
        if (AEConfig.instance().isEnableFacadeRecipesInRecipeViewer()) {
            registry.addDeferredRecipes(this::registerFacades);
        }
        for (Map.Entry<IUpgradeableItem, Set<Item>> entry : Upgrades.getUpgradableItems().entrySet()) {
            for (Item upgrade : entry.getValue()) {
                registry.addRecipe((EmiRecipe)new EmiAddItemUpgradeRecipe(entry.getKey(), upgrade));
            }
        }
        registry.removeEmiStacks(emiStack -> {
            ItemStack stack = emiStack.getItemStack();
            return !stack.isEmpty() && ItemPredicates.shouldBeHidden(stack);
        });
    }

    private EmiRecipe convertStorageCellUpgradeRecipe(RecipeHolder<StorageCellUpgradeRecipe> holder) {
        StorageCellUpgradeRecipe recipe = (StorageCellUpgradeRecipe)holder.value();
        EmiStack cellStack = EmiStack.of((ItemLike)recipe.getInputCell());
        cellStack.setRemainder(EmiStack.of((ItemLike)recipe.getResultComponent()));
        return new EmiCraftingRecipe(this, List.of(cellStack, EmiStack.of((ItemLike)recipe.getInputComponent())), EmiStack.of((ItemLike)recipe.getResultCell()), holder.id(), true){

            public boolean supportsRecipeTree() {
                return false;
            }
        };
    }

    private void registerWorkstations(EmiRegistry registry) {
        ItemStack craftingTerminal = AEParts.CRAFTING_TERMINAL.stack();
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, (EmiIngredient)EmiStack.of((ItemStack)craftingTerminal));
        ItemStack wirelessCraftingTerminal = AEItems.WIRELESS_CRAFTING_TERMINAL.stack();
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, (EmiIngredient)EmiStack.of((ItemStack)wirelessCraftingTerminal));
    }

    private void registerDescriptions(EmiRegistry registry) {
        this.addDescription(registry, AEItems.CERTUS_QUARTZ_CRYSTAL, GuiText.CertusQuartzObtain);
        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            this.addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses);
            this.addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses);
            this.addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses);
            this.addDescription(registry, AEItems.SILICON_PRESS, GuiText.inWorldCraftingPresses);
        }
        this.addDescription(registry, AEBlocks.CRANK.item(), ItemModText.CRANK_DESCRIPTION);
    }

    private void addDescription(EmiRegistry registry, ItemDefinition<?> item, LocalizationEnum ... lines) {
        EmiInfoRecipe info = new EmiInfoRecipe(List.of(EmiStack.of(item)), Arrays.stream(lines).map(LocalizationEnum::text).toList(), null);
        registry.addRecipe((EmiRecipe)info);
    }

    private static <C extends RecipeInput, T extends Recipe<C>> void adaptRecipeType(EmiRegistry registry, RecipeType<T> recipeType, Function<RecipeHolder<T>, ? extends EmiRecipe> adapter) {
        registry.getRecipeManager().getAllRecipesFor(recipeType).stream().map(adapter).forEach(arg_0 -> ((EmiRegistry)registry).addRecipe(arg_0));
    }

    private static <T extends Recipe<?>> void adaptSpecialRecipes(EmiRegistry registry, Class<T> recipeClass, Function<RecipeHolder<T>, ? extends EmiRecipe> adapter) {
        registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream().filter(r -> recipeClass.isInstance(r.value())).map(r -> (EmiRecipe)adapter.apply(new RecipeHolder(r.id(), (Recipe)recipeClass.cast(r.value())))).forEach(arg_0 -> ((EmiRegistry)registry).addRecipe(arg_0));
    }

    private void registerP2PAttunements(Consumer<EmiRecipe> recipeConsumer) {
        List all = EmiApi.getIndexStacks();
        for (P2PTunnelAttunementInternal.Resultant resultant : P2PTunnelAttunementInternal.getApiTunnels()) {
            List<EmiStack> inputs = all.stream().filter(stack -> resultant.stackPredicate().test(stack.getItemStack())).toList();
            if (inputs.isEmpty()) continue;
            recipeConsumer.accept((EmiRecipe)new EmiP2PAttunementRecipe(EmiIngredient.of(inputs), EmiStack.of((ItemLike)resultant.tunnelType()), (Component)ItemModText.P2P_API_ATTUNEMENT.text().append("\n").append(resultant.description())));
        }
        for (Map.Entry entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
            EmiIngredient ingredient = EmiIngredient.of((TagKey)((TagKey)entry.getKey()));
            if (ingredient.isEmpty()) continue;
            recipeConsumer.accept((EmiRecipe)new EmiP2PAttunementRecipe(ingredient, EmiStack.of((ItemLike)((ItemLike)entry.getValue())), (Component)ItemModText.P2P_TAG_ATTUNEMENT.text()));
        }
    }

    private void registerFacades(Consumer<EmiRecipe> recipeConsumer) {
        EmiFacadeGenerator generator = new EmiFacadeGenerator();
        for (ItemStack facade : FacadeCreativeTab.getDisplayItems()) {
            generator.getRecipeFor(facade).ifPresent(recipeConsumer);
        }
    }
}

