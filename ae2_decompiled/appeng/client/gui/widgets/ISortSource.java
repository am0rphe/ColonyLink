/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.widgets;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.stacks.AEKeyType;
import java.util.Set;

public interface ISortSource {
    public SortOrder getSortBy();

    public SortDir getSortDir();

    public ViewItems getSortDisplay();

    public Set<AEKeyType> getSortKeyTypes();
}

