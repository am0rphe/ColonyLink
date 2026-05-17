/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu;

import appeng.menu.SlotSemantic;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public final class SlotSemantics {
    private static final Map<String, SlotSemantic> REGISTRY = new ConcurrentHashMap<String, SlotSemantic>();
    public static final SlotSemantic STORAGE = SlotSemantics.register("STORAGE", false);
    public static final SlotSemantic PLAYER_INVENTORY = SlotSemantics.register("PLAYER_INVENTORY", true, 2000);
    public static final SlotSemantic PLAYER_HOTBAR = SlotSemantics.register("PLAYER_HOTBAR", true, 1000);
    public static final SlotSemantic TOOLBOX = SlotSemantics.register("TOOLBOX", true, 3000);
    public static final SlotSemantic CONFIG = SlotSemantics.register("CONFIG", false);
    public static final SlotSemantic UPGRADE = SlotSemantics.register("UPGRADE", false);
    public static final SlotSemantic STORAGE_CELL = SlotSemantics.register("STORAGE_CELL", false);
    public static final SlotSemantic INSCRIBER_PLATE_TOP = SlotSemantics.register("INSCRIBER_PLATE_TOP", false);
    public static final SlotSemantic INSCRIBER_PLATE_BOTTOM = SlotSemantics.register("INSCRIBER_PLATE_BOTTOM", false);
    public static final SlotSemantic MACHINE_INPUT = SlotSemantics.register("MACHINE_INPUT", false);
    public static final SlotSemantic MACHINE_OUTPUT = SlotSemantics.register("MACHINE_OUTPUT", false);
    public static final SlotSemantic MACHINE_CRAFTING_GRID = SlotSemantics.register("MACHINE_CRAFTING_GRID", false);
    public static final SlotSemantic BLANK_PATTERN = SlotSemantics.register("BLANK_PATTERN", false);
    public static final SlotSemantic ENCODED_PATTERN = SlotSemantics.register("ENCODED_PATTERN", false);
    public static final SlotSemantic VIEW_CELL = SlotSemantics.register("VIEW_CELL", false);
    public static final SlotSemantic CRAFTING_GRID = SlotSemantics.register("CRAFTING_GRID", true);
    public static final SlotSemantic PROCESSING_INPUTS = SlotSemantics.register("PROCESSING_INPUTS", false);
    public static final SlotSemantic PROCESSING_OUTPUTS = SlotSemantics.register("PROCESSING_OUTPUTS", false);
    public static final SlotSemantic SMITHING_TABLE_TEMPLATE = SlotSemantics.register("SMITHING_TABLE_TEMPLATE", false);
    public static final SlotSemantic SMITHING_TABLE_BASE = SlotSemantics.register("SMITHING_TABLE_BASE", false);
    public static final SlotSemantic SMITHING_TABLE_ADDITION = SlotSemantics.register("SMITHING_TABLE_ADDITION", false);
    public static final SlotSemantic SMITHING_TABLE_RESULT = SlotSemantics.register("SMITHING_TABLE_RESULT", false);
    public static final SlotSemantic STONECUTTING_INPUT = SlotSemantics.register("STONECUTTING_INPUT", false);
    public static final SlotSemantic CRAFTING_RESULT = SlotSemantics.register("CRAFTING_RESULT", false);
    public static final SlotSemantic MISSING_INGREDIENT = SlotSemantics.register("MISSING_INGREDIENT", true);

    private SlotSemantics() {
    }

    public static SlotSemantic register(String id, boolean playerSide) {
        return SlotSemantics.register(id, playerSide, 0);
    }

    public static SlotSemantic register(String id, boolean playerSide, int quickMovePriority) {
        SlotSemantic semantic = new SlotSemantic(id, playerSide, quickMovePriority);
        SlotSemantic existing = REGISTRY.putIfAbsent(semantic.id(), semantic);
        if (existing != null) {
            throw new IllegalArgumentException("Semantic with id " + semantic.id() + "was already registered");
        }
        return semantic;
    }

    public static SlotSemantic getOrThrow(String key) {
        SlotSemantic semantic = REGISTRY.get(key);
        if (semantic == null) {
            throw new IllegalArgumentException("Unknown slot semantic: " + key);
        }
        return semantic;
    }

    @Nullable
    public static SlotSemantic get(String key) {
        return REGISTRY.get(key);
    }
}

