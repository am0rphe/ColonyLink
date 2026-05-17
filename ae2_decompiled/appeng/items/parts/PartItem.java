/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.context.UseOnContext
 */
package appeng.items.parts;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.items.AEBaseItem;
import java.util.function.Function;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class PartItem<T extends IPart>
extends AEBaseItem
implements IPartItem<T> {
    private final Class<T> partClass;
    private final Function<IPartItem<T>, T> factory;

    public PartItem(Item.Properties properties, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        super(properties);
        this.partClass = partClass;
        this.factory = factory;
    }

    public InteractionResult useOn(UseOnContext context) {
        return PartHelper.usePartItem(context);
    }

    @Override
    public Class<T> getPartClass() {
        return this.partClass;
    }

    @Override
    public T createPart() {
        return (T)((IPart)this.factory.apply(this));
    }
}

