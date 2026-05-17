/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.SingleRecipeInput
 *  net.minecraft.world.item.crafting.StonecutterRecipe
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.localization.GuiText;
import appeng.crafting.pattern.EncodedStonecuttingPattern;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AEStonecuttingPattern
implements IPatternDetails,
IMolecularAssemblerSupportedPattern {
    private static final int CRAFTING_GRID_SLOT = 4;
    private final AEItemKey definition;
    public final boolean canSubstitute;
    private final ResourceLocation recipeId;
    private final StonecutterRecipe recipe;
    private final AEItemKey input;
    private final ItemStack output;
    private final IPatternDetails.IInput[] inputs;
    private final List<GenericStack> outputs;
    private final Map<Item, Boolean> isValidCache = new IdentityHashMap<Item, Boolean>();

    public AEStonecuttingPattern(AEItemKey definition, Level level) {
        this.definition = definition;
        EncodedStonecuttingPattern encodedPattern = definition.get(AEComponents.ENCODED_STONECUTTING_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a stonecutting pattern: " + String.valueOf(definition));
        }
        if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }
        this.input = Objects.requireNonNull(AEItemKey.of(encodedPattern.input()));
        this.canSubstitute = encodedPattern.canSubstitute();
        this.recipeId = encodedPattern.recipeId();
        this.recipe = level.getRecipeManager().byKey(this.recipeId).map(holder -> (StonecutterRecipe)holder.value()).orElse(null);
        if (this.recipe == null) {
            throw new IllegalStateException("Stonecutting pattern references unknown recipe " + String.valueOf(this.recipeId));
        }
        SingleRecipeInput testInput = new SingleRecipeInput(this.input.toStack());
        if (!this.recipe.matches(testInput, level)) {
            throw new IllegalStateException("The recipe " + String.valueOf(this.recipeId) + " no longer matches the encoded input.");
        }
        this.output = this.recipe.assemble(testInput, (HolderLookup.Provider)level.registryAccess());
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + String.valueOf(this.recipeId) + " produced an empty item stack result.");
        }
        this.inputs = new IPatternDetails.IInput[]{new Input()};
        this.outputs = Collections.singletonList(GenericStack.fromItemStack(this.output));
    }

    public ResourceLocation getRecipeId() {
        return this.recipeId;
    }

    public int hashCode() {
        return this.definition.hashCode();
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && ((AEStonecuttingPattern)obj).definition.equals(this.definition);
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
        return this.outputs;
    }

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        PatternDetailsTooltip tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);
        tooltip.addInputsAndOutputs(this);
        if (flags.isAdvanced()) {
            tooltip.addProperty((Component)Component.literal((String)"Recipe"), (Component)Component.literal((String)this.recipeId.toString()));
        }
        return tooltip;
    }

    public static PatternDetailsTooltip getInvalidTooltip(ItemStack stack, Level level, @Nullable Exception cause, TooltipFlag flags) {
        PatternDetailsTooltip tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);
        EncodedStonecuttingPattern encodedPattern = (EncodedStonecuttingPattern)stack.get(AEComponents.ENCODED_STONECUTTING_PATTERN);
        if (encodedPattern != null) {
            if (encodedPattern.canSubstitute()) {
                tooltip.addProperty((Component)GuiText.PatternTooltipSubstitutions.text());
            }
            if (flags.isAdvanced()) {
                tooltip.addProperty((Component)Component.literal((String)"Recipe"), (Component)Component.literal((String)encodedPattern.recipeId().toString()));
            }
        }
        return tooltip;
    }

    private Ingredient getRecipeIngredient() {
        return (Ingredient)this.recipe.getIngredients().get(0);
    }

    public boolean isItemValid(AEItemKey key, Level level) {
        if (key == null) {
            return false;
        }
        if (!this.canSubstitute) {
            return this.input.equals(key);
        }
        Boolean result = this.getTestResult(key);
        if (result != null) {
            return result;
        }
        SingleRecipeInput testInput = new SingleRecipeInput(key.toStack());
        boolean newResult = this.recipe.matches(testInput, level) && ItemStack.matches((ItemStack)this.output, (ItemStack)this.recipe.assemble(testInput, (HolderLookup.Provider)level.registryAccess()));
        this.setTestResult(key, newResult);
        return newResult;
    }

    @Nullable
    private Boolean getTestResult(AEItemKey what) {
        if (what == null || what.hasComponents()) {
            return null;
        }
        return this.isValidCache.get(what.getItem());
    }

    private void setTestResult(AEItemKey what, boolean result) {
        if (what != null && !what.hasComponents()) {
            this.isValidCache.put(what.getItem(), result);
        }
    }

    public boolean canSubstitute() {
        return this.canSubstitute;
    }

    @Override
    public ItemStack assemble(CraftingInput container, Level level) {
        if (container.size() != 1) {
            return ItemStack.EMPTY;
        }
        SingleRecipeInput testInput = new SingleRecipeInput(container.getItem(0));
        if (this.recipe.matches(testInput, level)) {
            return this.recipe.assemble(testInput, (HolderLookup.Provider)level.registryAccess());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        return slot == 4 && this.isItemValid(key, level);
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot == 4;
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, IMolecularAssemblerSupportedPattern.CraftingGridAccessor gridAccessor) {
        Object object;
        Object2LongMap.Entry<AEKey> entry = table[0].getFirstEntry();
        if (entry != null && (object = entry.getKey()) instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)object;
            gridAccessor.set(4, itemKey.toStack());
            table[0].remove((AEKey)entry.getKey(), 1L);
        }
    }

    public AEItemKey getInput() {
        return this.input;
    }

    public static void encode(ItemStack stack, RecipeHolder<StonecutterRecipe> recipe, AEItemKey input, AEItemKey output, boolean allowSubstitution) {
        Preconditions.checkNotNull(recipe, (Object)"recipe");
        Preconditions.checkNotNull((Object)input, (Object)"input");
        Preconditions.checkNotNull((Object)output, (Object)"output");
        stack.set(AEComponents.ENCODED_STONECUTTING_PATTERN, (Object)new EncodedStonecuttingPattern(input.toStack(), output.toStack(), allowSubstitution, recipe.id()));
    }

    private class Input
    implements IPatternDetails.IInput {
        private final GenericStack[] possibleInputs;

        private Input() {
            if (!AEStonecuttingPattern.this.canSubstitute) {
                this.possibleInputs = new GenericStack[]{new GenericStack(AEStonecuttingPattern.this.input, 1L)};
            } else {
                ItemStack[] matchingStacks = AEStonecuttingPattern.this.getRecipeIngredient().getItems();
                this.possibleInputs = new GenericStack[matchingStacks.length + 1];
                this.possibleInputs[0] = new GenericStack(AEStonecuttingPattern.this.input, 1L);
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
            return 1L;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            if (input.matches(this.possibleInputs[0])) {
                return true;
            }
            if (AEStonecuttingPattern.this.canSubstitute() && input instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)input;
                return AEStonecuttingPattern.this.isItemValid(itemKey, level);
            }
            return false;
        }

        @Override
        @Nullable
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}

