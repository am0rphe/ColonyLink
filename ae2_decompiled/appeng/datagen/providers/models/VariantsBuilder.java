/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.neoforge.client.model.generators.BlockModelBuilder
 *  net.neoforged.neoforge.client.model.generators.IGeneratedBlockState
 */
package appeng.datagen.providers.models;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.IGeneratedBlockState;

class VariantsBuilder
implements IGeneratedBlockState {
    private final Block block;
    private final JsonObject blockStateDef = new JsonObject();
    private final JsonObject variantsDef;

    public VariantsBuilder(Block block) {
        this.block = block;
        this.variantsDef = new JsonObject();
        this.blockStateDef.add("variants", (JsonElement)this.variantsDef);
    }

    public VariantsBuilder generateRotations(BlockModelBuilder model) {
        this.generateRotations(this.block.defaultBlockState(), model);
        return this;
    }

    public VariantsBuilder generateRotations(BlockState baseState, BlockModelBuilder model) {
        IOrientationStrategy strategy = IOrientationStrategy.get(baseState);
        strategy.getAllStates(baseState).forEachOrdered(blockState -> {
            StringBuilder stateText = new StringBuilder();
            for (Property<?> property : strategy.getProperties()) {
                if (stateText.length() > 0) {
                    stateText.append(',');
                }
                VariantsBuilder.appendStateProperty(blockState, property, stateText);
            }
            BlockOrientation modelRotation = BlockOrientation.get(strategy, blockState);
            int rotationX = modelRotation.getAngleX();
            int rotationY = modelRotation.getAngleY();
            int rotationZ = modelRotation.getAngleZ();
            JsonObject modelObj = new JsonObject();
            modelObj.addProperty("model", model.getLocation().toString());
            if (rotationX != 0) {
                modelObj.addProperty("x", (Number)rotationX);
            }
            if (rotationY != 0) {
                modelObj.addProperty("x", (Number)rotationY);
            }
            if (rotationZ != 0) {
                modelObj.addProperty("ae2:z", (Number)rotationZ);
            }
            this.variantsDef.add(stateText.toString(), (JsonElement)modelObj);
        });
        return this;
    }

    public JsonObject toJson() {
        return this.blockStateDef;
    }

    private static <T extends Comparable<T>> void appendStateProperty(BlockState blockState, Property<T> property, StringBuilder stateText) {
        stateText.append(property.getName());
        stateText.append('=');
        stateText.append(property.getName(blockState.getValue(property)));
    }
}

