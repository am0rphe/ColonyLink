/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.inventory.Slot
 */
package appeng.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.Slot;

public class AESubScreen<C extends AEBaseMenu, P extends AEBaseScreen<C>>
extends AEBaseScreen<C> {
    private final P parent;
    private final List<Slot> clientSideSlots = new ArrayList<Slot>();

    public AESubScreen(P parent, String stylePath) {
        super((AEBaseMenu)parent.getMenu(), ((AEBaseMenu)parent.getMenu()).getPlayerInventory(), parent.getTitle(), StyleManager.loadStyleDoc(stylePath));
        this.parent = parent;
    }

    public P getParent() {
        return this.parent;
    }

    protected final void returnToParent() {
        for (Slot clientSideSlot : this.clientSideSlots) {
            ((AEBaseMenu)this.menu).removeClientSideSlot(clientSideSlot);
        }
        this.clientSideSlots.clear();
        this.onReturnToParent();
        P parent = this.getParent();
        this.switchToScreen((AEBaseScreen<?>)((Object)parent));
        ((AEBaseScreen)((Object)parent)).onReturnFromSubScreen(this);
    }

    protected void onReturnToParent() {
    }

    protected final Slot addClientSideSlot(Slot slot, SlotSemantic semantic) {
        this.clientSideSlots.add(slot);
        return ((AEBaseMenu)this.menu).addClientSideSlot(slot, semantic);
    }
}

