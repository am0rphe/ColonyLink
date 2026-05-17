/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemProperties
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.init.client;

import appeng.api.util.AEColor;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.tools.powered.ColorApplicatorItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public final class InitItemModelsProperties {
    public static final ResourceLocation COLORED_PREDICATE_ID = AppEng.makeId("colored");
    public static final ResourceLocation ENERGY_FILL_LEVEL_ID = AppEng.makeId("fill_level");

    private InitItemModelsProperties() {
    }

    public static void init() {
        ColorApplicatorItem colorApplicatorItem = AEItems.COLOR_APPLICATOR.get();
        ItemProperties.register((Item)colorApplicatorItem, (ResourceLocation)COLORED_PREDICATE_ID, (itemStack, level, entity, seed) -> {
            AEColor col = colorApplicatorItem.getActiveColor(itemStack);
            return col != null ? 1.0f : 0.0f;
        });
        BuiltInRegistries.ITEM.forEach(item -> {
            if (!(item instanceof EnergyCellBlockItem)) {
                return;
            }
            EnergyCellBlockItem energyCell = (EnergyCellBlockItem)item;
            ItemProperties.register((Item)energyCell, (ResourceLocation)ENERGY_FILL_LEVEL_ID, (is, level, entity, seed) -> {
                double curPower = energyCell.getAECurrentPower(is);
                double maxPower = energyCell.getAEMaxPower(is);
                return (float)(curPower / maxPower);
            });
        });
    }
}

