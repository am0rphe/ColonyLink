/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.saveddata.SavedData
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.core.worlddata;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AESavedData
extends SavedData {
    private static final Logger LOG = LoggerFactory.getLogger(AESavedData.class);

    public void save(File file, HolderLookup.Provider registries) {
        if (!this.isDirty()) {
            return;
        }
        Path targetPath = file.toPath().toAbsolutePath();
        Path tempFile = targetPath.getParent().resolve(file.getName() + ".temp");
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", (Tag)this.save(new CompoundTag(), registries));
        NbtUtils.addCurrentDataVersion((CompoundTag)compoundTag);
        try {
            NbtIo.writeCompressed((CompoundTag)compoundTag, (Path)tempFile);
            try {
                Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException iOException) {
            LOG.error("Could not save data {}", (Object)this, (Object)iOException);
        }
        this.setDirty(false);
    }
}

