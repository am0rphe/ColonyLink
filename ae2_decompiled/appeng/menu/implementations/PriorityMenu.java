/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.helpers.IPriorityHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class PriorityMenu
extends AEBaseMenu
implements ISubMenu {
    private static final String ACTION_SET_PRIORITY = "setPriority";
    public static final MenuType<PriorityMenu> TYPE = MenuTypeBuilder.create(PriorityMenu::new, IPriorityHost.class).withInitialData((host, buffer) -> buffer.writeVarInt(host.getPriority()), (host, menu, buffer) -> {
        menu.priorityValue = buffer.readVarInt();
    }).build("priority");
    private final IPriorityHost host;
    private int priorityValue;

    public PriorityMenu(int id, Inventory ip, IPriorityHost host) {
        super(TYPE, id, ip, host);
        this.host = host;
        this.priorityValue = host.getPriority();
        this.registerClientAction(ACTION_SET_PRIORITY, Integer.class, this::setPriority);
    }

    public void setPriority(int newValue) {
        if (newValue != this.priorityValue) {
            if (this.isClientSide()) {
                this.priorityValue = newValue;
                this.sendClientAction(ACTION_SET_PRIORITY, newValue);
            } else {
                this.host.setPriority(newValue);
                this.priorityValue = newValue;
            }
        }
    }

    public int getPriorityValue() {
        return this.priorityValue;
    }

    @Override
    public IPriorityHost getHost() {
        return this.host;
    }
}

