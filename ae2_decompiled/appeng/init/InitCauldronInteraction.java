/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.cauldron.CauldronInteraction
 */
package appeng.init;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.tools.MemoryCardItem;
import appeng.items.tools.powered.AbstractPortableCell;
import net.minecraft.core.cauldron.CauldronInteraction;

public class InitCauldronInteraction {
    public static void init() {
        for (ItemDefinition<?> def : AEItems.getItems()) {
            if (!(def.asItem() instanceof AbstractPortableCell) && !(def.asItem() instanceof MemoryCardItem)) continue;
            CauldronInteraction.WATER.map().put(def.asItem(), CauldronInteraction.DYED_ITEM);
        }
    }
}

