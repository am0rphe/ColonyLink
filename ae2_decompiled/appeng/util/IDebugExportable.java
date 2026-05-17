/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.core.HolderLookup$Provider
 */
package appeng.util;

import appeng.api.networking.IGridNode;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.IOException;
import net.minecraft.core.HolderLookup;

public interface IDebugExportable {
    public void debugExport(JsonWriter var1, HolderLookup.Provider var2, Reference2IntMap<Object> var3, Reference2IntMap<IGridNode> var4) throws IOException;
}

