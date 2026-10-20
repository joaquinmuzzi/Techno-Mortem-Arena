package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.gdx.graphics.Color;

import com.et35.technomortemarena.entrada.Controles;

class JugadorTest {

    @Test
    void unJugadorNuevoArrancaVivoYConVidaCompleta() {
        Jugador jugador = new Jugador(Controles.jugadorUno(), 100f, 64f, true, Color.WHITE, 3);
        assertTrue(jugador.isVivo());
        assertEquals(3, jugador.getVida());
    }

    @Test
    void morirDejaAlJugadorFueraDeCombate() {
        Jugador jugador = new Jugador(Controles.jugadorUno(), 100f, 64f, true, Color.WHITE, 3);
        jugador.morir();
        assertFalse(jugador.isVivo());
    }

    @Test
    void reiniciarVuelveAlSpawnVivoYConVidaCompleta() {
        Jugador jugador = new Jugador(Controles.jugadorUno(), 100f, 64f, true, Color.WHITE, 3);
        jugador.morir();
        jugador.reiniciar(240f, 96f, false);
        assertTrue(jugador.isVivo());
        assertEquals(3, jugador.getVida());
        assertEquals(240f, jugador.getPosicion().x);
        assertEquals(96f, jugador.getPosicion().y);
        assertFalse(jugador.isMirandoDerecha());
    }

    @Test
    void reiniciarUsaLasVidasConLasQueSeCreoElJugador() {
        // El HUD asume que cada ronda arranca con las mismas vidas con las que se creo el duelistas.
        Jugador jugador = new Jugador(Controles.jugadorDos(), 50f, 64f, false, Color.WHITE, 5);
        jugador.reiniciar(50f, 64f, false);
        assertEquals(5, jugador.getVida());
    }
}
