/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.api.orientation;

import appeng.api.orientation.IOrientationStrategy;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.world.level.block.state.properties.Property;

class NoOrientationStrategy
implements IOrientationStrategy {
    NoOrientationStrategy() {
    }

    @Override
    public Collection<Property<?>> getProperties() {
        return Collections.emptyList();
    }
}

