/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 */
package appeng.server.services.compass;

import appeng.core.localization.PlayerMessages;
import appeng.server.ISubCommand;
import appeng.server.services.compass.CompassRegion;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class TestCompassCommand
implements ISubCommand {
    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        ServerLevel level = sender.getLevel();
        ChunkPos chunkPos = new ChunkPos(BlockPos.containing((Position)sender.getPosition()));
        CompassRegion compassRegion = CompassRegion.get(level, chunkPos);
        int i = 0;
        while (i <= level.getSectionsCount()) {
            boolean hasSkyStone = compassRegion.hasCompassTarget(chunkPos.x, chunkPos.z, i);
            int yMin = i * 16;
            int yMax = (i + 1) * 16 - 1;
            int iFinal = i++;
            sender.sendSuccess(() -> PlayerMessages.CompassTestSection.text(yMin, yMax, iFinal, hasSkyStone), false);
        }
    }
}

