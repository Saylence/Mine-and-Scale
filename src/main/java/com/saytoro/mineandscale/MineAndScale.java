package com.saytoro.mineandscale;

import com.saytoro.mineandscale.command.SetStatsCommand;
import com.saytoro.mineandscale.data.PlayerProgression;
import com.saytoro.mineandscale.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(MineAndScale.MODID)
public class MineAndScale {
    public static final String MODID = "mineandscale";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<PlayerProgression>> PLAYER_PROGRESSION =
            ATTACHMENT_TYPES.register("progression",
                    () -> AttachmentType.serializable(() -> new PlayerProgression())
                            .copyOnDeath()
                            .build());

    public MineAndScale(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(ModNetworking::registerPayloads);

        NeoForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist.isClient()) {
            com.saytoro.mineandscale.client.ClientRegistration.register(modEventBus);
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SetStatsCommand.register(event.getDispatcher());
    }
}