/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package appeng.recipes.game;

import appeng.core.AppEng;
import appeng.recipes.game.StorageCellUpgradeRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class StorageCellUpgradeRecipe
extends CustomRecipe {
    public static final ResourceLocation SERIALIZER_ID = AppEng.makeId("storage_cell_upgrade");
    private final Item inputCell;
    private final Item inputComponent;
    private final Item resultCell;
    private final Item resultComponent;
    public static final MapCodec<StorageCellUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)BuiltInRegistries.ITEM.byNameCodec().fieldOf("input_cell").forGetter(StorageCellUpgradeRecipe::getInputCell), (App)BuiltInRegistries.ITEM.byNameCodec().fieldOf("input_component").forGetter(StorageCellUpgradeRecipe::getInputComponent), (App)BuiltInRegistries.ITEM.byNameCodec().fieldOf("result_cell").forGetter(StorageCellUpgradeRecipe::getResultCell), (App)BuiltInRegistries.ITEM.byNameCodec().fieldOf("result_component").forGetter(StorageCellUpgradeRecipe::getResultComponent)).apply((Applicative)builder, StorageCellUpgradeRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellUpgradeRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.ITEM), StorageCellUpgradeRecipe::getInputCell, (StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.ITEM), StorageCellUpgradeRecipe::getInputComponent, (StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.ITEM), StorageCellUpgradeRecipe::getResultCell, (StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.ITEM), StorageCellUpgradeRecipe::getResultComponent, StorageCellUpgradeRecipe::new);

    public StorageCellUpgradeRecipe(Item inputCell, Item inputComponent, Item resultCell, Item resultComponent) {
        super(CraftingBookCategory.MISC);
        this.inputCell = inputCell;
        this.inputComponent = inputComponent;
        this.resultCell = resultCell;
        this.resultComponent = resultComponent;
    }

    public Item getInputCell() {
        return this.inputCell;
    }

    public Item getInputComponent() {
        return this.inputComponent;
    }

    public Item getResultCell() {
        return this.resultCell;
    }

    public Item getResultComponent() {
        return this.resultComponent;
    }

    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of((Object)Ingredient.EMPTY, (Object[])new Ingredient[]{Ingredient.of((ItemLike[])new ItemLike[]{this.inputCell}), Ingredient.of((ItemLike[])new ItemLike[]{this.inputComponent})});
    }

    public boolean matches(CraftingInput container, Level level) {
        int cellsFound = 0;
        int componentsFound = 0;
        for (int i = 0; i < container.size(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(this.inputCell)) {
                cellsFound += stack.getCount();
            } else if (stack.is(this.inputComponent)) {
                ++componentsFound;
            } else {
                return false;
            }
            if (cellsFound <= 1 && componentsFound <= 1) continue;
            return false;
        }
        return cellsFound == 1 && componentsFound == 1;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack((ItemLike)this.resultCell);
    }

    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        ItemStack foundCell = ItemStack.EMPTY;
        int componentsFound = 0;
        for (int i = 0; i < container.size(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(this.inputCell)) {
                if (stack.getCount() > 1 || !foundCell.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                foundCell = stack;
                continue;
            }
            if (stack.is(this.inputComponent)) {
                if (++componentsFound <= 1) continue;
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        if (foundCell.isEmpty() || componentsFound == 0) {
            return ItemStack.EMPTY;
        }
        return foundCell.transmuteCopy((ItemLike)this.resultCell, 1);
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList remainder = NonNullList.withSize((int)input.size(), (Object)ItemStack.EMPTY);
        for (int i = 0; i < remainder.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.is(this.inputCell)) {
                remainder.set(i, (Object)new ItemStack((ItemLike)this.resultComponent));
                continue;
            }
            remainder.set(i, (Object)stack.getCraftingRemainingItem());
        }
        return remainder;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return StorageCellUpgradeRecipeSerializer.INSTANCE;
    }
}

