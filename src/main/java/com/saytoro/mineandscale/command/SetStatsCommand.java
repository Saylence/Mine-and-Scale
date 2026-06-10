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
                .requires(source -> source.hasPermission(2))

                // 1. /ms stats <str> <dex> <vit>
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

                                                    p.setValuesFromServer(p.getLevel(), p.getXp(), p.getTalentPoints(), p.getStatPoints(), str, dex, vit, new java.util.ArrayList<>(p.getUnlockedTalents()));

                                                    TalentLogic.applyTalentAttributes(player);
                                                    XpEventHandler.syncPlayerData(player);

                                                    player.sendSystemMessage(Component.literal("§aСтаты обновлены: Сила " + str + ", Ловкость " + dex + ", Выносливость " + vit));
                                                    return 1;
                                                })))))

                // 2. /ms talentpoints <amount>
                .then(Commands.literal("talentpoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    PlayerProgression p = player.getData(MineAndScale.PLAYER_PROGRESSION);

                                    p.setValuesFromServer(
                                            p.getLevel(),
                                            p.getXp(),
                                            amount,
                                            p.getStatPoints(),
                                            p.getBaseStrength(),
                                            p.getBaseDexterity(),
                                            p.getBaseVitality(),
                                            new java.util.ArrayList<>(p.getUnlockedTalents())
                                    );

                                    TalentLogic.applyTalentAttributes(player);
                                    XpEventHandler.syncPlayerData(player);

                                    player.sendSystemMessage(Component.literal("§aОчки талантов обновлены! Новое значение: " + amount));
                                    return 1;
                                })))

                // 3. /ms level <amount>
                .then(Commands.literal("level")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int targetLevel = IntegerArgumentType.getInteger(context, "amount");

                                    PlayerProgression p = player.getData(MineAndScale.PLAYER_PROGRESSION);
                                    p.setLevel(targetLevel); // Этот метод нужно добавить в PlayerProgression

                                    TalentLogic.applyTalentAttributes(player);
                                    XpEventHandler.syncPlayerData(player);

                                    player.sendSystemMessage(Component.literal("§aУровень установлен на §e" + targetLevel + "§a!"));
                                    return 1;
                                })))
        );
    }
}