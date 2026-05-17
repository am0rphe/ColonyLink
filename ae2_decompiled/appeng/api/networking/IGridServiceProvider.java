/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking;

import appeng.api.networking.IGridNode;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IGridServiceProvider {
    default public void onServerStartTick() {
    }

    default public void onLevelStartTick(Level level) {
    }

    default public void onLevelEndTick(Level level) {
    }

    default public void onServerEndTick() {
    }

    default public void removeNode(IGridNode gridNode) {
    }

    default public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
    }

    default public void saveNodeData(IGridNode gridNode, CompoundTag savedData) {
    }

    default public void debugDump(JsonWriter writer, HolderLookup.Provider registries) throws IOException {
    }
}

