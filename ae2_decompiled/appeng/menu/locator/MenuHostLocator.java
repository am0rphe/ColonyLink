/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.locator;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface MenuHostLocator {
    @Nullable
    public <T> T locate(Player var1, Class<T> var2);
}

