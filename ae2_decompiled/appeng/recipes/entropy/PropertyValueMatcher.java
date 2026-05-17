/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.level.block.state.StateHolder
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.recipes.entropy;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public sealed interface PropertyValueMatcher {
    public static final Codec<PropertyValueMatcher> CODEC = new Codec<PropertyValueMatcher>(){

        public <T> DataResult<Pair<PropertyValueMatcher, T>> decode(DynamicOps<T> ops, T input) {
            DataResult singleValueResult = Codec.STRING.decode(ops, input).map(pair -> pair.mapFirst(value -> new SingleValue((String)value)));
            if (singleValueResult.error().isEmpty()) {
                return singleValueResult;
            }
            DataResult listValueResult = Codec.STRING.listOf().decode(ops, input).map(pair -> pair.mapFirst(value -> new MultiValue((List<String>)value)));
            if (listValueResult.error().isEmpty()) {
                return listValueResult;
            }
            DataResult rangeValueResult = Range.CODEC.decode(ops, input).map(pair -> pair.mapFirst(value -> value));
            if (rangeValueResult.error().isEmpty()) {
                return rangeValueResult;
            }
            return DataResult.error(() -> "Property values need to be strings, list of strings, or objects with min/max properties");
        }

        public <T> DataResult<T> encode(PropertyValueMatcher input, DynamicOps<T> ops, T prefix) {
            if (input instanceof SingleValue) {
                SingleValue singleValue = (SingleValue)input;
                return Codec.STRING.encode((Object)singleValue.value(), ops, prefix);
            }
            if (input instanceof MultiValue) {
                MultiValue multiValue = (MultiValue)input;
                return Codec.STRING.listOf().encode(multiValue.values(), ops, prefix);
            }
            if (input instanceof Range) {
                Range range = (Range)input;
                return Range.CODEC.encode((Object)range, ops, prefix);
            }
            throw new IllegalStateException("This cannot happen");
        }
    };
    public static final StreamCodec<FriendlyByteBuf, PropertyValueMatcher> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, PropertyValueMatcher>(){

        public PropertyValueMatcher decode(FriendlyByteBuf buffer) {
            byte type = buffer.readByte();
            return switch (type) {
                case 0 -> new SingleValue(buffer.readUtf());
                case 1 -> new MultiValue(buffer.readList(FriendlyByteBuf::readUtf));
                case 2 -> new Range(buffer.readUtf(), buffer.readUtf());
                default -> throw new IllegalStateException("Invalid property value matcher type: " + type);
            };
        }

        public void encode(FriendlyByteBuf buffer, PropertyValueMatcher matcher) {
            matcher.toNetwork(buffer);
        }
    };
    public static final Codec<Map<String, PropertyValueMatcher>> MAP_CODEC = Codec.unboundedMap((Codec)Codec.STRING, CODEC);

    public void toNetwork(FriendlyByteBuf var1);

    public void validate(Property<? extends Comparable<?>> var1);

    public <T extends Comparable<T>> boolean matches(Property<T> var1, StateHolder<?, ?> var2);

    public record Range(String min, String max) implements PropertyValueMatcher
    {
        static Codec<Range> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)Codec.STRING.fieldOf("min").forGetter(Range::min), (App)Codec.STRING.fieldOf("max").forGetter(Range::max)).apply((Applicative)builder, Range::new));

        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(2);
            buffer.writeUtf(this.min);
            buffer.writeUtf(this.max);
        }

        @Override
        public void validate(Property<? extends Comparable<?>> property) {
            if (property.getValue(this.min).isEmpty()) {
                throw new IllegalStateException("Property " + property.getName() + " does not have value '" + this.min + "'");
            }
            if (property.getValue(this.max).isEmpty()) {
                throw new IllegalStateException("Property " + property.getName() + " does not have value '" + this.max + "'");
            }
        }

        @Override
        public <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state) {
            Comparable minValue = (Comparable)property.getValue(this.min).orElseThrow();
            Comparable maxValue = (Comparable)property.getValue(this.max).orElseThrow();
            Comparable value = state.getValue(property);
            return value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0;
        }
    }

    public record MultiValue(List<String> values) implements PropertyValueMatcher
    {
        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(1);
            buffer.writeCollection(this.values, FriendlyByteBuf::writeUtf);
        }

        @Override
        public void validate(Property<? extends Comparable<?>> property) {
            for (String value : this.values) {
                if (!property.getValue(value).isEmpty()) continue;
                throw new IllegalStateException("Property " + property.getName() + " does not have value '" + value + "'");
            }
        }

        @Override
        public <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state) {
            String currentValue = property.getName(state.getValue(property));
            Iterator<String> iterator = this.values.iterator();
            if (iterator.hasNext()) {
                String value = iterator.next();
                return value.equals(currentValue);
            }
            return true;
        }
    }

    public record SingleValue(String value) implements PropertyValueMatcher
    {
        @Override
        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeByte(0);
            buffer.writeUtf(this.value);
        }

        @Override
        public void validate(Property<? extends Comparable<?>> property) {
            if (property.getValue(this.value).isEmpty()) {
                throw new IllegalStateException("Property " + property.getName() + " does not have value '" + this.value + "'");
            }
        }

        @Override
        public <T extends Comparable<T>> boolean matches(Property<T> property, StateHolder<?, ?> state) {
            String currentValue = property.getName(state.getValue(property));
            return this.value.equals(currentValue);
        }
    }
}

