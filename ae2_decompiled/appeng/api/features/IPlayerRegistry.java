/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.features;

import appeng.api.features.PlayerRegistryInternal;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IPlayerRegistry {
    public static IPlayerRegistry getMapping(MinecraftServer server) {
        return PlayerRegistryInternal.get(server);
    }

    @Nullable
    public static IPlayerRegistry getMapping(Level level) {
        if (!level.isClientSide() && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return IPlayerRegistry.getMapping(serverLevel.getServer());
        }
        return null;
    }

    public static int getPlayerId(ServerPlayer player) {
        return IPlayerRegistry.getMapping(player.getServer()).getPlayerId(player.getGameProfile());
    }

    @Nullable
    public static ServerPlayer getConnected(MinecraftServer server, int playerId) {
        UUID uuid = IPlayerRegistry.getMapping(server).getProfileId(playerId);
        if (uuid == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(uuid);
    }

    default public int getPlayerId(GameProfile gameProfile) {
        UUID profileId = gameProfile.getId();
        if (profileId == null) {
            return -1;
        }
        return this.getPlayerId(profileId);
    }

    public int getPlayerId(UUID var1);

    @Nullable
    public UUID getProfileId(int var1);
}

