/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.integrations.igtooltip;

import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface ClientRegistration {
    default public <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, BodyProvider<? super T> provider) {
        this.addBlockEntityBody(blockEntityClass, blockClass, id, provider, 1000);
    }

    public <T extends BlockEntity> void addBlockEntityBody(Class<T> var1, Class<? extends Block> var2, ResourceLocation var3, BodyProvider<? super T> var4, int var5);

    default public <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, IconProvider<? super T> provider) {
        this.addBlockEntityIcon(blockEntityClass, blockClass, id, provider, 1000);
    }

    public <T extends BlockEntity> void addBlockEntityIcon(Class<T> var1, Class<? extends Block> var2, ResourceLocation var3, IconProvider<? super T> var4, int var5);

    default public <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, NameProvider<? super T> provider) {
        this.addBlockEntityName(blockEntityClass, blockClass, id, provider, 1000);
    }

    public <T extends BlockEntity> void addBlockEntityName(Class<T> var1, Class<? extends Block> var2, ResourceLocation var3, NameProvider<? super T> var4, int var5);

    default public <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass, Class<? extends Block> blockClass, ResourceLocation id, ModNameProvider<? super T> provider) {
        this.addBlockEntityModName(blockEntityClass, blockClass, id, provider, 1000);
    }

    public <T extends BlockEntity> void addBlockEntityModName(Class<T> var1, Class<? extends Block> var2, ResourceLocation var3, ModNameProvider<? super T> var4, int var5);
}

