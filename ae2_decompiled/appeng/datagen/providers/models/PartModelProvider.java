/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.PackOutput
 *  net.neoforged.neoforge.client.model.generators.BlockModelBuilder
 *  net.neoforged.neoforge.client.model.generators.ModelProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.models;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PartModelProvider
extends ModelProvider<BlockModelBuilder> {
    public PartModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, "ae2", "part", BlockModelBuilder::new, existingFileHelper);
    }

    public String getName() {
        return "Part Models: " + this.modid;
    }

    protected void registerModels() {
        this.addBuiltInModel("part/annihilation_plane");
        this.addBuiltInModel("part/annihilation_plane_on");
        this.addBuiltInModel("part/identity_annihilation_plane");
        this.addBuiltInModel("part/identity_annihilation_plane_on");
        this.addBuiltInModel("part/formation_plane");
        this.addBuiltInModel("part/formation_plane_on");
        this.addBuiltInModel("part/p2p/p2p_tunnel_frequency");
    }

    private void addBuiltInModel(String name) {
        this.getBuilder(name);
    }
}

