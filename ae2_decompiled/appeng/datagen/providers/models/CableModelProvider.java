/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.PackOutput
 *  net.neoforged.neoforge.client.model.generators.ItemModelBuilder
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.models;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.datagen.providers.models.AE2BlockStateProvider;
import java.util.Locale;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CableModelProvider
extends AE2BlockStateProvider {
    public CableModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, "ae2", existingFileHelper);
    }

    protected void registerStatesAndModels() {
        this.buildCableItems(AEParts.GLASS_CABLE, "item/glass_cable_base", "part/cable/glass/");
        this.buildCableItems(AEParts.COVERED_CABLE, "item/covered_cable_base", "part/cable/covered/");
        this.buildCableItems(AEParts.COVERED_DENSE_CABLE, "item/covered_dense_cable_base", "part/cable/dense_covered/");
        this.buildCableItems(AEParts.SMART_CABLE, "item/smart_cable_base", "part/cable/smart/");
        this.buildCableItems(AEParts.SMART_DENSE_CABLE, "item/smart_dense_cable_base", "part/cable/dense_smart/");
    }

    private void buildCableItems(ColoredItemDefinition cable, String baseModel, String textureBase) {
        for (AEColor color : AEColor.values()) {
            ((ItemModelBuilder)this.itemModels().withExistingParent(cable.id(color).getPath(), AppEng.makeId(baseModel))).texture("base", AppEng.makeId(textureBase + color.name().toLowerCase(Locale.ROOT)));
        }
    }
}

