/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.client.event.RecipesUpdatedEvent
 *  net.neoforged.neoforge.event.AddReloadListenerEvent
 *  net.neoforged.neoforge.event.server.ServerStartedEvent
 */
package appeng.recipes.transform;

import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;
import appeng.recipes.transform.TransformRecipeInput;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class TransformLogic {
    static Map<Fluid, Set<Item>> fluidCache = new IdentityHashMap<Fluid, Set<Item>>();
    static Set<Item> explosionCache = null;
    static Set<Item> anyFluidCache = null;

    public static boolean canTransformInFluid(ItemEntity entity, FluidState fluid) {
        return TransformLogic.getTransformableItems(entity.level(), fluid.getType()).contains(entity.getItem().getItem());
    }

    public static boolean canTransformInAnyFluid(ItemEntity entity) {
        return TransformLogic.getTransformableItemsAnyFluid(entity.level()).contains(entity.getItem().getItem());
    }

    public static boolean canTransformInExplosion(ItemEntity entity) {
        return TransformLogic.getTransformableItemsExplosion(entity.level()).contains(entity.getItem().getItem());
    }

    public static boolean tryTransform(ItemEntity entity, Predicate<TransformCircumstance> circumstancePredicate) {
        Level level = entity.level();
        AABB region = new AABB(entity.getX() - 1.0, entity.getY() - 1.0, entity.getZ() - 1.0, entity.getX() + 1.0, entity.getY() + 1.0, entity.getZ() + 1.0);
        List<ItemEntity> itemEntities = level.getEntities(null, region).stream().filter(e -> e instanceof ItemEntity && !e.isRemoved()).map(e -> (ItemEntity)e).toList();
        for (RecipeHolder holder : level.getRecipeManager().byType(TransformRecipe.TYPE)) {
            TransformRecipe recipe = (TransformRecipe)holder.value();
            if (!circumstancePredicate.test(recipe.circumstance) || recipe.ingredients.isEmpty()) continue;
            ArrayList missingIngredients = Lists.newArrayList(recipe.ingredients);
            Reference2IntOpenHashMap consumedItems = new Reference2IntOpenHashMap(missingIngredients.size());
            if (recipe.circumstance.isExplosion() ? missingIngredients.stream().noneMatch(i -> i.test(entity.getItem())) : !((Ingredient)missingIngredients.getFirst()).test(entity.getItem())) continue;
            for (ItemEntity itemEntity : itemEntities) {
                ItemStack other = itemEntity.getItem();
                if (other.isEmpty()) continue;
                Iterator it = missingIngredients.iterator();
                while (it.hasNext()) {
                    Ingredient ing = (Ingredient)it.next();
                    int alreadyClaimed = consumedItems.getInt((Object)itemEntity);
                    if (!ing.test(other) || other.getCount() - alreadyClaimed <= 0) continue;
                    consumedItems.merge((Object)itemEntity, 1, Integer::sum);
                    it.remove();
                }
            }
            if (!missingIngredients.isEmpty()) continue;
            ArrayList<ItemStack> items = new ArrayList<ItemStack>(consumedItems.size());
            for (Reference2IntMap.Entry e2 : consumedItems.reference2IntEntrySet()) {
                ItemEntity itemEntity = (ItemEntity)e2.getKey();
                items.add(itemEntity.getItem().split(e2.getIntValue()));
                if (itemEntity.getItem().getCount() > 0) continue;
                itemEntity.discard();
            }
            TransformRecipeInput recipeInput = new TransformRecipeInput(items);
            ItemStack craftResult = recipe.assemble(recipeInput, (HolderLookup.Provider)level.registryAccess());
            RandomSource random = level.getRandom();
            double x = Math.floor(entity.getX()) + 0.25 + random.nextDouble() * 0.5;
            double y = Math.floor(entity.getY()) + 0.25 + random.nextDouble() * 0.5;
            double z = Math.floor(entity.getZ()) + 0.25 + random.nextDouble() * 0.5;
            double xSpeed = random.nextDouble() * 0.25 - 0.125;
            double ySpeed = random.nextDouble() * 0.25 - 0.125;
            double zSpeed = random.nextDouble() * 0.25 - 0.125;
            ItemEntity newEntity = new ItemEntity(level, x, y, z, craftResult);
            newEntity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
            level.addFreshEntity((Entity)newEntity);
            return true;
        }
        return false;
    }

    private static void clearCache() {
        fluidCache.clear();
        explosionCache = null;
        anyFluidCache = null;
    }

    private static Set<Item> getTransformableItems(Level level, Fluid fluid) {
        return fluidCache.computeIfAbsent(fluid, f -> {
            Set ret = Collections.newSetFromMap(new IdentityHashMap());
            for (RecipeHolder holder : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                Iterator iterator;
                TransformRecipe recipe = (TransformRecipe)holder.value();
                if (!recipe.circumstance.isFluid(fluid) || !(iterator = recipe.ingredients.iterator()).hasNext()) continue;
                Ingredient ingredient = (Ingredient)iterator.next();
                for (ItemStack stack : ingredient.getItems()) {
                    ret.add(stack.getItem());
                }
            }
            return ret;
        });
    }

    private static Set<Item> getTransformableItemsAnyFluid(Level level) {
        Set<Object> ret = anyFluidCache;
        if (ret == null) {
            ret = Collections.newSetFromMap(new IdentityHashMap());
            for (RecipeHolder holder : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                Iterator iterator;
                TransformRecipe recipe = (TransformRecipe)holder.value();
                if (!recipe.circumstance.isFluid() || !(iterator = recipe.ingredients.iterator()).hasNext()) continue;
                Ingredient ingredient = (Ingredient)iterator.next();
                for (ItemStack stack : ingredient.getItems()) {
                    ret.add(stack.getItem());
                }
            }
            anyFluidCache = ret;
        }
        return ret;
    }

    private static Set<Item> getTransformableItemsExplosion(Level level) {
        Set<Object> ret = explosionCache;
        if (ret == null) {
            ret = Collections.newSetFromMap(new IdentityHashMap());
            for (RecipeHolder holder : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                TransformRecipe recipe = (TransformRecipe)holder.value();
                if (!recipe.circumstance.isExplosion()) continue;
                for (Ingredient ingredient : recipe.ingredients) {
                    for (ItemStack stack : ingredient.getItems()) {
                        ret.add(stack.getItem());
                    }
                }
            }
            explosionCache = ret;
        }
        return ret;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent e) {
        TransformLogic.clearCache();
    }

    @SubscribeEvent
    public static void onReloadServerResources(AddReloadListenerEvent e) {
        TransformLogic.clearCache();
    }

    @SubscribeEvent
    public static void onClientRecipesUpdated(RecipesUpdatedEvent e) {
        TransformLogic.clearCache();
    }

    private TransformLogic() {
    }
}

