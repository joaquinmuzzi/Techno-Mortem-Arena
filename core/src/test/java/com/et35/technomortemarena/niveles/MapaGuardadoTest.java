package com.et35.technomortemarena.niveles;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MapaGuardadoTest {

    @Test
    void guardaElNombreYLasFilas() {
        MapaGuardado mapa = new MapaGuardado("arena", new String[] {"..", "##"});

        assertEquals("arena", mapa.getNombre());
        assertArrayEquals(new String[] {"..", "##"}, mapa.getFilas());
    }

    @Test
    void cambiarElArregloOriginalNoAfectaAlMapa() {
        String[] filas = {"..", "##"};
        MapaGuardado mapa = new MapaGuardado("arena", filas);

        filas[0] = "XX";

        assertEquals("..", mapa.getFilas()[0]);
    }

    @Test
    void cambiarLasFilasDevueltasNoAfectaAlMapa() {
        MapaGuardado mapa = new MapaGuardado("arena", new String[] {"..", "##"});

        mapa.getFilas()[0] = "XX";

        assertEquals("..", mapa.getFilas()[0]);
    }
}
