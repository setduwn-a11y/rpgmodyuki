package com.example.rpgmod.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class RpgData {
    private static final String TAG_RAZA = "rpg_raza";
    private static final String TAG_CLASE = "rpg_clase";
    private static final String TAG_NIVEL = "rpg_nivel";
    private static final String TAG_XP = "rpg_xp";
    private static final String TAG_PUNTOS = "rpg_puntos";

    private static final ResourceLocation MOD_HEALTH = ResourceLocation.fromNamespaceAndPath("rpgmod", "bonus_health");
    private static final ResourceLocation MOD_SPEED = ResourceLocation.fromNamespaceAndPath("rpgmod", "bonus_speed");

    public static void setRaza(ServerPlayer p, RpgEnums.Raza raza) {
        p.getPersistentData().putString(TAG_RAZA, raza.name());
        actualizarEstadisticas(p);
    }

    public static RpgEnums.Raza getRaza(ServerPlayer p) {
        String name = p.getPersistentData().getString(TAG_RAZA);
        try { return RpgEnums.Raza.valueOf(name); } 
        catch (Exception e) { return RpgEnums.Raza.HUMANO; }
    }

    public static void setClase(ServerPlayer p, RpgEnums.Clase clase) {
        p.getPersistentData().putString(TAG_CLASE, clase.name());
        actualizarEstadisticas(p);
    }

    public static RpgEnums.Clase getClase(ServerPlayer p) {
        String name = p.getPersistentData().getString(TAG_CLASE);
        try { return RpgEnums.Clase.valueOf(name); } 
        catch (Exception e) { return RpgEnums.Clase.NINGUNA; }
    }

    // --- NIVELES Y XP ---
    public static int getNivel(ServerPlayer p) {
        int n = p.getPersistentData().getInt(TAG_NIVEL);
        return n <= 0 ? 1 : n;
    }

    public static int getXp(ServerPlayer p) {
        return p.getPersistentData().getInt(TAG_XP);
    }

    public static int getXpNecesaria(int nivel) {
        return nivel * 50;
    }

    public static int getPuntos(ServerPlayer p) {
        return p.getPersistentData().getInt(TAG_PUNTOS);
    }

    public static void setPuntos(ServerPlayer p, int puntos) {
        p.getPersistentData().putInt(TAG_PUNTOS, Math.max(0, puntos));
    }

    public static void ganarXp(ServerPlayer p, int cantidad) {
        int nivel = getNivel(p);
        int xp = getXp(p) + cantidad;
        int puntos = getPuntos(p);
        boolean subio = false;

        while (xp >= getXpNecesaria(nivel)) {
            xp -= getXpNecesaria(nivel);
            nivel++;
            puntos++;
            subio = true;
        }

        p.getPersistentData().putInt(TAG_NIVEL, nivel);
        p.getPersistentData().putInt(TAG_XP, xp);
        p.getPersistentData().putInt(TAG_PUNTOS, puntos);

        if (subio) {
            p.sendSystemMessage(Component.literal("§6§l¡SUBISTE AL NIVEL " + nivel + "! §e(+1 Punto de Habilidad. Usa /rpg arbol)"));
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            p.sendSystemMessage(Component.literal("§a+" + cantidad + " XP RPG §7(" + xp + "/" + getXpNecesaria(nivel) + ")"));
        }
    }

    // --- ÁRBOL DE HABILIDADES ---
    public static boolean tieneHabilidad(ServerPlayer p, String id) {
        return p.getPersistentData().getBoolean("skill_" + id);
    }

    public static void desbloquearHabilidad(ServerPlayer p, String id) {
        p.getPersistentData().putBoolean("skill_" + id, true);
        actualizarEstadisticas(p);
    }

    public static void actualizarEstadisticas(ServerPlayer p) {
        RpgEnums.Raza raza = getRaza(p);
        RpgEnums.Clase clase = getClase(p);

        double totalVida = raza.vidaExtra + clase.vidaExtra;
        if (tieneHabilidad(p, "vitalidad")) totalVida += 4; // +2 corazones del árbol

        double totalVelocidad = (raza.velocidadExtra + clase.velocidadExtra) / 100.0;
        if (tieneHabilidad(p, "agilidad")) totalVelocidad += 0.10; // +10% velocidad del árbol

        AttributeInstance attrVida = p.getAttribute(Attributes.MAX_HEALTH);
        if (attrVida != null) {
            attrVida.removeModifier(MOD_HEALTH);
            if (totalVida != 0) {
                attrVida.addTransientModifier(new AttributeModifier(MOD_HEALTH, totalVida, AttributeModifier.Operation.ADD_VALUE));
            }
            if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        }

        AttributeInstance attrVel = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attrVel != null) {
            attrVel.removeModifier(MOD_SPEED);
            if (totalVelocidad != 0) {
                attrVel.addTransientModifier(new AttributeModifier(MOD_SPEED, totalVelocidad, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }
}
