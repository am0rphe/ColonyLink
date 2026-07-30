package com.colonylink.colonylink;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * ColonyLinkJeiPlugin — v1.6.6
 *
 * Intégration JEI minimale et non intrusive : survol d'un item dans les panneaux
 * Warehouse/ME du terminal → R affiche les recettes, U les utilisations (comme sur
 * un vrai slot). Rien d'autre : pas de catégorie de recette, pas de transfert, pas
 * de ghost, pas de drag-and-drop.
 *
 * {@code @JeiPlugin} est auto-découvert par JEI ; cette classe n'est chargée que si
 * JEI est présent (dépendance {@code optional} côté CLIENT + {@code compileOnly}),
 * donc le mod boote normalement sans JEI.
 *
 * L'ingrédient survolé et sa zone de surlignage viennent d'une seule source de
 * hit-test dans {@link WarehouseLinkTerminalScreen} ({@code panelHitAt}), en
 * coordonnées ÉCRAN : {@link WarehouseLinkTerminalScreen#getPanelStackAt} pour le
 * stack, {@link WarehouseLinkTerminalScreen#getPanelSlotArea} pour la case 16×16.
 */
@JeiPlugin
public class ColonyLinkJeiPlugin implements IModPlugin
{
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "jei"); // colonylink:jei

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
    }
}
