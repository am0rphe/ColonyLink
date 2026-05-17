/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ICraftingService
extends IGridService {
    public Collection<IPatternDetails> getCraftingFor(AEKey var1);

    default public boolean isCraftable(AEKey whatToCraft) {
        return !this.getCraftingFor(whatToCraft).isEmpty();
    }

    public void refreshNodeCraftingProvider(IGridNode var1);

    public void addGlobalCraftingProvider(ICraftingProvider var1);

    public void removeGlobalCraftingProvider(ICraftingProvider var1);

    public void refreshGlobalCraftingProvider(ICraftingProvider var1);

    @Nullable
    public AEKey getFuzzyCraftable(AEKey var1, AEKeyFilter var2);

    public Future<ICraftingPlan> beginCraftingCalculation(Level var1, ICraftingSimulationRequester var2, AEKey var3, long var4, CalculationStrategy var6);

    public ICraftingSubmitResult submitJob(ICraftingPlan var1, @Nullable ICraftingRequester var2, @Nullable ICraftingCPU var3, boolean var4, IActionSource var5);

    public ImmutableSet<ICraftingCPU> getCpus();

    public boolean canEmitFor(AEKey var1);

    public Set<AEKey> getCraftables(AEKeyFilter var1);

    public boolean isRequesting(AEKey var1);

    public long getRequestedAmount(AEKey var1);

    public boolean isRequestingAny();
}

