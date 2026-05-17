/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.server.MinecraftServer
 */
package appeng.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public interface ISubCommand {
    default public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
    }

    public void call(MinecraftServer var1, CommandContext<CommandSourceStack> var2, CommandSourceStack var3);
}

