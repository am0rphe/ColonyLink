/*
 * Decompiled with CFR 0.152.
 */
package appeng.integration.abstraction;

public interface ItemListModAdapter {
    public boolean isEnabled();

    public String getShortName();

    default public String getSearchText() {
        return "";
    }

    default public void setSearchText(String text) {
    }

    default public boolean hasSearchFocus() {
        return false;
    }

    public static ItemListModAdapter none() {
        return new ItemListModAdapter(){

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public String getShortName() {
                return "REI/EMI";
            }
        };
    }
}

