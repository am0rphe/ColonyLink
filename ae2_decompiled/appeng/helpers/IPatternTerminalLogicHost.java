/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 */
package appeng.helpers;

import appeng.parts.encoding.PatternEncodingLogic;
import net.minecraft.world.level.Level;

public interface IPatternTerminalLogicHost {
    public PatternEncodingLogic getLogic();

    public Level getLevel();

    public void markForSave();
}

