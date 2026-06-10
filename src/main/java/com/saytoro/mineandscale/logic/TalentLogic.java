package com.saytoro.mineandscale.logic;

import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.data.PlayerProgression;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class TalentLogic {

    private static final ResourceLocation STR_HP = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "str_hp");
    private static final ResourceLocation STR_DAMAGE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "str_damage");
    private static final ResourceLocation STR_LIFESTEAL = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "str_lifesteal");
    private static final ResourceLocation STR_ARMOR_PIERCE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "str_armor_pierce");

    private static final ResourceLocation DEX_SPEED = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_speed");
    private static final ResourceLocation DEX_ATTACK_SPEED = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_attack_speed");
    private static final ResourceLocation DEX_CRIT_CHANCE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_crit_chance");
    private static final ResourceLocation DEX_CRIT_DAMAGE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_crit_damage");
    private static final ResourceLocation DEX_RANGED = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_ranged");
    private static final ResourceLocation DEX_DODGE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_dodge");
    private static final ResourceLocation DEX_VELOCITY = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_velocity");
    private static final ResourceLocation DEX_DRAW = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "dex_draw");

    private static final ResourceLocation VIT_HP = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "vit_hp");
    private static final ResourceLocation VIT_ARMOR = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "vit_armor");

    private static final ResourceLocation TALENT_HP = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "talent_hp");
    private static final ResourceLocation TALENT_SPEED = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "talent_speed");
    private static final ResourceLocation TALENT_CRIT_CHANCE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "talent_crit_chance");
    private static final ResourceLocation TALENT_CRIT_DAMAGE = ResourceLocation.fromNamespaceAndPath(MineAndScale.MODID, "talent_crit_damage");

    private static Holder<Attribute> getApothicAttribute(String path) {
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.fromNamespaceAndPath("apothic_attributes", path));
        return attr != null ? BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr) : null;
    }

    public static void applyTalentAttributes(Player player) {
        PlayerProgression progression = player.getData(MineAndScale.PLAYER_PROGRESSION);

        // --- СИЛА ---
        applyModifier(player, Attributes.MAX_HEALTH, STR_HP, progression.getStrength() * 0.5, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.ATTACK_DAMAGE, STR_DAMAGE, progression.getStrength() * 0.15, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> lifeSteal = getApothicAttribute("life_steal");
        if (lifeSteal != null) applyModifier(player, lifeSteal, STR_LIFESTEAL, progression.getStrength() * 0.005, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> armorPierce = getApothicAttribute("armor_pierce");
        if (armorPierce != null) applyModifier(player, armorPierce, STR_ARMOR_PIERCE, progression.getStrength() * 0.12, AttributeModifier.Operation.ADD_VALUE);

        // --- ЛОВКОСТЬ ---
        applyModifier(player, Attributes.ATTACK_SPEED, DEX_ATTACK_SPEED, progression.getDexterity() * 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(player, Attributes.MOVEMENT_SPEED, DEX_SPEED, progression.getDexterity() * 0.01, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        Holder<Attribute> critChance = getApothicAttribute("crit_chance");
        if (critChance != null) applyModifier(player, critChance, DEX_CRIT_CHANCE, progression.getDexterity() * 0.01, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> critDamage = getApothicAttribute("crit_damage");
        if (critDamage != null) applyModifier(player, critDamage, DEX_CRIT_DAMAGE, progression.getDexterity() * 0.03, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> arrowDmg = getApothicAttribute("arrow_damage");
        if (arrowDmg != null) applyModifier(player, arrowDmg, DEX_RANGED, progression.getDexterity() * 0.01, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> dodgeChance = getApothicAttribute("dodge_chance");
        if (dodgeChance != null) applyModifier(player, dodgeChance, DEX_DODGE, progression.getDexterity() * 0.005, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> arrowVel = getApothicAttribute("arrow_velocity");
        if (arrowVel != null) applyModifier(player, arrowVel, DEX_VELOCITY, progression.getDexterity() * 0.01, AttributeModifier.Operation.ADD_VALUE);

        Holder<Attribute> drawSpd = getApothicAttribute("draw_speed");
        if (drawSpd != null) applyModifier(player, drawSpd, DEX_DRAW, progression.getDexterity() * 0.01, AttributeModifier.Operation.ADD_VALUE);

        // --- ВЫНОСЛИВОСТЬ ---
        applyModifier(player, Attributes.MAX_HEALTH, VIT_HP, progression.getVitality() * 1.0, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.ARMOR, VIT_ARMOR, progression.getVitality() * 0.5, AttributeModifier.Operation.ADD_VALUE);

        // --- ТАЛАНТЫ ---
        applyModifier(player, Attributes.MAX_HEALTH, TALENT_HP, progression.hasTalent("health_boost_1") ? 5.0 : 0.0, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.MOVEMENT_SPEED, TALENT_SPEED, progression.hasTalent("runner") ? 0.15 : 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (critChance != null) applyModifier(player, critChance, TALENT_CRIT_CHANCE, progression.hasTalent("crit_boost") ? 0.10 : 0.0, AttributeModifier.Operation.ADD_VALUE);
        if (critDamage != null) applyModifier(player, critDamage, TALENT_CRIT_DAMAGE, progression.hasTalent("crit_boost") ? 0.20 : 0.0, AttributeModifier.Operation.ADD_VALUE);

        // Принудительно отправляем клиенту пакет со всеми syncable-атрибутами
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket(
                    serverPlayer.getId(), serverPlayer.getAttributes().getSyncableAttributes()
            ));
        }
    }

    private static void applyModifier(Player player, Holder<Attribute> attributeHolder, ResourceLocation id, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attributeHolder);
        if (instance != null) {
            instance.removeModifier(id);
            if (value != 0) {
                instance.addTransientModifier(new AttributeModifier(id, value, op));
            }
        }
    }
}