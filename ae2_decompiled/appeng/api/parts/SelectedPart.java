/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.parts;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class SelectedPart {
    public final IPart part;
    public final IFacadePart facade;
    @Nullable
    public final Direction side;

    public SelectedPart() {
        this.part = null;
        this.facade = null;
        this.side = null;
    }

    public SelectedPart(IPart part, Direction side) {
        this.part = part;
        this.facade = null;
        this.side = side;
    }

    public SelectedPart(IFacadePart facade, Direction side) {
        this.part = null;
        this.facade = facade;
        this.side = side;
    }
}

