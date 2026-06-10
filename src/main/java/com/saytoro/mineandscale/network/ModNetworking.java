package com.saytoro.mineandscale.network;

// ДОБАВЬ ЭТУ СТРОКУ:
import com.saytoro.mineandscale.MineAndScale;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        // Теперь компилятор увидит MineAndScale.MODID
        final PayloadRegistrar registrar = event.registrar(MineAndScale.MODID).versioned("1.1");

        registrar.playToClient(SyncProgressionPayload.TYPE, SyncProgressionPayload.STREAM_CODEC, SyncProgressionPayload::handleClient);
        registrar.playToServer(RequestUnlockTalentPayload.TYPE, RequestUnlockTalentPayload.STREAM_CODEC, RequestUnlockTalentPayload::handleServer);
        registrar.playToServer(RequestUpgradeStatPayload.TYPE, RequestUpgradeStatPayload.STREAM_CODEC, RequestUpgradeStatPayload::handleServer);
    }
}