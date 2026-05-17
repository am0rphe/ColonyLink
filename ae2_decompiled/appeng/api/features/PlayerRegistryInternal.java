/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.saveddata.SavedData$Factory
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.features;

import appeng.api.features.IPlayerRegistry;
import appeng.core.AELog;
import appeng.core.worlddata.AESavedData;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

final class PlayerRegistryInternal
extends AESavedData
implements IPlayerRegistry {
    private static final String NAME = "ae2_players";
    private static final String TAG_PLAYER_IDS = "playerIds";
    private static final String TAG_PROFILE_IDS = "profileIds";
    private final BiMap<UUID, Integer> mapping = HashBiMap.create();
    private final MinecraftServer server;
    private int nextPlayerId = 0;

    private PlayerRegistryInternal(MinecraftServer server) {
        this.server = server;
    }

    static PlayerRegistryInternal get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Cannot retrieve player data for a server that has no overworld.");
        }
        return (PlayerRegistryInternal)overworld.getDataStorage().computeIfAbsent(new SavedData.Factory(() -> new PlayerRegistryInternal(server), (nbt, provider) -> PlayerRegistryInternal.load(server, nbt), null), NAME);
    }

    @Override
    @Nullable
    public UUID getProfileId(int playerId) {
        return (UUID)this.mapping.inverse().get((Object)playerId);
    }

    @Override
    public int getPlayerId(UUID profileId) {
        Objects.requireNonNull(profileId, "profileId");
        Integer playerId = (Integer)this.mapping.get((Object)profileId);
        if (playerId == null) {
            playerId = this.nextPlayerId++;
            this.mapping.put((Object)profileId, (Object)playerId);
            this.setDirty();
            ServerPlayer player = this.server.getPlayerList().getPlayer(profileId);
            String name = player != null ? player.getGameProfile().getName() : "[UNKNOWN]";
            AELog.info("Assigning ME player id %s to Minecraft profile %s (%s)", playerId, profileId, name);
        }
        return playerId;
    }

    private static PlayerRegistryInternal load(MinecraftServer server, CompoundTag nbt) {
        long[] profileIds;
        int[] playerIds = nbt.getIntArray(TAG_PLAYER_IDS);
        if (playerIds.length * 2 != (profileIds = nbt.getLongArray(TAG_PROFILE_IDS)).length) {
            throw new IllegalStateException("Player ID mapping is corrupted. " + playerIds.length + " player IDs vs. " + profileIds.length + " profile IDs (latter must be 2 * the former)");
        }
        PlayerRegistryInternal result = new PlayerRegistryInternal(server);
        int highestPlayerId = -1;
        for (int i = 0; i < playerIds.length; ++i) {
            int playerId = playerIds[i];
            UUID profileId = new UUID(profileIds[i * 2], profileIds[i * 2 + 1]);
            highestPlayerId = Math.max(playerId, highestPlayerId);
            result.mapping.put((Object)profileId, (Object)playerId);
            AELog.debug("AE player ID %s is assigned to profile ID %s", playerId, profileId);
        }
        result.nextPlayerId = highestPlayerId + 1;
        return result;
    }

    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        int index = 0;
        int[] playerIds = new int[this.mapping.size()];
        long[] profileIds = new long[this.mapping.size() * 2];
        for (Map.Entry entry : this.mapping.entrySet()) {
            profileIds[index * 2] = ((UUID)entry.getKey()).getMostSignificantBits();
            profileIds[index * 2 + 1] = ((UUID)entry.getKey()).getLeastSignificantBits();
            playerIds[index++] = (Integer)entry.getValue();
        }
        compound.putIntArray(TAG_PLAYER_IDS, playerIds);
        compound.putLongArray(TAG_PROFILE_IDS, profileIds);
        return compound;
    }
}

