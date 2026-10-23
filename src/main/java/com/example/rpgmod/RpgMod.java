package com.example.rpgmod;

import com.example.rpgmod.command.RpgCommands;
import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.data.RpgEnums;
import com.example.rpgmod.item.ModItems;
import com.example.rpgmod.mana.ManaManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(RpgMod.MOD_ID)
public class RpgMod {
    public static final String MOD_ID = "rpgmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public RpgMod() {
        // Registrar armas y pestañas del modo creativo
        ModItems.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Eventos del juego
        MinecraftForge.EVENT_BUS.addListener(RpgCommands::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(RpgCommands::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onMobDeath);
        MinecraftForge.EVENT_BUS.addListener(RpgMod::onRightClickItem);

        LOGGER.info("¡RPG Mod - Fase 5 (Armas de Clases) cargada!");
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
            comprobarArmaEquipada(p);
        }
    }

    // Si llevas en la mano un arma que NO es de tu clase, te pesa (da lentitud)
    private static void comprobarArmaEquipada(ServerPlayer p) {
        ItemStack mano = p.getMainHandItem();
        RpgEnums.Clase clase = RpgData.getClase(p);

        boolean esGuerrero = mano.is(ModItems.MANDOBLE_GUERRERO.get());
        boolean esMago = mano.is(ModItems.BACULO_MAGO.get());
        boolean esPicaro = mano.is(ModItems.DAGA_PICARO.get());
        boolean esArquero = mano.is(ModItems.ARCO_ARQUERO.get());
        boolean esClerigo = mano.is(ModItems.MAZA_CLERIGO.get());

        if ((esGuerrero && clase != RpgEnums.Clase.GUERRERO) ||
            (esMago && clase != RpgEnums.Clase.MAGO) ||
            (esPicaro && clase != RpgEnums.Clase.PICARO) ||
            (esArquero && clase != RpgEnums.Clase.ARQUERO) ||
            (esClerigo && clase != RpgEnums.Clase.CLERIGO)) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false));
            p.displayClientMessage(Component.literal("§c¡Esta arma es muy pesada para tu clase!"), true);
        }
    }

    private static void onMobDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (event.getSource().getEntity() instanceof ServerPlayer p) {
            int xpGanada = Math.max(10, (int) event.getEntity().getMaxHealth());
            RpgData.ganarXp(p, xpGanada);
        }
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity() instanceof ServerPlayer p && p.isShiftKeyDown()) {
            p.swing(InteractionHand.MAIN_HAND, true);
            RpgCommands.usarHabilidad(p);
        }
    }
}
