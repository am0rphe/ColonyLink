/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.handler.codec.DecoderException
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 */
package appeng.recipes.transform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class TransformCircumstance {
    public static final TransformCircumstance EXPLOSION = new TransformCircumstance("explosion");
    private static final MapCodec<TransformCircumstance> EXPLOSION_CODEC = MapCodec.unit((Object)EXPLOSION);
    private static final MapCodec<FluidType> FLUID_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)TagKey.codec((ResourceKey)Registries.FLUID).fieldOf("tag").forGetter(FluidType::getFluidTag)).apply((Applicative)builder, FluidType::new));
    public static final Codec<TransformCircumstance> CODEC = Codec.STRING.dispatch(t -> t.type, type -> switch (type) {
        case "explosion" -> EXPLOSION_CODEC;
        case "fluid" -> FLUID_CODEC;
        default -> throw new IllegalStateException("Invalid type: " + type);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, TransformCircumstance> STREAM_CODEC = StreamCodec.ofMember(TransformCircumstance::toNetwork, TransformCircumstance::fromNetwork);
    private final String type;

    public TransformCircumstance(String type) {
        this.type = type;
    }

    static TransformCircumstance fromJson(JsonObject obj) {
        String type = obj.get("type").getAsString();
        if (type.equals("explosion")) {
            return TransformCircumstance.explosion();
        }
        if (type.equals("fluid")) {
            return TransformCircumstance.fluid((TagKey<Fluid>)TagKey.create((ResourceKey)Registries.FLUID, (ResourceLocation)ResourceLocation.parse((String)obj.get("tag").getAsString())));
        }
        throw new JsonParseException("Invalid transform recipe type " + type);
    }

    static TransformCircumstance fromNetwork(FriendlyByteBuf buf) {
        String type = buf.readUtf();
        if (type.equals("explosion")) {
            return TransformCircumstance.explosion();
        }
        if (type.equals("fluid")) {
            return TransformCircumstance.fluid((TagKey<Fluid>)TagKey.create((ResourceKey)Registries.FLUID, (ResourceLocation)buf.readResourceLocation()));
        }
        throw new DecoderException("Invalid transform recipe type " + type);
    }

    public static TransformCircumstance fluid(TagKey<Fluid> tag) {
        return new FluidType(tag);
    }

    public static TransformCircumstance explosion() {
        return EXPLOSION;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", this.type);
        return obj;
    }

    void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.type);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof TransformCircumstance)) return false;
        TransformCircumstance other = (TransformCircumstance)obj;
        if (!this.type.equals(other.type)) return false;
        return true;
    }

    public int hashCode() {
        return this.type.hashCode();
    }

    public boolean isExplosion() {
        return this.type.equals("explosion");
    }

    public boolean isFluid() {
        return false;
    }

    public boolean isFluidTag(TagKey<Fluid> tag) {
        return false;
    }

    public boolean isFluid(FluidState state) {
        return false;
    }

    public boolean isFluid(Fluid fluid) {
        return false;
    }

    public List<Fluid> getFluidsForRendering() {
        return List.of();
    }

    private static class FluidType
    extends TransformCircumstance {
        public final TagKey<Fluid> fluidTag;

        public FluidType(TagKey<Fluid> fluidTag) {
            super("fluid");
            this.fluidTag = fluidTag;
        }

        public TagKey<Fluid> getFluidTag() {
            return this.fluidTag;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FluidType)) return false;
            FluidType other = (FluidType)obj;
            if (!Objects.equals(this.fluidTag, other.fluidTag)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.fluidTag);
        }

        @Override
        public JsonObject toJson() {
            JsonObject obj = super.toJson();
            obj.addProperty("tag", this.fluidTag.location().toString());
            return obj;
        }

        @Override
        void toNetwork(FriendlyByteBuf buf) {
            super.toNetwork(buf);
            buf.writeResourceLocation(this.fluidTag.location());
        }

        @Override
        public boolean isFluid() {
            return true;
        }

        @Override
        public boolean isFluid(Fluid fluid) {
            return fluid.is(this.fluidTag);
        }

        @Override
        public boolean isFluidTag(TagKey<Fluid> tag) {
            return this.fluidTag.equals(tag);
        }

        @Override
        public boolean isFluid(FluidState state) {
            return state.is(this.fluidTag);
        }

        @Override
        public List<Fluid> getFluidsForRendering() {
            return BuiltInRegistries.FLUID.getTag(this.fluidTag).map(t -> t.stream().map(Holder::value).toList()).orElse(List.of());
        }
    }
}

