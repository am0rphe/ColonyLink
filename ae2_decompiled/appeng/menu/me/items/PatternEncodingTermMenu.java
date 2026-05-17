/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SingleRecipeInput
 *  net.minecraft.world.item.crafting.SmithingRecipe
 *  net.minecraft.world.item.crafting.SmithingRecipeInput
 *  net.minecraft.world.item.crafting.StonecutterRecipe
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.items;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Icon;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.PatternTermSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import appeng.util.ConfigMenuInventory;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class PatternEncodingTermMenu
extends MEStorageMenu {
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    private static final int CRAFTING_GRID_SLOTS = 9;
    private static final String ACTION_SET_MODE = "setMode";
    private static final String ACTION_ENCODE = "encode";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_SET_SUBSTITUTION = "setSubstitution";
    private static final String ACTION_SET_FLUID_SUBSTITUTION = "setFluidSubstitution";
    private static final String ACTION_SET_STONECUTTING_RECIPE_ID = "setStonecuttingRecipeId";
    private static final String ACTION_CYCLE_PROCESSING_OUTPUT = "cycleProcessingOutput";
    public static final MenuType<PatternEncodingTermMenu> TYPE = MenuTypeBuilder.create(PatternEncodingTermMenu::new, IPatternTerminalMenuHost.class).build("patternterm");
    private final PatternEncodingLogic encodingLogic;
    private final FakeSlot[] craftingGridSlots = new FakeSlot[9];
    private final FakeSlot[] processingInputSlots = new FakeSlot[81];
    private final FakeSlot[] processingOutputSlots = new FakeSlot[27];
    private final FakeSlot stonecuttingInputSlot;
    private final FakeSlot smithingTableTemplateSlot;
    private final FakeSlot smithingTableBaseSlot;
    private final FakeSlot smithingTableAdditionSlot;
    private final PatternTermSlot craftOutputSlot;
    private final RestrictedInputSlot blankPatternSlot;
    private final RestrictedInputSlot encodedPatternSlot;
    private final ConfigInventory encodedInputsInv;
    private final ConfigInventory encodedOutputsInv;
    private RecipeHolder<CraftingRecipe> currentRecipe;
    private EncodingMode currentMode;
    @GuiSync(value=97)
    public EncodingMode mode = EncodingMode.CRAFTING;
    @GuiSync(value=96)
    public boolean substitute = false;
    @GuiSync(value=95)
    public boolean substituteFluids = true;
    @GuiSync(value=94)
    @Nullable
    public ResourceLocation stonecuttingRecipeId;
    private final List<RecipeHolder<StonecutterRecipe>> stonecuttingRecipes = new ArrayList<RecipeHolder<StonecutterRecipe>>();
    public IntSet slotsSupportingFluidSubstitution = new IntArraySet();

    public PatternEncodingTermMenu(int id, Inventory ip, IPatternTerminalMenuHost host) {
        this(TYPE, id, ip, host, true);
    }

    public PatternEncodingTermMenu(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host, boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        int i;
        this.encodingLogic = host.getLogic();
        this.encodedInputsInv = this.encodingLogic.getEncodedInputInv();
        this.encodedOutputsInv = this.encodingLogic.getEncodedOutputInv();
        ConfigMenuInventory encodedInputs = this.encodedInputsInv.createMenuWrapper();
        ConfigMenuInventory encodedOutputs = this.encodedOutputsInv.createMenuWrapper();
        for (i = 0; i < 9; ++i) {
            FakeSlot slot = new FakeSlot(encodedInputs, i);
            slot.setHideAmount(true);
            this.craftingGridSlots[i] = slot;
            this.addSlot(this.craftingGridSlots[i], SlotSemantics.CRAFTING_GRID);
        }
        this.craftOutputSlot = new PatternTermSlot();
        this.addSlot(this.craftOutputSlot, SlotSemantics.CRAFTING_RESULT);
        for (i = 0; i < this.processingInputSlots.length; ++i) {
            this.processingInputSlots[i] = new FakeSlot(encodedInputs, i);
            this.addSlot(this.processingInputSlots[i], SlotSemantics.PROCESSING_INPUTS);
        }
        for (i = 0; i < this.processingOutputSlots.length; ++i) {
            this.processingOutputSlots[i] = new FakeSlot(encodedOutputs, i);
            this.addSlot(this.processingOutputSlots[i], SlotSemantics.PROCESSING_OUTPUTS);
        }
        this.processingOutputSlots[0].setIcon(Icon.BACKGROUND_PRIMARY_OUTPUT);
        this.stonecuttingInputSlot = new FakeSlot(encodedInputs, 0);
        this.addSlot(this.stonecuttingInputSlot, SlotSemantics.STONECUTTING_INPUT);
        this.stonecuttingInputSlot.setHideAmount(true);
        this.smithingTableTemplateSlot = new FakeSlot(encodedInputs, 0);
        this.addSlot(this.smithingTableTemplateSlot, SlotSemantics.SMITHING_TABLE_TEMPLATE);
        this.smithingTableTemplateSlot.setHideAmount(true);
        this.smithingTableBaseSlot = new FakeSlot(encodedInputs, 1);
        this.addSlot(this.smithingTableBaseSlot, SlotSemantics.SMITHING_TABLE_BASE);
        this.smithingTableBaseSlot.setHideAmount(true);
        this.smithingTableAdditionSlot = new FakeSlot(encodedInputs, 2);
        this.addSlot(this.smithingTableAdditionSlot, SlotSemantics.SMITHING_TABLE_ADDITION);
        this.smithingTableAdditionSlot.setHideAmount(true);
        this.blankPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BLANK_PATTERN, this.encodingLogic.getBlankPatternInv(), 0);
        this.addSlot(this.blankPatternSlot, SlotSemantics.BLANK_PATTERN);
        this.encodedPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN, this.encodingLogic.getEncodedPatternInv(), 0);
        this.addSlot(this.encodedPatternSlot, SlotSemantics.ENCODED_PATTERN);
        this.encodedPatternSlot.setStackLimit(1);
        this.registerClientAction(ACTION_ENCODE, this::encode);
        this.registerClientAction(ACTION_SET_STONECUTTING_RECIPE_ID, ResourceLocation.class, this.encodingLogic::setStonecuttingRecipeId);
        this.registerClientAction(ACTION_CLEAR, this::clear);
        this.registerClientAction(ACTION_SET_MODE, EncodingMode.class, this.encodingLogic::setMode);
        this.registerClientAction(ACTION_SET_SUBSTITUTION, Boolean.class, this.encodingLogic::setSubstitution);
        this.registerClientAction(ACTION_SET_FLUID_SUBSTITUTION, Boolean.class, this.encodingLogic::setFluidSubstitution);
        this.registerClientAction(ACTION_CYCLE_PROCESSING_OUTPUT, this::cycleProcessingOutput);
        this.updateStonecuttingRecipes();
    }

    public void setItem(int slotID, int stateId, ItemStack stack) {
        super.setItem(slotID, stateId, stack);
        this.getAndUpdateOutput();
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        super.initializeContents(stateId, items, carried);
        this.getAndUpdateOutput();
    }

    private ItemStack getAndUpdateOutput() {
        Level level = this.getPlayerInventory().player.level();
        NonNullList items = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
        boolean invalidIngredients = false;
        for (int x = 0; x < items.size(); ++x) {
            ItemStack stack = this.getEncodedCraftingIngredient(x);
            if (stack != null) {
                items.set(x, (Object)stack);
                continue;
            }
            invalidIngredients = true;
        }
        CraftingInput input = CraftingInput.of((int)3, (int)3, (List)items);
        if (this.currentRecipe == null || !((CraftingRecipe)this.currentRecipe.value()).matches((RecipeInput)input, level)) {
            this.currentRecipe = invalidIngredients ? null : (RecipeHolder)level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)input, level).orElse(null);
            this.currentMode = this.mode;
            this.checkFluidSubstitutionSupport();
        }
        ItemStack is = this.currentRecipe == null ? ItemStack.EMPTY : ((CraftingRecipe)this.currentRecipe.value()).assemble((RecipeInput)input, (HolderLookup.Provider)level.registryAccess());
        this.craftOutputSlot.setResultItem(is);
        return is;
    }

    private void checkFluidSubstitutionSupport() {
        IPatternDetails decodedPattern;
        this.slotsSupportingFluidSubstitution.clear();
        if (this.currentRecipe == null) {
            return;
        }
        ItemStack encodedPattern = this.encodePattern();
        if (encodedPattern != null && (decodedPattern = PatternDetailsHelper.decodePattern(encodedPattern, this.getPlayerInventory().player.level())) instanceof AECraftingPattern) {
            AECraftingPattern craftingPattern = (AECraftingPattern)decodedPattern;
            for (int i = 0; i < craftingPattern.getSparseInputs().size(); ++i) {
                if (craftingPattern.getValidFluid(i) == null) continue;
                this.slotsSupportingFluidSubstitution.add(i);
            }
        }
    }

    public void encode() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_ENCODE);
            return;
        }
        ItemStack encodedPattern = this.encodePattern();
        if (encodedPattern != null) {
            ItemStack encodeOutput = this.encodedPatternSlot.getItem();
            if (!(encodeOutput.isEmpty() || PatternDetailsHelper.isEncodedPattern(encodeOutput) || AEItems.BLANK_PATTERN.is(encodeOutput))) {
                return;
            }
            if (encodeOutput.isEmpty()) {
                ItemStack blankPattern = this.blankPatternSlot.getItem();
                if (!this.isPattern(blankPattern)) {
                    return;
                }
                blankPattern.shrink(1);
                if (blankPattern.getCount() <= 0) {
                    this.blankPatternSlot.set(ItemStack.EMPTY);
                }
            }
            this.encodedPatternSlot.set(encodedPattern);
        } else {
            this.clearPattern();
        }
    }

    private void clearPattern() {
        ItemStack encodedPattern = this.encodedPatternSlot.getItem();
        if (PatternDetailsHelper.isEncodedPattern(encodedPattern)) {
            this.encodedPatternSlot.set(AEItems.BLANK_PATTERN.stack(encodedPattern.getCount()));
        }
    }

    @Nullable
    private ItemStack encodePattern() {
        return switch (this.mode) {
            default -> throw new MatchException(null, null);
            case EncodingMode.CRAFTING -> this.encodeCraftingPattern();
            case EncodingMode.PROCESSING -> this.encodeProcessingPattern();
            case EncodingMode.SMITHING_TABLE -> this.encodeSmithingTablePattern();
            case EncodingMode.STONECUTTING -> this.encodeStonecuttingPattern();
        };
    }

    @Nullable
    private ItemStack encodeCraftingPattern() {
        ItemStack[] ingredients = new ItemStack[9];
        boolean valid = false;
        for (int x = 0; x < ingredients.length; ++x) {
            ingredients[x] = this.getEncodedCraftingIngredient(x);
            if (ingredients[x] == null) {
                return null;
            }
            if (ingredients[x].isEmpty()) continue;
            valid = true;
        }
        if (!valid) {
            return null;
        }
        ItemStack result = this.getAndUpdateOutput();
        if (result.isEmpty() || this.currentRecipe == null) {
            return null;
        }
        return PatternDetailsHelper.encodeCraftingPattern(this.currentRecipe, ingredients, result, this.isSubstitute(), this.isSubstituteFluids());
    }

    @Nullable
    private ItemStack encodeProcessingPattern() {
        GenericStack[] inputs = new GenericStack[this.encodedInputsInv.size()];
        boolean valid = false;
        for (int slot = 0; slot < this.encodedInputsInv.size(); ++slot) {
            inputs[slot] = this.encodedInputsInv.getStack(slot);
            if (inputs[slot] == null) continue;
            valid = true;
        }
        if (!valid) {
            return null;
        }
        GenericStack[] outputs = new GenericStack[this.encodedOutputsInv.size()];
        for (int slot = 0; slot < this.encodedOutputsInv.size(); ++slot) {
            outputs[slot] = this.encodedOutputsInv.getStack(slot);
        }
        if (outputs[0] == null) {
            return null;
        }
        return PatternDetailsHelper.encodeProcessingPattern(Arrays.asList(inputs), Arrays.asList(outputs));
    }

    @Nullable
    private ItemStack encodeSmithingTablePattern() {
        AEItemKey base;
        AEItemKey template;
        AEKey aEKey;
        block5: {
            block4: {
                aEKey = this.encodedInputsInv.getKey(0);
                if (!(aEKey instanceof AEItemKey)) break block4;
                template = (AEItemKey)aEKey;
                aEKey = this.encodedInputsInv.getKey(1);
                if (!(aEKey instanceof AEItemKey)) break block4;
                base = (AEItemKey)aEKey;
                aEKey = this.encodedInputsInv.getKey(2);
                if (aEKey instanceof AEItemKey) break block5;
            }
            return null;
        }
        AEItemKey addition = (AEItemKey)aEKey;
        SmithingRecipeInput input = new SmithingRecipeInput(template.toStack(), base.toStack(), addition.toStack());
        Level level = this.getPlayer().level();
        RecipeHolder recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, (RecipeInput)input, level).orElse(null);
        if (recipe == null) {
            return null;
        }
        AEItemKey output = AEItemKey.of(((SmithingRecipe)recipe.value()).assemble((RecipeInput)input, (HolderLookup.Provider)level.registryAccess()));
        return PatternDetailsHelper.encodeSmithingTablePattern((RecipeHolder<SmithingRecipe>)recipe, template, base, addition, output, this.encodingLogic.isSubstitution());
    }

    @Nullable
    private ItemStack encodeStonecuttingPattern() {
        if (this.stonecuttingRecipeId == null) {
            return null;
        }
        AEKey aEKey = this.encodedInputsInv.getKey(0);
        if (!(aEKey instanceof AEItemKey)) {
            return null;
        }
        AEItemKey input = (AEItemKey)aEKey;
        SingleRecipeInput recipeInput = new SingleRecipeInput(input.toStack());
        Level level = this.getPlayer().level();
        RecipeHolder recipe = level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, (RecipeInput)recipeInput, level, this.stonecuttingRecipeId).orElse(null);
        if (recipe == null) {
            return null;
        }
        AEItemKey output = AEItemKey.of(((StonecutterRecipe)recipe.value()).getResultItem((HolderLookup.Provider)level.registryAccess()));
        return PatternDetailsHelper.encodeStonecuttingPattern((RecipeHolder<StonecutterRecipe>)recipe, input, output, this.encodingLogic.isSubstitution());
    }

    @Nullable
    private ItemStack getEncodedCraftingIngredient(int slot) {
        AEKey what = this.encodedInputsInv.getKey(slot);
        if (what == null) {
            return ItemStack.EMPTY;
        }
        if (what instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)what;
            return itemKey.toStack(1);
        }
        return null;
    }

    private boolean isPattern(ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        return AEItems.BLANK_PATTERN.is(output);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.isServerSide()) {
            if (this.mode != this.encodingLogic.getMode()) {
                this.setMode(this.encodingLogic.getMode());
            }
            this.substitute = this.encodingLogic.isSubstitution();
            this.substituteFluids = this.encodingLogic.isFluidSubstitution();
            this.stonecuttingRecipeId = this.encodingLogic.getStonecuttingRecipeId();
        }
    }

    @Override
    public void onServerDataSync(ShortSet updatedFields) {
        super.onServerDataSync(updatedFields);
        for (FakeSlot slot : this.craftingGridSlots) {
            slot.setActive(this.mode == EncodingMode.CRAFTING);
        }
        this.craftOutputSlot.setActive(this.mode == EncodingMode.CRAFTING);
        for (FakeSlot slot : this.processingInputSlots) {
            slot.setActive(this.mode == EncodingMode.PROCESSING);
        }
        for (FakeSlot slot : this.processingOutputSlots) {
            slot.setActive(this.mode == EncodingMode.PROCESSING);
        }
        if (this.currentMode != this.mode) {
            this.encodingLogic.setMode(this.mode);
            this.getAndUpdateOutput();
            this.updateStonecuttingRecipes();
        }
    }

    @Override
    public void onSlotChange(Slot s) {
        if (s == this.encodedPatternSlot && this.isServerSide()) {
            this.broadcastChanges();
        }
        if (s == this.stonecuttingInputSlot) {
            this.updateStonecuttingRecipes();
        }
    }

    private void updateStonecuttingRecipes() {
        this.stonecuttingRecipes.clear();
        AEKey aEKey = this.encodedInputsInv.getKey(0);
        if (aEKey instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)aEKey;
            Level level = this.getPlayer().level();
            RecipeManager recipeManager = level.getRecipeManager();
            SingleRecipeInput recipeInput = new SingleRecipeInput(itemKey.toStack());
            this.stonecuttingRecipes.addAll(recipeManager.getRecipesFor(RecipeType.STONECUTTING, (RecipeInput)recipeInput, level));
        }
        if (this.stonecuttingRecipeId != null && this.stonecuttingRecipes.stream().noneMatch(r -> r.id().equals((Object)this.stonecuttingRecipeId))) {
            this.stonecuttingRecipeId = null;
        }
    }

    public void clear() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR);
            return;
        }
        this.encodedInputsInv.clear();
        this.encodedOutputsInv.clear();
        this.broadcastChanges();
        this.getAndUpdateOutput();
    }

    public EncodingMode getMode() {
        return this.mode;
    }

    public void setMode(EncodingMode mode) {
        if (this.mode != mode && mode == EncodingMode.STONECUTTING) {
            this.updateStonecuttingRecipes();
        }
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_MODE, mode);
        } else {
            this.mode = mode;
        }
    }

    public boolean isSubstitute() {
        return this.substitute;
    }

    public void setSubstitute(boolean substitute) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_SUBSTITUTION, substitute);
        } else {
            this.substitute = substitute;
        }
    }

    public boolean isSubstituteFluids() {
        return this.substituteFluids;
    }

    public void setSubstituteFluids(boolean substituteFluids) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_FLUID_SUBSTITUTION, substituteFluids);
        } else {
            this.substituteFluids = substituteFluids;
        }
    }

    @Nullable
    public ResourceLocation getStonecuttingRecipeId() {
        return this.stonecuttingRecipeId;
    }

    public void setStonecuttingRecipeId(ResourceLocation id) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_SET_STONECUTTING_RECIPE_ID, id);
        } else {
            this.encodingLogic.setStonecuttingRecipeId(id);
        }
    }

    @Override
    protected int transferStackToMenu(ItemStack input) {
        int initialCount = input.getCount();
        if (this.blankPatternSlot.mayPlace(input) && (input = this.blankPatternSlot.safeInsert(input)).isEmpty()) {
            return initialCount;
        }
        if (this.encodedPatternSlot.mayPlace(input) && (input = this.encodedPatternSlot.safeInsert(input)).isEmpty()) {
            return initialCount;
        }
        int transferred = initialCount - input.getCount();
        return transferred + super.transferStackToMenu(input);
    }

    @Contract(value="null -> false")
    public boolean canModifyAmountForSlot(@Nullable Slot slot) {
        return this.isProcessingPatternSlot(slot) && slot.hasItem();
    }

    @Contract(value="null -> false")
    public boolean isProcessingPatternSlot(@Nullable Slot slot) {
        if (slot == null || this.mode != EncodingMode.PROCESSING) {
            return false;
        }
        for (FakeSlot processingOutputSlot : this.processingOutputSlots) {
            if (processingOutputSlot != slot) continue;
            return true;
        }
        for (FakeSlot craftingSlot : this.processingInputSlots) {
            if (craftingSlot != slot) continue;
            return true;
        }
        return false;
    }

    public FakeSlot[] getCraftingGridSlots() {
        return this.craftingGridSlots;
    }

    public FakeSlot[] getProcessingInputSlots() {
        return this.processingInputSlots;
    }

    public FakeSlot[] getProcessingOutputSlots() {
        return this.processingOutputSlots;
    }

    public FakeSlot getSmithingTableTemplateSlot() {
        return this.smithingTableTemplateSlot;
    }

    public FakeSlot getSmithingTableBaseSlot() {
        return this.smithingTableBaseSlot;
    }

    public FakeSlot getSmithingTableAdditionSlot() {
        return this.smithingTableAdditionSlot;
    }

    public void cycleProcessingOutput() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CYCLE_PROCESSING_OUTPUT);
        } else {
            int i;
            if (this.mode != EncodingMode.PROCESSING) {
                return;
            }
            ItemStack[] newOutputs = new ItemStack[this.getProcessingOutputSlots().length];
            block0: for (i = 0; i < this.processingOutputSlots.length; ++i) {
                newOutputs[i] = ItemStack.EMPTY;
                if (this.processingOutputSlots[i].getItem().isEmpty()) continue;
                for (int j = 1; j < this.processingOutputSlots.length; ++j) {
                    ItemStack nextItem = this.processingOutputSlots[(i + j) % this.processingOutputSlots.length].getItem();
                    if (nextItem.isEmpty()) continue;
                    newOutputs[i] = nextItem;
                    continue block0;
                }
            }
            for (i = 0; i < newOutputs.length; ++i) {
                this.processingOutputSlots[i].set(newOutputs[i]);
            }
        }
    }

    public boolean canCycleProcessingOutputs() {
        return this.mode == EncodingMode.PROCESSING && Arrays.stream(this.processingOutputSlots).filter(s -> !s.getItem().isEmpty()).count() > 1L;
    }

    public List<RecipeHolder<StonecutterRecipe>> getStonecuttingRecipes() {
        return this.stonecuttingRecipes;
    }
}

