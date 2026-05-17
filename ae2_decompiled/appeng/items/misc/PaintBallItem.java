/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$Properties
 */
package appeng.items.misc;

import appeng.api.util.AEColor;
import appeng.items.AEBaseItem;
import net.minecraft.world.item.Item;

public class PaintBallItem
extends AEBaseItem {
    private final AEColor color;
    private final boolean lumen;

    public PaintBallItem(Item.Properties properties, AEColor color, boolean lumen) {
        super(properties);
        this.color = color;
        this.lumen = lumen;
    }

    public AEColor getColor() {
        return this.color;
    }

    public boolean isLumen() {
        return this.lumen;
    }
}

