/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking.crafting;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingLink;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface ICraftingSubmitResult {
    default public boolean successful() {
        return this.errorCode() == null;
    }

    @Nullable
    public CraftingSubmitErrorCode errorCode();

    @Nullable
    public Object errorDetail();

    @Nullable
    public ICraftingLink link();
}

