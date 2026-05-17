/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentSerialization
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.menu.guisync;

import appeng.api.stacks.GenericStack;
import appeng.menu.guisync.PacketWritable;
import com.google.common.base.Preconditions;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public abstract class SynchronizedField<T> {
    private final Object source;
    protected final MethodHandle getter;
    protected final MethodHandle setter;
    protected T clientVersion = null;

    private SynchronizedField(Object source, Field field) {
        this.source = source;
        field.setAccessible(true);
        try {
            this.getter = MethodHandles.publicLookup().unreflectGetter(field);
            this.setter = MethodHandles.publicLookup().unreflectSetter(field);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get accessor for field " + String.valueOf(field) + ". Did you forget to make it public?");
        }
    }

    private T getCurrentValue() {
        try {
            return (T)this.getter.invoke(this.source);
        }
        catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public boolean hasChanges() {
        return !Objects.equals(this.getCurrentValue(), this.clientVersion);
    }

    public final void write(RegistryFriendlyByteBuf data) {
        T currentValue = this.getCurrentValue();
        this.clientVersion = currentValue;
        this.writeValue(data, currentValue);
    }

    public final void read(RegistryFriendlyByteBuf data) {
        T value = this.readValue(data);
        try {
            this.setter.invoke(this.source, value);
        }
        catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    protected abstract void writeValue(RegistryFriendlyByteBuf var1, T var2);

    protected abstract T readValue(RegistryFriendlyByteBuf var1);

    public static SynchronizedField<?> create(Object source, Field field) {
        Class<Object> fieldType = field.getType();
        if (PacketWritable.class.isAssignableFrom(fieldType)) {
            return new CustomField(source, field);
        }
        if (fieldType.isAssignableFrom(Component.class)) {
            return new TextComponentField(source, field);
        }
        if (fieldType.isAssignableFrom(GenericStack.class)) {
            return new GenericStackField(source, field);
        }
        if (fieldType.isAssignableFrom(ResourceLocation.class)) {
            return new ResourceLocationField(source, field);
        }
        if (fieldType == String.class) {
            return new StringField(source, field);
        }
        if (fieldType == Integer.TYPE || fieldType == Integer.class) {
            return new IntegerField(source, field);
        }
        if (fieldType == Long.TYPE || fieldType == Long.class) {
            return new LongField(source, field);
        }
        if (fieldType == Double.TYPE) {
            return new DoubleField(source, field);
        }
        if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
            return new BooleanField(source, field);
        }
        if (fieldType.isEnum()) {
            return SynchronizedField.createEnumField(source, field, fieldType.asSubclass(Enum.class));
        }
        throw new IllegalArgumentException("Cannot synchronize field " + String.valueOf(field));
    }

    private static <T extends Enum<T>> EnumField<T> createEnumField(Object source, Field field, Class<T> fieldType) {
        return new EnumField(source, field, (Enum[])fieldType.getEnumConstants());
    }

    private static class CustomField
    extends SynchronizedField<Object> {
        private static final Map<Class<?>, Function<RegistryFriendlyByteBuf, Object>> factories = new HashMap();
        private final Class<?> fieldType;

        private CustomField(Object source, Field field) {
            super(source, field);
            this.fieldType = field.getType();
            Preconditions.checkArgument((boolean)PacketWritable.class.isAssignableFrom(this.fieldType));
            if (!this.fieldType.isRecord()) {
                throw new RuntimeException("Use records to synchronize custom class on " + String.valueOf(field) + " to enable easier equals comparisons");
            }
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, Object value) {
            ((PacketWritable)value).writeToPacket(data);
        }

        @Override
        protected Object readValue(RegistryFriendlyByteBuf data) {
            Function factory = factories.computeIfAbsent(this.fieldType, CustomField::getFactory);
            return factory.apply(data);
        }

        private static Function<RegistryFriendlyByteBuf, Object> getFactory(Class<?> clazz) {
            try {
                Constructor<?> constructor = clazz.getConstructor(RegistryFriendlyByteBuf.class);
                return buffer -> {
                    try {
                        return constructor.newInstance(buffer);
                    }
                    catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        throw new RuntimeException("Failed to deserialize " + String.valueOf(clazz), e);
                    }
                };
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException("No constructor taking RegistryFriendlyByteBuf on " + String.valueOf(clazz));
            }
        }
    }

    private static class TextComponentField
    extends SynchronizedField<Component> {
        private TextComponentField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, Component value) {
            if (value == null) {
                data.writeBoolean(false);
            } else {
                data.writeBoolean(true);
                ComponentSerialization.TRUSTED_STREAM_CODEC.encode((Object)data, (Object)value);
            }
        }

        @Override
        protected Component readValue(RegistryFriendlyByteBuf data) {
            if (data.readBoolean()) {
                return (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode((Object)data);
            }
            return null;
        }
    }

    private static class GenericStackField
    extends SynchronizedField<GenericStack> {
        private GenericStackField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, GenericStack value) {
            GenericStack.writeBuffer(value, data);
        }

        @Override
        protected GenericStack readValue(RegistryFriendlyByteBuf data) {
            return GenericStack.readBuffer(data);
        }
    }

    private static class ResourceLocationField
    extends SynchronizedField<ResourceLocation> {
        private ResourceLocationField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, ResourceLocation value) {
            if (value == null) {
                data.writeBoolean(false);
            } else {
                data.writeBoolean(true);
                data.writeResourceLocation(value);
            }
        }

        @Override
        protected ResourceLocation readValue(RegistryFriendlyByteBuf data) {
            if (data.readBoolean()) {
                return data.readResourceLocation();
            }
            return null;
        }
    }

    private static class StringField
    extends SynchronizedField<String> {
        private StringField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, String value) {
            data.writeUtf(value);
        }

        @Override
        protected String readValue(RegistryFriendlyByteBuf data) {
            return data.readUtf();
        }
    }

    private static class IntegerField
    extends SynchronizedField<Integer> {
        private IntegerField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, Integer value) {
            data.writeInt(value.intValue());
        }

        @Override
        protected Integer readValue(RegistryFriendlyByteBuf data) {
            return data.readInt();
        }
    }

    private static class LongField
    extends SynchronizedField<Long> {
        private LongField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, Long value) {
            data.writeLong(value.longValue());
        }

        @Override
        protected Long readValue(RegistryFriendlyByteBuf data) {
            return data.readLong();
        }
    }

    private static class DoubleField
    extends SynchronizedField<Double> {
        private DoubleField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, Double value) {
            data.writeDouble(value.doubleValue());
        }

        @Override
        protected Double readValue(RegistryFriendlyByteBuf data) {
            return data.readDouble();
        }
    }

    private static class BooleanField
    extends SynchronizedField<Boolean> {
        private BooleanField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, Boolean value) {
            data.writeBoolean(value.booleanValue());
        }

        @Override
        protected Boolean readValue(RegistryFriendlyByteBuf data) {
            return data.readBoolean();
        }
    }

    private static class EnumField<T extends Enum<T>>
    extends SynchronizedField<T> {
        private final T[] values;

        private EnumField(Object source, Field field, T[] values) {
            super(source, field);
            this.values = values;
        }

        @Override
        protected void writeValue(RegistryFriendlyByteBuf data, T value) {
            if (value == null) {
                data.writeShort(-1);
            } else {
                data.writeShort((int)((short)((Enum)value).ordinal()));
            }
        }

        @Override
        protected T readValue(RegistryFriendlyByteBuf data) {
            short ordinal = data.readShort();
            if (ordinal == -1) {
                return null;
            }
            return this.values[ordinal];
        }
    }
}

