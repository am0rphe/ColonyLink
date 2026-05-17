/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.client.registry.display.DisplayRegistry
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandler
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandler$Context
 *  me.shedaniel.rei.api.client.registry.transfer.TransferHandler$Result
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.rei.transfer;

import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.AEBaseMenu;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTransferHandler<T extends AEBaseMenu>
implements TransferHandler {
    protected static final int CRAFTING_GRID_WIDTH = 3;
    protected static final int CRAFTING_GRID_HEIGHT = 3;
    private static final CategoryIdentifier<?> CRAFTING = CategoryIdentifier.of((String)"minecraft", (String)"plugins/crafting");
    private final Class<T> containerClass;

    AbstractTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    protected abstract TransferHandler.Result transferRecipe(T var1, @Nullable RecipeHolder<?> var2, Display var3, boolean var4);

    public final TransferHandler.Result handle(TransferHandler.Context context) {
        if (!this.containerClass.isInstance(context.getMenu())) {
            return TransferHandler.Result.createNotApplicable();
        }
        Display display = context.getDisplay();
        AEBaseMenu menu = (AEBaseMenu)((Object)this.containerClass.cast(context.getMenu()));
        RecipeHolder<?> holder = this.getRecipeHolder(display);
        return this.transferRecipe(menu, holder, display, context.isActuallyCrafting());
    }

    @Nullable
    private RecipeHolder<?> getRecipeHolder(Display display) {
        RecipeHolder holder;
        Object origin = DisplayRegistry.getInstance().getDisplayOrigin(display);
        return origin instanceof RecipeHolder ? (holder = (RecipeHolder)origin) : null;
    }

    protected final boolean isCraftingRecipe(Recipe<?> recipe, Display display) {
        return EncodingHelper.isSupportedCraftingRecipe(recipe) || display.getCategoryIdentifier().equals(CRAFTING);
    }

    protected final boolean fitsIn3x3Grid(Recipe<?> recipe, Display display) {
        if (recipe != null) {
            return recipe.canCraftInDimensions(3, 3);
        }
        if (display instanceof SimpleGridMenuDisplay) {
            SimpleGridMenuDisplay gridDisplay = (SimpleGridMenuDisplay)display;
            return gridDisplay.getWidth() <= 3 && gridDisplay.getHeight() <= 3;
        }
        return true;
    }
}

