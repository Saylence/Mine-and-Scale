package com.saytoro.mineandscale.client;

import net.neoforged.bus.api.IEventBus;

public class ClientRegistration {
    public static void register(IEventBus modEventBus) {
        // Безопасно привязываем метод регистрации кнопок к шине мода
        modEventBus.addListener(ModClientEvents::registerKeyBindings);
    }
}