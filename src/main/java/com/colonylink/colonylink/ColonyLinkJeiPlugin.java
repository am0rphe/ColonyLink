package com.colonylink.colonylink;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ColonyLinkJeiPlugin — v1.6.6
 *
 * Minimal, non-intrusive JEI integration: hovering an item in the terminal's
 * Warehouse/ME panels → R shows recipes, U shows uses (like a real slot). Nothing
 * else: no recipe category, no transfer, no ghost, no drag-and-drop.
 *
 * {@code @JeiPlugin} is auto-discovered by JEI; this class loads only when JEI is
 * present (CLIENT {@code optional} + {@code compileOnly} dependency), so the mod
 * boots normally without JEI.
 *
 * The hovered ingredient and its highlight area come from a single hit-test source
 * in {@link WarehouseLinkTerminalScreen} ({@code panelHitAt}), in SCREEN
 * coordinates: {@link WarehouseLinkTerminalScreen#getPanelStackAt} for the stack,
 * {@link WarehouseLinkTerminalScreen#getPanelSlotArea} for the 16×16 cell.
 */
@JeiPlugin
public class ColonyLinkJeiPlugin implements IModPlugin
{
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid()
    {
        return UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration reg)
    {
        reg.addGuiContainerHandler(WarehouseLinkTerminalScreen.class,
                new IGuiContainerHandler<WarehouseLinkTerminalScreen>()
                {
                    @Override
                    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
                            IClickableIngredientFactory factory,
                            WarehouseLinkTerminalScreen screen,
                            double mouseX, double mouseY)
                    {
                        ItemStack stack = screen.getPanelStackAt(mouseX, mouseY);
                        if (stack.isEmpty()) return Optional.empty();

                        Rect2i area = screen.getPanelSlotArea(mouseX, mouseY);
                        if (area == null) return Optional.empty();

                        return factory.createBuilder(stack).buildWithArea(area);
                    }
                });

        // UI pass 1 — the Redirector's upgrade-cell box overflows the panel's
        // right edge (AE2 UpgradesPanel style): declare it as an extra area so the
        // JEI overlay never overlaps it (mirror of AE2's addExclusionZones).
        reg.addGuiContainerHandler(ColonyLinkRedirectorScreen.class,
                new IGuiContainerHandler<ColonyLinkRedirectorScreen>()
                {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(ColonyLinkRedirectorScreen screen)
                    {
                        return screen.getJeiExclusionAreas();
                    }
                });
    }

    /**
     * v1.8.0 — Skill Tomes visibility in JEI.
     *
     * When the server config {@code enable_skill_books} is false, the 11 tomes make
     * no sense, so they are removed from the JEI ingredient list AND their crafting
     * recipes are hidden. The SERVER config is synced to the client by NeoForge, and
     * JEI rebuilds its runtime on world join, so the value read here reflects the
     * connected server. A runtime change via {@code /colonylink config set} needs a
     * JEI reload (F3+T) to re-evaluate.
     *
     * This runs only when JEI is present (whole class is a soft {@code @JeiPlugin}).
     */
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime)
    {
        // Default to shown if the config is not loaded yet (e.g. main menu).
        if (ColonyLinkConfig.safeGet(ColonyLinkConfig.ENABLE_SKILL_BOOKS, true))
        {
            return;
        }

        // 1) Remove the 11 tome items from the ingredient list.
        final List<ItemStack> tomeStacks = new ArrayList<>();
        for (final com.minecolonies.api.entity.citizen.Skill skill
                : com.minecolonies.api.entity.citizen.Skill.values())
        {
            tomeStacks.add(new ItemStack(ColonyLink.SKILL_TOMES.get(skill).get()));
        }
        runtime.getIngredientManager()
                .removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, tomeStacks);

        // 2) Hide their shaped crafting recipes (ids colonylink:tome_of_*), so they
        //    do not surface under the "uses" of paper/leather/potions either.
        final RecipeType<RecipeHolder<CraftingRecipe>> crafting = RecipeTypes.CRAFTING;
        final List<RecipeHolder<CraftingRecipe>> tomeRecipes = runtime.getRecipeManager()
                .createRecipeLookup(crafting)
                .get()
                .filter(h -> h.id().getNamespace().equals(ColonyLink.MODID)
                        && h.id().getPath().startsWith("tome_of_"))
                .toList();
        if (!tomeRecipes.isEmpty())
        {
            runtime.getRecipeManager().hideRecipes(crafting, tomeRecipes);
        }
    }
}
