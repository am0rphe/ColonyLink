/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.implementations;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.ISubMenuHost;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.AEBaseMenu;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

public class KeyTypeSelectionScreen<C extends AEBaseMenu, P extends AEBaseScreen<C>>
extends AESubScreen<C, P> {
    private final KeyTypeCheckboxes keyTypesWidget = new KeyTypeCheckboxes();

    public KeyTypeSelectionScreen(P parent, ISubMenuHost subMenuHost, Component dialogTitle) {
        super(parent, "/screens/key_type_selection.json");
        this.addBackButton(subMenuHost);
        this.widgets.add("keytypes", this.keyTypesWidget);
        this.setTextContent("dialog_title", dialogTitle);
    }

    private void addBackButton(ISubMenuHost subMenuHost) {
        ItemStack icon = subMenuHost.getMainMenuIcon();
        Component label = icon.getHoverName();
        TabButton button = new TabButton(Icon.BACK, label, btn -> this.returnToParent());
        this.widgets.add("back", (AbstractWidget)button);
    }

    private void setHeight(int height) {
        this.style.getGeneratedBackground().setHeight(height);
        this.imageHeight = height;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        int selectedEntryCount = 0;
        AECheckbox selectedEntry = null;
        for (Map.Entry<AEKeyType, AECheckbox> entry : this.keyTypesWidget.checkboxes.entrySet()) {
            boolean selected = ((KeyTypeSelectionMenu)((Object)((AEBaseMenu)this.getMenu()))).getClientKeyTypeSelection().keyTypes().get(entry.getKey());
            entry.getValue().setSelected(selected);
            entry.getValue().active = true;
            if (!selected) continue;
            ++selectedEntryCount;
            selectedEntry = entry.getValue();
        }
        if (selectedEntryCount == 1) {
            selectedEntry.active = false;
        }
    }

    private class KeyTypeCheckboxes
    implements ICompositeWidget {
        private static final int PADDING = 6;
        private static final int KEY_TYPE_SPACING = 20;
        private Rect2i bounds = new Rect2i(0, 0, 0, 0);
        private final Map<AEKeyType, AECheckbox> checkboxes = new LinkedHashMap<AEKeyType, AECheckbox>();

        private KeyTypeCheckboxes() {
        }

        @Override
        public void setPosition(Point position) {
            this.bounds = new Rect2i(position.getX(), position.getY(), this.bounds.getWidth(), this.bounds.getHeight());
        }

        @Override
        public void setSize(int width, int height) {
            this.bounds = new Rect2i(this.bounds.getX(), this.bounds.getY(), width, height);
        }

        @Override
        public Rect2i getBounds() {
            return this.bounds;
        }

        @Override
        public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
            int xPos = this.bounds.getX() + bounds.getX();
            int yPos = this.bounds.getY() + bounds.getY();
            this.checkboxes.clear();
            for (AEKeyType keyType : ((KeyTypeSelectionMenu)((Object)((AEBaseMenu)KeyTypeSelectionScreen.this.getMenu()))).getClientKeyTypeSelection().keyTypes().keySet()) {
                Component text = keyType.getDescription();
                int textboxWidth = 24 + Minecraft.getInstance().font.width((FormattedText)text);
                AECheckbox checkbox = new AECheckbox(xPos, yPos, textboxWidth, 14, screen.getStyle(), keyType.getDescription());
                checkbox.setChangeListener(() -> ((KeyTypeSelectionMenu)((Object)((AEBaseMenu)KeyTypeSelectionScreen.this.getMenu()))).selectKeyType(keyType, checkbox.isSelected()));
                addWidget.accept((AbstractWidget)checkbox);
                this.checkboxes.put(keyType, checkbox);
                yPos += 20;
            }
            int height = this.bounds.getY() + AEKeyTypes.getAll().size() * 20 + 6;
            KeyTypeSelectionScreen.this.setHeight(height);
        }
    }
}

