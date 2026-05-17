/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.worldgen.BootstrapContext
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.valueproviders.ConstantInt
 *  net.minecraft.util.valueproviders.IntProvider
 *  net.minecraft.world.level.dimension.DimensionType
 *  net.minecraft.world.level.dimension.DimensionType$MonsterSettings
 *  org.jetbrains.annotations.NotNull
 */
package appeng.init.worldgen;

import appeng.spatial.SpatialStorageDimensionIds;
import java.util.OptionalLong;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

public final class InitDimensionTypes {
    private InitDimensionTypes() {
    }

    public static void init(BootstrapContext<DimensionType> context) {
        DimensionType dimensionType = InitDimensionTypes.createSpatialDimensionType();
        context.register(SpatialStorageDimensionIds.DIMENSION_TYPE_ID, (Object)dimensionType);
    }

    @NotNull
    private static DimensionType createSpatialDimensionType() {
        return new DimensionType(OptionalLong.of(12000L), false, false, false, false, 1.0, false, false, 0, 256, 256, BlockTags.INFINIBURN_OVERWORLD, SpatialStorageDimensionIds.SKY_PROPERTIES_ID, 1.0f, new DimensionType.MonsterSettings(false, false, (IntProvider)ConstantInt.of((int)0), 0));
    }
}

