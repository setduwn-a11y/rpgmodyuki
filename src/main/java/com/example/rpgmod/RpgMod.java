package com.example.rpgmod;

import com.example.rpgmod.command.RpgCommands;
import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.mana.ManaManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onMobDeath);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onRightClickItem);

        LOGGER.info("¡RPG Mod Fase 4 cargada con exito!");
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

    private static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.tickCount % 20 == 0) {
            ManaManager.regenerar(p);
        }
    }

    // Ganar XP cuando matas a cualquier monstruo o enemigo
    private static void onMobDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getSource().getEntity() instanceof ServerPlayer p) {
            int xpGanada = Math.max(10, (int) event.getEntity().getMaxHealth());
            RpgData.ganarXp(p, xpGanada);
        }
    }

    // Lanzar habilidad al hacer Shift (Agacharse) + Clic Derecho con cualquier arma/objeto en la mano
    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity() instanceof ServerPlayer p && p.isShiftKeyDown()) {
            p.swing(InteractionHand.MAIN_HAND, true); // Animacion del brazo
            RpgCommands.usarHabilidad(p);
        }
    }
}
