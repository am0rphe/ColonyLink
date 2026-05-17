/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.encoding;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.parts.encoding.EncodingMode;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.AEItemDefinitionFilter;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PatternEncodingLogic
implements InternalInventoryHost {
    private final IPatternTerminalLogicHost host;
    private static final int MAX_INPUT_SLOTS = Math.max(9, 81);
    private static final int MAX_OUTPUT_SLOTS = 27;
    private final ConfigInventory encodedInputInv = ConfigInventory.configStacks(MAX_INPUT_SLOTS).changeListener(this::onEncodedInputChanged).allowOverstacking(true).build();
    private final ConfigInventory encodedOutputInv = ConfigInventory.configStacks(27).changeListener(this::onEncodedOutputChanged).allowOverstacking(true).build();
    private final AppEngInternalInventory blankPatternInv = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory encodedPatternInv = new AppEngInternalInventory(this, 1);
    private EncodingMode mode = EncodingMode.CRAFTING;
    private boolean substitute = false;
    private boolean substituteFluids = true;
    private boolean isLoading = false;
    @Nullable
    private ResourceLocation stonecuttingRecipeId;

    public PatternEncodingLogic(IPatternTerminalLogicHost host) {
        this.host = host;
        this.blankPatternInv.setFilter(new AEItemDefinitionFilter(AEItems.BLANK_PATTERN));
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == this.encodedPatternInv) {
            this.loadEncodedPattern(this.encodedPatternInv.getStackInSlot(0));
        }
        this.saveChanges();
    }

    public void saveChanges() {
        if (!this.isLoading) {
            this.host.markForSave();
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    @Override
    public boolean isClientSide() {
        return this.host.getLevel().isClientSide();
    }

    private void onEncodedInputChanged() {
        this.fixCraftingRecipes();
        this.saveChanges();
    }

    private void onEncodedOutputChanged() {
        this.saveChanges();
    }

    private void loadEncodedPattern(ItemStack pattern) {
        if (pattern.isEmpty()) {
            return;
        }
        IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, this.host.getLevel());
        if (details instanceof AECraftingPattern) {
            AECraftingPattern craftingPattern = (AECraftingPattern)details;
            this.loadCraftingPattern(craftingPattern);
        } else if (details instanceof AEProcessingPattern) {
            AEProcessingPattern processingPattern = (AEProcessingPattern)details;
            this.loadProcessingPattern(processingPattern);
        } else if (details instanceof AESmithingTablePattern) {
            AESmithingTablePattern smithingTablePattern = (AESmithingTablePattern)details;
            this.loadSmithingTablePattern(smithingTablePattern);
        } else if (details instanceof AEStonecuttingPattern) {
            AEStonecuttingPattern stonecuttingPattern = (AEStonecuttingPattern)details;
            this.loadStonecuttingPattern(stonecuttingPattern);
        }
        this.saveChanges();
    }

    private void loadCraftingPattern(AECraftingPattern pattern) {
        this.setMode(EncodingMode.CRAFTING);
        this.substitute = pattern.canSubstitute();
        this.substituteFluids = pattern.canSubstituteFluids();
        PatternEncodingLogic.fillInventoryFromSparseStacks(this.encodedInputInv, pattern.getSparseInputs());
        PatternEncodingLogic.fillInventoryFromSparseStacks(this.encodedOutputInv, pattern.getSparseOutputs());
    }

    private void loadProcessingPattern(AEProcessingPattern pattern) {
        this.setMode(EncodingMode.PROCESSING);
        PatternEncodingLogic.fillInventoryFromSparseStacks(this.encodedInputInv, pattern.getSparseInputs());
        PatternEncodingLogic.fillInventoryFromSparseStacks(this.encodedOutputInv, pattern.getSparseOutputs());
    }

    private void loadSmithingTablePattern(AESmithingTablePattern pattern) {
        this.setMode(EncodingMode.SMITHING_TABLE);
        this.substitute = pattern.canSubstitute();
        this.encodedInputInv.clear();
        this.encodedInputInv.setStack(0, new GenericStack(pattern.getTemplate(), 1L));
        this.encodedInputInv.setStack(1, new GenericStack(pattern.getBase(), 1L));
        this.encodedInputInv.setStack(2, new GenericStack(pattern.getAddition(), 1L));
        this.encodedOutputInv.clear();
    }

    private void loadStonecuttingPattern(AEStonecuttingPattern pattern) {
        this.setMode(EncodingMode.STONECUTTING);
        this.stonecuttingRecipeId = pattern.getRecipeId();
        this.substitute = pattern.canSubstitute;
        this.encodedInputInv.clear();
        this.encodedInputInv.setStack(0, new GenericStack(pattern.getInput(), 1L));
        this.encodedOutputInv.clear();
    }

    private static void fillInventoryFromSparseStacks(ConfigInventory inv, List<GenericStack> stacks) {
        inv.beginBatch();
        try {
            for (int i = 0; i < inv.size(); ++i) {
                inv.setStack(i, i < stacks.size() ? stacks.get(i) : null);
            }
        }
        finally {
            inv.endBatch();
        }
    }

    public EncodingMode getMode() {
        return this.mode;
    }

    public void setMode(EncodingMode mode) {
        this.mode = mode;
        this.fixCraftingRecipes();
        this.saveChanges();
    }

    public boolean isSubstitution() {
        return this.substitute;
    }

    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
        this.saveChanges();
    }

    public boolean isFluidSubstitution() {
        return this.substituteFluids;
    }

    public void setFluidSubstitution(boolean canSubstitute) {
        this.substituteFluids = canSubstitute;
        this.saveChanges();
    }

    @Nullable
    public ResourceLocation getStonecuttingRecipeId() {
        return this.stonecuttingRecipeId;
    }

    public void setStonecuttingRecipeId(ResourceLocation stonecuttingRecipeId) {
        this.stonecuttingRecipeId = stonecuttingRecipeId;
        this.saveChanges();
    }

    public ConfigInventory getEncodedInputInv() {
        return this.encodedInputInv;
    }

    public ConfigInventory getEncodedOutputInv() {
        return this.encodedOutputInv;
    }

    public InternalInventory getBlankPatternInv() {
        return this.blankPatternInv;
    }

    public InternalInventory getEncodedPatternInv() {
        return this.encodedPatternInv;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.isLoading = true;
        try {
            try {
                this.mode = EncodingMode.valueOf(data.getString("mode"));
            }
            catch (IllegalArgumentException ignored) {
                this.mode = EncodingMode.CRAFTING;
            }
            this.setSubstitution(data.getBoolean("substitute"));
            this.setFluidSubstitution(data.getBoolean("substituteFluids"));
            this.stonecuttingRecipeId = data.contains("stonecuttingRecipeId", 8) ? ResourceLocation.parse((String)data.getString("stonecuttingRecipeId")) : null;
            this.blankPatternInv.readFromNBT(data, "blankPattern", registries);
            this.encodedPatternInv.readFromNBT(data, "encodedPattern", registries);
            this.encodedInputInv.readFromChildTag(data, "encodedInputs", registries);
            this.encodedOutputInv.readFromChildTag(data, "encodedOutputs", registries);
        }
        finally {
            this.isLoading = false;
        }
    }

    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        data.putString("mode", this.mode.name());
        data.putBoolean("substitute", this.substitute);
        data.putBoolean("substituteFluids", this.substituteFluids);
        if (this.stonecuttingRecipeId != null) {
            data.putString("stonecuttingRecipeId", this.stonecuttingRecipeId.toString());
        }
        this.blankPatternInv.writeToNBT(data, "blankPattern", registries);
        this.encodedPatternInv.writeToNBT(data, "encodedPattern", registries);
        this.encodedInputInv.writeToChildTag(data, "encodedInputs", registries);
        this.encodedOutputInv.writeToChildTag(data, "encodedOutputs", registries);
    }

    private void fixCraftingRecipes() {
        if (this.host.getLevel() == null || this.host.getLevel().isClientSide()) {
            return;
        }
        if (this.getMode() != EncodingMode.PROCESSING) {
            ConfigInventory craftingGrid = this.getEncodedInputInv();
            for (int slot = 0; slot < craftingGrid.size(); ++slot) {
                GenericStack stack = craftingGrid.getStack(slot);
                if (stack == null) continue;
                if (!AEItemKey.is(stack.what())) {
                    craftingGrid.setStack(slot, null);
                    continue;
                }
                if (stack.amount() == 1L) continue;
                craftingGrid.setStack(slot, new GenericStack(stack.what(), 1L));
            }
        }
    }
}

