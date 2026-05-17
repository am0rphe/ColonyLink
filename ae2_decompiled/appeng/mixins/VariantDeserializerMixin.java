/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.block.model.Variant
 *  net.minecraft.client.renderer.block.model.Variant$Deserializer
 *  net.minecraft.util.GsonHelper
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins;

import appeng.hooks.BlockstateDefinitionHook;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Variant.Deserializer.class})
public class VariantDeserializerMixin {
    @Inject(method={"deserialize"}, at={@At(value="RETURN")}, cancellable=true)
    public void addAdditionalRotationOptions(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<Variant> cri) {
        Variant variant = (Variant)cri.getReturnValue();
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("ae2:z")) {
            int xRot = GsonHelper.getAsInt((JsonObject)jsonObject, (String)"x", (int)0);
            int yRot = GsonHelper.getAsInt((JsonObject)jsonObject, (String)"y", (int)0);
            int zRot = GsonHelper.getAsInt((JsonObject)jsonObject, (String)"ae2:z", (int)0);
            cri.setReturnValue((Object)BlockstateDefinitionHook.rotateVariant(variant, xRot, yRot, zRot));
        }
    }
}

