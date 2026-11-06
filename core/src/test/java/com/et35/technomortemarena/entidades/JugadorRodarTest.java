package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import org.junit.jupiter.api.Test;

class JugadorRodarTest {
    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;

    private static final class Guion implements FuenteEntrada {
        boolean izquierda;
        boolean derecha;
        boolean rodar;

        @Override
        public void leer(EntradaJugador destino) {
            destino.limpiar();
            destino.izquierda = izquierda;
            destino.derecha = derecha;
            destino.rodar = rodar;
            rodar = false;
        }
    }

    private final Arena arena = Arena.porDefecto();
    private final Guion guion = new Guion();
    private final Jugador rival = nuevoRival();

    private static Jugador nuevoRival() {
        Jugador rival = new Jugador(FuenteEntrada.NINGUNA, 3000f, PISO, false, Color.WHITE, 3);
        rival.setDesarmado(true);
        return rival;
    }

    private Jugador jugador(boolean mirandoDerecha) {
        Jugador jugador = new Jugador(guion, 1500f, PISO, mirandoDerecha, Color.WHITE, 3);
        jugador.setDesarmado(true);
        for (int i = 0; i < 5; i++) {
            jugador.actualizar(DT, arena, rival);
        }
        return jugador;
    }

    private void pasos(Jugador jugador, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            jugador.actualizar(DT, arena, rival);
        }
    }

    @Test
    void rodarConDireccionVaEnEsaDireccionYAchicaLaCaja() {
        Jugador j = jugador(true);
        float x0 = j.getPosicion().x;
        guion.izquierda = true;
        guion.rodar = true;
        pasos(j, 2);

        assertTrue(j.estaRodando());
        assertTrue(j.getPosicion().x < x0, "rodo hacia la izquierda");
        Rectangle caja = new Rectangle(j.getPosicion().x, j.getPosicion().y, Jugador.ANCHO, Jugador.ALTO_RODANDO);
        assertEquals(Jugador.ALTO / 2f, caja.height, 0.001f);
    }

    @Test
    void quietoRuedaHaciaDondeMira() {
        Jugador izquierda = jugador(false);
        float x0 = izquierda.getPosicion().x;
        guion.rodar = true;
        pasos(izquierda, 5);
        assertTrue(izquierda.getPosicion().x < x0);

        Jugador derecha = jugador(true);
        float x1 = derecha.getPosicion().x;
        guion.rodar = true;
        pasos(derecha, 5);
        assertTrue(derecha.getPosicion().x > x1);
    }

    @Test
    void rodandoSeAvanzaMasQueCorriendo() {
        Jugador corre = jugador(true);
        float c0 = corre.getPosicion().x;
        guion.derecha = true;
        pasos(corre, 15);
        float corrido = corre.getPosicion().x - c0;

        Jugador rueda = jugador(true);
        float r0 = rueda.getPosicion().x;
        guion.rodar = true;
        pasos(rueda, 15);
        assertTrue(rueda.getPosicion().x - r0 > corrido, "la rodada es mas rapida");
    }

    @Test
    void laRodadaTerminaYTieneEnfriamiento() {
        Jugador j = jugador(true);
        guion.derecha = true;
        guion.rodar = true;
        pasos(j, 30);
        assertFalse(j.estaRodando());

        guion.rodar = true;
        pasos(j, 1);
        assertFalse(j.estaRodando(), "todavia en enfriamiento");

        pasos(j, 60);
        guion.rodar = true;
        pasos(j, 1);
        assertTrue(j.estaRodando());
    }

    @Test
    void noRuedaEnElAire() {
        Jugador j = jugador(true);
        j.impulsar(Jugador.IMPULSO_SALTO);
        guion.rodar = true;
        pasos(j, 1);
        assertFalse(j.estaRodando());
    }

    @Test
    void rodandoNoEsInvulnerable() {
        Jugador atacante = new Jugador(new FuenteEntrada() {
            @Override
            public void leer(EntradaJugador destino) {
                destino.limpiar();
                destino.hayPuntero = true;
                destino.apuntaX = 2000f;
                destino.apuntaY = PISO + 40f;
            }
        }, 1500f, PISO, true, Color.WHITE, 3);
        Jugador victima = new Jugador(guion, 1620f, PISO, false, Color.WHITE, 3);
        victima.setDesarmado(true);
        guion.rodar = true;
        for (int i = 0; i < 30; i++) {
            atacante.actualizar(DT, arena, victima);
            victima.actualizar(DT, arena, atacante);
        }
        assertTrue(victima.getVida() < 3, "la espada igual lo hiere rodando");
    }
}
