package com.korrona.tpsultimate;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;

import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = "tpsultimate")
public class TpsUltimateCommandHandler {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(
                Commands.literal("tpsultimate")
                        .then(Commands.literal("updtime")
                                .then(Commands.argument("time", StringArgumentType.string())
                                        .executes(context -> setUpdateTime(context,
                                                StringArgumentType.getString(context, "time"))))));
    }

    private static int setUpdateTime(CommandContext<CommandSourceStack> context, String timeArg) {
        CommandSourceStack source = context.getSource();

        try {
            long newInterval = parseTimeToMillis(timeArg);
            if (newInterval > 0) {
                TpsUltimateServerTickHandler.UPDATE_INTERVAL = newInterval;
                source.sendSuccess(
                        () -> Component.literal("Интервал обновления установлен на " + formatTime(newInterval)), false);
            } else {
                source.sendFailure(Component.literal("Ошибка: Неверный формат времени"));
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Ошибка: Неверный формат времени"));
        }
        return 1;
    }

    private static long parseTimeToMillis(String timeArg) throws Exception {
        if (timeArg.endsWith("s")) {
            return TimeUnit.SECONDS.toMillis(Long.parseLong(timeArg.replace("s", "")));
        } else if (timeArg.endsWith("m")) {
            return TimeUnit.MINUTES.toMillis(Long.parseLong(timeArg.replace("m", "")));
        } else if (timeArg.endsWith("ms")) {
            return Long.parseLong(timeArg.replace("ms", ""));
        } else {
            throw new Exception("Неизвестная единица времени");
        }
    }

    private static String formatTime(long millis) {
        if (millis >= TimeUnit.MINUTES.toMillis(1)) {
            return String.format("%d минут", TimeUnit.MILLISECONDS.toMinutes(millis));
        } else if (millis >= TimeUnit.SECONDS.toMillis(1)) {
            return String.format("%d секунд", TimeUnit.MILLISECONDS.toSeconds(millis));
        } else {
            return String.format("%d миллисекунд", millis);
        }
    }
}
