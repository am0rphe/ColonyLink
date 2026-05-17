/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.StateDefinition
 *  net.minecraft.world.level.block.state.StateHolder
 *  net.minecraft.world.level.block.state.properties.Property
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.recipes.entropy;

import appeng.recipes.entropy.PropertyValueMatcher;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PropertyUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PropertyUtils.class);

    private PropertyUtils() {
    }

    static Property<?> getRequiredProperty(StateDefinition<?, ?> stateDefinition, String name) {
        Objects.requireNonNull(stateDefinition, "stateDefinition must not be null");
        Property property = stateDefinition.getProperty(name);
        if (property == null) {
            throw new IllegalArgumentException("Unknown property: " + name + " on " + String.valueOf(stateDefinition.getOwner()));
        }
        return property;
    }

    static <T extends Comparable<T>> T getRequiredPropertyValue(Property<T> property, String name) {
        Objects.requireNonNull(property, "property must be not null");
        return (T)((Comparable)property.getValue(name).orElseThrow(() -> new IllegalArgumentException("Invalid value '" + name + "' for property " + property.getName())));
    }

    static void validatePropertyMatchers(StateDefinition<?, ?> stateDefinition, Map<String, PropertyValueMatcher> properties) {
        for (Map.Entry<String, PropertyValueMatcher> entry : properties.entrySet()) {
            Property property = stateDefinition.getProperty(entry.getKey());
            if (property == null) {
                throw new IllegalArgumentException("State definition " + String.valueOf(stateDefinition) + " does not have property '" + entry.getKey() + "'");
            }
            entry.getValue().validate(property);
        }
    }

    public static <SH extends StateHolder<?, SH>> boolean doPropertiesMatch(StateDefinition<?, SH> stateDefinition, SH state, Map<String, PropertyValueMatcher> properties) {
        for (Map.Entry<String, PropertyValueMatcher> entry : properties.entrySet()) {
            Property property = stateDefinition.getProperty(entry.getKey());
            if (property == null) {
                throw new IllegalArgumentException("State definition " + String.valueOf(stateDefinition) + " does not have property '" + entry.getKey() + "'");
            }
            if (entry.getValue().matches(property, state)) continue;
            return false;
        }
        return true;
    }

    static <SH extends StateHolder<?, SH>> SH applyProperties(StateDefinition<?, SH> stateDefinition, SH state, Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Property property = stateDefinition.getProperty(entry.getKey());
            if (property != null) {
                state = PropertyUtils.applyProperty(state, property, entry.getValue());
                continue;
            }
            LOG.warn("Cannot apply property {} since {} does not have that property", (Object)entry.getKey(), stateDefinition);
        }
        return state;
    }

    static <T extends Comparable<T>, SH extends StateHolder<?, SH>> SH applyProperty(SH state, Property<T> property, String value) {
        Optional parsedValue = property.getValue(value);
        return (SH)parsedValue.map(t -> (StateHolder)state.trySetValue(property, t)).orElse(state);
    }
}

