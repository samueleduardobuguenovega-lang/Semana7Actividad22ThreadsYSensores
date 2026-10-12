package com.example.semana7actividad22threadsysensores;

/**
 * Matematica pura de los sensores: angulos de orientacion a partir de la matriz de rotacion y
 * utilidades de vectores. Sin dependencias de Android, para poder probarla en la JVM.
 */
public final class Orientacion {

    private Orientacion() {
    }

    /**
     * Devuelve {azimut, pitch, roll} en grados a partir de la matriz de rotacion de 9 elementos
     * entregada por {@code SensorManager.getRotationMatrixFromVector}. Usa la misma convencion que
     * {@code SensorManager.getOrientation}, pero implementada a mano para poder probarla.
     */
    public static float[] calcularAngulos(float[] sMatrizRotacion) {
        if (sMatrizRotacion == null || sMatrizRotacion.length < 9) {
            throw new IllegalArgumentException("La matriz de rotacion necesita 9 elementos");
        }
        float sAzimut = (float) Math.toDegrees(Math.atan2(sMatrizRotacion[1], sMatrizRotacion[4]));
        float sPitch = (float) Math.toDegrees(Math.asin(recortar(-sMatrizRotacion[7])));
        float sRoll = (float) Math.toDegrees(Math.atan2(-sMatrizRotacion[6], sMatrizRotacion[8]));
        return new float[]{normalizar(sAzimut), sPitch, sRoll};
    }

    /** Lleva un angulo al rango [0, 360). */
    public static float normalizar(float sAngulo) {
        float sResultado = sAngulo % 360f;
        if (sResultado < 0f) {
            sResultado += 360f;
        }
        return sResultado;
    }

    /** Magnitud de un vector de 3 componentes: sirve para el modulo de la aceleracion. */
    public static double modulo(double sX, double sY, double sZ) {
        return Math.sqrt(sX * sX + sY * sY + sZ * sZ);
    }

    /** Evita que un valor de la matriz fuera de [-1, 1] rompa el asin (por error de redondeo). */
    private static double recortar(double sValor) {
        if (sValor < -1.0) {
            return -1.0;
        }
        if (sValor > 1.0) {
            return 1.0;
        }
        return sValor;
    }
}
