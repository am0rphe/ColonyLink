/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SingleRecipeInput
 *  net.minecraft.world.item.crafting.SmeltingRecipe
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.TntBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools.powered;

import appeng.api.config.Actionable;
import appeng.api.util.DimensionalBlockPos;
import appeng.block.misc.TinyTNTBlock;
import appeng.core.AEConfig;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class EntropyManipulatorItem
extends AEBasePoweredItem
implements IBlockTool {
    public static final int ENERGY_PER_USE = 1600;

    public EntropyManipulatorItem(Item.Properties props) {
        super(AEConfig.instance().getEntropyManipulatorBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800.0;
    }

    public boolean hurtEnemy(ItemStack item, LivingEntity target, LivingEntity hitter) {
        if (this.getAECurrentPower(item) > 1600.0) {
            this.extractAEPower(item, 1600.0, Actionable.MODULATE);
            target.igniteForSeconds(8.0f);
        }
        return false;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        BlockHitResult target = EntropyManipulatorItem.getPlayerPOVHitResult((Level)level, (Player)p, (ClipContext.Fluid)ClipContext.Fluid.ANY);
        if (target.getType() != HitResult.Type.BLOCK) {
            return new InteractionResultHolder(InteractionResult.FAIL, (Object)p.getItemInHand(hand));
        }
        BlockPos pos = target.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.getFluidState().isEmpty() && Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
            UseOnContext context = new UseOnContext(p, hand, target);
            this.useOn(context);
        }
        return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)p.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockHitResult target;
        Level level = context.getLevel();
        ItemStack item = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player p = context.getPlayer();
        boolean tryBoth = false;
        if (p == null) {
            if (level.isClientSide) {
                return InteractionResult.FAIL;
            }
            p = Platform.getFakePlayer((ServerLevel)level, null);
            tryBoth = true;
        }
        if ((target = EntropyManipulatorItem.getPlayerPOVHitResult((Level)level, (Player)p, (ClipContext.Fluid)ClipContext.Fluid.ANY)).getType() == HitResult.Type.BLOCK) {
            pos = target.getBlockPos();
        }
        if (this.getAECurrentPower(item) > 1600.0) {
            if (!p.mayUseItemAt(pos, side, item)) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide() && !this.tryApplyEffect(level, item, pos, side, p, tryBoth)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private boolean tryApplyEffect(Level level, ItemStack item, BlockPos pos, Direction side, Player p, boolean tryBoth) {
        EntropyRecipe coolRecipe;
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        FluidState fluidState = level.getFluidState(pos);
        if ((tryBoth || InteractionUtil.isInAlternateUseMode(p)) && (coolRecipe = EntropyManipulatorItem.findRecipe(level, EntropyMode.COOL, blockState, fluidState)) != null) {
            this.extractAEPower(item, 1600.0, Actionable.MODULATE);
            EntropyManipulatorItem.applyRecipe(coolRecipe, level, pos, blockState, fluidState);
            return true;
        }
        if (tryBoth || !InteractionUtil.isInAlternateUseMode(p)) {
            if (block instanceof TntBlock) {
                level.removeBlock(pos, false);
                block.onCaughtFire(level.getBlockState(pos), level, pos, side, (LivingEntity)p);
                return true;
            }
            if (block instanceof TinyTNTBlock) {
                level.removeBlock(pos, false);
                ((TinyTNTBlock)block).startFuse(level, pos, (LivingEntity)p);
                return true;
            }
            EntropyRecipe heatRecipe = EntropyManipulatorItem.findRecipe(level, EntropyMode.HEAT, blockState, fluidState);
            if (heatRecipe != null) {
                this.extractAEPower(item, 1600.0, Actionable.MODULATE);
                EntropyManipulatorItem.applyRecipe(heatRecipe, level, pos, blockState, fluidState);
                return true;
            }
            if (this.performInWorldSmelting(item, level, p, pos, block)) {
                return true;
            }
            if (this.applyFlintAndSteelEffect(level, item, pos, side, p)) {
                return true;
            }
        }
        return false;
    }

    private boolean applyFlintAndSteelEffect(Level level, ItemStack item, BlockPos pos, Direction side, Player p) {
        BlockPos offsetPos = pos.relative(side);
        if (!p.mayUseItemAt(offsetPos, side, item)) {
            return false;
        }
        if (level.isEmptyBlock(offsetPos)) {
            this.extractAEPower(item, 1600.0, Actionable.MODULATE);
            level.playSound(p, (double)offsetPos.getX() + 0.5, (double)offsetPos.getY() + 0.5, (double)offsetPos.getZ() + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, level.random.nextFloat() * 0.4f + 0.8f);
            level.setBlockAndUpdate(offsetPos, Blocks.FIRE.defaultBlockState());
        }
        return true;
    }

    private boolean performInWorldSmelting(ItemStack item, Level level, Player p, BlockPos pos, Block block) {
        BlockState state = level.getBlockState(pos);
        List drops = Collections.emptyList();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockEntity be = level.getBlockEntity(pos);
            drops = Block.getDrops((BlockState)state, (ServerLevel)serverLevel, (BlockPos)pos, (BlockEntity)be, (Entity)p, (ItemStack)item);
        }
        BlockState smeltedBlockState = null;
        ArrayList<ItemStack> smeltedDrops = new ArrayList<ItemStack>();
        for (ItemStack i : drops) {
            SingleRecipeInput tempInv = new SingleRecipeInput(i);
            Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, (RecipeInput)tempInv, level).map(RecipeHolder::value);
            if (recipe.isEmpty()) {
                return false;
            }
            ItemStack result = recipe.get().assemble(tempInv, (HolderLookup.Provider)level.registryAccess());
            if (result.getItem() instanceof BlockItem) {
                Block smeltedBlock = Block.byItem((Item)result.getItem());
                if (smeltedBlock == block) {
                    return false;
                }
                if (smeltedBlockState == null && !smeltedBlock.defaultBlockState().isAir()) {
                    smeltedBlockState = smeltedBlock.defaultBlockState();
                    continue;
                }
            }
            smeltedDrops.add(result);
        }
        if (smeltedBlockState == null && smeltedDrops.isEmpty()) {
            return false;
        }
        this.extractAEPower(item, 1600.0, Actionable.MODULATE);
        level.playSound(p, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, level.random.nextFloat() * 0.4f + 0.8f);
        if (smeltedBlockState == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            level.setBlock(pos, smeltedBlockState, 3);
        }
        Platform.spawnDrops(level, pos, smeltedDrops);
        return true;
    }

    @Nullable
    private static EntropyRecipe findRecipe(Level level, EntropyMode mode, BlockState blockState, FluidState fluidState) {
        for (RecipeHolder holder : level.getRecipeManager().byType(EntropyRecipe.TYPE)) {
            EntropyRecipe recipe = (EntropyRecipe)holder.value();
            if (!recipe.matches(mode, blockState, fluidState)) continue;
            return recipe;
        }
        return null;
    }

    private static void applyRecipe(EntropyRecipe recipe, Level level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        BlockState outputBlockState = recipe.getOutputBlockState(blockState);
        if (outputBlockState != null) {
            level.setBlock(pos, outputBlockState, 3);
        } else {
            FluidState outputFluidState = recipe.getOutputFluidState(fluidState);
            if (outputFluidState != null) {
                level.setBlock(pos, outputFluidState.createLegacyBlock(), 3);
            } else {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
        if (!level.isClientSide) {
            for (ItemStack drop : recipe.getDrops()) {
                Containers.dropItemStack((Level)level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (ItemStack)drop.copy());
            }
        }
        if (recipe.getMode() == EntropyMode.HEAT && !level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
            for (int l = 0; l < 8; ++l) {
                level.addParticle((ParticleOptions)ParticleTypes.LARGE_SMOKE, (double)pos.getX() + Math.random(), (double)pos.getY() + Math.random(), (double)pos.getZ() + Math.random(), 0.0, 0.0, 0.0);
            }
        }
    }
}

