package com.saytoro.mineandscale.network;

import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.data.PlayerProgression;
import com.saytoro.mineandscale.events.XpEventHandler;
import com.saytoro.mineandscale.logic.TalentLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestUnlockTalentPayload(String talentId) implements CustomPacketPayload {

    public static final Type<RequestUnlockTalentPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "request_unlock_talent"));

    public static final StreamCodec<FriendlyByteBuf, RequestUnlockTalentPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.talentId),
            buf -> new RequestUnlockTalentPayload(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Этот метод выполняется на СЕРВЕРЕ
    public void handleServer(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerProgression progression = player.getData(MineAndScale.PLAYER_PROGRESSION);

                // Проверяем: есть ли очки и не изучен ли талант ранее
                if (progression.getTalentPoints() > 0 && !progression.hasTalent(this.talentId)) {
                    progression.consumeTalentPoint(); // Списываем 1 очко
                    progression.unlockTalent(this.talentId); // Открываем талант

                    // Обновляем атрибуты (здоровье и т.д.)
                    TalentLogic.applyTalentAttributes(player);

                    // Отправляем клиенту новые данные (чтобы кнопка заблокировалась, а очки уменьшились)
                    XpEventHandler.syncPlayerData(player);
                }
            }
        });
    }
}