package com.saytoro.mineandscale.events;

import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.data.PlayerProgression;
import com.saytoro.mineandscale.logic.TalentLogic;
import com.saytoro.mineandscale.network.SyncProgressionPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

@EventBusSubscriber(modid = MineAndScale.MODID)
public class XpEventHandler {

    public static void syncPlayerData(ServerPlayer player) {
        PlayerProgression progression = player.getData(MineAndScale.PLAYER_PROGRESSION);

        SyncProgressionPayload packet = new SyncProgressionPayload(
                progression.getLevel(),
                progression.getXp(),
                progression.getTalentPoints(),
                progression.getStatPoints(),
                progression.getStrength(),
                progression.getDexterity(),
                progression.getVitality(),
                new ArrayList<>(progression.getUnlockedTalents())
        );

        PacketDistributor.sendToPlayer(player, packet);
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            int xpReward = (int) (target.getMaxHealth() * 0.8f);
            if (xpReward <= 0) xpReward = 1;

            PlayerProgression progression = player.getData(MineAndScale.PLAYER_PROGRESSION);
            progression.addXp(xpReward, player);

            syncPlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TalentLogic.applyTalentAttributes(player);
            syncPlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isWasDeath()) {
            return; // Если это не смерть — пропускаем (так как NeoForge сохраняет аттачмент сам)
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            // Задержка выполнения, чтобы новое тело игрока успело полностью инициализироваться после смерти
            player.server.execute(() -> {
                TalentLogic.applyTalentAttributes(player);
                syncPlayerData(player);
            });
        }
    }

    // НОВЫЙ ИВЕНТ: Ловит момент перехода между мирами
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Выполняем в конце тика через планировщик. Это гарантирует, что клиент уже
            // получил пакет респавна в новом мире и создал сущность игрока до отправки наших статов.
            player.server.execute(() -> {
                TalentLogic.applyTalentAttributes(player);
                syncPlayerData(player);
            });
        }
    }
}