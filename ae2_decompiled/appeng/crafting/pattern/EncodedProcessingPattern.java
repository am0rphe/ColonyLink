/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package appeng.crafting.pattern;

import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EncodedProcessingPattern(List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs) {
    public static final Codec<EncodedProcessingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC.fieldOf("sparseInputs").forGetter(EncodedProcessingPattern::sparseInputs), (App)GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC.fieldOf("sparseOutputs").forGetter(EncodedProcessingPattern::sparseOutputs)).apply((Applicative)builder, EncodedProcessingPattern::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedProcessingPattern> STREAM_CODEC = StreamCodec.composite((StreamCodec)GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()), EncodedProcessingPattern::sparseInputs, (StreamCodec)GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()), EncodedProcessingPattern::sparseOutputs, EncodedProcessingPattern::new);

    public EncodedProcessingPattern {
        sparseInputs = Collections.unmodifiableList(sparseInputs);
        sparseOutputs = Collections.unmodifiableList(sparseOutputs);
    }

    public boolean containsMissingContent() {
        return Stream.concat(this.sparseInputs.stream(), this.sparseOutputs.stream()).anyMatch(stack -> stack != null && AEItems.MISSING_CONTENT.is(stack.what()));
    }
}

