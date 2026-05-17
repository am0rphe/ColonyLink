/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 */
package appeng.server;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.server.Commands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public final class AECommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder builder = net.minecraft.commands.Commands.literal((String)"ae2");
        for (Commands command : Commands.values()) {
            this.add((LiteralArgumentBuilder<CommandSourceStack>)builder, command);
        }
        dispatcher.register(builder);
    }

    private void add(LiteralArgumentBuilder<CommandSourceStack> builder, Commands subCommand) {
        LiteralArgumentBuilder subCommandBuilder = (LiteralArgumentBuilder)net.minecraft.commands.Commands.literal((String)subCommand.literal()).requires(src -> {
            if (subCommand.test && !AEConfig.instance().isDebugToolsEnabled()) {
                return false;
            }
            return src.hasPermission(subCommand.level);
        });
        subCommand.command.addArguments((LiteralArgumentBuilder<CommandSourceStack>)subCommandBuilder);
        subCommandBuilder.executes(ctx -> {
            subCommand.command.call(AppEng.instance().getCurrentServer(), (CommandContext<CommandSourceStack>)ctx, (CommandSourceStack)ctx.getSource());
            return 1;
        });
        builder.then((ArgumentBuilder)subCommandBuilder);
    }
}

