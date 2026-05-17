/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.EnumHashBiMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.Sheep
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SnowballItem
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools.powered;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.block.networking.CableBusBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.IBlockTool;
import appeng.items.contents.CellConfig;
import appeng.items.misc.PaintBallItem;
import appeng.items.storage.StorageTier;
import appeng.items.tools.powered.BlockRecolorer;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.cells.BasicCellHandler;
import appeng.me.cells.BasicCellInventory;
import appeng.me.helpers.BaseActionSource;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class ColorApplicatorItem
extends AEBasePoweredItem
implements IBasicCellItem,
IBlockTool,
IMouseWheelItem {
    private static final double POWER_PER_USE = 100.0;
    private static final Map<TagKey<Item>, AEColor> TAG_TO_COLOR = AEColor.VALID_COLORS.stream().collect(Collectors.toMap(aeColor -> ConventionTags.dye(aeColor.dye), Function.identity()));
    private static final BiMap<DyeColor, Item> VANILLA_DYES = EnumHashBiMap.create(DyeColor.class);

    public ColorApplicatorItem(Item.Properties props) {
        super(AEConfig.instance().getColorApplicatorBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80.0 + 80.0 * (double)Upgrades.getEnergyCardMultiplier(this.getUpgrades(stack));
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        this.cycleColors(stack, this.getColor(stack), 1);
        if (level.isClientSide) {
            player.displayClientMessage(stack.getHoverName(), true);
        }
        return InteractionResultHolder.fail((Object)stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack is = context.getItemInHand();
        Direction side = context.getClickedFace();
        Player p = context.getPlayer();
        if (p == null && level instanceof ServerLevel) {
            p = Platform.getFakePlayer((ServerLevel)level, null);
        }
        Block blk = level.getBlockState(pos).getBlock();
        AEColor color = this.getColor(is);
        StorageCell inv = StorageCells.getCellInventory(is, null);
        if (inv != null) {
            if (p != null && !Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
                return InteractionResult.FAIL;
            }
            if (!this.consumeColor(is, color, true)) {
                color = null;
            }
            if (color != null) {
                if (color == AEColor.TRANSPARENT) {
                    BlockEntity blockEntity;
                    if (p != null && (blockEntity = level.getBlockEntity(pos)) instanceof IColorableBlockEntity) {
                        IColorableBlockEntity colorableBlockEntity = (IColorableBlockEntity)blockEntity;
                        if (this.getAECurrentPower(is) > 100.0 && colorableBlockEntity.getColor() != AEColor.TRANSPARENT && colorableBlockEntity.recolourBlock(side, AEColor.TRANSPARENT, p)) {
                            this.consumeColor(is, color, false);
                            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
                        }
                    }
                    Block testBlk = level.getBlockState(pos.relative(side)).getBlock();
                    BlockEntity painted = level.getBlockEntity(pos.relative(side));
                    if (this.getAECurrentPower(is) > 100.0 && testBlk instanceof PaintSplotchesBlock && painted instanceof PaintSplotchesBlockEntity) {
                        this.consumeColor(is, color, false);
                        ((PaintSplotchesBlockEntity)painted).cleanSide(side.getOpposite());
                        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
                    }
                }
                if (this.getAECurrentPower(is) > 100.0 && this.recolourBlock(blk, side, level, pos, color, p)) {
                    this.consumeColor(is, color, false);
                    return InteractionResult.sidedSuccess((boolean)level.isClientSide());
                }
            }
        }
        if (p != null && InteractionUtil.isInAlternateUseMode(p)) {
            this.cycleColors(is, color, 1);
            if (level.isClientSide) {
                p.displayClientMessage(is.getHoverName(), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    public InteractionResult interactLivingEntity(ItemStack is, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        Sheep sheep;
        AEColor paintBallColor = this.getColor(is);
        if (paintBallColor != null && interactionTarget instanceof Sheep && (sheep = (Sheep)interactionTarget).isAlive() && !sheep.isSheared() && sheep.getColor() != paintBallColor.dye) {
            if (!player.level().isClientSide && this.getAECurrentPower(is) > 100.0) {
                sheep.setColor(paintBallColor.dye);
                sheep.level().playSound(player, (Entity)sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                this.consumeColor(is, paintBallColor, false);
            }
            return InteractionResult.sidedSuccess((boolean)player.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    public Component getName(ItemStack is) {
        MutableComponent extra = GuiText.Empty.text();
        AEColor selected = this.getActiveColor(is);
        if (selected != null && Platform.isClient()) {
            extra = Component.translatable((String)selected.translationKey);
        }
        return super.getName(is).copy().append(" - ").append((Component)extra);
    }

    public AEColor getActiveColor(ItemStack tol) {
        return this.getColor(tol);
    }

    public boolean consumeColor(ItemStack applicator, AEColor color, boolean simulate) {
        StorageCell inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return false;
        }
        KeyCounter availableItems = inv.getAvailableStacks();
        for (AEKey what : availableItems.keySet()) {
            if (this.getColorFrom(what) != color) continue;
            return this.consumeItem(applicator, what, simulate);
        }
        return false;
    }

    public boolean consumeItem(ItemStack applicator, AEKey key, boolean simulate) {
        boolean success;
        StorageCell inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return false;
        }
        Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        boolean bl = success = inv.extract(key, 1L, mode, new BaseActionSource()) >= 1L && this.extractAEPower(applicator, 100.0, mode) >= 100.0;
        if (success && !simulate && this.getColorFrom(key) == this.getColor(applicator) && inv.getAvailableStacks().get(key) == 0L) {
            this.setColor(applicator, null);
        }
        return success;
    }

    @Nullable
    private AEColor getColorFrom(AEKey key) {
        AEFluidKey fluidKey;
        if (key instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)key;
            Item item = itemKey.getItem();
            if (item instanceof SnowballItem) {
                return AEColor.TRANSPARENT;
            }
            if (item instanceof PaintBallItem) {
                PaintBallItem ipb = (PaintBallItem)item;
                return ipb.getColor();
            }
            DyeColor vanillaDye = (DyeColor)VANILLA_DYES.inverse().get((Object)item);
            if (vanillaDye != null) {
                return AEColor.fromDye(vanillaDye);
            }
            for (Map.Entry<TagKey<Item>, AEColor> entry : TAG_TO_COLOR.entrySet()) {
                if (!item.builtInRegistryHolder().is(entry.getKey())) continue;
                return entry.getValue();
            }
        } else if (key instanceof AEFluidKey && (fluidKey = (AEFluidKey)key).isTagged(FluidTags.WATER)) {
            return AEColor.TRANSPARENT;
        }
        return null;
    }

    public AEColor getColor(ItemStack is) {
        AEColor selectedPaint = (AEColor)((Object)is.get(AEComponents.SELECTED_COLOR));
        if (selectedPaint != null) {
            return selectedPaint;
        }
        return this.findNextColor(is, null, 0);
    }

    @Nullable
    private AEColor findNextColor(ItemStack is, @Nullable AEColor anchorColor, int scrollOffset) {
        AEColor newColor = null;
        StorageCell inv = StorageCells.getCellInventory(is, null);
        if (inv != null) {
            KeyCounter keyList = inv.getAvailableStacks();
            if (anchorColor == null) {
                AEKey firstItem = keyList.getFirstKey();
                if (firstItem != null) {
                    newColor = this.getColorFrom(firstItem);
                }
            } else {
                LinkedList<AEKey> list = new LinkedList<AEKey>();
                for (Object2LongMap.Entry<AEKey> i : keyList) {
                    list.add((AEKey)i.getKey());
                }
                if (list.isEmpty()) {
                    return null;
                }
                list.sort(Comparator.comparingInt(a -> {
                    AEColor color = this.getColorFrom((AEKey)a);
                    return color != null ? color.ordinal() : Integer.MAX_VALUE;
                }));
                AEKey where = (AEKey)list.getFirst();
                for (int cycles = 1 + list.size(); cycles > 0 && this.getColorFrom(where) != anchorColor; --cycles) {
                    list.addLast((AEKey)list.removeFirst());
                    where = (AEKey)list.getFirst();
                }
                if (scrollOffset > 0) {
                    list.addLast((AEKey)list.removeFirst());
                }
                if (scrollOffset < 0) {
                    list.addFirst((AEKey)list.removeLast());
                }
                return this.getColorFrom((AEKey)list.get(0));
            }
        }
        if (newColor != null) {
            this.setColor(is, newColor);
        }
        return newColor;
    }

    private void setColor(ItemStack is, @Nullable AEColor newColor) {
        is.set(AEComponents.SELECTED_COLOR, (Object)newColor);
    }

    private boolean recolourBlock(Block blk, Direction side, Level level, BlockPos pos, AEColor newColor, @Nullable Player p) {
        IColorableBlockEntity ct;
        BlockEntity be;
        BlockState state = level.getBlockState(pos);
        Block recolored = BlockRecolorer.recolor(blk, newColor);
        if (recolored != blk) {
            BlockState newState = recolored.defaultBlockState();
            for (Property prop : newState.getProperties()) {
                newState = ColorApplicatorItem.copyProp(state, newState, prop);
            }
            return level.setBlockAndUpdate(pos, newState);
        }
        if (blk instanceof CableBusBlock) {
            CableBusBlock cableBusBlock = (CableBusBlock)blk;
            if (p != null) {
                return cableBusBlock.recolorBlock((BlockGetter)level, pos, side, newColor.dye, p);
            }
        }
        if ((be = level.getBlockEntity(pos)) instanceof IColorableBlockEntity && (ct = (IColorableBlockEntity)be).getColor() != newColor) {
            ct.recolourBlock(side, newColor, p);
            return true;
        }
        return false;
    }

    private static <T extends Comparable<T>> BlockState copyProp(BlockState oldState, BlockState newState, Property<T> prop) {
        if (newState.hasProperty(prop)) {
            return (BlockState)newState.setValue(prop, oldState.getValue(prop));
        }
        return newState;
    }

    public void cycleColors(ItemStack is, @Nullable AEColor currentColor, int i) {
        if (currentColor == null) {
            this.setColor(is, this.getColor(is));
        } else {
            this.setColor(is, this.findNextColor(is, currentColor, i));
        }
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        this.addCellInformationToTooltip(stack, lines);
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return this.getCellTooltipImage(stack);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return StorageTier.SIZE_4K.bytes() / 2;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return StorageTier.SIZE_4K.bytes() / 128;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 27;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        return this.getColorFrom(requestedAddition) == null;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        this.setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades) * 8);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(AEKeyType.items()), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return (FuzzyMode)((Object)is.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)FuzzyMode.IGNORE_ALL));
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.set(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)fzMode);
    }

    @Override
    public void onWheel(ItemStack is, boolean up) {
        this.cycleColors(is, this.getColor(is), up ? 1 : -1);
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);
        output.accept(ColorApplicatorItem.createFullColorApplicator());
    }

    public static ItemStack createFullColorApplicator() {
        ColorApplicatorItem item = AEItems.COLOR_APPLICATOR.get();
        ItemStack applicator = new ItemStack((ItemLike)item);
        BasicCellInventory dyeStorage = BasicCellHandler.INSTANCE.getCellInventory(applicator, null);
        for (Item dyeItem : VANILLA_DYES.values()) {
            dyeStorage.insert(AEItemKey.of((ItemLike)dyeItem), 128L, Actionable.MODULATE, new BaseActionSource());
        }
        dyeStorage.insert(AEItemKey.of((ItemLike)Items.SNOWBALL), 128L, Actionable.MODULATE, new BaseActionSource());
        IUpgradeInventory upgrades = item.getUpgrades(applicator);
        upgrades.addItems(AEItems.ENERGY_CARD.stack());
        upgrades.addItems(AEItems.ENERGY_CARD.stack());
        item.injectAEPower(applicator, item.getAEMaxPower(applicator), Actionable.MODULATE);
        return applicator;
    }

    public void setActiveColor(ItemStack applicator, @Nullable AEColor color) {
        if (color == null) {
            this.setColor(applicator, null);
            return;
        }
        StorageCell inv = StorageCells.getCellInventory(applicator, null);
        if (inv == null) {
            return;
        }
        for (Object2LongMap.Entry<AEKey> entry : inv.getAvailableStacks()) {
            AEItemKey itemKey;
            Object object = entry.getKey();
            if (!(object instanceof AEItemKey) || this.getColorFrom(itemKey = (AEItemKey)object) != color) continue;
            this.setColor(applicator, color);
            return;
        }
    }

    static {
        VANILLA_DYES.put((Object)DyeColor.WHITE, (Object)Items.WHITE_DYE);
        VANILLA_DYES.put((Object)DyeColor.LIGHT_GRAY, (Object)Items.LIGHT_GRAY_DYE);
        VANILLA_DYES.put((Object)DyeColor.GRAY, (Object)Items.GRAY_DYE);
        VANILLA_DYES.put((Object)DyeColor.BLACK, (Object)Items.BLACK_DYE);
        VANILLA_DYES.put((Object)DyeColor.LIME, (Object)Items.LIME_DYE);
        VANILLA_DYES.put((Object)DyeColor.YELLOW, (Object)Items.YELLOW_DYE);
        VANILLA_DYES.put((Object)DyeColor.ORANGE, (Object)Items.ORANGE_DYE);
        VANILLA_DYES.put((Object)DyeColor.BROWN, (Object)Items.BROWN_DYE);
        VANILLA_DYES.put((Object)DyeColor.RED, (Object)Items.RED_DYE);
        VANILLA_DYES.put((Object)DyeColor.PINK, (Object)Items.PINK_DYE);
        VANILLA_DYES.put((Object)DyeColor.MAGENTA, (Object)Items.MAGENTA_DYE);
        VANILLA_DYES.put((Object)DyeColor.PURPLE, (Object)Items.PURPLE_DYE);
        VANILLA_DYES.put((Object)DyeColor.BLUE, (Object)Items.BLUE_DYE);
        VANILLA_DYES.put((Object)DyeColor.LIGHT_BLUE, (Object)Items.LIGHT_BLUE_DYE);
        VANILLA_DYES.put((Object)DyeColor.CYAN, (Object)Items.CYAN_DYE);
        VANILLA_DYES.put((Object)DyeColor.GREEN, (Object)Items.GREEN_DYE);
    }
}

