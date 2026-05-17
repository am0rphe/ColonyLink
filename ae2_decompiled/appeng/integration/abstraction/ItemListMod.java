/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 */
package appeng.integration.abstraction;

import appeng.integration.abstraction.ItemListModAdapter;
import com.google.common.base.Strings;

public class ItemListMod {
    private static ItemListModAdapter adapter = ItemListModAdapter.none();

    private ItemListMod() {
    }

    public static boolean isEnabled() {
        return adapter.isEnabled();
    }

    public static String getShortName() {
        return adapter.getShortName();
    }

    public static String getSearchText() {
        return Strings.nullToEmpty((String)adapter.getSearchText());
    }

    public static void setSearchText(String text) {
        adapter.setSearchText(Strings.nullToEmpty((String)text));
    }

    public static boolean hasSearchFocus() {
        return adapter.hasSearchFocus();
    }

    public static void setAdapter(ItemListModAdapter adapter) {
        ItemListMod.adapter = adapter;
    }
}

