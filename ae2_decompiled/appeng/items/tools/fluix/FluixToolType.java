/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.block.Block
 */
package appeng.items.tools.fluix;

import appeng.datagen.providers.tags.ConventionTags;
import java.util.function.Supplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum FluixToolType {
    FLUIX("fluix", () -> Ingredient.of(ConventionTags.FLUIX_CRYSTAL));

    private final String name;
    private final Tier toolTier;

    private FluixToolType(final String name, final Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.toolTier = new Tier(){

            public int getUses() {
                return Tiers.IRON.getUses() * 3;
            }

            public float getSpeed() {
                return Tiers.IRON.getSpeed() * 1.2f;
            }

            public float getAttackDamageBonus() {
                return Tiers.IRON.getAttackDamageBonus() * 1.2f;
            }

            public TagKey<Block> getIncorrectBlocksForDrops() {
                return Tiers.IRON.getIncorrectBlocksForDrops();
            }

            public int getEnchantmentValue() {
                return Tiers.IRON.getEnchantmentValue();
            }

            public Ingredient getRepairIngredient() {
                return (Ingredient)repairIngredient.get();
            }

            public String toString() {
                return "ae2:" + name;
            }
        };
    }

    public final String getName() {
        return this.name;
    }

    public final Tier getToolTier() {
        return this.toolTier;
    }
}

