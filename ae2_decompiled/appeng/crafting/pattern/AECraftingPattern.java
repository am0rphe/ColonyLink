/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.BucketItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.MilkBucketItem
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingInput$Positioned
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.pattern;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.localization.GuiText;
import appeng.crafting.pattern.AEPatternHelper;
import appeng.crafting.pattern.EncodedCraftingPattern;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AECraftingPattern
implements IPatternDetails,
IMolecularAssemblerSupportedPattern {
    public static final int CRAFTING_GRID_DIMENSION = 3;
    public static final int CRAFTING_GRID_SLOTS = 9;
    private final AEItemKey definition;
    public final boolean canSubstitute;
    public final boolean canSubstituteFluids;
    private final RecipeHolder<?> recipeHolder;
    private final CraftingRecipe recipe;
    private final List<GenericStack> sparseInputs;
    private final int[] sparseToCompressed = new int[9];
    private final Input[] inputs;
    private final ItemStack output;
    private final List<GenericStack> outputsArray;
    private final CraftingInput.Positioned positionedPattern;
    private final Map<Item, Boolean>[] isValidCache = new Map[9];

    public AECraftingPattern(AEItemKey definition, Level level) {
        this.definition = definition;
        EncodedCraftingPattern encodedPattern = definition.get(AEComponents.ENCODED_CRAFTING_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a crafting pattern: " + String.valueOf(definition));
        }
        if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }
        this.canSubstitute = encodedPattern.canSubstitute();
        this.canSubstituteFluids = encodedPattern.canSubstituteFluids();
        this.sparseInputs = AECraftingPattern.getCraftingInputs(encodedPattern.inputs());
        this.recipeHolder = level.getRecipeManager().byKey(encodedPattern.recipeId()).orElse(null);
        if (this.recipeHolder == null || !(this.recipeHolder.value() instanceof CraftingRecipe)) {
            throw new IllegalArgumentException("Pattern references unknown recipe " + String.valueOf(encodedPattern.recipeId()));
        }
        this.recipe = (CraftingRecipe)this.recipeHolder.value();
        this.positionedPattern = this.makeCraftingInput();
        if (!this.recipe.matches((RecipeInput)this.positionedPattern.input(), level)) {
            throw new IllegalStateException("The recipe " + String.valueOf(this.recipe) + " no longer matches the encoded input.");
        }
        this.output = this.recipe.assemble((RecipeInput)this.positionedPattern.input(), (HolderLookup.Provider)level.registryAccess());
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + String.valueOf(encodedPattern.recipeId()) + " produced an empty item stack result.");
        }
        this.outputsArray = Collections.singletonList(Objects.requireNonNull(GenericStack.fromItemStack(this.output)));
        List<GenericStack> condensedInputs = AEPatternHelper.condenseStacks(this.sparseInputs);
        this.inputs = new Input[condensedInputs.size()];
        for (int i = 0; i < 9; ++i) {
            this.sparseToCompressed[i] = -1;
        }
        for (int j = 0; j < condensedInputs.size(); ++j) {
            GenericStack condensedInput = condensedInputs.get(j);
            for (int i = 0; i < 9; ++i) {
                if (this.sparseInputs.get(i) == null || !this.sparseInputs.get(i).what().equals(condensedInput.what())) continue;
                if (this.inputs[j] == null) {
                    this.inputs[j] = new Input(i, condensedInput);
                }
                this.sparseToCompressed[i] = j;
            }
        }
    }

    public int hashCode() {
        return this.definition.hashCode();
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && ((AECraftingPattern)obj).definition.equals(this.definition);
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    @Override
    public IPatternDetails.IInput[] getInputs() {
        return this.inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return this.outputsArray;
    }

    private Ingredient getRecipeIngredient(int slot) {
        CraftingRecipe craftingRecipe = this.recipe;
        if (craftingRecipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)craftingRecipe;
            return this.getShapedRecipeIngredient(slot, shapedRecipe.getWidth());
        }
        return this.getShapelessRecipeIngredient(slot);
    }

    private Ingredient getShapedRecipeIngredient(int slot, int recipeWidth) {
        int topOffset = 0;
        if (this.sparseInputs.get(0) == null && this.sparseInputs.get(1) == null && this.sparseInputs.get(2) == null) {
            ++topOffset;
            if (this.sparseInputs.get(3) == null && this.sparseInputs.get(4) == null && this.sparseInputs.get(5) == null) {
                ++topOffset;
            }
        }
        int leftOffset = 0;
        if (this.sparseInputs.get(0) == null && this.sparseInputs.get(3) == null && this.sparseInputs.get(6) == null) {
            ++leftOffset;
            if (this.sparseInputs.get(1) == null && this.sparseInputs.get(4) == null && this.sparseInputs.get(7) == null) {
                ++leftOffset;
            }
        }
        int slotX = slot % 3 - leftOffset;
        int slotY = slot / 3 - topOffset;
        int ingredientIndex = slotY * recipeWidth + slotX;
        NonNullList ingredients = this.recipe.getIngredients();
        if (ingredientIndex < 0 || ingredientIndex > ingredients.size()) {
            return Ingredient.EMPTY;
        }
        return (Ingredient)ingredients.get(ingredientIndex);
    }

    private Ingredient getShapelessRecipeIngredient(int slot) {
        int ingredientIndex = 0;
        for (int i = 0; i < slot; ++i) {
            if (this.sparseInputs.get(i) == null) continue;
            ++ingredientIndex;
        }
        NonNullList ingredients = this.recipe.getIngredients();
        if (ingredientIndex < ingredients.size()) {
            return (Ingredient)ingredients.get(ingredientIndex);
        }
        return Ingredient.EMPTY;
    }

    @Nullable
    public GenericStack getValidFluid(int slot) {
        GenericStack itemOrFluid;
        int compressed = this.sparseToCompressed[slot];
        if (compressed != -1 && (itemOrFluid = this.inputs[compressed].possibleInputs[0]).what() instanceof AEFluidKey) {
            return itemOrFluid;
        }
        return null;
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        if (!this.canSubstitute) {
            return this.sparseInputs.get(slot) == null && key == null || this.sparseInputs.get(slot) != null && this.sparseInputs.get(slot).what().equals(key);
        }
        if (key == null) {
            return this.sparseInputs.get(slot) == null;
        }
        Boolean result = this.getTestResult(slot, key);
        if (result != null) {
            return result;
        }
        CraftingInput.Positioned testCraftingInput = this.makeCraftingInputWithReplacedSlot(slot, key);
        boolean newResult = this.recipe.matches((RecipeInput)testCraftingInput.input(), level) && ItemStack.matches((ItemStack)this.output, (ItemStack)this.recipe.assemble((RecipeInput)testCraftingInput.input(), (HolderLookup.Provider)level.registryAccess()));
        this.setTestResult(slot, key, newResult);
        return newResult;
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return this.sparseInputs.get(slot) != null;
    }

    private ItemStack getRecipeRemainder(int slot, AEItemKey key) {
        CraftingInput.Positioned positioned = this.makeCraftingInputWithReplacedSlot(slot, key);
        NonNullList remainingItems = this.recipe.getRemainingItems((RecipeInput)positioned.input());
        int x = slot % 3 - positioned.left();
        int y = slot / 3 - positioned.top();
        int remainderIdx = y * positioned.input().width() + x;
        if (remainderIdx >= 0 && remainderIdx < remainingItems.size()) {
            return (ItemStack)remainingItems.get(remainderIdx);
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    private Boolean getTestResult(int slot, AEItemKey what) {
        if (what == null || what.hasComponents()) {
            return null;
        }
        Map<Item, Boolean> cache = this.isValidCache[slot];
        if (cache == null) {
            return null;
        }
        return cache.get(what.getItem());
    }

    private void setTestResult(int slot, AEItemKey what, boolean result) {
        if (what != null && !what.hasComponents()) {
            Map<Item, Boolean> cache = this.isValidCache[slot];
            if (cache == null) {
                cache = this.isValidCache[slot] = new IdentityHashMap<Item, Boolean>();
            }
            cache.put(what.getItem(), result);
        }
    }

    public List<GenericStack> getSparseInputs() {
        return this.sparseInputs;
    }

    public List<GenericStack> getSparseOutputs() {
        return this.outputsArray;
    }

    public boolean canSubstitute() {
        return this.canSubstitute;
    }

    public boolean canSubstituteFluids() {
        return this.canSubstituteFluids;
    }

    private int getCompressedIndexFromSparse(int sparse) {
        return this.sparseToCompressed[sparse];
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, IMolecularAssemblerSupportedPattern.CraftingGridAccessor gridAccessor) {
        block0: for (int sparseIndex = 0; sparseIndex < 9; ++sparseIndex) {
            int requiredAmount;
            AEKey validFluidKey;
            long amount;
            int inputId = this.getCompressedIndexFromSparse(sparseIndex);
            if (inputId == -1) continue;
            KeyCounter list = table[inputId];
            GenericStack validFluid = this.getValidFluid(sparseIndex);
            if (validFluid != null && (amount = list.get(validFluidKey = validFluid.what())) >= (long)(requiredAmount = (int)validFluid.amount())) {
                gridAccessor.set(sparseIndex, GenericStack.wrapInItemStack(validFluidKey, requiredAmount));
                list.remove(validFluidKey, requiredAmount);
                continue;
            }
            for (Object2LongMap.Entry<AEKey> entry : list) {
                Object object;
                if (entry.getLongValue() <= 0L || !((object = entry.getKey()) instanceof AEItemKey)) continue;
                AEItemKey itemKey = (AEItemKey)object;
                gridAccessor.set(sparseIndex, itemKey.toStack());
                list.remove(itemKey, 1L);
                continue block0;
            }
        }
    }

    @Override
    public ItemStack assemble(CraftingInput container, Level level) {
        if (this.positionedPattern.input().width() != container.width() || this.positionedPattern.input().height() != container.height()) {
            return ItemStack.EMPTY;
        }
        if (this.canSubstitute && this.recipe.isSpecial()) {
            NonNullList items = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
            for (int x = 0; x < container.size(); ++x) {
                GenericStack validFluid;
                ItemStack item = container.getItem(x);
                GenericStack stack = GenericStack.unwrapItemStack(item);
                if (stack != null && (validFluid = this.getValidFluid(x)) != null && validFluid.equals(stack)) {
                    items.set(x, (Object)((AEItemKey)this.sparseInputs.get(x).what()).toStack());
                    continue;
                }
                items.set(x, (Object)item.copy());
            }
            CraftingInput testInput = CraftingInput.of((int)3, (int)3, (List)items);
            return this.recipe.assemble((RecipeInput)testInput, (HolderLookup.Provider)level.registryAccess());
        }
        for (int i = 0; i < this.sparseInputs.size(); ++i) {
            GenericStack validFluid;
            ItemStack item;
            GenericStack stack;
            int x = i % 3 - this.positionedPattern.left();
            int y = i / 3 - this.positionedPattern.top();
            if (x < 0 || x >= container.width() || y < 0 || y >= container.height() || (stack = GenericStack.unwrapItemStack(item = container.getItem(x, y))) != null && (validFluid = this.getValidFluid(i)) != null && validFluid.equals(stack) || this.isItemValid(i, AEItemKey.of(item), level)) continue;
            return ItemStack.EMPTY;
        }
        return this.output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        if (this.canSubstituteFluids) {
            ArrayList<ItemStack> adjustedItems = new ArrayList<ItemStack>(container.size());
            for (int i = 0; i < container.size(); ++i) {
                adjustedItems.add(container.getItem(i));
            }
            boolean[] slotsToClear = new boolean[container.size()];
            for (int x = 0; x < container.size(); ++x) {
                ItemStack item;
                GenericStack stack;
                GenericStack validFluid = this.getValidFluid(x);
                if (validFluid == null || !validFluid.equals(stack = GenericStack.unwrapItemStack(item = container.getItem(x)))) continue;
                adjustedItems.set(x, ((AEItemKey)this.sparseInputs.get(x).what()).toStack());
                slotsToClear[x] = true;
            }
            CraftingInput adjustedInput = CraftingInput.of((int)container.width(), (int)container.height(), adjustedItems);
            if (adjustedInput.size() != container.size()) {
                throw new IllegalStateException("After fluid substitution, the container size changed: " + adjustedInput.size() + " != " + container.size());
            }
            NonNullList result = this.recipe.getRemainingItems((RecipeInput)adjustedInput);
            for (int i = 0; i < slotsToClear.length; ++i) {
                if (!slotsToClear[i]) continue;
                result.set(i, (Object)ItemStack.EMPTY);
            }
            return result;
        }
        return this.recipe.getRemainingItems((RecipeInput)container);
    }

    private GenericStack getItemOrFluidInput(int slot, GenericStack item) {
        boolean isBucket;
        AEKey aEKey = item.what();
        if (!(aEKey instanceof AEItemKey)) {
            return item;
        }
        AEItemKey itemKey = (AEItemKey)aEKey;
        GenericStack containedFluid = ContainerItemStrategies.getContainedStack(itemKey.toStack(), AEKeyType.fluids());
        boolean bl = isBucket = itemKey.getItem() instanceof BucketItem || itemKey.getItem() instanceof MilkBucketItem;
        if (this.canSubstituteFluids && containedFluid != null && isBucket) {
            ItemStack slotRemainder;
            CraftingInput.Positioned positioned = this.makeCraftingInput();
            NonNullList remainingItems = this.recipe.getRemainingItems((RecipeInput)positioned.input());
            int x = slot % 3 - positioned.left();
            int y = slot / 3 - positioned.top();
            int remainderIdx = y * positioned.input().width() + x;
            if (remainderIdx >= 0 && remainderIdx < remainingItems.size() && (slotRemainder = (ItemStack)remainingItems.get(remainderIdx)).getCount() == 1 && slotRemainder.is(Items.BUCKET)) {
                return new GenericStack(containedFluid.what(), containedFluid.amount());
            }
        }
        return item;
    }

    public static void encode(ItemStack result, RecipeHolder<CraftingRecipe> recipe, ItemStack[] sparseInputs, ItemStack output, boolean allowSubstitutes, boolean allowFluidSubstitutes) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(sparseInputs, "sparseInputs");
        Objects.requireNonNull(output, "output");
        result.set(AEComponents.ENCODED_CRAFTING_PATTERN, (Object)new EncodedCraftingPattern(Stream.of(sparseInputs).map(ItemStack::copy).toList(), output.copy(), recipe.id(), allowSubstitutes, allowFluidSubstitutes));
    }

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        PatternDetailsTooltip tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);
        tooltip.addInputsAndOutputs(this);
        if (this.canSubstitute) {
            tooltip.addProperty((Component)GuiText.PatternTooltipSubstitutions.text());
        }
        if (this.canSubstituteFluids) {
            tooltip.addProperty((Component)GuiText.PatternTooltipFluidSubstitutions.text());
        }
        if (flags.isAdvanced()) {
            tooltip.addProperty((Component)Component.literal((String)"Recipe"), (Component)Component.literal((String)this.recipeHolder.id().toString()));
        }
        return tooltip;
    }

    public static PatternDetailsTooltip getInvalidPatternTooltip(ItemStack stack, Level level, @Nullable Exception cause, TooltipFlag flags) {
        PatternDetailsTooltip tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);
        EncodedCraftingPattern encodedPattern = (EncodedCraftingPattern)stack.get(AEComponents.ENCODED_CRAFTING_PATTERN);
        if (encodedPattern != null) {
            for (ItemStack input : encodedPattern.inputs()) {
                if (input.isEmpty()) continue;
                tooltip.addInput(AEItemKey.of(input), input.getCount());
            }
            tooltip.addOutput(AEItemKey.of(encodedPattern.result()), encodedPattern.result().getCount());
            if (encodedPattern.canSubstitute()) {
                tooltip.addProperty((Component)GuiText.PatternTooltipSubstitutions.text());
            }
            if (encodedPattern.canSubstituteFluids()) {
                tooltip.addProperty((Component)GuiText.PatternTooltipFluidSubstitutions.text());
            }
            if (flags.isAdvanced()) {
                tooltip.addProperty((Component)Component.literal((String)"Recipe"), (Component)Component.literal((String)encodedPattern.recipeId().toString()));
            }
        }
        return tooltip;
    }

    public static List<GenericStack> getCraftingInputs(List<ItemStack> stacks) {
        Preconditions.checkArgument((stacks.size() <= 9 ? 1 : 0) != 0, (Object)"Cannot use more than 9 ingredients");
        GenericStack[] result = new GenericStack[stacks.size()];
        for (int x = 0; x < stacks.size(); ++x) {
            if (stacks.get(x).isEmpty()) continue;
            result[x] = GenericStack.fromItemStack(stacks.get(x));
        }
        return Arrays.asList(result);
    }

    private CraftingInput.Positioned makeCraftingInput() {
        return CraftingInput.ofPositioned((int)3, (int)3, this.makeCraftingInputItems());
    }

    private CraftingInput.Positioned makeCraftingInputWithReplacedSlot(int slot, AEItemKey replacement) {
        List<ItemStack> items = this.makeCraftingInputItems();
        items.set(slot, replacement.toStack());
        return CraftingInput.ofPositioned((int)3, (int)3, items);
    }

    private List<ItemStack> makeCraftingInputItems() {
        ArrayList<ItemStack> testFrameItems = new ArrayList<ItemStack>(this.sparseInputs.size());
        for (int i = 0; i < this.sparseInputs.size(); ++i) {
            if (this.sparseInputs.get(i) != null) {
                AEItemKey itemKey = (AEItemKey)this.sparseInputs.get(i).what();
                testFrameItems.add(i, itemKey.toStack());
                continue;
            }
            testFrameItems.add(ItemStack.EMPTY);
        }
        return testFrameItems;
    }

    private class Input
    implements IPatternDetails.IInput {
        private final int slot;
        private final GenericStack[] possibleInputs;
        private final long multiplier;

        private Input(int slot, GenericStack condensedInput) {
            this.slot = slot;
            this.multiplier = condensedInput.amount();
            GenericStack itemOrFluidInput = AECraftingPattern.this.getItemOrFluidInput(slot, AECraftingPattern.this.sparseInputs.get(slot));
            if (!AECraftingPattern.this.canSubstitute) {
                this.possibleInputs = new GenericStack[]{itemOrFluidInput};
            } else {
                ItemStack[] matchingStacks = AECraftingPattern.this.getRecipeIngredient(slot).getItems();
                this.possibleInputs = new GenericStack[matchingStacks.length + 1];
                this.possibleInputs[0] = itemOrFluidInput;
                for (int i = 0; i < matchingStacks.length; ++i) {
                    this.possibleInputs[i + 1] = GenericStack.fromItemStack(matchingStacks[i]);
                }
            }
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return this.possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return this.multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            if (input.matches(this.possibleInputs[0])) {
                return true;
            }
            if (AECraftingPattern.this.canSubstitute() && input instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)input;
                return AECraftingPattern.this.isItemValid(this.slot, itemKey, level);
            }
            return false;
        }

        @Override
        @Nullable
        public AEKey getRemainingKey(AEKey template) {
            if (template instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)template;
                return AEItemKey.of(AECraftingPattern.this.getRecipeRemainder(this.slot, itemKey));
            }
            return null;
        }
    }
}

