/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.client.event.ModelEvent$RegisterAdditional
 */
package appeng.init.client;

import appeng.api.parts.PartModelsInternal;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.tesr.CrankRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ModelEvent;

@OnlyIn(value=Dist.CLIENT)
public class InitAdditionalModels {
    public static void init(ModelEvent.RegisterAdditional event) {
        event.register(MolecularAssemblerRenderer.LIGHTS_MODEL);
        event.register(CrankRenderer.BASE_MODEL);
        event.register(CrankRenderer.HANDLE_MODEL);
        PartModelsInternal.freeze();
        PartModelsInternal.getModels().stream().map(ModelResourceLocation::standalone).forEach(arg_0 -> ((ModelEvent.RegisterAdditional)event).register(arg_0));
    }
}

