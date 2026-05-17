/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package appeng.recipes.game;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.game.StorageCellDisassemblyRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class StorageCellDisassemblyRecipe
extends CustomRecipe {
    public static final MapCodec<StorageCellDisassemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)BuiltInRegistries.ITEM.byNameCodec().fieldOf("cell").forGetter(StorageCellDisassemblyRecipe::getStorageCell), (App)ItemStack.CODEC.listOf().fieldOf("cell_disassembly_items").forGetter(StorageCellDisassemblyRecipe::getCellDisassemblyItems)).apply((Applicative)builder, StorageCellDisassemblyRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)BuiltInRegistries.ITEM.key()), StorageCellDisassemblyRecipe::getStorageCell, (StreamCodec)ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), StorageCellDisassemblyRecipe::getCellDisassemblyItems, StorageCellDisassemblyRecipe::new);
    private final List<ItemStack> disassemblyItems;
    private final Item storageCell;

    public StorageCellDisassemblyRecipe(Item storageCell, List<ItemStack> disassemblyItems) {
        super(CraftingBookCategory.MISC);
        this.storageCell = storageCell;
        this.disassemblyItems = disassemblyItems;
    }

    public Item getStorageCell() {
        return this.storageCell;
    }

    public List<ItemStack> getCellDisassemblyItems() {
        return this.disassemblyItems.stream().map(ItemStack::copy).toList();
    }

    public boolean canDisassemble() {
        return !this.disassemblyItems.isEmpty();
    }

    public static List<ItemStack> getDisassemblyResult(Level level, Item cell) {
        RecipeManager recipeManager = level.getRecipeManager();
        for (RecipeHolder holder : recipeManager.byType(AERecipeTypes.CELL_DISASSEMBLY)) {
            if (((StorageCellDisassemblyRecipe)holder.value()).storageCell != cell) continue;
            return ((StorageCellDisassemblyRecipe)holder.value()).getCellDisassemblyItems();
        }
        return List.of();
    }

    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public RecipeSerializer<?> getSerializer() {
        return StorageCellDisassemblyRecipeSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return AERecipeTypes.CELL_DISASSEMBLY;
    }
}

