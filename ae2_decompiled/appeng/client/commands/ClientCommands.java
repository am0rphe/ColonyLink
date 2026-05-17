/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 */
package appeng.client.commands;

import appeng.core.AEConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ClientCommands {
    public static final List<CommandBuilder> DEBUG_COMMANDS = List.of(ClientCommands::highlightGuiAreas);

    private ClientCommands() {
    }

    private static void highlightGuiAreas(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal((String)"highlight_gui_areas").executes(context -> {
            CommandSourceStack src = (CommandSourceStack)context.getSource();
            boolean toggle = !AEConfig.instance().isShowDebugGuiOverlays();
            AEConfig.instance().setShowDebugGuiOverlays(toggle);
            AEConfig.instance().save();
            src.sendSystemMessage((Component)Component.literal((String)("GUI Overlays: " + toggle)));
            return 0;
        }));
    }

    @FunctionalInterface
    public static interface CommandBuilder {
        public void build(LiteralArgumentBuilder<CommandSourceStack> var1);
    }
}

