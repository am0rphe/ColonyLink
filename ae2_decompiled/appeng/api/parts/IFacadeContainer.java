/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.parts;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public interface IFacadeContainer {
    public boolean canAddFacade(IFacadePart var1);

    public boolean addFacade(IFacadePart var1);

    public void removeFacade(IPartHost var1, Direction var2);

    @Nullable
    public IFacadePart getFacade(Direction var1);

    public void writeToNBT(CompoundTag var1, HolderLookup.Provider var2);

    public boolean readFromStream(RegistryFriendlyByteBuf var1);

    public void readFromNBT(CompoundTag var1, HolderLookup.Provider var2);

    public void writeToStream(RegistryFriendlyByteBuf var1);

    public boolean isEmpty();
}

