package com.example.semana7actividad22threadsysensores;

/**
 * Logica pura de conversion a escala de grises.
 * Esta separada de Android a proposito: asi se puede probar con tests unitarios de JVM.
 */
public final class EscalaGris {

    private EscalaGris() {
    }

    /**
     * Calcula la luminancia percibida de un color (coeficientes Rec. 601) y recorta el
     * resultado al rango 0..255.
     */
    public static int canalGris(int sRojo, int sVerde, int sAzul) {
        int sGris = (int) Math.round(0.299 * sRojo + 0.587 * sVerde + 0.114 * sAzul);
        if (sGris < 0) {
            return 0;
        }
        if (sGris > 255) {
            return 255;
        }
        return sGris;
    }

    /** Convierte un pixel ARGB a escala de grises conservando el canal alfa. */
    public static int pixelGris(int sPixel) {
        int sAlfa = (sPixel >>> 24) & 0xFF;
        int sRojo = (sPixel >> 16) & 0xFF;
        int sVerde = (sPixel >> 8) & 0xFF;
        int sAzul = sPixel & 0xFF;
        int sGris = canalGris(sRojo, sVerde, sAzul);
        return (sAlfa << 24) | (sGris << 16) | (sGris << 8) | sGris;
    }
}
