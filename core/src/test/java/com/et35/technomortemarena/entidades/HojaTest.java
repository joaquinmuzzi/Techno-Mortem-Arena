package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import org.junit.jupiter.api.Test;

class HojaTest {

    private static final float EPS = 0.01f;

    private static Hoja hoja(float x, float y, float angulo, float avance) {
        Hoja h = new Hoja();
        h.colocar(x, y, angulo, avance);
        return h;
    }

    @Test
    void unaHojaHorizontalSeExtiendeHaciaLaDerecha() {
        Hoja h = hoja(100f, 50f, 0f, 0f);

        assertEquals(100f, h.getBaseX(), EPS);
        assertEquals(100f + Hoja.LARGO, h.getPuntaX(), EPS);
        assertEquals(50f, h.getPuntaY(), EPS);
    }

    @Test
    void unaHojaHaciaArribaTieneLaPuntaEncima() {
        Hoja h = hoja(100f, 50f, (float) Math.PI / 2f, 0f);

        assertEquals(100f, h.getPuntaX(), EPS);
        assertEquals(50f + Hoja.LARGO, h.getPuntaY(), EPS);
    }

    @Test
    void laEstocadaAdelantaLaBaseYLaPunta() {
        Hoja quieta = hoja(0f, 0f, 0f, 0f);
        Hoja estocada = hoja(0f, 0f, 0f, Hoja.ALCANCE_ESTOCADA);

        assertEquals(Hoja.ALCANCE_ESTOCADA, estocada.getBaseX() - quieta.getBaseX(), EPS);
        assertEquals(Hoja.ALCANCE_ESTOCADA, estocada.getPuntaX() - quieta.getPuntaX(), EPS);
    }

    @Test
    void elLargoYElGrosorSeConservanAlGirar() {
        Hoja h = hoja(10f, 10f, 0.7f, 0f);
        float[] v = h.getVertices();

        double largo = Math.hypot(v[6] - v[0], v[7] - v[1]);
        double grosor = Math.hypot(v[2] - v[0], v[3] - v[1]);

        assertEquals(Hoja.LARGO, (float) largo, EPS);
        assertEquals(Hoja.GROSOR, (float) grosor, EPS);
    }

    @Test
    void laPuntaTocaUnCuerpoDelanteYNoUnoAtras() {
        Hoja h = hoja(0f, 100f, 0f, 0f);
        Rectangle delante = new Rectangle(Hoja.LARGO - 5f, 50f, 40f, 100f);
        Rectangle atras = new Rectangle(-80f, 50f, 40f, 100f);

        assertTrue(h.solapa(delante));
        assertFalse(h.solapa(atras));
    }

    @Test
    void uncuerpoJustoFueraDelAlcanceNoSeToca() {
        Hoja h = hoja(0f, 100f, 0f, 0f);
        Rectangle fuera = new Rectangle(Hoja.LARGO + 1f, 50f, 40f, 100f);

        assertFalse(h.solapa(fuera));
    }

    @Test
    void laEstocadaAlcanzaLoQueLaHojaQuietaNo() {
        Rectangle lejos = new Rectangle(Hoja.LARGO + 5f, 50f, 40f, 100f);

        assertFalse(hoja(0f, 100f, 0f, 0f).solapa(lejos));
        assertTrue(hoja(0f, 100f, 0f, Hoja.ALCANCE_ESTOCADA).solapa(lejos));
    }

    @Test
    void unaHojaDiagonalChocaConLoQueEstaEnSuLineaYNoConLoQueEstaDeLado() {
        float angulo = (float) Math.PI / 4f;
        Hoja h = hoja(0f, 0f, angulo, 0f);
        float cx = (float) Math.cos(angulo) * Hoja.LARGO * 0.9f;
        float cy = (float) Math.sin(angulo) * Hoja.LARGO * 0.9f;
        Rectangle enLaLinea = new Rectangle(cx - 5f, cy - 5f, 10f, 10f);
        // Dentro de la caja que contiene a la hoja, pero lejos de la hoja misma (la esquina de abajo a la derecha).
        Rectangle enLaEsquina = new Rectangle(60f, 10f, 10f, 10f);

        assertTrue(h.solapa(enLaLinea));
        assertTrue(h.limites(new Rectangle()).overlaps(enLaEsquina), "la caja alineada con los ejes si lo abarca");
        assertFalse(h.solapa(enLaEsquina), "pero la hoja de verdad no lo toca");
    }

    @Test
    void dosHojasQueSeCruzanSeSolapan() {
        Hoja a = hoja(0f, 0f, (float) Math.PI / 4f, 0f);
        Hoja b = hoja(0f, Hoja.LARGO * 0.7f, (float) -Math.PI / 4f, 0f);

        assertTrue(a.solapa(b));
        assertTrue(b.solapa(a));
    }

    @Test
    void dosHojasParalelasSeparadasNoSeSolapan() {
        Hoja a = hoja(0f, 0f, 0f, 0f);
        Hoja b = hoja(0f, Hoja.GROSOR * 3f, 0f, 0f);

        assertFalse(a.solapa(b));
    }

    @Test
    void elBarridoDeUnTajoGrandeProbaPosicionesIntermedias() {
        float desde = 0f;
        float hasta = 3f; // casi media vuelta

        int pasos = Hoja.pasosDeBarrido(desde, hasta);

        assertTrue(pasos >= 15, "casi media vuelta tiene que probarse en muchos pasos");
        assertEquals(hasta, Hoja.anguloDeBarrido(desde, hasta, pasos, pasos), EPS);
        assertTrue(Hoja.anguloDeBarrido(desde, hasta, 1, pasos) < 0.3f);
    }

    @Test
    void elBarridoVaPorElArcoMasCorto() {
        float casiVuelta = (float) (2 * Math.PI) - 0.1f;

        assertEquals(1, Hoja.pasosDeBarrido(0f, casiVuelta));
        assertEquals(-0.1f, Hoja.diferenciaAngular(0f, casiVuelta), EPS);
    }

    @Test
    void sinGiroHayUnSoloPaso() {
        assertEquals(1, Hoja.pasosDeBarrido(1.2f, 1.2f));
    }
}
