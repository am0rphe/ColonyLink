/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class CableBusStorage {
    @Nullable
    private ICablePart center;
    @Nullable
    private IPart[] parts;
    @Nullable
    private IFacadePart[] facades;

    protected ICablePart getCenter() {
        return this.center;
    }

    protected void setCenter(ICablePart center) {
        this.center = center;
    }

    protected IPart getPart(Direction side) {
        if (this.parts == null) {
            return null;
        }
        int index = side.ordinal();
        return this.parts[index];
    }

    protected void setPart(Direction side, IPart part) {
        if (this.parts == null) {
            this.parts = new IPart[Direction.values().length];
        }
        int index = side.ordinal();
        this.parts[index] = part;
    }

    protected void removePart(Direction side) {
        if (this.parts == null) {
            return;
        }
        int index = side.ordinal();
        this.parts[index] = null;
        if (CableBusStorage.isNullArray(this.parts)) {
            this.parts = null;
        }
    }

    public IFacadePart getFacade(Direction side) {
        if (this.facades == null) {
            return null;
        }
        int index = side.ordinal();
        return this.facades[index];
    }

    public void setFacade(Direction side, @Nullable IFacadePart facade) {
        if (facade == null) {
            this.removeFacade(side);
            return;
        }
        if (this.facades == null) {
            this.facades = new IFacadePart[Direction.values().length];
        }
        int index = side.ordinal();
        this.facades[index] = facade;
    }

    public void removeFacade(Direction side) {
        if (this.facades == null) {
            return;
        }
        int index = side.ordinal();
        this.facades[index] = null;
        if (CableBusStorage.isNullArray(this.facades)) {
            this.facades = null;
        }
    }

    private static <T> boolean isNullArray(T[] array) {
        if (array == null) {
            return true;
        }
        for (T o : array) {
            if (o == null) continue;
            return false;
        }
        return true;
    }
}

