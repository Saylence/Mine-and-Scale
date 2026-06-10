package com.saytoro.mineandscale.client.gui;

import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.data.PlayerProgression;
import com.saytoro.mineandscale.network.RequestUnlockTalentPayload;
import com.saytoro.mineandscale.network.RequestUpgradeStatPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class TalentTreeScreen extends Screen {
    private static int activeTab = 0;

    public TalentTreeScreen() {
        super(Component.literal("RPG Меню"));
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null || this.minecraft.player == null) return;
        PlayerProgression progression = this.minecraft.player.getData(MineAndScale.PLAYER_PROGRESSION);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Button tabStats = Button.builder(Component.literal("Характеристики"), b -> { activeTab = 0; this.rebuildWidgets(); })
                .bounds(centerX - 110, centerY - 85, 105, 20).build();
        Button tabTalents = Button.builder(Component.literal("Пассивные таланты"), b -> { activeTab = 1; this.rebuildWidgets(); })
                .bounds(centerX + 5, centerY - 85, 105, 20).build();

        if (activeTab == 0) tabStats.active = false;
        if (activeTab == 1) tabTalents.active = false;

        this.addRenderableWidget(tabStats);
        this.addRenderableWidget(tabTalents);

        if (activeTab == 0) {
            boolean hasPoints = progression.getStatPoints() > 0;
            Button plusStr = Button.builder(Component.literal("+"), b -> PacketDistributor.sendToServer(new RequestUpgradeStatPayload("strength")))
                    .bounds(centerX + 60, centerY - 43, 20, 14).build();
            Button plusDex = Button.builder(Component.literal("+"), b -> PacketDistributor.sendToServer(new RequestUpgradeStatPayload("dexterity")))
                    .bounds(centerX + 60, centerY - 23, 20, 14).build();
            Button plusVit = Button.builder(Component.literal("+"), b -> PacketDistributor.sendToServer(new RequestUpgradeStatPayload("vitality")))
                    .bounds(centerX + 60, centerY - 3, 20, 14).build();

            plusStr.active = hasPoints;
            plusDex.active = hasPoints;
            plusVit.active = hasPoints;

            this.addRenderableWidget(plusStr);
            this.addRenderableWidget(plusDex);
            this.addRenderableWidget(plusVit);
        }

        if (activeTab == 1) {
            String[] talentIds = {
                    "health_boost_1", "crit_boost", "runner", "double_jump",
                    "berserker", "ninja", "tank",
                    "bloodlust", "acrobat", "fortress"
            };

            String[] talentNames = {
                    "+5 макс. HP",
                    "+10% Крит, +20% Крит Урон",
                    "+15% Скор. бега",
                    "Двойной прыжок",
                    "+15 Силы",
                    "+15 Ловкости",
                    "+15 Выносливости",
                    "+8% Вампиризм",
                    "+20% Скор. Атаки +10% Уклонение",
                    "+8 Брони +30 HP"
            };

            int startX = centerX - 165;
            int startY = centerY - 40;

            for (int i = 0; i < talentIds.length; i++) {
                String tId = talentIds[i];
                String tName = talentNames[i];

                int col = i % 2;
                int row = i / 2;

                int x = startX + (col * 170);
                int y = startY + (row * 24);

                boolean isUnlocked = progression.hasTalent(tId);

                Component btnText = Component.literal(tName);
                Button btn = Button.builder(btnText, b -> {
                    PacketDistributor.sendToServer(new RequestUnlockTalentPayload(tId));
                }).bounds(x, y, 160, 20).build();

                if (isUnlocked) {
                    btn.active = false;
                    btn.setMessage(Component.literal("§a" + tName));
                } else {
                    btn.active = progression.getTalentPoints() > 0;
                }

                this.addRenderableWidget(btn);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.minecraft == null || this.minecraft.player == null) return;
        PlayerProgression progression = this.minecraft.player.getData(MineAndScale.PLAYER_PROGRESSION);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (activeTab == 0) {
            int baseStr = progression.getBaseStrength();
            int baseDex = progression.getBaseDexterity();
            int baseVit = progression.getBaseVitality();

            int effStr = progression.getStrength();
            int effDex = progression.getDexterity();
            int effVit = progression.getVitality();

            guiGraphics.drawString(this.font, "Свободные очки характеристик: §e" + progression.getStatPoints(),
                    centerX - 100, centerY - 65, 0xFFFFFF);

            guiGraphics.drawString(this.font, "Сила: §a" + baseStr + " §7(+" + (effStr - baseStr) + ")",
                    centerX - 100, centerY - 45, 0xFFFFFF);

            guiGraphics.drawString(this.font, "Ловкость: §a" + baseDex + " §7(+" + (effDex - baseDex) + ")",
                    centerX - 100, centerY - 25, 0xFFFFFF);

            guiGraphics.drawString(this.font, "Выносливость: §a" + baseVit + " §7(+" + (effVit - baseVit) + ")",
                    centerX - 100, centerY - 5, 0xFFFFFF);
        }

        if (activeTab == 1) {
            guiGraphics.drawCenteredString(this.font, "Доступные очки талантов: §e" + progression.getTalentPoints(),
                    centerX, centerY - 55, 0xFFFFFF);
        }

        // Информация об уровне — сдвинута ниже
        String xpText = String.format("Уровень %d  (%d / %d XP)",
                progression.getLevel(), progression.getXp(), progression.getXpNeededForNextLevel());
        guiGraphics.drawCenteredString(this.font, xpText, centerX, centerY + 95, 0xE6AA1C);
    }

    @Override
    public void tick() {
        super.tick();
        this.rebuildWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}