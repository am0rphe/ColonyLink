/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.me.service.helpers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import appeng.me.service.CraftingService;
import net.minecraft.network.chat.Component;

public class CraftingServiceStorage
implements IStorageProvider {
    private final CraftingService craftingService;
    private final MEStorage inventory = new MEStorage(){

        @Override
        public boolean isPreferredStorageFor(AEKey key, IActionSource source) {
            return true;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return CraftingServiceStorage.this.craftingService.insertIntoCpus(what, amount, mode);
        }

        @Override
        public Component getDescription() {
            return GuiText.AutoCrafting.text();
        }
    };

    public CraftingServiceStorage(CraftingService craftingService) {
        this.craftingService = craftingService;
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        mounts.mount(this.inventory, Integer.MAX_VALUE);
    }
}

