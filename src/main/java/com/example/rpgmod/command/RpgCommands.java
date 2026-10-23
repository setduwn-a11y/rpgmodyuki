package com.example.rpgmod.command;

import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.data.RpgEnums;
import com.example.rpgmod.mana.ManaManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;

public class RpgCommands {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("rpg")
            // Perfil
            .then(Commands.literal("info").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                RpgEnums.Raza raza = RpgData.getRaza(p);
                RpgEnums.Clase clase = RpgData.getClase(p);
                int corazones = (int) (p.getMaxHealth() / 2);
                int mana = (int) ManaManager.getMana(p);
                int maxMana = ManaManager.getMaxMana(p);

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6=== PERFIL RPG ===\n" +
                    "§eRaza: §f" + raza.nombre + "\n" +
                    "§eClase: §f" + clase.nombre + "\n" +
                    "§eVida: §c" + corazones + " corazones\n" +
                    "§eManá: §9" + mana + "/" + maxMana
                ), false);
                return 1;
            }))
            // Cambiar clase
            .then(Commands.literal("clase")
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String arg = StringArgumentType.getString(ctx, "nombre").toUpperCase();
                        try {
                            RpgEnums.Clase clase = RpgEnums.Clase.valueOf(arg);
                            RpgData.setClase(p, clase);
                            ManaManager.setMana(p, ManaManager.getMaxMana(p)); // Llenar maná al cambiar
                            ctx.getSource().sendSuccess(() -> Component.literal("§aAhora eres de la clase: §f" + clase.nombre), false);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().sendFailure(Component.literal("§cClase no valida. Opciones: GUERRERO, MAGO, ARQUERO, PICARO, CLERIGO"));
                            return 0;
                        }
                    })))
            // Cambiar raza
            .then(Commands.literal("raza")
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String arg = StringArgumentType.getString(ctx, "nombre").toUpperCase();
                        try {
                            RpgEnums.Raza raza = RpgEnums.Raza.valueOf(arg);
                            RpgData.setRaza(p, raza);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aTu raza ahora es: §f" + raza.nombre), false);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().sendFailure(Component.literal("§cRaza no valida. Opciones: HUMANO, ELFO, ENANO, ORCO"));
                            return 0;
                        }
                    })))
            // Lanzar habilidad de tu clase
            .then(Commands.literal("habilidad").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                usarHabilidad(p);
                return 1;
            }))
        );
    }

    private static void usarHabilidad(ServerPlayer p) {
        RpgEnums.Clase clase = RpgData.getClase(p);
        ServerLevel level = (ServerLevel) p.level();

        switch (clase) {
            case MAGO -> {
                if (!ManaManager.gastarMana(p, 25)) {
                    p.displayClientMessage(Component.literal("§c¡No tienes suficiente maná! (Necesitas 25)"), true);
                    return;
                }
                // Rayo de fuego hacia donde mira el mago
                Vec3 ojos = p.getEyePosition();
                Vec3 mira = p.getLookAngle();
                for (double d = 1; d <= 12; d += 0.5) {
                    Vec3 punto = ojos.add(mira.scale(d));
                    level.sendParticles(ParticleTypes.FLAME, punto.x, punto.y, punto.z, 2, 0.05, 0.05, 0.05, 0.01);
                }
                // Dañar al enemigo apuntado
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().expandTowards(mira.scale(12)).inflate(1.0), e -> e != p && e.isAlive())) {
                    target.hurt(p.damageSources().indirectMagic(p, p), 8.0f);
                    target.setRemainingFireTicks(60);
                }
                level.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
                p.displayClientMessage(Component.literal("§6¡Rayo de Fuego!"), true);
            }
            case GUERRERO -> {
                if (!ManaManager.gastarMana(p, 20)) {
                    p.displayClientMessage(Component.literal("§c¡No tienes suficiente maná! (Necesitas 20)"), true);
                    return;
                }
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1)); // Fuerza II por 10 seg
                level.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7f, 1.2f);
                p.displayClientMessage(Component.literal("§c¡Grito de Guerra activado! (+Fuerza)"), true);
            }
            case ARQUERO -> {
                if (!ManaManager.gastarMana(p, 15)) {
                    p.displayClientMessage(Component.literal("§c¡No tienes suficiente maná! (Necesitas 15)"), true);
                    return;
                }
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2)); // Velocidad III por 8 seg
                level.playSound(null, p.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.5f);
                p.displayClientMessage(Component.literal("§b¡Paso del Viento! (+Velocidad)"), true);
            }
            case PICARO -> {
                if (!ManaManager.gastarMana(p, 20)) {
                    p.displayClientMessage(Component.literal("§c¡No tienes suficiente maná! (Necesitas 20)"), true);
                    return;
                }
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0)); // Invisibilidad por 8 seg
                level.playSound(null, p.blockPosition(), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.0f, 1.0f);
                p.displayClientMessage(Component.literal("§8¡Te desvaneces en las sombras!"), true);
            }
            case CLERIGO -> {
                if (!ManaManager.gastarMana(p, 30)) {
                    p.displayClientMessage(Component.literal("§c¡No tienes suficiente maná! (Necesitas 30)"), true);
                    return;
                }
                p.heal(6.0f); // Cura 3 corazones
                level.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 1.2, p.getZ(), 8, 0.3, 0.3, 0.3, 0.0);
                level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.8f);
                p.displayClientMessage(Component.literal("§a¡Sanación Divina aplicada!"), true);
            }
            default -> p.displayClientMessage(Component.literal("§cElige una clase primero con /rpg clase"), true);
        }
    }
}
