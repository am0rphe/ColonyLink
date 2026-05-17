/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.widgets;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.ISubMenuHost;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.KeyTypeSelectionScreen;
import appeng.client.gui.widgets.IconButton;
import appeng.menu.AEBaseMenu;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class KeyTypeSelectionButton
extends IconButton {
    private final Component title;
    private final Supplier<Component> descriptionSupplier;

    public static <C extends AEBaseMenu, P extends AEBaseScreen<C>> KeyTypeSelectionButton create(P parentScreen, ISubMenuHost subMenuHost, Component title) {
        return new KeyTypeSelectionButton(() -> {
            if (Screen.hasShiftDown()) {
                KeyTypeSelectionButton.handleShiftClick((AEBaseMenu)parentScreen.getMenu());
            } else {
                parentScreen.switchToScreen(new KeyTypeSelectionScreen(parentScreen, subMenuHost, title));
            }
        }, title, () -> Component.literal((String)((KeyTypeSelectionMenu)((Object)((AEBaseMenu)parentScreen.getMenu()))).getClientKeyTypeSelection().enabledSet().stream().map(x -> x.getDescription().getString()).collect(Collectors.joining(", "))));
    }

    private static <C extends AEBaseMenu> void handleShiftClick(C menu) {
        Set<AEKeyType> newSelection = KeyTypeSelectionButton.getNextSelection(((KeyTypeSelectionMenu)((Object)menu)).getClientKeyTypeSelection());
        for (AEKeyType keyType : newSelection) {
            ((KeyTypeSelectionMenu)((Object)menu)).selectKeyType(keyType, true);
        }
        for (AEKeyType keyType : ((KeyTypeSelectionMenu)((Object)menu)).getClientKeyTypeSelection().enabledSet()) {
            if (newSelection.contains(keyType)) continue;
            ((KeyTypeSelectionMenu)((Object)menu)).selectKeyType(keyType, false);
        }
    }

    private static Set<AEKeyType> getNextSelection(KeyTypeSelectionMenu.SyncedKeyTypes keyTypes) {
        int enabledCount;
        int totalCount = keyTypes.keyTypes().size();
        if (totalCount == (enabledCount = keyTypes.enabledSet().size())) {
            return Set.of((AEKeyType)keyTypes.keyTypes().keySet().stream().findFirst().orElseThrow());
        }
        if (enabledCount > 1) {
            return Set.copyOf(keyTypes.keyTypes().keySet());
        }
        AEKeyType currentKey = keyTypes.enabledSet().get(0);
        boolean foundCurrent = false;
        for (AEKeyType keyType : keyTypes.keyTypes().keySet()) {
            if (foundCurrent) {
                return Set.of(keyType);
            }
            if (keyType != currentKey) continue;
            foundCurrent = true;
        }
        return Set.copyOf(keyTypes.keyTypes().keySet());
    }

    private KeyTypeSelectionButton(Runnable onPress, Component title, Supplier<Component> descriptionSupplier) {
        super(btn -> onPress.run());
        this.title = title;
        this.descriptionSupplier = descriptionSupplier;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(this.title, this.descriptionSupplier.get());
    }

    @Override
    protected Icon getIcon() {
        return Icon.TYPE_FILTER_ALL;
    }
}

