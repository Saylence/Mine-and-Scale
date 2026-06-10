package com.saytoro.mineandscale.client;

import com.saytoro.mineandscale.MineAndScale;
import com.saytoro.mineandscale.client.gui.TalentTreeScreen;
import com.saytoro.mineandscale.data.PlayerProgression;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MineAndScale.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    public static final KeyMapping TALENT_TREE_KEY = new KeyMapping(
            "key.mineandscale.open_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.mineandscale"
    );

    // Переменные для отслеживания двойного прыжка
    private static boolean jumpPressed = false;
    private static boolean canDoubleJump = false;

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(TALENT_TREE_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (TALENT_TREE_KEY.consumeClick() && Minecraft.getInstance().screen == null) {
            Minecraft.getInstance().setScreen(new TalentTreeScreen());
        }
    }

    // ЛОГИКА ДВОЙНОГО ПРЫЖКА
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PlayerProgression prog = mc.player.getData(MineAndScale.PLAYER_PROGRESSION);

        // Если игрок на земле, перезаряжаем прыжок
        if (mc.player.onGround()) {
            canDoubleJump = true;
            jumpPressed = mc.options.keyJump.isDown();
        } else if (prog.hasTalent("double_jump")) {
            // Если игрок в воздухе и у него есть талант
            boolean isJumping = mc.options.keyJump.isDown();

            // Если он только что нажал кнопку прыжка
            if (isJumping && !jumpPressed && canDoubleJump) {
                mc.player.jumpFromGround(); // Толкаем игрока
                canDoubleJump = false; // Тратим заряд
            }
            jumpPressed = isJumping;
        }
    }
}