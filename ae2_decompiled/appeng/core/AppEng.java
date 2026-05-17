/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.PageAnchor
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core;

import appeng.api.parts.CableRenderMode;
import appeng.client.EffectType;
import appeng.core.AppEngBase;
import appeng.core.network.ClientboundPacket;
import guideme.PageAnchor;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public interface AppEng {
    public static final String MOD_NAME = "Applied Energistics 2";
    public static final String MOD_ID = "ae2";

    public static AppEng instance() {
        return AppEngBase.INSTANCE;
    }

    public static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath((String)MOD_ID, (String)id);
    }

    default public HitResult getCurrentMouseOver() {
        return null;
    }

    public Collection<ServerPlayer> getPlayers();

    public void sendToAllNearExcept(Player var1, double var2, double var4, double var6, double var8, Level var10, ClientboundPacket var11);

    public void spawnEffect(EffectType var1, Level var2, double var3, double var5, double var7, Object var9);

    public void setPartInteractionPlayer(Player var1);

    public CableRenderMode getCableRenderMode();

    @Nullable
    public Level getClientLevel();

    @Nullable
    public MinecraftServer getCurrentServer();

    public void registerHotkey(String var1);

    default public void openGuideAtAnchor(PageAnchor anchor) {
    }
}

