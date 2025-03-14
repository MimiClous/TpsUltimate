package com.korrona.tpsultimate;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TpsUltimateMod.MOD_ID)
public class TpsUltimateMod {
    public static final String MOD_ID = "tpsultimate";

    public TpsUltimateMod() { // Конструктор по умолчанию
        // Получаем шину событий из контекста загрузки
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем слушатель событий
        modEventBus.addListener(this::setup);

        // Регистрируем мод в EVENT_BUS
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Настройка мода
        System.out.println("TpsUltimate mod is setting up!");
    }
}