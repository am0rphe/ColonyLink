/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.crafting.Ingredient
 */
package appeng.menu.me.common;

import appeng.menu.me.common.GridInventoryEntry;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.crafting.Ingredient;

public interface IClientRepo {
    public void handleUpdate(boolean var1, List<GridInventoryEntry> var2);

    public Set<GridInventoryEntry> getAllEntries();

    public Collection<GridInventoryEntry> getByIngredient(Ingredient var1);
}

