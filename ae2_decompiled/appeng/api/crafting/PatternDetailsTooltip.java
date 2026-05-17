/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.GuiText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class PatternDetailsTooltip {
    public static final Component OUTPUT_TEXT_CRAFTS = GuiText.Crafts.text();
    public static final Component OUTPUT_TEXT_PRODUCES = GuiText.Produces.text();
    private Component outputMethod;
    private final List<Property> additionalProperties = new ArrayList<Property>();
    private final List<GenericStack> inputs = new ArrayList<GenericStack>();
    private final List<GenericStack> outputs = new ArrayList<GenericStack>();

    public PatternDetailsTooltip(Component outputMethod) {
        this.setOutputMethod(outputMethod);
    }

    public void setOutputMethod(Component outputMethod) {
        this.outputMethod = Objects.requireNonNull(outputMethod, "outputMethod");
    }

    public List<Property> getProperties() {
        return this.additionalProperties;
    }

    public List<GenericStack> getInputs() {
        return this.inputs;
    }

    public List<GenericStack> getOutputs() {
        return this.outputs;
    }

    public void addInput(AEKey what, long amount) {
        this.inputs.add(new GenericStack(what, amount));
    }

    public void addInput(GenericStack stack) {
        this.inputs.add(new GenericStack(stack.what(), stack.amount()));
    }

    public void addOutput(AEKey what, long amount) {
        this.outputs.add(new GenericStack(what, amount));
    }

    public void addOutput(GenericStack stack) {
        this.outputs.add(new GenericStack(stack.what(), stack.amount()));
    }

    public void addProperty(Component name, Component value) {
        this.additionalProperties.add(new Property(name, value));
    }

    public void addProperty(Component description) {
        this.additionalProperties.add(new Property(description, null));
    }

    public void addInputsAndOutputs(IPatternDetails details) {
        for (IPatternDetails.IInput input : details.getInputs()) {
            if (input == null) continue;
            this.addInput(input.getPossibleInputs()[0].what(), input.getPossibleInputs()[0].amount() * input.getMultiplier());
        }
        for (GenericStack output : details.getOutputs()) {
            if (output == null) continue;
            this.addOutput(output.what(), output.amount());
        }
    }

    public Component getOutputMethod() {
        return this.outputMethod;
    }

    public record Property(Component name, @Nullable Component value) {
    }
}

