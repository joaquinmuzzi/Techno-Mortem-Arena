package com.et35.technomortemarena.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.partida.RondasPartida.Fase;
import org.junit.jupiter.api.Test;

class RondasPartidaTest {

    private final RondasPartida rondas = new RondasPartida();

    /** Hace pasar la espera entre rondas y devuelve si arranco una ronda nueva. */
    private boolean esperarLaProximaRonda() {
        return rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true);
    }

    @Test
    void empiezaEnLaRondaUnoSinPuntos() {
        assertEquals(Fase.RONDA, rondas.getFase());
        assertEquals(1, rondas.getNumeroRonda());
        assertEquals(0, rondas.getRondasGanadasUno());
        assertEquals(0, rondas.getRondasGanadasDos());
    }

    @Test
    void mientrasLosDosSiguenVivosLaRondaSigue() {
        assertFalse(rondas.actualizar(1f, true, true));

        assertEquals(Fase.RONDA, rondas.getFase());
    }

    @Test
    void ganaLaRondaQuienSigueVivo() {
        rondas.actualizar(0.016f, true, false);

        assertEquals(Fase.ENTRE_RONDAS, rondas.getFase());
        assertEquals(1, rondas.getRondasGanadasUno());
        assertEquals(0, rondas.getRondasGanadasDos());
        assertEquals(0, rondas.getGanadorDeLaRonda());
    }

    @Test
    void ganaElJugadorDos() {
        rondas.actualizar(0.016f, false, true);

        assertEquals(1, rondas.getRondasGanadasDos());
        assertEquals(1, rondas.getGanadorDeLaRonda());
    }

    @Test
    void unEmpateNoSumaYLaRondaSeVuelveAJugar() {
        rondas.actualizar(0.016f, false, false);

        assertEquals(Fase.ENTRE_RONDAS, rondas.getFase());
        assertEquals(0, rondas.getRondasGanadasUno());
        assertEquals(0, rondas.getRondasGanadasDos());
        assertEquals(-1, rondas.getGanadorDeLaRonda());
    }

    @Test
    void entreRondasSeEsperaYLuegoArrancaLaSiguiente() {
        rondas.actualizar(0.016f, true, false);

        assertFalse(rondas.actualizar(1f, true, true), "todavia falta esperar");
        assertEquals(Fase.ENTRE_RONDAS, rondas.getFase());

        assertTrue(rondas.actualizar(2f, true, true), "pasada la espera arranca una ronda nueva");
        assertEquals(Fase.RONDA, rondas.getFase());
        assertEquals(2, rondas.getNumeroRonda());
    }

    @Test
    void despuesDeUnEmpateSeJuegaOtraRondaSinPuntos() {
        rondas.actualizar(0.016f, false, false);

        assertTrue(esperarLaProximaRonda());

        assertEquals(Fase.RONDA, rondas.getFase());
        assertEquals(0, rondas.getRondasGanadasUno() + rondas.getRondasGanadasDos());
    }

    @Test
    void elPrimeroEnGanarTresRondasSeLlevaLaPartida() {
        for (int ronda = 0; ronda < 2; ronda++) {
            rondas.actualizar(0.016f, true, false);
            assertTrue(esperarLaProximaRonda());
        }
        assertEquals(Fase.RONDA, rondas.getFase());

        rondas.actualizar(0.016f, true, false);

        assertEquals(Fase.GANADOR, rondas.getFase());
        assertTrue(rondas.ganoJugadorUno());
        assertEquals(3, rondas.getRondasGanadasUno());
    }

    @Test
    void enElCartelDeVictoriaLaPartidaNoAvanzaSola() {
        for (int ronda = 0; ronda < 3; ronda++) {
            rondas.actualizar(0.016f, false, true);
            esperarLaProximaRonda();
        }
        assertEquals(Fase.GANADOR, rondas.getFase());

        assertFalse(rondas.actualizar(100f, true, true));

        assertEquals(Fase.GANADOR, rondas.getFase());
        assertFalse(rondas.ganoJugadorUno());
    }

    @Test
    void laRevanchaVuelveATodoACero() {
        for (int ronda = 0; ronda < 3; ronda++) {
            rondas.actualizar(0.016f, true, false);
            esperarLaProximaRonda();
        }

        rondas.reiniciar();

        assertEquals(Fase.RONDA, rondas.getFase());
        assertEquals(1, rondas.getNumeroRonda());
        assertEquals(0, rondas.getRondasGanadasUno());
        assertEquals(-1, rondas.getGanadorDeLaRonda());
    }

    @Test
    void alAgotarseElTiempoEntraEnMuerteSubita() {
        rondas.actualizar(RondasPartida.DURACION_RONDA + 0.01f, true, true);

        assertEquals(Fase.MUERTE_SUBITA, rondas.getFase());
    }

    @Test
    void enMuerteSubitaSiUnJugadorMuereSeCierraLaRonda() {
        rondas.actualizar(RondasPartida.DURACION_RONDA + 0.01f, true, true);
        rondas.actualizar(0.016f, true, false);

        assertEquals(Fase.ENTRE_RONDAS, rondas.getFase());
        assertEquals(1, rondas.getRondasGanadasUno());
    }

    @Test
    void enMuerteSubitaSiAmbosSiguenVivosYSeAgotaElTiempoHayEmpateYSerepite() {
        rondas.actualizar(RondasPartida.DURACION_RONDA + 0.01f, true, true);
        rondas.actualizar(RondasPartida.DURACION_MUERTE_SUBITA + 0.01f, true, true);

        assertEquals(Fase.ENTRE_RONDAS, rondas.getFase());
        assertEquals(0, rondas.getRondasGanadasUno());
        assertEquals(0, rondas.getRondasGanadasDos());
    }

    @Test
    void elEstadoExportadoYImportadoCoincide() {
        rondas.actualizar(0.016f, true, false);
        esperarLaProximaRonda();
        rondas.actualizar(0.016f, false, true);
        EstadoPartida estado = new EstadoPartida();
        rondas.exportar(estado);

        RondasPartida copia = new RondasPartida();
        copia.importar(estado);

        assertEquals(rondas.getFase(), copia.getFase());
        assertEquals(rondas.getNumeroRonda(), copia.getNumeroRonda());
        assertEquals(1, copia.getRondasGanadasUno());
        assertEquals(1, copia.getRondasGanadasDos());
        assertEquals(1, copia.getGanadorDeLaRonda());
    }
}
