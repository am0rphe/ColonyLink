/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.DefaultedRegistry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition
 *  net.minecraft.world.level.block.state.StateHolder
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 *  org.jetbrains.annotations.Nullable
 */
package appeng.recipes.entropy;

import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.entropy.PropertyUtils;
import appeng.recipes.entropy.PropertyValueMatcher;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Nullable;

public class EntropyRecipe
implements Recipe<RecipeInput> {
    public static final MapCodec<EntropyRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)EntropyMode.CODEC.fieldOf("mode").forGetter(EntropyRecipe::getMode), (App)Input.CODEC.fieldOf("input").forGetter(EntropyRecipe::getInput), (App)Output.CODEC.fieldOf("output").forGetter(EntropyRecipe::getOutput)).apply((Applicative)builder, EntropyRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntropyRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)NeoForgeStreamCodecs.enumCodec(EntropyMode.class), EntropyRecipe::getMode, Input.STREAM_CODEC, EntropyRecipe::getInput, Output.STREAM_CODEC, EntropyRecipe::getOutput, EntropyRecipe::new);
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("entropy");
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final RecipeType<EntropyRecipe> TYPE = AERecipeTypes.ENTROPY;
    private final EntropyMode mode;
    private final Input input;
    private final Output output;

    EntropyRecipe(EntropyMode mode, Input input, Output output) {
        this.mode = mode;
        this.input = input;
        this.output = output;
    }

    public boolean matches(RecipeInput inv, Level level) {
        return false;
    }

    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public RecipeSerializer<?> getSerializer() {
        return EntropyRecipeSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return TYPE;
    }

    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    public EntropyMode getMode() {
        return this.mode;
    }

    @Nullable
    public BlockState getOutputBlockState(BlockState originalBlockState) {
        return this.output.block().map(blockOutput -> blockOutput.apply(originalBlockState)).orElse(null);
    }

    @Nullable
    public FluidState getOutputFluidState(FluidState originalFluidState) {
        return this.output.fluid().map(fluidOutput -> fluidOutput.apply(originalFluidState)).orElse(null);
    }

    public List<ItemStack> getDrops() {
        return this.output.drops();
    }

    public boolean matches(EntropyMode mode, BlockState blockState, FluidState fluidState) {
        if (this.getMode() != mode) {
            return false;
        }
        return this.input.matches(blockState, fluidState);
    }

    private static <T extends Comparable<T>, SH extends StateHolder<?, SH>> SH copyProperty(SH from, SH to, Property<T> property) {
        if (to.hasProperty(property)) {
            return (SH)((StateHolder)to.setValue(property, from.getValue(property)));
        }
        return to;
    }

    public Input getInput() {
        return this.input;
    }

    public Output getOutput() {
        return this.output;
    }

    public record Input(Optional<BlockInput> block, Optional<FluidInput> fluid) {
        public static Codec<Input> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)BlockInput.CODEC.optionalFieldOf("block").forGetter(Input::block), (App)FluidInput.CODEC.optionalFieldOf("fluid").forGetter(Input::fluid)).apply((Applicative)builder, Input::new));
        public static StreamCodec<RegistryFriendlyByteBuf, Input> STREAM_CODEC = StreamCodec.composite((StreamCodec)BlockInput.STREAM_CODEC.apply(ByteBufCodecs::optional), Input::block, (StreamCodec)FluidInput.STREAM_CODEC.apply(ByteBufCodecs::optional), Input::fluid, Input::new);

        public boolean matches(BlockState blockState, FluidState fluidState) {
            StateDefinition stateDefinition;
            if (this.block.isPresent()) {
                Block inputBlock = this.block.get().block();
                if (blockState.getBlock() != inputBlock) {
                    return false;
                }
                stateDefinition = inputBlock.getStateDefinition();
                if (!PropertyUtils.doPropertiesMatch(stateDefinition, blockState, this.block.get().properties())) {
                    return false;
                }
            }
            if (this.fluid.isPresent()) {
                Fluid inputFluid = this.fluid.get().fluid();
                if (fluidState.getType() != inputFluid) {
                    return false;
                }
                stateDefinition = inputFluid.getStateDefinition();
                if (!PropertyUtils.doPropertiesMatch(stateDefinition, fluidState, this.fluid.get().properties())) {
                    return false;
                }
            }
            return true;
        }
    }

    public record Output(Optional<BlockOutput> block, Optional<FluidOutput> fluid, List<ItemStack> drops) {
        public static Codec<Output> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)BlockOutput.CODEC.optionalFieldOf("block").forGetter(Output::block), (App)FluidOutput.CODEC.optionalFieldOf("fluid").forGetter(Output::fluid), (App)ItemStack.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(Output::drops)).apply((Applicative)builder, Output::new));
        public static StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.composite((StreamCodec)BlockOutput.STREAM_CODEC.apply(ByteBufCodecs::optional), Output::block, (StreamCodec)FluidOutput.STREAM_CODEC.apply(ByteBufCodecs::optional), Output::fluid, (StreamCodec)ItemStack.LIST_STREAM_CODEC, Output::drops, Output::new);
    }

    public record FluidOutput(Fluid fluid, boolean keepProperties, Map<String, String> properties) {
        public static Codec<FluidOutput> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidOutput::fluid), (App)Codec.BOOL.optionalFieldOf("", (Object)false).forGetter(FluidOutput::keepProperties), (App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING).optionalFieldOf("properties", Map.of()).forGetter(FluidOutput::properties)).apply((Applicative)builder, FluidOutput::new));
        public static StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.FLUID), FluidOutput::fluid, (StreamCodec)ByteBufCodecs.BOOL, FluidOutput::keepProperties, (StreamCodec)ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, (StreamCodec)ByteBufCodecs.STRING_UTF8, (StreamCodec)ByteBufCodecs.STRING_UTF8), FluidOutput::properties, FluidOutput::new);

        public FluidState apply(FluidState originalFluidState) {
            FluidState state = this.fluid.defaultFluidState();
            if (this.keepProperties) {
                for (Property property : originalFluidState.getProperties()) {
                    state = EntropyRecipe.copyProperty(originalFluidState, state, property);
                }
            }
            StateDefinition stateDefinition = state.getType().getStateDefinition();
            state = PropertyUtils.applyProperties(stateDefinition, state, this.properties);
            return state;
        }

        public static void toNetwork(FriendlyByteBuf buffer, FluidOutput output) {
            buffer.writeById(arg_0 -> ((DefaultedRegistry)BuiltInRegistries.FLUID).getId(arg_0), (Object)output.fluid);
            buffer.writeBoolean(output.keepProperties);
            buffer.writeMap(output.properties, FriendlyByteBuf::writeUtf, (fbb, value) -> fbb.writeUtf(value));
        }

        public static FluidOutput fromNetwork(FriendlyByteBuf buffer) {
            Fluid fluid = (Fluid)buffer.readById(arg_0 -> ((DefaultedRegistry)BuiltInRegistries.FLUID).byId(arg_0));
            boolean keepProperties = buffer.readBoolean();
            Map properties = buffer.readMap(FriendlyByteBuf::readUtf, fbb -> fbb.readUtf());
            return new FluidOutput(fluid, keepProperties, properties);
        }
    }

    public record BlockOutput(Block block, boolean keepProperties, Map<String, String> properties) {
        public static Codec<BlockOutput> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("id").forGetter(BlockOutput::block), (App)Codec.BOOL.optionalFieldOf("", (Object)false).forGetter(BlockOutput::keepProperties), (App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING).optionalFieldOf("properties", Map.of()).forGetter(BlockOutput::properties)).apply((Applicative)builder, BlockOutput::new));
        public static StreamCodec<RegistryFriendlyByteBuf, BlockOutput> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.BLOCK), BlockOutput::block, (StreamCodec)ByteBufCodecs.BOOL, BlockOutput::keepProperties, (StreamCodec)ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, (StreamCodec)ByteBufCodecs.STRING_UTF8, (StreamCodec)ByteBufCodecs.STRING_UTF8), BlockOutput::properties, BlockOutput::new);

        public BlockState apply(BlockState originalBlockState) {
            BlockState state = this.block.defaultBlockState();
            if (this.keepProperties) {
                for (Property property : originalBlockState.getProperties()) {
                    state = EntropyRecipe.copyProperty(originalBlockState, state, property);
                }
            }
            StateDefinition stateDefinition = originalBlockState.getBlock().getStateDefinition();
            state = PropertyUtils.applyProperties(stateDefinition, state, this.properties);
            return state;
        }
    }

    public record FluidInput(Fluid fluid, Map<String, PropertyValueMatcher> properties) {
        public static Codec<FluidInput> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidInput::fluid), (App)PropertyValueMatcher.MAP_CODEC.optionalFieldOf("properties", Map.of()).forGetter(FluidInput::properties)).apply((Applicative)builder, FluidInput::new));
        public static StreamCodec<RegistryFriendlyByteBuf, FluidInput> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.FLUID), FluidInput::fluid, (StreamCodec)ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, (StreamCodec)ByteBufCodecs.STRING_UTF8, PropertyValueMatcher.STREAM_CODEC), FluidInput::properties, FluidInput::new);
    }

    public record BlockInput(Block block, Map<String, PropertyValueMatcher> properties) {
        public static Codec<BlockInput> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("id").forGetter(BlockInput::block), (App)PropertyValueMatcher.MAP_CODEC.optionalFieldOf("properties", Map.of()).forGetter(BlockInput::properties)).apply((Applicative)builder, BlockInput::new));
        public static StreamCodec<RegistryFriendlyByteBuf, BlockInput> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)Registries.BLOCK), BlockInput::block, (StreamCodec)ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, (StreamCodec)ByteBufCodecs.STRING_UTF8, PropertyValueMatcher.STREAM_CODEC), BlockInput::properties, BlockInput::new);
    }
}

