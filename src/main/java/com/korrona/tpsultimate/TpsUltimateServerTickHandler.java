package com.korrona.tpsultimate;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "tpsultimate")
public class TpsUltimateServerTickHandler {
    private static final Logger LOGGER = LogManager.getLogger(); // Логгер для отладки
    private static final long[] TICK_TIMES = new long[100]; // Массив для хранения времени последних 100 тиков
    private static int TICK_INDEX = 0; // Индекс текущего тика
    private static long LAST_TICK_TIME = System.nanoTime(); // Время последнего тика (в наносекундах)
    private static long LAST_UPDATE_TIME = 0; // Время последнего обновления таба
    public static long UPDATE_INTERVAL = 3 * 1000; // Интервал обновления (3 секунды)

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = event.getServer(); // Получаем сервер
            if (server == null || !server.isRunning()) {
                return; // Проверяем, что сервер не null и запущен
            }

            long currentTime = System.nanoTime();
            long tickDuration = (currentTime - LAST_TICK_TIME) / 1_000_000; // Переводим в миллисекунды
            LAST_TICK_TIME = currentTime;

            TICK_TIMES[TICK_INDEX] = tickDuration;
            TICK_INDEX = (TICK_INDEX + 1) % TICK_TIMES.length;

            if (System.currentTimeMillis() - LAST_UPDATE_TIME >= UPDATE_INTERVAL) {
                LAST_UPDATE_TIME = System.currentTimeMillis();

                long totalTime = 0;
                for (long time : TICK_TIMES) {
                    totalTime += time;
                }
                double averageTickTime = (double) totalTime / TICK_TIMES.length;
                double tps = averageTickTime > 0 ? 1000.0 / averageTickTime : 20.0; // Защита от деления на ноль
                double mspt = averageTickTime;

                LOGGER.debug("TPS: {:.2f}, MSPT: {:.2f}", tps, mspt); // Логируем значения

                if (!server.getPlayerList().getPlayers().isEmpty()) { // Проверяем, есть ли игроки
                    updateTab(server, tps, mspt);
                }
            }
        }
    }

    private static void updateTab(MinecraftServer server, double tps, double mspt) {
        // Создаем текст для заголовка и подзаголовка таба
        Component header = Component.literal(""); // Пустой заголовок
        Component footer = Component.literal("TPS: ")
                .append(Component.literal(String.format("%.2f", tps)).withStyle(getColorForTps(tps)))
                .append(Component.literal(" | MSPT: "))
                .append(Component.literal(String.format("%.2f", mspt)).withStyle(getColorForMspt(mspt)));

        // Создаем пакет для обновления таба
        ClientboundTabListPacket packet = new ClientboundTabListPacket(header, footer);

        // Отправляем пакет каждому игроку
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }

    private static ChatFormatting getColorForTps(double tps) {
        if (tps >= 19.5) return ChatFormatting.GREEN; // Отлично
        if (tps >= 18.0) return ChatFormatting.YELLOW; // Нормально
        if (tps >= 15.0) return ChatFormatting.GOLD; // Предупреждение
        return ChatFormatting.RED; // Критично
    }

    private static ChatFormatting getColorForMspt(double mspt) {
        if (mspt <= 50.0) return ChatFormatting.GREEN; // Отлично
        if (mspt <= 65.0) return ChatFormatting.YELLOW; // Нормально
        if (mspt <= 60.0) return ChatFormatting.GOLD; // Предупреждение
        return ChatFormatting.RED; // Критично
    }
}