package com.korrona.tpsultimate; // Поменяй на новое имя пакета, если необходимо

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("tpsultimate") // Название мода в аннотации
public class TpsUltimateMod { // Переименуй класс
    public TpsUltimateMod() { // Инициализация мода
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Настройка мода
        System.out.println("TpsUltimate mod is setting up!"); // Убедись, что имя нового мода выводится правильно
    }
}