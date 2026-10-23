package com.example.rpgmod.mana;

import com.example.rpgmod.data.RpgData;
import com.example.rpgmod.data.RpgEnums;
import com.example.rpgmod.network.ManaSyncPacket;
import com.example.rpgmod.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class ManaManager {
    private static final String TAG_MANA = "rpg_mana_actual";

    // Maná máximo según tu clase
    public static int getMaxMana(ServerPlayer p) {
        RpgEnums.Clase clase = RpgData.getClase(p);
        return switch (clase) {
            case MAGO -> 150;
            case CLERIGO -> 120;
            case ARQUERO, PICARO -> 80;
            case GUERRERO -> 60;
            default -> 50; // Sin clase
        };
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
        sincronizarConCliente(p);
    }

    public static boolean gastarMana(ServerPlayer p, float coste) {
        float actual = getMana(p);
        if (actual >= coste) {
            setMana(p, actual - coste);
            return true;
        }
        return false;
    }

    // Se llama cada segundo para recargar 2 puntos de maná
    public static void regenerar(ServerPlayer p) {
        float actual = getMana(p);
        int max = getMaxMana(p);
        if (actual < max) {
            setMana(p, actual + 2.0f);
        }
    }

    public static void sincronizarConCliente(ServerPlayer p) {
        NetworkHandler.CHANNEL.send(
            new ManaSyncPacket(getMana(p), getMaxMana(p)),
            PacketDistributor.PLAYER.with(p)
        );
    }
}
