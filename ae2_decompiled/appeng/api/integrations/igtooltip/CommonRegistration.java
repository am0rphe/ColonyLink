/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.integrations.igtooltip;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface CommonRegistration {
    public <T extends BlockEntity> void addBlockEntityData(ResourceLocation var1, Class<T> var2, ServerDataProvider<? super T> var3);
}

