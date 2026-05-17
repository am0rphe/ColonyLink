/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.ListBuilder
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.stacks;

import appeng.api.stacks.GenericStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

class GenericStackListCodec
implements Codec<List<GenericStack>> {
    private final Codec<GenericStack> innerCodec;

    public GenericStackListCodec(Codec<GenericStack> innerCodec) {
        this.innerCodec = innerCodec;
    }

    public <T> DataResult<T> encode(List<@Nullable GenericStack> input, DynamicOps<T> ops, T prefix) {
        ListBuilder builder = ops.listBuilder();
        for (GenericStack genericStack : input) {
            if (genericStack == null) {
                builder.add(ops.emptyMap());
                continue;
            }
            builder.add(this.innerCodec.encodeStart(ops, (Object)genericStack));
        }
        return builder.build(prefix);
    }

    public <T> DataResult<Pair<List<@Nullable GenericStack>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            ArrayList elements = new ArrayList();
            Stream.Builder failed = Stream.builder();
            MutableObject result = new MutableObject((Object)DataResult.success((Object)Unit.INSTANCE, (Lifecycle)Lifecycle.stable()));
            stream.accept(t -> {
                if (ops.emptyMap().equals(t)) {
                    elements.add(null);
                } else {
                    DataResult element = this.innerCodec.decode(ops, t);
                    element.error().ifPresent(e -> failed.add(t));
                    result.setValue((Object)((DataResult)result.getValue()).apply2stable((r, v) -> {
                        elements.add((GenericStack)v.getFirst());
                        return r;
                    }, element));
                }
            });
            Object errors = ops.createList(failed.build());
            Pair pair = Pair.of(Collections.unmodifiableList(elements), (Object)errors);
            return ((DataResult)result.getValue()).map(unit -> pair).setPartial((Object)pair);
        });
    }
}

