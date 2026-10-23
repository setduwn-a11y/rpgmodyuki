package com.example.rpgmod.command;

import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.data.RpgEnums;
import com.example.rpgmod.item.ModItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class RpgCommands {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("rpg")
            .then(Commands.literal("info").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                int nivel = RpgData.getNivel(p);
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6=== PERFIL RPG ===\n" +
                    "§eRaza: §f" + RpgData.getRaza(p).nombre + " §7| §eClase: §f" + RpgData.getClase(p).nombre + "\n" +
                    "§eNivel: §a" + nivel + " §7(" + RpgData.getXp(p) + "/" + RpgData.getXpNecesaria(nivel) + " XP)\n" +
                    "§ePuntos de Habilidad: §b" + RpgData.getPuntos(p) + "\n" +
                    "§eVida: §c" + (int)(p.getMaxHealth() / 2) + " corazones §7| §eManá: §9" + (int)ManaManager.getMana(p) + "/" + ManaManager.getMaxMana(p)
                ), false);
                return 1;
            }))
            .then(Commands.literal("arbol").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6=== ÁRBOL DE HABILIDADES (Puntos: " + RpgData.getPuntos(p) + ") ===\n" +
                    estadoSkill(p, "vitalidad", "+2 Corazones permanentes") + "\n" +
                    estadoSkill(p, "sabiduria", "+40 de Maná máximo") + "\n" +
                    estadoSkill(p, "agilidad", "+10% de Velocidad") + "\n" +
                    estadoSkill(p, "poder", "Doble de daño en habilidades") + "\n" +
                    estadoSkill(p, "maestria", "30% menos coste de maná") + "\n" +
                    "§eUsa: §f/rpg aprender <nombre>"
                ), false);
                return 1;
            }))
            .then(Commands.literal("aprender")
                .then(Commands.argument("habilidad", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String id = StringArgumentType.getString(ctx, "habilidad").toLowerCase();
                        if (!id.equals("vitalidad") && !id.equals("sabiduria") && !id.equals("agilidad") && !id.equals("poder") && !id.equals("maestria")) {
                            ctx.getSource().sendFailure(Component.literal("§cHabilidad no válida."));
                            return 0;
                        }
                        if (RpgData.tieneHabilidad(p, id)) {
                            ctx.getSource().sendFailure(Component.literal("§cYa la tienes aprendida."));
                            return 0;
                        }
                        if (RpgData.getPuntos(p) < 1) {
                            ctx.getSource().sendFailure(Component.literal("§cNo tienes puntos de habilidad."));
                            return 0;
                        }
                        RpgData.setPuntos(p, RpgData.getPuntos(p) - 1);
                        RpgData.desbloquearHabilidad(p, id);
                        ctx.getSource().sendSuccess(() -> Component.literal("§a¡Aprendiste: §e" + id.toUpperCase() + "§a!"), false);
                        return 1;
                    })))
            .then(Commands.literal("clase")
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String arg = StringArgumentType.getString(ctx, "nombre").toUpperCase();
                        try {
                            RpgEnums.Clase clase = RpgEnums.Clase.valueOf(arg);
                            RpgData.setClase(p, clase);
                            ManaManager.setMana(p, ManaManager.getMaxMana(p));
                            ctx.getSource().sendSuccess(() -> Component.literal("§aClase elegida: §f" + clase.nombre), false);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().sendFailure(Component.literal("§cOpciones: GUERRERO, MAGO, ARQUERO, PICARO, CLERIGO"));
                            return 0;
                        }
                    })))
            .then(Commands.literal("raza")
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String arg = StringArgumentType.getString(ctx, "nombre").toUpperCase();
                        try {
                            RpgEnums.Raza raza = RpgEnums.Raza.valueOf(arg);
                            RpgData.setRaza(p, raza);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aRaza elegida: §f" + raza.nombre), false);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().sendFailure(Component.literal("§cOpciones: HUMANO, ELFO, ENANO, ORCO"));
                            return 0;
                        }
                    })))
            .then(Commands.literal("habilidad").executes(ctx -> {
                usarHabilidad(ctx.getSource().getPlayerOrException());
                return 1;
            }))
        );
    }

    private static String estadoSkill(ServerPlayer p, String id, String desc) {
        return RpgData.tieneHabilidad(p, id)
            ? "§a[✔] " + id + ": §7" + desc
            : "§7[ ] §f" + id + " §e(1 pto)§7: " + desc;
    }

    // Muestra la descripción en colores al poner el cursor sobre las armas
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.MANDOBLE_GUERRERO.get())) {
            event.getToolTip().add(Component.literal("§6Clase recomendada: §cGuerrero"));
            event.getToolTip().add(Component.literal("§7Golpe sísmico explosivo con Shift + Clic"));
        } else if (stack.is(ModItems.BACULO_MAGO.get())) {
            event.getToolTip().add(Component.literal("§6Clase recomendada: §bMago"));
            event.getToolTip().add(Component.literal("§7Dispara Rayos de Fuego mágicos"));
        } else if (stack.is(ModItems.DAGA_PICARO.get())) {
            event.getToolTip().add(Component.literal("§6Clase recomendada: §8Pícaro"));
            event.getToolTip().add(Component.literal("§7Ataque ultra rápido y paso invisible"));
        } else if (stack.is(ModItems.ARCO_ARQUERO.get())) {
            event.getToolTip().add(Component.literal("§6Clase recomendada: §aArquero"));
            event.getToolTip().add(Component.literal("§7Propulsión por ráfaga de aire"));
        } else if (stack.is(ModItems.MAZA_CLERIGO.get())) {
            event.getToolTip().add(Component.literal("§6Clase recomendada: §eClérigo"));
            event.getToolTip().add(Component.literal("§7Canaliza luz divina y curación"));
        }
    }

    public static void usarHabilidad(ServerPlayer p) {
        RpgEnums.Clase clase = RpgData.getClase(p);
        ServerLevel level = (ServerLevel) p.level();
        float mult = RpgData.tieneHabilidad(p, "poder") ? 2.0f : 1.0f;

        switch (clase) {
            case MAGO -> {
                if (!ManaManager.gastarMana(p, 25)) {
                    p.sendSystemMessage(Component.literal("§c¡Maná insuficiente!"));
                    return;
                }
                Vec3 ojos = p.getEyePosition();
                Vec3 mira = p.getLookAngle();
                for (double d = 1; d <= 14; d += 0.4) {
                    Vec3 pt = ojos.add(mira.scale(d));
                    level.sendParticles(ParticleTypes.FLAME, pt.x, pt.y, pt.z, 3, 0.08, 0.08, 0.08, 0.01);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pt.x, pt.y, pt.z, 1, 0.02, 0.02, 0.02, 0.0);
                }
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().expandTowards(mira.scale(14)).inflate(1.2), e -> e != p && e.isAlive())) {
                    target.hurt(p.damageSources().indirectMagic(p, p), 8.0f * mult);
                    target.setRemainingFireTicks(80);
                }
                level.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            case GUERRERO -> {
                if (!ManaManager.gastarMana(p, 20)) {
                    p.sendSystemMessage(Component.literal("§c¡Maná insuficiente!"));
                    return;
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 4, 1.5, 0.2, 1.5, 0.0);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1.0, p.getZ(), 6, 1.5, 0.5, 1.5, 0.0);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(4.5), e -> e != p && e.isAlive())) {
                    target.hurt(p.damageSources().playerAttack(p), 6.0f * mult);
                    target.setDeltaMovement(target.getDeltaMovement().add(0, 0.6, 0));
                }
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
                level.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
            }
            case ARQUERO -> {
                if (!ManaManager.gastarMana(p, 15)) {
                    p.sendSystemMessage(Component.literal("§c¡Maná insuficiente!"));
                    return;
                }
                level.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.5, p.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 2));
                Vec3 impulso = p.getLookAngle().scale(1.3);
                p.setDeltaMovement(impulso.x, 0.4, impulso.z);
                p.hurtMarked = true;
                level.playSound(null, p.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.5f);
            }
            case PICARO -> {
                if (!ManaManager.gastarMana(p, 20)) {
                    p.sendSystemMessage(Component.literal("§c¡Maná insuficiente!"));
                    return;
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1, p.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
                level.playSound(null, p.blockPosition(), SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            case CLERIGO -> {
                if (!ManaManager.gastarMana(p, 30)) {
                    p.sendSystemMessage(Component.literal("§c¡Maná insuficiente!"));
                    return;
                }
                p.heal(6.0f * mult);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                level.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 1.2, p.getZ(), 10, 0.6, 0.5, 0.6, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.8, 0.8, 0.8, 0.05);
                level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.8f);
            }
            default -> p.sendSystemMessage(Component.literal("§cElige una clase primero con /rpg clase"));
        }
    }
}
