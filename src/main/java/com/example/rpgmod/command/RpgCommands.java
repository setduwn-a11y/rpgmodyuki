package com.example.rpgmod.command;

import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.data.RpgEnums;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

public class RpgCommands {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("rpg")
            // Comando /rpg info
            .then(Commands.literal("info").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                RpgEnums.Raza raza = RpgData.getRaza(p);
                RpgEnums.Clase clase = RpgData.getClase(p);
                int corazones = (int) (p.getMaxHealth() / 2);

                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6=== PERFIL RPG ===\n" +
                    "§eRaza: §f" + raza.nombre + "\n" +
                    "§eClase: §f" + clase.nombre + "\n" +
                    "§eVida maxima: §c" + corazones + " corazones"
                ), false);
                return 1;
            }))
            // Comando /rpg clase <nombre>
            .then(Commands.literal("clase")
                .then(Commands.argument("nombre", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String arg = StringArgumentType.getString(ctx, "nombre").toUpperCase();
                        try {
                            RpgEnums.Clase clase = RpgEnums.Clase.valueOf(arg);
                            RpgData.setClase(p, clase);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aAhora eres de la clase: §f" + clase.nombre), false);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().sendFailure(Component.literal("§cClase no valida. Opciones: GUERRERO, MAGO, ARQUERO, PICARO, CLERIGO"));
                            return 0;
                        }
                    })))
            // Comando /rpg raza <nombre>
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
        );
    }
}
