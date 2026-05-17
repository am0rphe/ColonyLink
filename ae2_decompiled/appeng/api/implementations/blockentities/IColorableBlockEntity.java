/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 */
package appeng.api.implementations.blockentities;

import appeng.api.util.AEColor;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public interface IColorableBlockEntity {
    public AEColor getColor();

    public boolean recolourBlock(Direction var1, AEColor var2, Player var3);
}

