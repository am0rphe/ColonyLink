/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  snownee.jade.api.BlockAccessor
 *  snownee.jade.api.IServerDataProvider
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

class ServerDataProviderAdapter<T>
implements IServerDataProvider<BlockAccessor> {
    private final ResourceLocation id;
    private final ServerDataProvider<? super T> provider;
    private final Class<T> objectClass;

    public ServerDataProviderAdapter(ResourceLocation id, ServerDataProvider<? super T> provider, Class<T> objectClass) {
        this.id = id;
        this.provider = provider;
        this.objectClass = objectClass;
    }

    public ResourceLocation getUid() {
        return this.id;
    }

    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        T obj = this.objectClass.cast(blockAccessor.getBlockEntity());
        Player player = blockAccessor.getPlayer();
        this.provider.provideServerData(player, obj, compoundTag);
    }
}

