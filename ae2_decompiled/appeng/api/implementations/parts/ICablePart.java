/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 */
package appeng.api.implementations.parts;

import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public interface ICablePart
extends IPart {
    public BusSupport supportsBuses();

    public AEColor getCableColor();

    public AECableType getCableConnectionType();

    public boolean changeColor(AEColor var1, Player var2);

    public void setExposedOnSides(EnumSet<Direction> var1);

    public boolean isConnected(Direction var1);
}

