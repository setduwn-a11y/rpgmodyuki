package com.example.rpgmod.data;

public class RpgEnums {

    // --- RAZAS ---
    public enum Raza {
        HUMANO("Humano", 2, 0),       // +1 corazon, velocidad normal
        ELFO("Elfo", -2, 8),          // -1 corazon, +8% velocidad
        ENANO("Enano", 6, -5),        // +3 corazones, -5% velocidad
        ORCO("Orco", 8, -8);          // +4 corazones, -8% velocidad

        public final String nombre;
        public final int vidaExtra;      // 2 puntos = 1 corazon
        public final int velocidadExtra; // porcentaje

        Raza(String nombre, int vidaExtra, int velocidadExtra) {
            this.nombre = nombre;
            this.vidaExtra = vidaExtra;
            this.velocidadExtra = velocidadExtra;
        }
    }

    // --- CLASES ---
    public enum Clase {
        NINGUNA("Sin clase", 0, 0),
        GUERRERO("Guerrero", 8, 0),    // +4 corazones
        MAGO("Mago", -2, 0),           // -1 corazon (usara mana mas adelante)
        ARQUERO("Arquero", 2, 5),      // +1 corazon, +5% velocidad
        PICARO("Picaro", 0, 10),       // +10% velocidad
        CLERIGO("Clerigo", 4, 0);      // +2 corazones

        public final String nombre;
        public final int vidaExtra;
        public final int velocidadExtra;

        Clase(String nombre, int vidaExtra, int velocidadExtra) {
            this.nombre = nombre;
            this.vidaExtra = vidaExtra;
            this.velocidadExtra = velocidadExtra;
        }
    }
}
