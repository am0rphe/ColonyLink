/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.server.MinecraftServer
 */
package appeng.server.subcommands;

import appeng.api.networking.pathing.ChannelMode;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import appeng.server.ISubCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public class ChannelModeCommand
implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (ChannelMode mode : ChannelMode.values()) {
            builder.then(Commands.literal((String)mode.name().toLowerCase(Locale.ROOT)).executes(ctx -> {
                this.setChannelMode((CommandContext<CommandSourceStack>)ctx, mode);
                return 1;
            }));
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        ChannelMode mode = AEConfig.instance().getChannelMode();
        sender.sendSuccess(() -> PlayerMessages.ChannelModeCurrent.text(mode.name().toLowerCase(Locale.ROOT)), true);
    }

    private void setChannelMode(CommandContext<CommandSourceStack> ctx, ChannelMode mode) {
        AELog.info("%s is changing channel mode to %s", new Object[]{ctx.getSource(), mode});
        AEConfig.instance().setChannelModel(mode);
        AEConfig.instance().save();
        int gridCount = 0;
        for (Grid grid : TickHandler.instance().getGridList()) {
            grid.getPathingService().repath();
            ++gridCount;
        }
        int finalGridCount = gridCount;
        String modeName = mode.name().toLowerCase(Locale.ROOT);
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> PlayerMessages.ChannelModeSet.text(modeName, finalGridCount), true);
    }
}

