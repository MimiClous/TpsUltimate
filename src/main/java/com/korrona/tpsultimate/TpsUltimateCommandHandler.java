package com.korrona.tpsultimate;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = "tpsultimate")
public class TpsUltimateCommandHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String SECONDS_SUFFIX = "s";
    private static final String MINUTES_SUFFIX = "m";
    private static final String MILLISECONDS_SUFFIX = "ms";
    private static final String INVALID_TIME_FORMAT = "Ошибка: Неверный формат времени";
    private static final String INVALID_NUMBER_FORMAT = "Ошибка: Неверный формат числа";
    private static final String INVALID_UNIT_FORMAT = "Ошибка: Неизвестная единица времени";
    private static final String INTERVAL_SET_SUCCESS = "Интервал обновления установлен на %s";

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(
                Commands.literal("tpsultimate")
                        .requires(source -> source.hasPermission(4)) // Только для операторов сервера
                        .then(Commands.literal("updtime")
                                .then(Commands.argument("time", StringArgumentType.string())
                                        .executes(context -> setUpdateTime(context, StringArgumentType.getString(context, "time"))))));
    }

    private static int setUpdateTime(CommandContext<CommandSourceStack> context, String timeArg) {
        CommandSourceStack source = context.getSource();

        try {
            long newInterval = parseTimeToMillis(timeArg);
            if (newInterval <= 0) {
                source.sendFailure(Component.literal("Ошибка: Интервал должен быть больше 0"));
                return 0;
            }

            TpsUltimateServerTickHandler.UPDATE_INTERVAL = newInterval;
            String formattedTime = formatTime(newInterval);
            source.sendSuccess(
                    () -> Component.literal(String.format(INTERVAL_SET_SUCCESS, formattedTime)), false);
            LOGGER.info("Интервал обновления изменен на {}", formattedTime);
        } catch (NumberFormatException e) {
            LOGGER.error("Неверный формат числа: {}", timeArg, e);
            source.sendFailure(Component.literal(INVALID_NUMBER_FORMAT));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Неизвестная единица времени: {}", timeArg, e);
            source.sendFailure(Component.literal(INVALID_UNIT_FORMAT));
        } catch (Exception e) {
            LOGGER.error("Ошибка при установке интервала обновления", e);
            source.sendFailure(Component.literal(INVALID_TIME_FORMAT));
        }
        return 1;
    }

    private static long parseTimeToMillis(String timeArg) throws Exception {
        String numberPart = timeArg.replaceAll("[^0-9]", "");
        if (numberPart.isEmpty()) {
            throw new NumberFormatException("Неверный формат числа");
        }

        long value = Long.parseLong(numberPart);
        if (value <= 0) {
            throw new IllegalArgumentException("Интервал должен быть больше 0");
        }

        if (timeArg.endsWith(SECONDS_SUFFIX)) {
            return TimeUnit.SECONDS.toMillis(value);
        } else if (timeArg.endsWith(MINUTES_SUFFIX)) {
            return TimeUnit.MINUTES.toMillis(value);
        } else if (timeArg.endsWith(MILLISECONDS_SUFFIX)) {
            return value;
        } else {
            throw new IllegalArgumentException("Неизвестная единица времени");
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