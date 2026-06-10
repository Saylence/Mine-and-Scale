package com.saytoro.mineandscale.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.Set;

public class PlayerProgression implements INBTSerializable<CompoundTag> {
    private int level = 1;
    private int xp = 0;
    private int talentPoints = 0;
    private int statPoints = 20; // 20 очков со старта

    private int strength = 0;
    private int dexterity = 0;
    private int vitality = 0;

    private final Set<String> unlockedTalents = new HashSet<>();

    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getTalentPoints() { return talentPoints; }
    public int getStatPoints() { return statPoints; }

    // --- ГЛОБАЛЬНЫЕ СТАТЫ (База + Таланты) ---
    public int getStrength() {
        return this.strength + (hasTalent("berserker") ? 15 : 0);
    }
    public int getDexterity() {
        return this.dexterity + (hasTalent("ninja") ? 15 : 0);
    }
    public int getVitality() {
        return this.vitality + (hasTalent("tank") ? 15 : 0);
    }

    // --- ЧИСТЫЕ БАЗОВЫЕ СТАТЫ ---
    public int getBaseStrength() { return strength; }
    public int getBaseDexterity() { return dexterity; }
    public int getBaseVitality() { return vitality; }

    public void setStrength(int value) { strength = value; }
    public void setDexterity(int value) { dexterity = value; }
    public void setVitality(int value) { vitality = value; }

    public void setTalentPoints(int value) {
        this.talentPoints = value;
    }

    public void setLevel(int newLevel) {
        if (newLevel < 1) newLevel = 1;
        this.level = newLevel;

        // Пересчёт очков характеристик
        this.statPoints = 20 + (newLevel - 1) * 3;

        // Пересчёт очков талантов (1 каждые 10 уровней)
        this.talentPoints = newLevel / 10;

        // Сбрасываем таланты? (по желанию можно оставить)
        // this.unlockedTalents.clear();
    }

    public int getXpNeededForNextLevel() { return (int) (13.5 * Math.pow(this.level, 2) - 18 * this.level + 105);  }

    public void addXp(int amount, Player player) {
        this.xp += amount;
        player.sendSystemMessage(Component.literal("§b+[MineAndScale] Опыт: " + amount + " XP"));

        while (this.xp >= getXpNeededForNextLevel()) {
            this.xp -= getXpNeededForNextLevel();
            this.level++;
            this.statPoints += 3;

            boolean gotTalent = false;
            if (this.level % 10 == 0) {
                this.talentPoints++;
                gotTalent = true;
            }

            player.sendSystemMessage(Component.literal("§6========================================"));
            player.sendSystemMessage(Component.literal("§e🎉 ПОЗДРАВЛЯЕМ! Уровень повышен до §a" + this.level + "§e!"));
            player.sendSystemMessage(Component.literal("§e✨ Получено: §a3 очка характеристик§e!"));
            if (gotTalent) {
                player.sendSystemMessage(Component.literal("§e🌟 Получено: §a1 очко талантов§e!"));
            }
            player.sendSystemMessage(Component.literal("§6========================================"));
        }
    }

    public boolean upgradeStat(String statName) {
        if (this.statPoints <= 0) return false;
        switch (statName.toLowerCase()) {
            case "strength" -> this.strength++;
            case "dexterity" -> this.dexterity++;
            case "vitality" -> this.vitality++;
            default -> { return false; }
        }
        this.statPoints--;
        return true;
    }

    public void consumeTalentPoint() { if (talentPoints > 0) talentPoints--; }
    public Set<String> getUnlockedTalents() { return unlockedTalents; }
    public void unlockTalent(String talentId) { this.unlockedTalents.add(talentId); }
    public boolean hasTalent(String talentId) { return this.unlockedTalents.contains(talentId); }

    public void setValuesFromServer(int level, int xp, int talentPoints, int statPoints, int str, int dex, int vit, java.util.List<String> talents) {
        this.level = level;
        this.xp = xp;
        this.talentPoints = talentPoints;
        this.statPoints = statPoints;
        this.strength = str;
        this.dexterity = dex;
        this.vitality = vit;
        this.unlockedTalents.clear();
        this.unlockedTalents.addAll(talents);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Level", level);
        tag.putInt("Xp", xp);
        tag.putInt("TalentPoints", talentPoints);
        tag.putInt("StatPoints", statPoints);
        tag.putInt("Strength", strength);
        tag.putInt("Dexterity", dexterity);
        tag.putInt("Vitality", vitality);

        CompoundTag talentsTag = new CompoundTag();
        int i = 0;
        for (String talent : unlockedTalents) {
            talentsTag.putString("talent_" + i++, talent);
        }
        tag.put("Talents", talentsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.level = tag.getInt("Level");
        this.xp = tag.getInt("Xp");
        this.talentPoints = tag.getInt("TalentPoints");
        this.statPoints = tag.contains("StatPoints") ? tag.getInt("StatPoints") : 0;
        this.strength = tag.getInt("Strength");
        this.dexterity = tag.getInt("Dexterity");
        this.vitality = tag.getInt("Vitality");

        CompoundTag talentsTag = tag.getCompound("Talents");
        this.unlockedTalents.clear();
        for (String key : talentsTag.getAllKeys()) {
            this.unlockedTalents.add(talentsTag.getString(key));
        }
    }
}