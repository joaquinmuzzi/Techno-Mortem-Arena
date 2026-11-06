package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Prueba las mejoras sin abrir una ventana: cada efecto, cuanto dura y como cae y se agarra. */
class PowerUpTest {

    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;

    private static final class Guion implements FuenteEntrada {
        boolean derecha;
        boolean saltar;
        boolean clic;

        @Override
        public void leer(EntradaJugador destino) {
            destino.limpiar();
            destino.hayPuntero = true;
            destino.apuntaX = 7000f;
            destino.apuntaY = PISO + 112f;
            destino.derecha = derecha;
            destino.saltar = saltar;
            destino.estocada = clic;
            saltar = false;
            clic = false;
        }
    }

    private final Arena arena = Arena.porDefecto();

    private Jugador jugador(Guion guion) {
        return new Jugador(guion, 1000f, PISO, true, Color.WHITE, 3);
    }

    private Jugador blanco() {
        Jugador blanco = new Jugador(FuenteEntrada.NINGUNA, 7000f, PISO, false, Color.WHITE, 3);
        blanco.setDesarmado(true);
        return blanco;
    }

    private static void pasos(Jugador j, Jugador otro, Arena arena, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            j.actualizar(DT, arena, otro);
        }
    }

    private float recorrido(boolean conVelocidad) {
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        Jugador otro = blanco();
        pasos(j, otro, arena, 5);
        if (conVelocidad) {
            j.activarPowerUp(PowerUp.VELOCIDAD);
        }
        float x0 = j.getPosicion().x;
        guion.derecha = true;
        pasos(j, otro, arena, 30);
        return j.getPosicion().x - x0;
    }

    @Test
    void conVelocidadCorreUnCuarentaPorCientoMas() {
        float normal = recorrido(false);
        float rapido = recorrido(true);
        assertEquals(PowerUp.FACTOR_VELOCIDAD, rapido / normal, 0.08f);
    }

    private float alturaMaxima(boolean conSalto) {
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        Jugador otro = blanco();
        pasos(j, otro, arena, 5);
        if (conSalto) {
            j.activarPowerUp(PowerUp.SALTO);
        }
        guion.saltar = true;
        float maxima = 0f;
        for (int i = 0; i < 120; i++) {
            j.actualizar(DT, arena, otro);
            maxima = Math.max(maxima, j.getPosicion().y - PISO);
        }
        return maxima;
    }

    @Test
    void conSaltoSaltaMasAlto() {
        float normal = alturaMaxima(false);
        float alto = alturaMaxima(true);
        // la altura crece con el cuadrado del impulso
        assertEquals(PowerUp.FACTOR_SALTO * PowerUp.FACTOR_SALTO, alto / normal, 0.1f);
    }

    private int disparosEnUnSegundo(boolean conPunteria) {
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        Jugador otro = blanco();
        j.equipar(TipoArma.PISTOLA);
        if (conPunteria) {
            j.activarPowerUp(PowerUp.PUNTERIA);
        }
        List<Proyectil> salidas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            guion.clic = true;
            j.actualizar(DT, arena, otro);
            j.tomarLanzados(salidas);
        }
        return salidas.size();
    }

    @Test
    void conPunteriaDisparaMasSeguido() {
        assertTrue(disparosEnUnSegundo(true) > disparosEnUnSegundo(false));
    }

    @Test
    void conPunteriaLaEscopetaAbreMenosLosPerdigones() {
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        j.equipar(TipoArma.ESCOPETA);
        j.activarPowerUp(PowerUp.PUNTERIA);
        guion.clic = true;
        j.actualizar(DT, arena, blanco());
        List<Proyectil> salidas = new ArrayList<>();
        j.tomarLanzados(salidas);
        float minimo = Float.MAX_VALUE;
        float maximo = -Float.MAX_VALUE;
        for (Proyectil p : salidas) {
            minimo = Math.min(minimo, p.getAngulo());
            maximo = Math.max(maximo, p.getAngulo());
        }
        assertTrue(maximo - minimo < 0.15f, "el abanico se cierra: " + (maximo - minimo));
    }

    @Test
    void lasMejorasDuranDiezSegundos() {
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        Jugador otro = blanco();
        j.activarPowerUp(PowerUp.VELOCIDAD);
        pasos(j, otro, arena, (int) (PowerUp.DURACION / DT) - 10);
        assertTrue(j.tienePowerUp(PowerUp.VELOCIDAD));
        pasos(j, otro, arena, 20);
        assertFalse(j.tienePowerUp(PowerUp.VELOCIDAD));
    }

    @Test
    void reiniciarSacaLasMejoras() {
        Jugador j = jugador(new Guion());
        j.activarPowerUp(PowerUp.SALTO);
        j.reiniciar(1000f, PISO, true);
        assertFalse(j.tienePowerUp(PowerUp.SALTO));
    }

    @Test
    void lasMejorasViajanAlCliente() {
        Jugador j = jugador(new Guion());
        j.activarPowerUp(PowerUp.PUNTERIA);
        EstadoJugador estado = new EstadoJugador();
        j.exportar(estado);
        Jugador copia = new Jugador(FuenteEntrada.NINGUNA, 0f, PISO, true, Color.WHITE, 3);
        copia.importar(estado);
        assertEquals(PowerUp.DURACION, copia.getTiempoPowerUp(PowerUp.PUNTERIA), 0.001f);
    }

    @Test
    void cadaTantoCaeUnaMejoraDelCieloYLaAgarraQuienPasaAunqueTengaArma() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(2), new ArrayList<>());
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        Jugador otro = blanco();
        for (int i = 0; i < (int) (ArmasEnJuego.INTERVALO_POWER_UP / DT) + 2; i++) {
            j.actualizar(DT, arena, otro);
            armas.actualizar(DT, arena, j, otro);
        }
        assertEquals(1, armas.getPowerUps().size());
        float[] mejora = armas.getPowerUps().get(0);
        assertTrue(mejora[2] > Arena.ALTO_MUNDO / 2f, "arranca en el cielo");
        for (int i = 0; i < 240; i++) {
            armas.actualizar(DT, arena, j, otro);
        }
        assertEquals(PISO, mejora[2], 0.01f, "y cae al piso");

        mejora[1] = j.getPosicion().x; // la ponemos donde esta el jugador, que tiene la espada en la mano
        armas.actualizar(DT, arena, j, otro);
        assertTrue(armas.getPowerUps().isEmpty());
        assertTrue(j.tienePowerUp(PowerUp.porIndice((int) mejora[0])));
    }

    @Test
    void recogerUnaMejoraSumaSuContadorYEquiparUnArmaNo() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(2), new ArrayList<>());
        Guion guion = new Guion();
        Jugador j = jugador(guion);
        Jugador otro = blanco();
        assertEquals(0, j.getContadorPowerUps());
        j.equipar(TipoArma.PISTOLA);
        assertEquals(0, j.getContadorPowerUps(), "un arma no es una mejora");

        armas.aparecerPowerUp(arena);
        float[] mejora = armas.getPowerUps().get(0);
        for (int i = 0; i < 300; i++) {
            armas.actualizar(DT, arena, j, otro);
        }
        mejora[1] = j.getPosicion().x;
        mejora[2] = PISO;
        armas.actualizar(DT, arena, j, otro);
        assertTrue(armas.getPowerUps().isEmpty(), "la agarra aunque tenga un arma");
        assertEquals(1, j.getContadorPowerUps(), "suena el power-up");
    }

    @Test
    void elBlancoDePruebaNoAgarraMejorasYLimpiarLasSaca() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(2), new ArrayList<>());
        Jugador j = jugador(new Guion());
        Jugador otro = blanco();
        armas.aparecerPowerUp(arena);
        for (int i = 0; i < 300; i++) {
            armas.actualizar(DT, arena, j, otro);
        }
        armas.getPowerUps().get(0)[1] = otro.getPosicion().x;
        armas.actualizar(DT, arena, j, otro);
        assertEquals(1, armas.getPowerUps().size());
        armas.limpiar();
        assertTrue(armas.getPowerUps().isEmpty());
    }
}
