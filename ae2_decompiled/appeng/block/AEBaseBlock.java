/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block;

import appeng.api.orientation.IOrientableBlock;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.hooks.WrenchHook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public abstract class AEBaseBlock
extends Block
implements IOrientableBlock {
    protected AEBaseBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.none();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        for (Property<?> property : this.getOrientationStrategy().getProperties()) {
            builder.add(new Property[]{property});
        }
    }

    public static BlockBehaviour.Properties defaultProps(MapColor mapColor, SoundType soundType) {
        return BlockBehaviour.Properties.of().strength(2.2f, 11.0f).mapColor(mapColor).sound(soundType);
    }

    public static BlockBehaviour.Properties stoneProps() {
        return AEBaseBlock.defaultProps(MapColor.STONE, SoundType.STONE).forceSolidOn();
    }

    public static BlockBehaviour.Properties metalProps() {
        return AEBaseBlock.defaultProps(MapColor.METAL, SoundType.METAL).forceSolidOn();
    }

    public static BlockBehaviour.Properties glassProps() {
        return AEBaseBlock.defaultProps(MapColor.NONE, SoundType.GLASS);
    }

    public static BlockBehaviour.Properties fixtureProps() {
        return AEBaseBlock.defaultProps(MapColor.METAL, SoundType.GLASS).noCollission().noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept((ItemLike)this);
    }

    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey((Object)this);
        return id != BuiltInRegistries.BLOCK.getDefaultKey() ? id : null;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        return this.getOrientationStrategy().getStateForPlacement(state, context);
    }

    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        if (!WrenchHook.isDisassembling()) {
            super.spawnDestroyParticles(level, player, pos, state);
        }
    }
}

