package com.colonylink.colonylink;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Global Loot Modifier that ADDS the Colony Link Package to targeted loot tables.
 *
 * Config-driven (server-side): the per-source toggle and chance come from
 * {@link ColonyLinkConfig}, so a server operator can tune or disable the loot
 * without editing datapacks. This is a COMPLEMENT to crafting — the Package
 * recipe is untouched and this modifier only ever ADDS to loot, never removes.
 *
 * Targeting is done in the data JSON via a {@code neoforge:loot_table_id}
 * condition (never global): the inherited final {@link LootModifier#apply} runs
 * those conditions BEFORE calling {@link #doApply}, so doApply only handles the
 * toggle, the chance roll and the item add.
 *
 * Two sources share one serializer, distinguished by the {@code source} field:
 *   CHEST  → package_in_chests / package_chest_chance
 *   RAIDER → package_from_raiders / package_raider_chance
 *
 * Serializer id: {@code colonylink:add_package}.
 */
public class AddPackageLootModifier extends LootModifier
{
    public enum Source implements StringRepresentable
    {
        CHEST("chest"),
        RAIDER("raider");

        private final String name;

        Source(String name) { this.name = name; }

        @Override
        public String getSerializedName() { return name; }
    }

    public static final MapCodec<AddPackageLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(StringRepresentable.fromEnum(Source::values)
                            .fieldOf("source")
                            .forGetter(m -> m.source))
                    .apply(inst, AddPackageLootModifier::new));

    /**
     * Serializer registry. Registered on the mod event bus from
     * {@link ColonyLink} (see ColonyLink.java, next to the other DeferredRegisters).
     */
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ColonyLink.MODID);

    static
    {
        LOOT_MODIFIERS.register("add_package", () -> CODEC);
    }

    private final Source source;

    public AddPackageLootModifier(LootItemCondition[] conditions, Source source)
    {
        super(conditions);
        this.source = source;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context)
    {
        final boolean enabled;
        final double chance;
        switch (source)
        {
            case CHEST ->
            {
                enabled = ColonyLinkConfig.PACKAGE_IN_CHESTS.get();
                chance = ColonyLinkConfig.PACKAGE_CHEST_CHANCE.get();
            }
            case RAIDER ->
            {
                enabled = ColonyLinkConfig.PACKAGE_FROM_RAIDERS.get();
                chance = ColonyLinkConfig.PACKAGE_RAIDER_CHANCE.get();
            }
            default -> { return loot; }
        }

        if (!enabled || chance <= 0.0D) return loot;

        // The neoforge:loot_table_id condition already gated us to the right tables
        // (LootModifier.apply runs conditions before doApply). Roll once per table.
        if (context.getRandom().nextFloat() < chance)
            loot.add(new ItemStack(ColonyLink.COLONY_LINK_PACKAGE.get()));

        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec()
    {
        return CODEC;
    }
}
