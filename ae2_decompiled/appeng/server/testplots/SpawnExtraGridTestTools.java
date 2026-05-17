/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.bus.api.Event
 */
package appeng.server.testplots;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.server.testplots.TestPlotClass;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

@TestPlotClass
public class SpawnExtraGridTestTools
extends Event {
    private final ResourceLocation plotId;
    private final InternalInventory inventory;
    private final IGrid grid;

    public SpawnExtraGridTestTools(ResourceLocation plotId, InternalInventory inventory, IGrid grid) {
        this.plotId = plotId;
        this.inventory = inventory;
        this.grid = grid;
    }

    public ResourceLocation getPlotId() {
        return this.plotId;
    }

    public InternalInventory getInventory() {
        return this.inventory;
    }

    public IGrid getGrid() {
        return this.grid;
    }
}

