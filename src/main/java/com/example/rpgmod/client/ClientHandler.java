package com.example.rpgmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiEvent;

public final class ClientHandler {
    private static float manaActual = 0;
    private static int manaMaximo = 0;

    public static void actualizarMana(float mana, int max) {
        manaActual = mana;
        manaMaximo = max;
    }

    public static void renderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || manaMaximo <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        int anchoPantalla = mc.getWindow().getGuiScaledWidth();
        int altoPantalla = mc.getWindow().getGuiScaledHeight();

        // Posición: justo sobre la barra de comida (lado derecho)
        int anchoBarra = 80;
        int altoBarra = 5;
        int x = anchoPantalla / 2 + 10;
        int y = altoPantalla - 49;

        // Fondo negro
        g.fill(x - 1, y - 1, x + anchoBarra + 1, y + altoBarra + 1, 0x90000000);

        // Barra azul de maná
        int relleno = (int) (anchoBarra * (manaActual / manaMaximo));
        g.fill(x, y, x + relleno, y + altoBarra, 0xFF2B78E4);

        // Texto numérico centrado encima
        String texto = (int) manaActual + "/" + manaMaximo;
        g.drawString(mc.font, texto, x + (anchoBarra / 2) - (mc.font.width(texto) / 2), y - 8, 0x80D0FF, true);
    }
}
