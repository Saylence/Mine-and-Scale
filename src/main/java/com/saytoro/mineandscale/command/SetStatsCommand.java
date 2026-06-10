package com.saytoro.mineandscale.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.data.PlayerProgression;
import com.saytoro.mineandscale.events.XpEventHandler;
import com.saytoro.mineandscale.logic.TalentLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetStatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ms")
                .requires(source -> source.hasPermission(2)) // Защита сразу на весь корень /ms

                // ВЕТКА 1: /ms stats <str> <dex> <vit>
                .then(Commands.literal("stats")
                        .then(Commands.argument("str", IntegerArgumentType.integer())
                                .then(Commands.argument("dex", IntegerArgumentType.integer())
                                        .then(Commands.argument("vit", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    int str = IntegerArgumentType.getInteger(context, "str");
                                                    int dex = IntegerArgumentType.getInteger(context, "dex");
                                                    int vit = IntegerArgumentType.getInteger(context, "vit");

                                                    PlayerProgression p = player.getData(MineAndScale.PLAYER_PROGRESSION);

                                                    // Принудительно меняем характеристики
                                                    p.setValuesFromServer(p.getLevel(), p.getXp(), p.getTalentPoints(), p.getStatPoints(), str, dex, vit, new java.util.ArrayList<>(p.getUnlockedTalents()));

                                                    TalentLogic.applyTalentAttributes(player);
                                                    XpEventHandler.syncPlayerData(player);

                                                    player.sendSystemMessage(Component.literal("§aСтаты обновлены: Сила " + str + ", Ловкость " + dex + ", Выносливость " + vit));
                                                    return 1;
                                                })))))

                // ВЕТКА 2: /ms talentpoints <amount>
                .then(Commands.literal("talentpoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0)) // Запрещаем отрицательные числа
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    PlayerProgression p = player.getData(MineAndScale.PLAYER_PROGRESSION);

                                    // ВАЖНО: Так как мы сделали глобальное обновление стат, мы ДОЛЖНЫ использовать getBase...() методы!
                                    // Если передать обычный getDexterity(), он вернет значение с бонусом от таланта (+10),
                                    // и эта команда навсегда запишет этот бонус в базу как чистый стат, поломав логику.
                                    p.setValuesFromServer(
                                            p.getLevel(),
                                            p.getXp(),
                                            amount, // Устанавливаем новое количество очков талантов
                                            p.getStatPoints(),
                                            p.getBaseStrength(),
                                            p.getBaseDexterity(),
                                            p.getBaseVitality(),
                                            new java.util.ArrayList<>(p.getUnlockedTalents())
                                    );

                                    // Пересчитываем атрибуты и отправляем пакет синхронизации на клиент
                                    TalentLogic.applyTalentAttributes(player);
                                    XpEventHandler.syncPlayerData(player);

                                    player.sendSystemMessage(Component.literal("§aОчки талантов обновлены! Новое значение: " + amount));
                                    return 1;
                                }))));
    }
}