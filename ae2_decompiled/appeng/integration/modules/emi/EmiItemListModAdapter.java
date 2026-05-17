/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  dev.emi.emi.api.EmiApi
 */
package appeng.integration.modules.emi;

import appeng.integration.abstraction.ItemListModAdapter;
import com.google.common.base.Strings;
import dev.emi.emi.api.EmiApi;

class EmiItemListModAdapter
implements ItemListModAdapter {
    EmiItemListModAdapter() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getShortName() {
        return "EMI";
    }

    @Override
    public String getSearchText() {
        return Strings.nullToEmpty((String)EmiApi.getSearchText());
    }

    @Override
    public void setSearchText(String text) {
        EmiApi.setSearchText((String)Strings.nullToEmpty((String)text));
    }

    @Override
    public boolean hasSearchFocus() {
        return EmiApi.isSearchFocused();
    }
}

