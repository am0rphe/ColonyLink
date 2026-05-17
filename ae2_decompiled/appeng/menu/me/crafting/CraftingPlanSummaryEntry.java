/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 */
package appeng.menu.me.crafting;

import appeng.api.stacks.AEKey;
import java.util.Comparator;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CraftingPlanSummaryEntry
implements Comparable<CraftingPlanSummaryEntry> {
    private static final Comparator<CraftingPlanSummaryEntry> COMPARATOR = Comparator.comparing(CraftingPlanSummaryEntry::getMissingAmount).thenComparing(CraftingPlanSummaryEntry::getCraftAmount).thenComparing(CraftingPlanSummaryEntry::getStoredAmount).reversed();
    private final AEKey what;
    private final long missingAmount;
    private final long storedAmount;
    private final long craftAmount;

    public CraftingPlanSummaryEntry(AEKey what, long missingAmount, long storedAmount, long craftAmount) {
        this.what = what;
        this.missingAmount = missingAmount;
        this.storedAmount = storedAmount;
        this.craftAmount = craftAmount;
    }

    public AEKey getWhat() {
        return this.what;
    }

    public long getMissingAmount() {
        return this.missingAmount;
    }

    public long getStoredAmount() {
        return this.storedAmount;
    }

    public long getCraftAmount() {
        return this.craftAmount;
    }

    @Override
    public int compareTo(CraftingPlanSummaryEntry o) {
        return COMPARATOR.compare(this, o);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        AEKey.writeKey(buffer, this.what);
        buffer.writeVarLong(this.missingAmount);
        buffer.writeVarLong(this.storedAmount);
        buffer.writeVarLong(this.craftAmount);
    }

    public static CraftingPlanSummaryEntry read(RegistryFriendlyByteBuf buffer) {
        AEKey what = AEKey.readKey(buffer);
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        return new CraftingPlanSummaryEntry(what, missingAmount, storedAmount, craftAmount);
    }
}

