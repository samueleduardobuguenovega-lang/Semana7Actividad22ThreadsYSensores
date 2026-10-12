package com.example.semana7actividad22threadsysensores;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Pruebas unitarias de la logica pura de conversion a escala de grises.
 * Corren en la JVM del equipo, sin necesidad de un dispositivo:
 * gradlew.bat testDebugUnitTest
 */
public class EscalaGrisTest {

    @Test
    public void blancoSigueSiendoBlanco() {
        assertEquals(255, EscalaGris.canalGris(255, 255, 255));
    }

    @Test
    public void negroSigueSiendoNegro() {
        assertEquals(0, EscalaGris.canalGris(0, 0, 0));
    }

    @Test
    public void rojoPuroUsaLaLuminanciaPercibida() {
        // Rec. 601: 0.299 * 255 = 76.245 -> 76
        assertEquals(76, EscalaGris.canalGris(255, 0, 0));
    }

    @Test
    public void verdePuroPesaMasQueElAzulPuro() {
        // El ojo humano percibe el verde mas brillante que el azul: 0.587 vs 0.114
        assertEquals(150, EscalaGris.canalGris(0, 255, 0));
        assertEquals(29, EscalaGris.canalGris(0, 0, 255));
    }

    @Test
    public void pixelGrisConservaElAlfaYRepiteElGrisEnLosTresCanales() {
        int sPixel = (0x80 << 24) | (255 << 16); // alfa 128, rojo 255, verde 0, azul 0
        int sGris = EscalaGris.pixelGris(sPixel);
        assertEquals(0x80, (sGris >>> 24) & 0xFF);
        assertEquals(76, (sGris >> 16) & 0xFF);
        assertEquals(76, (sGris >> 8) & 0xFF);
        assertEquals(76, sGris & 0xFF);
    }

    @Test
    public void losCanalesFueraDeRangoSeRecortan() {
        assertEquals(255, EscalaGris.canalGris(300, 300, 300));
        assertEquals(0, EscalaGris.canalGris(-5, -5, -5));
    }
}
