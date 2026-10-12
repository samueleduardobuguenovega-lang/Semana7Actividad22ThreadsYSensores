package com.example.semana7actividad22threadsysensores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

/**
 * Pruebas unitarias de la matematica de sensores (angulos y magnitudes).
 * Corren en la JVM: gradlew.bat testDebugUnitTest
 */
public class OrientacionTest {

    @Test
    public void laMatrizIdentidadDaAngulosEnCero() {
        float[] sMatriz = {
                1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f};
        float[] sAngulos = Orientacion.calcularAngulos(sMatriz);
        assertEquals(0f, sAngulos[0], 0.001f);
        assertEquals(0f, sAngulos[1], 0.001f);
        assertEquals(0f, sAngulos[2], 0.001f);
    }

    @Test
    public void unaRotacionDeNoventaGradosEnZSeLeeComoAzimut270() {
        // Matriz de rotacion de +90 grados sobre el eje Z (giro del equipo sobre si mismo):
        //   [  0  -1   0 ]
        //   [  1   0   0 ]
        //   [  0   0   1 ]
        float[] sMatriz = {
                0f, -1f, 0f,
                1f, 0f, 0f,
                0f, 0f, 1f};
        float[] sAngulos = Orientacion.calcularAngulos(sMatriz);
        assertEquals(270f, sAngulos[0], 0.001f);
        assertEquals(0f, sAngulos[1], 0.001f);
        assertEquals(0f, sAngulos[2], 0.001f);
    }

    @Test
    public void normalizarLlevaCualquierAnguloAlRango0a360() {
        assertEquals(270f, Orientacion.normalizar(-90f), 0.001f);
        assertEquals(40f, Orientacion.normalizar(400f), 0.001f);
        assertEquals(0f, Orientacion.normalizar(0f), 0.001f);
    }

    @Test
    public void elModuloCalculaLaMagnitudDelVectorDeAceleracion() {
        assertEquals(5.0, Orientacion.modulo(3, 4, 0), 0.0001);
        assertEquals(1.0, Orientacion.modulo(0, 0, 1), 0.0001);
    }

    @Test
    public void unaMatrizIncompletaSeRechaza() {
        assertThrows(IllegalArgumentException.class, new org.junit.function.ThrowingRunnable() {
            @Override
            public void run() {
                Orientacion.calcularAngulos(new float[]{1f, 0f, 0f});
            }
        });
    }
}
