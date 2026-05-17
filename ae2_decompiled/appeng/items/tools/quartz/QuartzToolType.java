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
package appeng.items.tools.quartz;

import appeng.datagen.providers.tags.ConventionTags;
import java.util.function.Supplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum QuartzToolType {
    CERTUS("certus_quartz", () -> Ingredient.of(ConventionTags.CERTUS_QUARTZ)),
    NETHER("nether_quartz", () -> Ingredient.of(ConventionTags.NETHER_QUARTZ));

    private final String name;
    private final Tier toolTier;

    private QuartzToolType(final String name, final Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.toolTier = new Tier(){

            public int getUses() {
                return Tiers.IRON.getUses();
            }

            public float getSpeed() {
                return Tiers.IRON.getSpeed();
            }

            public float getAttackDamageBonus() {
                return Tiers.IRON.getAttackDamageBonus();
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

    public String getName() {
        return this.name;
    }

    public final Tier getToolTier() {
        return this.toolTier;
    }
}

