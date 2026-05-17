/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.architectury.event.CompoundEventResult
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor
 *  me.shedaniel.rei.api.client.plugins.REIClientPlugin
 *  me.shedaniel.rei.api.client.registry.category.ButtonArea
 *  me.shedaniel.rei.api.client.registry.category.CategoryRegistry
 *  me.shedaniel.rei.api.client.registry.display.DisplayCategory
 *  me.shedaniel.rei.api.client.registry.display.DisplayRegistry
 *  me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
 *  me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry
 *  me.shedaniel.rei.api.client.registry.entry.EntryRegistry
 *  me.shedaniel.rei.api.client.registry.screen.ExclusionZones
 *  me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
 *  me.shedaniel.rei.api.common.util.EntryIngredients
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  me.shedaniel.rei.forge.REIPluginClient
 *  me.shedaniel.rei.plugin.common.BuiltinPlugin
 *  me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay
 *  me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 */
package appeng.integration.modules.rei;

import appeng.api.config.Actionable;
import appeng.api.config.CondenserOutput;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.integrations.rei.IngredientConverters;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.core.localization.ItemModText;
import appeng.integration.abstraction.ItemListMod;
import appeng.integration.modules.itemlists.CompatLayerHelper;
import appeng.integration.modules.itemlists.ItemPredicates;
import appeng.integration.modules.rei.AttunementCategory;
import appeng.integration.modules.rei.AttunementDisplay;
import appeng.integration.modules.rei.ChargerCategory;
import appeng.integration.modules.rei.ChargerDisplay;
import appeng.integration.modules.rei.CondenserCategory;
import appeng.integration.modules.rei.CondenserOutputDisplay;
import appeng.integration.modules.rei.EntropyRecipeCategory;
import appeng.integration.modules.rei.EntropyRecipeDisplay;
import appeng.integration.modules.rei.FacadeRegistryGenerator;
import appeng.integration.modules.rei.FluidIngredientConverter;
import appeng.integration.modules.rei.GhostIngredientHandler;
import appeng.integration.modules.rei.InscriberRecipeCategory;
import appeng.integration.modules.rei.InscriberRecipeDisplay;
import appeng.integration.modules.rei.ItemIngredientConverter;
import appeng.integration.modules.rei.ReiItemListModAdapter;
import appeng.integration.modules.rei.TransformCategory;
import appeng.integration.modules.rei.TransformRecipeWrapper;
import appeng.integration.modules.rei.transfer.EncodePatternTransferHandler;
import appeng.integration.modules.rei.transfer.UseCraftingRecipeTransfer;
import appeng.items.parts.FacadeItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.game.StorageCellUpgradeRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import dev.architectury.event.CompoundEventResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

