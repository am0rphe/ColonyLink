/*
 * Decompiled with CFR 0.152.
 */
package appeng.crafting;

import appeng.api.stacks.AEKey;

public class CraftBranchFailure
extends Exception {
    public CraftBranchFailure(AEKey what, long howMany) {
        super("Failed: " + String.valueOf(what) + " x " + howMany);
    }
}

