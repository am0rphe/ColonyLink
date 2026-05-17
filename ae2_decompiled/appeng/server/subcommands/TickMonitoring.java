/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.server.MinecraftServer
 */
package appeng.server.subcommands;

import appeng.me.service.TickManagerService;
import appeng.server.ISubCommand;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public class TickMonitoring
implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.argument((String)"enable", (ArgumentType)BoolArgumentType.bool()).executes(ctx -> {
            Boolean enable = (Boolean)ctx.getArgument("enable", Boolean.class);
            TickManagerService.MONITORING_ENABLED = enable;
            return 1;
        }));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> data, CommandSourceStack sender) {
    }
}

