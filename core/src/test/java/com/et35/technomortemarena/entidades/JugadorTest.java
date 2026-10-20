package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.gdx.graphics.Color;

import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;

class JugadorTest {

    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;

    @Test
    void unJugadorNuevoArrancaVivoYConVidaCompleta() {
        Jugador jugador = new Jugador(FuenteEntrada.NINGUNA, 100f, 64f, true, Color.WHITE, 3);
        assertTrue(jugador.isVivo());
        assertEquals(3, jugador.getVida());
    }

    @Test
    void morirDejaAlJugadorFueraDeCombate() {
        Jugador jugador = new Jugador(FuenteEntrada.NINGUNA, 100f, 64f, true, Color.WHITE, 3);
        jugador.morir();
        assertFalse(jugador.isVivo());
    }

    @Test
    void reiniciarVuelveAlSpawnVivoYConVidaCompleta() {
        Jugador jugador = new Jugador(FuenteEntrada.NINGUNA, 100f, 64f, true, Color.WHITE, 3);
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
        // El HUD asume que cada ronda arranca con las mismas vidas con las que se creo el duelista.
        Jugador jugador = new Jugador(FuenteEntrada.NINGUNA, 50f, 64f, false, Color.WHITE, 5);
        jugador.reiniciar(50f, 64f, false);
        assertEquals(5, jugador.getVida());
    }

    @Test
    void alMorirPorUnGolpeLaVidaQuedaEnCero() {
        Arena arena = Arena.porDefecto();
        FuenteEntrada haciaElRival = destino -> {
            destino.limpiar();
            destino.hayPuntero = true;
            destino.apuntaX = 1200f;
            destino.apuntaY = PISO + 112f;
        };
        Jugador atacante = new Jugador(haciaElRival, 1000f, PISO, false, Color.WHITE, 3);
        Jugador rival = new Jugador(FuenteEntrada.NINGUNA, 1150f, PISO, false, Color.WHITE, 1);
        rival.setDesarmado(true);

        atacante.actualizar(1f / 60f, arena, rival);

        assertFalse(rival.isVivo());
        assertEquals(0, rival.getVida(), "el HUD tiene que mostrar las vidas vacias");
    }

    @Test
    void reiniciarLimpiaLaEstocadaYLaHojaVuelveAlLugar() {
        Arena arena = Arena.porDefecto();
        FuenteEntrada estoca = destino -> {
            destino.limpiar();
            destino.hayPuntero = true;
            destino.apuntaX = 2000f;
            destino.apuntaY = PISO + 112f;
            destino.estocada = true;
        };
        Jugador jugador = new Jugador(estoca, 1000f, PISO, true, Color.WHITE, 3);
        Jugador lejos = new Jugador(FuenteEntrada.NINGUNA, 3000f, PISO, false, Color.WHITE, 3);
        for (int i = 0; i < 4; i++) {
            jugador.actualizar(1f / 60f, arena, lejos);
        }
        assertTrue(jugador.getExtension() > 0f);

        jugador.reiniciar(1000f, PISO, false);

        assertEquals(0f, jugador.getExtension());
        assertEquals(Math.PI, jugador.getAngulo(), 0.001);
    }
}
