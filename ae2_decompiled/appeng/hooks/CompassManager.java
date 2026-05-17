/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.level.ChunkPos
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.hooks;

import appeng.core.network.serverbound.RequestClosestMeteoritePacket;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class CompassManager {
    public static final CompassManager INSTANCE = new CompassManager();
    private static final int REFRESH_CACHE_AFTER = 30000;
    private static final int EXPIRE_CACHE_AFTER = 60000;
    private final Long2ObjectOpenHashMap<CachedResult> requests = new Long2ObjectOpenHashMap();

    private CompassManager() {
    }

    public void postResult(ChunkPos requestedPos, @Nullable BlockPos closestMeteorite) {
        this.requests.put(requestedPos.toLong(), (Object)new CachedResult(closestMeteorite, System.currentTimeMillis()));
    }

    @Nullable
    public BlockPos getClosestMeteorite(BlockPos pos, boolean prefetch) {
        return this.getClosestMeteorite(new ChunkPos(pos), prefetch);
    }

    @Nullable
    public BlockPos getClosestMeteorite(ChunkPos chunkPos, boolean prefetch) {
        boolean request;
        long now = System.currentTimeMillis();
        ObjectIterator it = this.requests.values().iterator();
        while (it.hasNext()) {
            CachedResult res = (CachedResult)it.next();
            long age = now - res.received();
            if (age <= 60000L) continue;
            it.remove();
        }
        BlockPos result = null;
        CachedResult cached = (CachedResult)this.requests.get(chunkPos.toLong());
        if (cached != null) {
            result = cached.closestMeteoritePos();
            long age = now - cached.received();
            request = age > 30000L;
        } else {
            request = true;
        }
        if (result == null) {
            result = this.findClosestKnownResult(chunkPos);
        }
        if (request) {
            this.requests.put(chunkPos.toLong(), (Object)new CachedResult(result, now));
            PacketDistributor.sendToServer((CustomPacketPayload)new RequestClosestMeteoritePacket(chunkPos), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        if (prefetch) {
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    if (i == 0 && j == 0) continue;
                    this.getClosestMeteorite(new ChunkPos(chunkPos.x + i, chunkPos.z + j), false);
                }
            }
        }
        return result;
    }

    @Nullable
    private BlockPos findClosestKnownResult(ChunkPos chunkPos) {
        long closestDistance = Long.MAX_VALUE;
        BlockPos result = null;
        for (Long2ObjectMap.Entry entry : this.requests.long2ObjectEntrySet()) {
            int distance;
            BlockPos closestPos = ((CachedResult)entry.getValue()).closestMeteoritePos();
            if (closestPos == null || (long)(distance = chunkPos.distanceSquared(entry.getLongKey())) >= closestDistance) continue;
            closestDistance = distance;
            result = closestPos;
        }
        return result;
    }

    private record CachedResult(@Nullable BlockPos closestMeteoritePos, long received) {
    }
}

