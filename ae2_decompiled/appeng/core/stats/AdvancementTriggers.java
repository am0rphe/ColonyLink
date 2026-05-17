/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Criterion
 *  net.minecraft.advancements.CriterionTriggerInstance
 *  net.minecraft.advancements.critereon.PlayerTrigger
 *  net.minecraft.advancements.critereon.PlayerTrigger$TriggerInstance
 */
package appeng.core.stats;

import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.PlayerTrigger;

public class AdvancementTriggers {
    public static final PlayerTrigger NETWORK_APPRENTICE = new PlayerTrigger();
    public static final PlayerTrigger NETWORK_ENGINEER = new PlayerTrigger();
    public static final PlayerTrigger NETWORK_ADMIN = new PlayerTrigger();
    public static final PlayerTrigger SPATIAL_EXPLORER = new PlayerTrigger();
    public static final PlayerTrigger RECURSIVE = new PlayerTrigger();

    public static Criterion<?> networkApprenticeCriterion() {
        return NETWORK_APPRENTICE.createCriterion((CriterionTriggerInstance)new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> networkEngineerCriterion() {
        return NETWORK_ENGINEER.createCriterion((CriterionTriggerInstance)new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> networkAdminCriterion() {
        return NETWORK_ADMIN.createCriterion((CriterionTriggerInstance)new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> spatialExplorerCriterion() {
        return SPATIAL_EXPLORER.createCriterion((CriterionTriggerInstance)new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> recursiveCriterion() {
        return RECURSIVE.createCriterion((CriterionTriggerInstance)new PlayerTrigger.TriggerInstance(Optional.empty()));
    }
}

