package com.example.rpgmod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class RpgData {
    private static final String TAG_RAZA = "rpg_raza";
    private static final String TAG_CLASE = "rpg_clase";

    private static final ResourceLocation MOD_HEALTH = ResourceLocation.fromNamespaceAndPath("rpgmod", "bonus_health");
    private static final ResourceLocation MOD_SPEED = ResourceLocation.fromNamespaceAndPath("rpgmod", "bonus_speed");

    // Guardar raza
    public static void setRaza(ServerPlayer p, RpgEnums.Raza raza) {
        p.getPersistentData().putString(TAG_RAZA, raza.name());
        actualizarEstadisticas(p);
    }

    // Obtener raza
    public static RpgEnums.Raza getRaza(ServerPlayer p) {
        String name = p.getPersistentData().getString(TAG_RAZA);
        try { return RpgEnums.Raza.valueOf(name); } 
        catch (Exception e) { return RpgEnums.Raza.HUMANO; }
    }

    // Guardar clase
    public static void setClase(ServerPlayer p, RpgEnums.Clase clase) {
        p.getPersistentData().putString(TAG_CLASE, clase.name());
        actualizarEstadisticas(p);
    }

    // Obtener clase
    public static RpgEnums.Clase getClase(ServerPlayer p) {
        String name = p.getPersistentData().getString(TAG_CLASE);
        try { return RpgEnums.Clase.valueOf(name); } 
        catch (Exception e) { return RpgEnums.Clase.NINGUNA; }
    }

    // Aplica los corazones y la velocidad segun tu raza + clase
    public static void actualizarEstadisticas(ServerPlayer p) {
        RpgEnums.Raza raza = getRaza(p);
        RpgEnums.Clase clase = getClase(p);

        double totalVida = raza.vidaExtra + clase.vidaExtra;
        double totalVelocidad = (raza.velocidadExtra + clase.velocidadExtra) / 100.0;

        // Modificar Vida
        AttributeInstance attrVida = p.getAttribute(Attributes.MAX_HEALTH);
        if (attrVida != null) {
            attrVida.removeModifier(MOD_HEALTH);
            if (totalVida != 0) {
                attrVida.addTransientModifier(new AttributeModifier(MOD_HEALTH, totalVida, AttributeModifier.Operation.ADD_VALUE));
            }
            if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        }

        // Modificar Velocidad
        AttributeInstance attrVel = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attrVel != null) {
            attrVel.removeModifier(MOD_SPEED);
            if (totalVelocidad != 0) {
                attrVel.addTransientModifier(new AttributeModifier(MOD_SPEED, totalVelocidad, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }
}
