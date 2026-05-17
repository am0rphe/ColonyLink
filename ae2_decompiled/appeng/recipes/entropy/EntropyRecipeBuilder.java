/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.recipes.entropy;

import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.entropy.PropertyValueMatcher;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class EntropyRecipeBuilder {
    private EntropyMode mode;
    private Block inputBlock;
    private final Map<String, PropertyValueMatcher> inputBlockMatchers = new HashMap<String, PropertyValueMatcher>();
    private Fluid inputFluid;
    private final Map<String, PropertyValueMatcher> inputFluidMatchers = new HashMap<String, PropertyValueMatcher>();
    private Block outputBlock;
    private final Map<String, String> outputBlockStateAppliers = new HashMap<String, String>();
    private boolean outputBlockKeep;
    private Fluid outputFluid;
    private final Map<String, String> outputFluidStateAppliers = new HashMap<String, String>();
    private boolean outputFluidKeep;
    private List<ItemStack> drops = Collections.emptyList();

    public static EntropyRecipeBuilder cool() {
        return new EntropyRecipeBuilder().setMode(EntropyMode.COOL);
    }

    public static EntropyRecipeBuilder heat() {
        return new EntropyRecipeBuilder().setMode(EntropyMode.HEAT);
    }

    public EntropyRecipeBuilder setMode(EntropyMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        return this;
    }

    public EntropyRecipeBuilder setInputBlock(Block inputBlock) {
        this.inputBlock = Objects.requireNonNull(inputBlock, "inputBlock must not be null");
        return this;
    }

    public EntropyRecipeBuilder setInputFluid(Fluid inputFluid) {
        this.inputFluid = Objects.requireNonNull(inputFluid, "inputFluid must not be null");
        return this;
    }

    public EntropyRecipeBuilder setOutputBlock(Block outputBlock) {
        this.outputBlock = Objects.requireNonNull(outputBlock, "outputBlock must not be null");
        return this;
    }

    public EntropyRecipeBuilder setOutputBlockKeep(boolean outputBlockKeep) {
        this.outputBlockKeep = outputBlockKeep;
        return this;
    }

    public EntropyRecipeBuilder setOutputFluid(Fluid outputFluid) {
        this.outputFluid = Objects.requireNonNull(outputFluid, "outputFluid must not be null");
        return this;
    }

    public EntropyRecipeBuilder setOutputFluidKeep(boolean outputFluidKeep) {
        this.outputFluidKeep = outputFluidKeep;
        return this;
    }

    public EntropyRecipeBuilder setDrops(List<ItemStack> drops) {
        Preconditions.checkArgument((!drops.isEmpty() ? 1 : 0) != 0, (Object)"drops needs to be a non empty list when set");
        this.drops = drops;
        return this;
    }

    public EntropyRecipeBuilder setDrops(ItemStack ... drops) {
        return this.setDrops(Arrays.asList(drops));
    }

    public EntropyRecipeBuilder addBlockStateMatcher(String property, PropertyValueMatcher valueMatcher) {
        Preconditions.checkState((this.inputBlock != null ? 1 : 0) != 0, (Object)"Can only add appliers when an input block is present.");
        this.inputBlockMatchers.put(property, valueMatcher);
        return this;
    }

    public EntropyRecipeBuilder setBlockStateMatchers(Map<String, PropertyValueMatcher> properties) {
        Preconditions.checkState((this.inputBlock != null ? 1 : 0) != 0, (Object)"Can only add appliers when an input block is present.");
        this.inputBlockMatchers.clear();
        this.inputBlockMatchers.putAll(properties);
        return this;
    }

    public EntropyRecipeBuilder addFluidStateMatcher(String property, PropertyValueMatcher valueMatcher) {
        Preconditions.checkState((this.inputFluid != null ? 1 : 0) != 0, (Object)"Can only add appliers when an input fluid is present.");
        this.inputFluidMatchers.put(property, valueMatcher);
        return this;
    }

    public EntropyRecipeBuilder setFluidStateMatchers(Map<String, PropertyValueMatcher> properties) {
        Preconditions.checkState((this.inputFluid != null ? 1 : 0) != 0, (Object)"Can only add appliers when an input fluid is present.");
        this.inputFluidMatchers.clear();
        this.inputFluidMatchers.putAll(properties);
        return this;
    }

    public EntropyRecipeBuilder addBlockStateAppliers(String property, String value) {
        Preconditions.checkState((this.outputBlock != null ? 1 : 0) != 0, (Object)"Can only add appliers when an output block is present.");
        this.outputBlockStateAppliers.put(property, value);
        return this;
    }

    public EntropyRecipeBuilder setBlockStateAppliers(Map<String, String> properties) {
        Preconditions.checkState((this.outputBlock != null ? 1 : 0) != 0, (Object)"Can only add appliers when an output block is present.");
        this.outputBlockStateAppliers.clear();
        this.outputBlockStateAppliers.putAll(properties);
        return this;
    }

    public EntropyRecipeBuilder addFluidStateAppliers(String property, String value) {
        Preconditions.checkState((this.outputFluid != null ? 1 : 0) != 0, (Object)"Can only add appliers when an output fluid is present.");
        this.outputFluidStateAppliers.put(property, value);
        return this;
    }

    public EntropyRecipeBuilder setFluidStateAppliers(Map<String, String> properties) {
        Preconditions.checkState((this.outputFluid != null ? 1 : 0) != 0, (Object)"Can only add appliers when an output fluid is present.");
        this.outputFluidStateAppliers.clear();
        this.outputFluidStateAppliers.putAll(properties);
        return this;
    }

    public EntropyRecipe build() {
        Preconditions.checkState((this.mode != null ? 1 : 0) != 0);
        Preconditions.checkState((this.inputBlock != null || this.inputFluid != null ? 1 : 0) != 0, (Object)"Either inputBlock or inputFluid needs to be not null");
        EntropyRecipe.BlockInput blockInput = null;
        if (this.inputBlock != null) {
            blockInput = new EntropyRecipe.BlockInput(this.inputBlock, this.inputBlockMatchers);
        }
        EntropyRecipe.FluidInput fluidInput = null;
        if (this.inputFluid != null) {
            fluidInput = new EntropyRecipe.FluidInput(this.inputFluid, this.inputFluidMatchers);
        }
        EntropyRecipe.Input input = new EntropyRecipe.Input(Optional.ofNullable(blockInput), Optional.ofNullable(fluidInput));
        EntropyRecipe.BlockOutput blockOutput = null;
        if (this.outputBlock != null) {
            blockOutput = new EntropyRecipe.BlockOutput(this.outputBlock, this.outputBlockKeep, this.outputBlockStateAppliers);
        }
        EntropyRecipe.FluidOutput fluidOutput = null;
        if (this.outputFluid != null) {
            fluidOutput = new EntropyRecipe.FluidOutput(this.outputFluid, this.outputFluidKeep, this.outputFluidStateAppliers);
        }
        EntropyRecipe.Output output = new EntropyRecipe.Output(Optional.ofNullable(blockOutput), Optional.ofNullable(fluidOutput), this.drops);
        return new EntropyRecipe(this.mode, input, output);
    }

    public void save(RecipeOutput consumer, ResourceLocation id) {
        consumer.accept(id, (Recipe)this.build(), null);
    }
}

