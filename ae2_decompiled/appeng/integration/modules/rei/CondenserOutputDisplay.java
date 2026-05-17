/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.util.EntryIngredients
 *  me.shedaniel.rei.api.common.util.EntryStacks
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.rei;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.core.definitions.AEItems;
import appeng.integration.modules.rei.CondenserCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;

public class CondenserOutputDisplay
implements Display {
    private final CondenserOutput type;
    private final List<EntryIngredient> output;
    private final List<EntryStack<ItemStack>> viableStorageComponents;

    public CondenserOutputDisplay(CondenserOutput output) {
        this.type = output;
        this.output = Collections.singletonList(EntryIngredients.of((ItemStack)CondenserOutputDisplay.getOutput(this.type)));
        this.viableStorageComponents = this.getViableStorageComponents(output);
    }

    public List<EntryIngredient> getInputEntries() {
        return Collections.emptyList();
    }

    public List<EntryIngredient> getOutputEntries() {
        return this.output;
    }

    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CondenserCategory.ID;
    }

    public CondenserOutput getType() {
        return this.type;
    }

    private static ItemStack getOutput(CondenserOutput recipe) {
        return switch (recipe) {
            case CondenserOutput.MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case CondenserOutput.SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    private List<EntryStack<ItemStack>> getViableStorageComponents(CondenserOutput condenserOutput) {
        ArrayList<EntryStack<ItemStack>> viableComponents = new ArrayList<EntryStack<ItemStack>>();
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_1K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_4K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_16K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_64K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_256K.stack());
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<EntryStack<ItemStack>> viableComponents, ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent)itemStack.getItem();
        int storage = comp.getBytes(itemStack) * 8;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add((EntryStack<ItemStack>)EntryStacks.of((ItemStack)itemStack));
        }
    }

    public List<EntryStack<ItemStack>> getViableStorageComponents() {
        return this.viableStorageComponents;
    }
}

