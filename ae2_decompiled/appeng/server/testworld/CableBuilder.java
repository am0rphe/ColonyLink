/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.server.testworld;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.server.testworld.PlotBuilder;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public class CableBuilder {
    private final PlotBuilder plotBuilder;
    private final String bb;

    public CableBuilder(PlotBuilder plotBuilder, String bb) {
        this.plotBuilder = plotBuilder;
        this.bb = bb;
    }

    public CableBuilder part(Direction side, ItemDefinition<? extends PartItem<?>> part) {
        this.plotBuilder.part(this.bb, side, part);
        return this;
    }

    public <T extends IPart> CableBuilder part(Direction side, ItemDefinition<? extends PartItem<T>> part, Consumer<T> partCustomizer) {
        this.plotBuilder.part(this.bb, side, part, partCustomizer);
        return this;
    }

    public <T extends IPart> CableBuilder facade(Direction side, ItemLike item) {
        this.plotBuilder.facade(this.bb, side, item);
        return this;
    }

    public CableBuilder craftingEmitter(Direction side, AEKey what) {
        return this.part(side, AEParts.LEVEL_EMITTER, emitter -> {
            emitter.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            emitter.getConfigManager().putSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.YES);
            emitter.getConfig().addFilter(what);
        });
    }

    public CableBuilder craftingEmitter(Direction side, ItemLike what) {
        return this.craftingEmitter(side, AEItemKey.of(what));
    }

    public CableBuilder craftingEmitter(Direction side, Fluid what) {
        return this.craftingEmitter(side, AEFluidKey.of(what));
    }
}

