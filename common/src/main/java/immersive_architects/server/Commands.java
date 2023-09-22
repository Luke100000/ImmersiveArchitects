package immersive_architects.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class Commands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(net.minecraft.commands.Commands.literal("architect")
                .then(register("debug", Commands::debug))
        );
    }

    private static int debug(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }


    private static ArgumentBuilder<CommandSourceStack, ?> register(String name, com.mojang.brigadier.Command<CommandSourceStack> cmd) {
        return net.minecraft.commands.Commands.literal(name).requires(cs -> cs.hasPermission(0)).executes(cmd);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> register(String name) {
        return net.minecraft.commands.Commands.literal(name).requires(cs -> cs.hasPermission(0));
    }

    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(Component.literal(ChatFormatting.GOLD + "[MCA] " + ChatFormatting.RESET + message));
    }
}
