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

        // Вкладки
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
            int playerLevel = progression.getLevel();

            // reqLevel, leftId, leftName, rightId, rightName, secondTalentUnlockLevel
            Object[][] rows = {
                    {10, "berserker", "+15 Силы",          "ninja",         "+15 Ловкости",          70},
                    {20, "tank",      "+15 Выносливости",  "fortress",      "+8 Брони +30 HP",       80},
                    {30, "bloodlust", "+8% Вампиризм",     "acrobat",       "+20% Скор. Атаки +10% Уклон", 90},
                    {40, "crit_boost","+10% Крит +20% Крит Урон", "runner", "+15% Скор. бега",      95},
                    {50, "health_boost_1", "+5 макс. HP",  "double_jump",  "Двойной прыжок",        100}
            };

            int startY = centerY - 70;

            for (int row = 0; row < rows.length; row++) {
                int reqLevel = (int) rows[row][0];
                String leftId = (String) rows[row][1];
                String leftName = (String) rows[row][2];
                String rightId = (String) rows[row][3];
                String rightName = (String) rows[row][4];
                int secondTalentLevel = (int) rows[row][5];

                boolean rowUnlocked = playerLevel >= reqLevel;
                boolean canPickSecond = playerLevel >= secondTalentLevel;

                boolean leftUnlocked = progression.hasTalent(leftId);
                boolean rightUnlocked = progression.hasTalent(rightId);

                // Левый талант
                Button leftBtn = createTalentButton(leftId, leftName, centerX - 180, startY + row * 48,
                        rowUnlocked, leftUnlocked, rightUnlocked, canPickSecond, progression, true, reqLevel);
                this.addRenderableWidget(leftBtn);

                // Правый талант
                Button rightBtn = createTalentButton(rightId, rightName, centerX + 20, startY + row * 48,
                        rowUnlocked, rightUnlocked, leftUnlocked, canPickSecond, progression, false, reqLevel);
                this.addRenderableWidget(rightBtn);
            }
        }
    }

    private Button createTalentButton(String talentId, String name, int x, int y,
                                      boolean rowUnlocked, boolean thisUnlocked, boolean otherUnlocked,
                                      boolean canPickSecond, PlayerProgression progression, boolean isLeft, int reqLevel) {

        Button btn = Button.builder(Component.literal(name), b -> {
            PacketDistributor.sendToServer(new RequestUnlockTalentPayload(talentId));
        }).bounds(x, y, 160, 20).build();

        if (!rowUnlocked) {
            btn.active = false;
            String side = isLeft ? "Левый" : "Правый";
            btn.setMessage(Component.literal("§8[" + side + " - Ур." + reqLevel + "+]"));
        } else if (thisUnlocked) {
            btn.active = false;
            btn.setMessage(Component.literal("§a" + name));
        } else if (otherUnlocked && !canPickSecond) {
            btn.active = false;
            btn.setMessage(Component.literal("§7" + name));
        } else {
            btn.active = progression.getTalentPoints() > 0;
        }

        return btn;
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
                    centerX, centerY - 125, 0xFFFFFF);
        }

        String xpText = String.format("Уровень %d  (%d / %d XP)",
                progression.getLevel(), progression.getXp(), progression.getXpNeededForNextLevel());
        guiGraphics.drawCenteredString(this.font, xpText, centerX, centerY + 145, 0xE6AA1C);
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