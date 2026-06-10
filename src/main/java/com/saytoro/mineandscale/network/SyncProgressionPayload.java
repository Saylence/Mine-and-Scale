package com.saytoro.mineandscale.network;

import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.data.PlayerProgression;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncProgressionPayload(int level, int xp, int talentPoints, int statPoints, int str, int dex, int vit, List<String> unlockedTalents) implements CustomPacketPayload {

    public static final Type<SyncProgressionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "sync_progression"));

    public static final StreamCodec<FriendlyByteBuf, SyncProgressionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.level);
                buf.writeInt(payload.xp);
                buf.writeInt(payload.talentPoints);
                buf.writeInt(payload.statPoints);
                buf.writeInt(payload.str);
                buf.writeInt(payload.dex);
                buf.writeInt(payload.vit);
                buf.writeCollection(payload.unlockedTalents, FriendlyByteBuf::writeUtf);
            },
            buf -> new SyncProgressionPayload(
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleClient(final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerProgression progression = player.getData(MineAndScale.PLAYER_PROGRESSION);
                progression.setValuesFromServer(this.level, this.xp, this.talentPoints, this.statPoints, this.str, this.dex, this.vit, this.unlockedTalents);

                // ИСПРАВЛЕНО: Вызываем созданный публичный метод refresh() вместо защищенного rebuildWidgets()
                if (Minecraft.getInstance().screen instanceof com.saytoro.mineandscale.client.gui.TalentTreeScreen talentScreen) {
                    talentScreen.refresh();
                }
            }
        });
    }
}