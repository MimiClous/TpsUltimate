package com.korrona.tpsultimate;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "tpsultimate") 
public class TpsUltimateServerTickHandler { 
    private static final long[] TICK_TIMES = new long[100]; // Массив для хранения времени последних 100 тиков
    private static int TICK_INDEX = 0; // Индекс текущего тика
    private static long LAST_TICK_TIME = System.currentTimeMillis(); // Время последнего тика
    private static long LAST_UPDATE_TIME = 0; // Время последнего обновления таба
    public static long UPDATE_INTERVAL = 3 * 1000; // Интервал обновления (30 секунд) - изменяемый

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = event.getServer(); // Получаем сервер
            if (server == null)
                return; // Проверяем, что сервер не null

            long currentTime = System.currentTimeMillis();
            long tickDuration = currentTime - LAST_TICK_TIME;
            LAST_TICK_TIME = currentTime;

            TICK_TIMES[TICK_INDEX] = tickDuration;
            TICK_INDEX = (TICK_INDEX + 1) % TICK_TIMES.length;

            if (currentTime - LAST_UPDATE_TIME >= UPDATE_INTERVAL) {
                LAST_UPDATE_TIME = currentTime;

                long totalTime = 0;
                for (long time : TICK_TIMES) {
                    totalTime += time;
                }
                double averageTickTime = (double) totalTime / TICK_TIMES.length;
                double tps = 1000.0 / averageTickTime;
                double mspt = averageTickTime;

                updateTab(server, tps, mspt);
            }
        }
    }

    private static void updateTab(MinecraftServer server, double tps, double mspt) {
        // Создаем текст для заголовка и подзаголовка таба
        Component header = Component.literal("Tps Ultimate"); // Название мода
        Component footer = Component.literal("TPS: ")
                .append(Component.literal(String.format("%.2f", tps)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" | MSPT: "))
                .append(Component.literal(String.format("%.2f", mspt)).withStyle(ChatFormatting.RED));

        // Создаем пакет для обновления таба
        ClientboundTabListPacket packet = new ClientboundTabListPacket(header, footer);

        // Отправляем пакет каждому игроку
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }
}
