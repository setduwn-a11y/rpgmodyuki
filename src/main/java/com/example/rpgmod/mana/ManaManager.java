package com.example.rpgmod.mana;

import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.data.RpgEnums;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ManaManager {
    private static final String TAG_MANA = "rpg_mana_actual";

    public static int getMaxMana(ServerPlayer p) {
        RpgEnums.Clase clase = RpgData.getClase(p);
        int base = switch (clase) {
            case MAGO -> 150;
            case CLERIGO -> 120;
            case ARQUERO, PICARO -> 80;
            case GUERRERO -> 60;
            default -> 50;
        };
        if (RpgData.tieneHabilidad(p, "sabiduria")) {
            base += 40;
        }
        return base;
    }

    public static float getMana(ServerPlayer p) {
        if (!p.getPersistentData().contains(TAG_MANA)) {
            return getMaxMana(p);
        }
        return p.getPersistentData().getFloat(TAG_MANA);
    }

    public static void setMana(ServerPlayer p, float mana) {
        float max = getMaxMana(p);
        float valor = Math.max(0, Math.min(mana, max));
        p.getPersistentData().putFloat(TAG_MANA, valor);
        mostrarBarra(p);
    }

    public static boolean gastarMana(ServerPlayer p, float coste) {
        if (RpgData.tieneHabilidad(p, "maestria")) {
            coste = coste * 0.7f; // 30% menos de coste de maná
        }
        float actual = getMana(p);
        if (actual >= coste) {
            setMana(p, actual - coste);
            return true;
        }
        return false;
    }

    public static void regenerar(ServerPlayer p) {
        float actual = getMana(p);
        int max = getMaxMana(p);
        if (actual < max) {
            setMana(p, Math.min(max, actual + 3.0f));
        } else {
            mostrarBarra(p);
        }
    }

    public static void mostrarBarra(ServerPlayer p) {
        if (RpgData.getClase(p) == RpgEnums.Clase.NINGUNA) return;

        float actual = getMana(p);
        int max = getMaxMana(p);
        int totalBloques = 10;
        int llenos = Math.round((actual / max) * totalBloques);

        StringBuilder barra = new StringBuilder("§bNv." + RpgData.getNivel(p) + " §9✦ [");
        for (int i = 0; i < totalBloques; i++) {
            if (i < llenos) barra.append("§b■");
            else barra.append("§8■");
        }
        barra.append("§9] §f").append((int) actual).append("§7/§f").append(max).append(" §9Maná ✦");

        p.displayClientMessage(Component.literal(barra.toString()), true);
    }
}
