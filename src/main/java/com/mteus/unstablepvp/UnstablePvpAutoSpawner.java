package com.mteus.unstablepvp;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Requires Carpet PvP Practice release 19+ installed alongside this mod. */
public class UnstablePvpAutoSpawner implements ModInitializer {
    private static final int INTERVAL_TICKS = 2400;
    private static final Random RANDOM = new Random();
    private static boolean enabled = false;
    private static int ticks = 0;
    private static long sequence = 0;

    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(UnstablePvpAutoSpawner::tick);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bots")
            .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN))
            .then(Commands.literal("start").executes(context -> {
                enabled = true;
                ticks = 0;
                context.getSource().sendSuccess(() -> Component.literal("PvP auto spawner enabled (every 2 minutes)."), false);
                return 1;
            }))
            .then(Commands.literal("stop").executes(context -> {
                enabled = false;
                context.getSource().sendSuccess(() -> Component.literal("PvP auto spawner disabled."), false);
                return 1;
            })));
    }

    private static void tick(MinecraftServer server) {
        if (!enabled || ++ticks < INTERVAL_TICKS) return;
        ticks = 0;
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            // Fake players from Carpet are also ServerPlayers; skip bot-looking names.
            // For guaranteed detection, integrate directly with Carpet PvP's bot registry.
            String name = player.getGameProfile().name();
            if (name.matches("UBot[0-9]+")) continue;
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            int distance = 20 + RANDOM.nextInt(16);
            int dx = (int)Math.round(Math.cos(angle) * distance);
            int dz = (int)Math.round(Math.sin(angle) * distance);
            String botName = "UBot" + (++sequence);
            // Run Carpet PvP's command as the actual player, at a nearby position.
            // Its own spawn implementation offsets the final bot position slightly.
            String command = String.format(Locale.ROOT,
                "execute as %s at %s positioned ~%d ~ ~%d run bot spawn %s smp average",
                name, name, dx, dz, botName);
            try {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
            } catch (Exception ex) {
                System.err.println("[Unstable PvP] Bot spawn failed: " + ex.getMessage());
            }
        }
    }
}
