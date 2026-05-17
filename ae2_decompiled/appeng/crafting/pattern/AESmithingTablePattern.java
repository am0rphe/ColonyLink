/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.SmithingRecipe
 *  net.minecraft.world.item.crafting.SmithingRecipeInput
 *  net.minecraft.world.item.crafting.SmithingTransformRecipe
 *  net.minecraft.world.item.crafting.SmithingTrimRecipe
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
import appeng.crafting.pattern.EncodedSmithingTablePattern;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AESmithingTablePattern
implements IPatternDetails,
IMolecularAssemblerSupportedPattern {
    private static final int TEMPLATE_CRAFTING_GRID_SLOT = 3;
    private static final int BASE_CRAFTING_GRID_SLOT = 4;
    private static final int ADDITION_CRAFTING_GRID_SLOT = 5;
    private final AEItemKey definition;
    public final boolean canSubstitute;
    private final ResourceLocation recipeId;
    private final SmithingRecipe recipe;
    private final ItemStack output;
    private final AEItemKey template;
    private final AEItemKey base;
    private final AEItemKey addition;
    private final IPatternDetails.IInput[] inputs;
    private final List<GenericStack> outputs;

    public AESmithingTablePattern(AEItemKey definition, Level level) {
        Ingredient additionIngredient;
        Ingredient baseIngredient;
        Ingredient templateIngredient;
        this.definition = definition;
        EncodedSmithingTablePattern encodedPattern = definition.get(AEComponents.ENCODED_SMITHING_TABLE_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a smithing table pattern: " + String.valueOf(definition));
        }
        if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }
        this.template = Objects.requireNonNull(AEItemKey.of(encodedPattern.template()), "template");
        this.base = Objects.requireNonNull(AEItemKey.of(encodedPattern.base()), "base");
        this.addition = Objects.requireNonNull(AEItemKey.of(encodedPattern.addition()), "addition");
        this.canSubstitute = encodedPattern.canSubstitute();
        this.recipeId = encodedPattern.recipeId();
        this.recipe = level.getRecipeManager().byKey(this.recipeId).map(holder -> (SmithingRecipe)holder.value()).orElse(null);
        if (this.recipe == null) {
            throw new IllegalStateException("Smithing pattern references unknown recipe " + String.valueOf(this.recipeId));
        }
        SmithingRecipeInput testFrame = new SmithingRecipeInput(this.template.toStack(), this.base.toStack(), this.addition.toStack());
        if (!this.recipe.matches((RecipeInput)testFrame, level)) {
            throw new IllegalStateException("The recipe " + String.valueOf(this.recipeId) + " no longer matches the encoded input.");
        }
        this.output = this.recipe.assemble((RecipeInput)testFrame, (HolderLookup.Provider)level.registryAccess());
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + String.valueOf(this.recipeId) + " produced an empty item stack result.");
        }
        SmithingRecipe smithingRecipe = this.recipe;
        if (smithingRecipe instanceof SmithingTransformRecipe) {
            SmithingTransformRecipe r = (SmithingTransformRecipe)smithingRecipe;
            templateIngredient = r.template;
            baseIngredient = r.base;
            additionIngredient = r.addition;
        } else {
            smithingRecipe = this.recipe;
            if (smithingRecipe instanceof SmithingTrimRecipe) {
                SmithingTrimRecipe r = (SmithingTrimRecipe)smithingRecipe;
                templateIngredient = r.template;
                baseIngredient = r.base;
                additionIngredient = r.addition;
            } else {
                throw new IllegalStateException("Don't know how to process non-vanilla smithing recipe: " + String.valueOf(this.recipe.getClass()));
            }
        }
        this.inputs = new IPatternDetails.IInput[]{new Input(this.template, templateIngredient, 3), new Input(this.base, baseIngredient, 4), new Input(this.addition, additionIngredient, 5)};
        this.outputs = Collections.singletonList(GenericStack.fromItemStack(this.output));
    }

    public ResourceLocation getRecipeId() {
        return this.recipeId;
    }

    public int hashCode() {
        return this.definition.hashCode();
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && ((AESmithingTablePattern)obj).definition.equals(this.definition);
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    public AEItemKey getTemplate() {
        return this.template;
    }

    public AEItemKey getBase() {
        return this.base;
    }

    public AEItemKey getAddition() {
        return this.addition;
    }

    @Override
    public IPatternDetails.IInput[] getInputs() {
        return this.inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return this.outputs;
    }

    public boolean canSubstitute() {
        return this.canSubstitute;
    }

    @Override
    public ItemStack assemble(CraftingInput container, Level level) {
        SmithingRecipeInput testFrame = new SmithingRecipeInput(container.size() >= 1 ? container.getItem(0) : ItemStack.EMPTY, container.size() >= 2 ? container.getItem(1) : ItemStack.EMPTY, container.size() >= 3 ? container.getItem(2) : ItemStack.EMPTY);
        if (this.recipe.matches((RecipeInput)testFrame, level)) {
            return this.recipe.assemble((RecipeInput)testFrame, (HolderLookup.Provider)level.registryAccess());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int gridSlot, AEItemKey key, Level level) {
        SmithingRecipeInput testInput;
        if (key == null) {
            return false;
        }
        if (!this.canSubstitute) {
            if (gridSlot == 3) {
                return this.template.equals(key);
            }
            if (gridSlot == 4) {
                return this.base.equals(key);
            }
            if (gridSlot == 5) {
                return this.addition.equals(key);
            }
            return false;
        }
        switch (gridSlot) {
            case 3: {
                SmithingRecipeInput smithingRecipeInput = new SmithingRecipeInput(key.toStack(), this.base.toStack(), this.addition.toStack());
                break;
            }
            case 4: {
                SmithingRecipeInput smithingRecipeInput = new SmithingRecipeInput(this.template.toStack(), key.toStack(), this.addition.toStack());
                break;
            }
            case 5: {
                SmithingRecipeInput smithingRecipeInput = new SmithingRecipeInput(this.template.toStack(), this.base.toStack(), key.toStack());
                break;
            }
            default: {
                SmithingRecipeInput smithingRecipeInput = testInput = null;
            }
        }
        if (testInput == null) {
            return false;
        }
        return this.recipe.matches((RecipeInput)testInput, level) && ItemStack.matches((ItemStack)this.output, (ItemStack)this.recipe.assemble((RecipeInput)testInput, (HolderLookup.Provider)level.registryAccess()));
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot == 3 || slot == 4 || slot == 5;
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, IMolecularAssemblerSupportedPattern.CraftingGridAccessor gridAccessor) {
        AEItemKey itemKey;
        Object object;
        Object2LongMap.Entry<AEKey> entry = table[0].getFirstEntry();
        if (entry != null && (object = entry.getKey()) instanceof AEItemKey) {
            itemKey = (AEItemKey)object;
            gridAccessor.set(3, itemKey.toStack());
            table[0].remove((AEKey)entry.getKey(), 1L);
        }
        if ((entry = table[1].getFirstEntry()) != null && (object = entry.getKey()) instanceof AEItemKey) {
            itemKey = (AEItemKey)object;
            gridAccessor.set(4, itemKey.toStack());
            table[1].remove((AEKey)entry.getKey(), 1L);
        }
        if ((entry = table[2].getFirstEntry()) != null && (object = entry.getKey()) instanceof AEItemKey) {
            itemKey = (AEItemKey)object;
            gridAccessor.set(5, itemKey.toStack());
            table[2].remove((AEKey)entry.getKey(), 1L);
        }
    }

    public static void encode(ItemStack stack, RecipeHolder<SmithingRecipe> recipe, AEItemKey template, AEItemKey base, AEItemKey addition, AEItemKey output, boolean allowSubstitutes) {
        Preconditions.checkNotNull(recipe, (Object)"recipe");
        Preconditions.checkNotNull(recipe, (Object)"template");
        Preconditions.checkNotNull((Object)base, (Object)"base");
        Preconditions.checkNotNull((Object)addition, (Object)"addition");
        Preconditions.checkNotNull((Object)output, (Object)"output");
        stack.set(AEComponents.ENCODED_SMITHING_TABLE_PATTERN, (Object)new EncodedSmithingTablePattern(template.toStack(), base.toStack(), addition.toStack(), output.toStack(), allowSubstitutes, recipe.id()));
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
        EncodedSmithingTablePattern encodedPattern = (EncodedSmithingTablePattern)stack.get(AEComponents.ENCODED_SMITHING_TABLE_PATTERN);
        if (encodedPattern != null) {
            tooltip.addInput(GenericStack.fromItemStack(encodedPattern.template()));
            tooltip.addInput(GenericStack.fromItemStack(encodedPattern.base()));
            tooltip.addInput(GenericStack.fromItemStack(encodedPattern.addition()));
            tooltip.addOutput(GenericStack.fromItemStack(encodedPattern.resultItem()));
            if (encodedPattern.canSubstitute()) {
                tooltip.addProperty((Component)GuiText.PatternTooltipSubstitutions.text());
            }
            if (flags.isAdvanced()) {
                tooltip.addProperty((Component)Component.literal((String)"Recipe"), (Component)Component.literal((String)encodedPattern.recipeId().toString()));
            }
        }
        return tooltip;
    }

    private class Input
    implements IPatternDetails.IInput {
        private final GenericStack[] possibleInputs;
        private final int gridSlot;

        private Input(AEItemKey what, Ingredient recipeIngredient, int gridSlot) {
            this.gridSlot = gridSlot;
            if (!AESmithingTablePattern.this.canSubstitute) {
                this.possibleInputs = new GenericStack[]{new GenericStack(what, 1L)};
            } else {
                ItemStack[] matchingStacks = recipeIngredient.getItems();
                this.possibleInputs = new GenericStack[matchingStacks.length + 1];
                this.possibleInputs[0] = new GenericStack(what, 1L);
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
            if (AESmithingTablePattern.this.canSubstitute() && input instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)input;
                return AESmithingTablePattern.this.isItemValid(this.gridSlot, itemKey, level);
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

