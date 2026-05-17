/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 */
package appeng.core;

import appeng.client.EffectType;
import appeng.core.AppEngBase;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value="ae2", dist={Dist.DEDICATED_SERVER})
public class AppEngServer
extends AppEngBase {
    public AppEngServer(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
    }

    @Override
    public Level getClientLevel() {
        return null;
    }

    @Override
    public void registerHotkey(String id) {
    }

    @Override
    public void spawnEffect(EffectType effect, Level level, double posX, double posY, double posZ, Object o) {
    }
}

