/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SharedConstants
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.EntityType$EntityFactory
 *  net.minecraft.world.entity.MobCategory
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package appeng.core.definitions;

import appeng.entity.TinyTNTPrimedEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AEEntities {
    public static final DeferredRegister<EntityType<?>> DR = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"ae2");
    public static final Map<String, String> ENTITY_ENGLISH_NAMES = new HashMap<String, String>();
    public static final DeferredHolder<EntityType<?>, EntityType<TinyTNTPrimedEntity>> TINY_TNT_PRIMED = AEEntities.create("tiny_tnt_primed", "Tiny TNT Primed", TinyTNTPrimedEntity::new, MobCategory.MISC, builder -> builder.setTrackingRange(16).setUpdateInterval(4).setShouldReceiveVelocityUpdates(true));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> create(String id, String englishName, EntityType.EntityFactory<T> entityFactory, MobCategory classification, Consumer<EntityType.Builder<T>> customizer) {
        ENTITY_ENGLISH_NAMES.put(id, englishName);
        return DR.register(id, () -> {
            EntityType.Builder builder = EntityType.Builder.of((EntityType.EntityFactory)entityFactory, (MobCategory)classification);
            customizer.accept(builder);
            boolean prev = SharedConstants.CHECK_DATA_FIXER_SCHEMA;
            SharedConstants.CHECK_DATA_FIXER_SCHEMA = false;
            EntityType result = builder.build(id);
            SharedConstants.CHECK_DATA_FIXER_SCHEMA = prev;
            return result;
        });
    }
}

