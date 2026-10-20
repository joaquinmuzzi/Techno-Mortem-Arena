package com.et35.technomortemarena.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MarcadorPartidaTest {

    @Test
    void unMarcadorNuevoEmpiezaEnCero() {
        MarcadorPartida marcador = new MarcadorPartida();
        assertEquals(0, marcador.getRondasGanadasUno());
        assertEquals(0, marcador.getRondasGanadasDos());
        assertFalse(marcador.hayGanador());
    }

    @Test
    void cadaRondaGanadaSumaAlDuelistaCorrespondiente() {
        MarcadorPartida marcador = new MarcadorPartida();
        marcador.registrarRondaGanada(true);
        marcador.registrarRondaGanada(true);
        marcador.registrarRondaGanada(false);
        assertEquals(2, marcador.getRondasGanadasUno());
        assertEquals(1, marcador.getRondasGanadasDos());
        assertFalse(marcador.hayGanador());
    }

    @Test
    void tresRondasGanadasTerminanLaPartida() {
        MarcadorPartida marcador = new MarcadorPartida();
        marcador.registrarRondaGanada(false);
        marcador.registrarRondaGanada(false);
        marcador.registrarRondaGanada(false);
        assertTrue(marcador.hayGanador());
        assertTrue(marcador.ganoJugadorDos());
        assertFalse(marcador.ganoJugadorUno());
    }

    @Test
    void dosRondasNoAlcanzanParaGanar() {
        MarcadorPartida marcador = new MarcadorPartida();
        marcador.registrarRondaGanada(true);
        marcador.registrarRondaGanada(true);
        assertFalse(marcador.hayGanador());
    }

    @Test
    void reiniciarVuelveElMarcadorACero() {
        MarcadorPartida marcador = new MarcadorPartida();
        marcador.registrarRondaGanada(true);
        marcador.registrarRondaGanada(true);
        marcador.registrarRondaGanada(true);
        marcador.reiniciar();
        assertEquals(0, marcador.getRondasGanadasUno());
        assertEquals(0, marcador.getRondasGanadasDos());
        assertFalse(marcador.hayGanador());
    }
}
