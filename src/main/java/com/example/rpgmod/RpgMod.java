package com.example.rpgmod;

import com.example.rpgmod.command.RpgCommands;
import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.mana.ManaManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(RpgMod.MOD_ID)
public class RpgMod {
    public static final String MOD_ID = "rpgmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public RpgMod() {
        MinecraftForge.EVENT_BUS.addListener(RpgCommands::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onLivingTick);

        LOGGER.info("¡RPG Mod iniciado con Sistema de Maná y Habilidades!");
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            RpgData.actualizarEstadisticas(p);
            ManaManager.mostrarBarra(p);
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            RpgData.actualizarEstadisticas(p);
            ManaManager.setMana(p, ManaManager.getMaxMana(p));
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getEntity().getPersistentData().merge(event.getOriginal().getPersistentData());
        }
    }

    // Cada segundo (20 ticks), regenera maná y actualiza la barra en pantalla
    private static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.tickCount % 20 == 0) {
            ManaManager.regenerar(p);
        }
    }
}
