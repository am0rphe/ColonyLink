/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.integrations.igtooltip;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface BaseClassRegistration {
    public void addBaseBlockEntity(Class<? extends BlockEntity> var1, Class<? extends Block> var2);

    public <T extends BlockEntity> void addPartHost(Class<T> var1, Class<? extends Block> var2);
}

