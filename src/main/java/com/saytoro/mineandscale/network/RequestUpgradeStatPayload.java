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

public record RequestUpgradeStatPayload(String statName) implements CustomPacketPayload {

    public static final Type<RequestUpgradeStatPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "request_upgrade_stat"));

    public static final StreamCodec<FriendlyByteBuf, RequestUpgradeStatPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.statName),
            buf -> new RequestUpgradeStatPayload(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleServer(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerProgression progression = player.getData(MineAndScale.PLAYER_PROGRESSION);
                if (progression.upgradeStat(this.statName)) {
                    TalentLogic.applyTalentAttributes(player);
                    XpEventHandler.syncPlayerData(player);
                }
            }
        });
    }
}