/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 */
package appeng.items.parts;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.items.parts.PartItem;
import java.util.function.Function;
import net.minecraft.world.item.Item;

public class ColoredPartItem<T extends IPart>
extends PartItem<T> {
    private final AEColor color;

    public ColoredPartItem(Item.Properties properties, Class<T> partClass, Function<ColoredPartItem<T>, T> factory, AEColor color) {
        super(properties, partClass, item -> (IPart)factory.apply((ColoredPartItem)item));
        this.color = color;
    }

    public AEColor getColor() {
        return this.color;
    }
}

