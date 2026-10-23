package com.example.rpgmod;

import com.example.rpgmod.command.RpgCommands;
import com.example.rpgmod.data.RpgData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(RpgMod.MOD_ID)
public class RpgMod {
    public static final String MOD_ID = "rpgmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public RpgMod() {
        // Registrar comandos en el chat
        MinecraftForge.EVENT_BUS.addListener(RpgCommands::onRegisterCommands);

        // Eventos para mantener tus estadísticas al entrar o morir
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerClone);

        LOGGER.info("¡RPG Mod iniciado con sistema de Clases y Razas!");
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            RpgData.actualizarEstadisticas(p);
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            RpgData.actualizarEstadisticas(p);
        }
    }

    // Conservar datos si el jugador muere
    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getEntity().getPersistentData().merge(event.getOriginal().getPersistentData());
        }
    }
}