@REIPluginClient
public class ReiPlugin
implements REIClientPlugin {
    static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    public ReiPlugin() {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        IngredientConverters.register(new ItemIngredientConverter());
        IngredientConverters.register(new FluidIngredientConverter());
        ItemListMod.setAdapter(new ReiItemListModAdapter());
    }

    public String getPluginProviderName() {
        return "AE2";
    }

    public void registerCategories(CategoryRegistry registry) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        registry.add((DisplayCategory)new TransformCategory());
        registry.add((DisplayCategory)new CondenserCategory());
        registry.add((DisplayCategory)new InscriberRecipeCategory());
        registry.add((DisplayCategory)new AttunementCategory());
        registry.add((DisplayCategory)new ChargerCategory());
        registry.add((DisplayCategory)new EntropyRecipeCategory());
        this.registerWorkingStations(registry);
    }

    public void registerDisplays(DisplayRegistry registry) {
        if (AEConfig.instance().isEnableFacadeRecipesInRecipeViewer()) {
            registry.registerGlobalDisplayGenerator((DynamicDisplayGenerator)new FacadeRegistryGenerator());
        }
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        registry.registerRecipeFiller(InscriberRecipe.class, AERecipeTypes.INSCRIBER, InscriberRecipeDisplay::new);
        registry.registerRecipeFiller(ChargerRecipe.class, AERecipeTypes.CHARGER, ChargerDisplay::new);
        registry.registerRecipeFiller(TransformRecipe.class, AERecipeTypes.TRANSFORM, TransformRecipeWrapper::new);
        registry.registerRecipeFiller(EntropyRecipe.class, AERecipeTypes.ENTROPY, EntropyRecipeDisplay::new);
        registry.registerRecipeFiller(StorageCellUpgradeRecipe.class, RecipeType.CRAFTING, this::convertStorageCellUpgradeRecipe);
        registry.add((Display)new CondenserOutputDisplay(CondenserOutput.MATTER_BALLS));
        registry.add((Display)new CondenserOutputDisplay(CondenserOutput.SINGULARITY));
        this.registerDescriptions(registry);
    }

    private Display convertStorageCellUpgradeRecipe(RecipeHolder<StorageCellUpgradeRecipe> holder) {
        StorageCellUpgradeRecipe recipe = (StorageCellUpgradeRecipe)holder.value();
        return new DefaultCustomShapelessDisplay(holder, List.of(EntryIngredients.of((ItemLike)recipe.getInputCell()), EntryIngredients.of((ItemLike)recipe.getInputComponent())), List.of(EntryIngredients.of((ItemLike)recipe.getResultCell()), EntryIngredients.of((ItemLike)recipe.getResultComponent())));
    }

    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        registry.register(new EncodePatternTransferHandler<PatternEncodingTermMenu>(PatternEncodingTermMenu.class));
        registry.register(new UseCraftingRecipeTransfer<CraftingTermMenu>(CraftingTermMenu.class));
    }

    public void registerScreens(ScreenRegistry registry) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        registry.registerDraggableStackVisitor((DraggableStackVisitor)new GhostIngredientHandler());
        registry.registerFocusedStack((screen, mouse) -> {
            AEBaseScreen aeScreen;
            StackWithBounds stack;
            if (screen instanceof AEBaseScreen && (stack = (aeScreen = (AEBaseScreen)screen).getStackUnderMouse(mouse.x, mouse.y)) != null) {
                for (IngredientConverter<?> converter : IngredientConverters.getConverters()) {
                    EntryStack<?> entryStack = converter.getIngredientFromStack(stack.stack());
                    if (entryStack == null) continue;
                    return CompoundEventResult.interruptTrue(entryStack);
                }
            }
            return CompoundEventResult.pass();
        });
        registry.registerContainerClickArea(new Rectangle(82, 39, 26, 16), InscriberScreen.class, new CategoryIdentifier[]{InscriberRecipeCategory.ID});
    }

    public void registerEntries(EntryRegistry registry) {
        registry.removeEntryIf(this::shouldEntryBeHidden);
        if (AEConfig.instance().isEnableFacadesInRecipeViewer()) {
            registry.addEntries((Collection)EntryIngredients.ofItemStacks(FacadeCreativeTab.getDisplayItems()));
        }
    }

    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        if (AEConfig.instance().isEnableFacadesInRecipeViewer()) {
            FacadeItem facadeItem = AEItems.FACADE.get();
            registry.group(AppEng.makeId("facades"), (Component)GuiText.CreativeTabFacades.text(), stack -> stack.getType() == VanillaEntryTypes.ITEM && ((ItemStack)stack.castValue()).is((Item)facadeItem));
        }
    }

    public void registerExclusionZones(ExclusionZones zones) {
        if (CompatLayerHelper.IS_LOADED) {
            return;
        }
        zones.register(AEBaseScreen.class, screen -> screen != null ? ReiPlugin.mapRects(screen.getExclusionZones()) : Collections.emptyList());
    }

    private static List<Rectangle> mapRects(List<Rect2i> exclusionZones) {
        return exclusionZones.stream().map(ez -> new Rectangle(ez.getX(), ez.getY(), ez.getWidth(), ez.getHeight())).collect(Collectors.toList());
    }

    private void registerWorkingStations(CategoryRegistry registry) {
        ItemStack condenser = AEBlocks.CONDENSER.stack();
        registry.addWorkstations(CondenserCategory.ID, new EntryStack[]{EntryStacks.of((ItemStack)condenser)});
        ItemStack inscriber = AEBlocks.INSCRIBER.stack();
        registry.addWorkstations(InscriberRecipeCategory.ID, new EntryStack[]{EntryStacks.of((ItemStack)inscriber)});
        registry.setPlusButtonArea(InscriberRecipeCategory.ID, ButtonArea.defaultArea());
        ItemStack craftingTerminal = AEParts.CRAFTING_TERMINAL.stack();
        registry.addWorkstations(BuiltinPlugin.CRAFTING, new EntryStack[]{EntryStacks.of((ItemStack)craftingTerminal)});
        ItemStack wirelessCraftingTerminal = ReiPlugin.chargeFully(AEItems.WIRELESS_CRAFTING_TERMINAL.stack());
        registry.addWorkstations(BuiltinPlugin.CRAFTING, new EntryStack[]{EntryStacks.of((ItemStack)wirelessCraftingTerminal)});
        registry.addWorkstations(ChargerDisplay.ID, new EntryStack[]{EntryStacks.of((ItemStack)AEBlocks.CHARGER.stack())});
        registry.addWorkstations(ChargerDisplay.ID, new EntryStack[]{EntryStacks.of((ItemStack)AEBlocks.CRANK.stack())});
        ItemStack entropyManipulator = ReiPlugin.chargeFully(ReiPlugin.chargeFully(AEItems.ENTROPY_MANIPULATOR.stack()));
        registry.addWorkstations(EntropyRecipeCategory.ID, new EntryStack[]{EntryStacks.of((ItemStack)entropyManipulator)});
    }

    private static ItemStack chargeFully(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof AEBasePoweredItem) {
            AEBasePoweredItem poweredItem = (AEBasePoweredItem)item;
            poweredItem.injectAEPower(stack, poweredItem.getAEMaxPower(stack), Actionable.MODULATE);
        }
        return stack;
    }

    private void registerDescriptions(DisplayRegistry registry) {
        EntryIngredient all = (EntryIngredient)EntryRegistry.getInstance().getEntryStacks().collect(EntryIngredient.collector());
        for (P2PTunnelAttunementInternal.Resultant resultant : P2PTunnelAttunementInternal.getApiTunnels()) {
            List<EntryIngredient> inputs = List.of(all.filter(stack -> {
                Object patt0$temp = stack.getValue();
                if (!(patt0$temp instanceof ItemStack)) return false;
                ItemStack s = (ItemStack)patt0$temp;
                if (!resultant.stackPredicate().test(s)) return false;
                return true;
            }));
            if (inputs.isEmpty()) continue;
            registry.add((Display)new AttunementDisplay(inputs, List.of(EntryIngredient.of((EntryStack)EntryStacks.of((ItemLike)resultant.tunnelType()))), new Component[]{ItemModText.P2P_API_ATTUNEMENT.text(), resultant.description()}));
        }
        for (Map.Entry entry : P2PTunnelAttunementInternal.getTagTunnels().entrySet()) {
            Ingredient ingredient = Ingredient.of((TagKey)((TagKey)entry.getKey()));
            if (ingredient.isEmpty()) continue;
            registry.add((Display)new AttunementDisplay(List.of(EntryIngredients.ofIngredient((Ingredient)ingredient)), List.of(EntryIngredient.of((EntryStack)EntryStacks.of((ItemLike)((ItemLike)entry.getValue())))), new Component[]{ItemModText.P2P_TAG_ATTUNEMENT.text()}));
        }
        ReiPlugin.addDescription(registry, AEItems.CERTUS_QUARTZ_CRYSTAL, GuiText.CertusQuartzObtain.getTranslationKey());
        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            ReiPlugin.addDescription(registry, AEItems.LOGIC_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
            ReiPlugin.addDescription(registry, AEItems.CALCULATION_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
            ReiPlugin.addDescription(registry, AEItems.ENGINEERING_PROCESSOR_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
            ReiPlugin.addDescription(registry, AEItems.SILICON_PRESS, GuiText.inWorldCraftingPresses.getTranslationKey());
        }
        ReiPlugin.addDescription(registry, AEBlocks.CRANK.item(), ItemModText.CRANK_DESCRIPTION.getTranslationKey());
    }

    private static void addDescription(DisplayRegistry registry, ItemDefinition<?> itemDefinition, String ... message) {
        DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntry((EntryStack)EntryStacks.of(itemDefinition), (Component)itemDefinition.get().getDescription());
        info.lines((Collection)Arrays.stream(message).map(Component::translatable).collect(Collectors.toList()));
        registry.add((Display)info);
    }

    private boolean shouldEntryBeHidden(EntryStack<?> entryStack) {
        if (entryStack.getType() != VanillaEntryTypes.ITEM) {
            return false;
        }
        return ItemPredicates.shouldBeHidden((ItemStack)entryStack.castValue());
    }
}

