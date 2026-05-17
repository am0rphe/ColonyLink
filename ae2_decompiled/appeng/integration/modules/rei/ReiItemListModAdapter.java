/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  me.shedaniel.rei.api.client.REIRuntime
 *  me.shedaniel.rei.api.client.gui.widgets.TextField
 */
package appeng.integration.modules.rei;

import appeng.integration.abstraction.ItemListModAdapter;
import com.google.common.base.Strings;
import java.util.Objects;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;

class ReiItemListModAdapter
implements ItemListModAdapter {
    private final REIRuntime runtime = Objects.requireNonNull(REIRuntime.getInstance(), "REI helper was null");

    ReiItemListModAdapter() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getShortName() {
        return "REI";
    }

    @Override
    public String getSearchText() {
        TextField searchField = this.runtime.getSearchTextField();
        if (searchField == null) {
            return "";
        }
        return Strings.nullToEmpty((String)searchField.getText());
    }

    @Override
    public void setSearchText(String text) {
        TextField searchField = this.runtime.getSearchTextField();
        if (searchField != null) {
            searchField.setText(text);
        }
    }

    @Override
    public boolean hasSearchFocus() {
        TextField searchField = this.runtime.getSearchTextField();
        return searchField != null && searchField.isFocused();
    }
}

