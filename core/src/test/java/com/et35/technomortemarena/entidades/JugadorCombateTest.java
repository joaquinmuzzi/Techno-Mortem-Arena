package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import org.junit.jupiter.api.Test;

class JugadorCombateTest {
    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;
    private static final float HOMBRO_Y = PISO + 112f;

    private static final class Guion implements FuenteEntrada {
        private float apuntaX;
        private float apuntaY;
        private boolean apunta;
        private boolean clic;

        void apuntar(float x, float y) {
            apuntaX = x;
            apuntaY = y;
            apunta = true;
        }

        void hacerClic() {
            clic = true;
        }

        @Override
        public void leer(EntradaJugador destino) {
            destino.limpiar();
            destino.hayPuntero = apunta;
            destino.apuntaX = apuntaX;
            destino.apuntaY = apuntaY;
            destino.estocada = clic;
            clic = false;
        }
    }

    private final Arena arena = Arena.porDefecto();
    private final Guion guionA = new Guion();
    private final Guion guionD = new Guion();

    private Jugador jugador(FuenteEntrada fuente, float x, float y, boolean derecha) {
        return new Jugador(fuente, x, y, derecha, Color.WHITE, 3);
    }

    private Jugador blanco(float x, float y) {
        Jugador blanco = jugador(FuenteEntrada.NINGUNA, x, y, false);
        blanco.setDesarmado(true);
        return blanco;
    }

    private void paso(Jugador a, Jugador d) {
        a.actualizar(DT, arena, d);
        d.actualizar(DT, arena, a);
    }

    private void pasos(Jugador a, Jugador d, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            paso(a, d);
        }
    }

    @Test
    void apuntarAlRivalConLaHojaLoHiere() {
        Jugador a = jugador(guionA, 1000f, PISO, false);
        Jugador d = blanco(1150f, PISO);
        pasos(a, d, 5);
        assertEquals(3, d.getVida());
        guionA.apuntar(1200f, HOMBRO_Y);

        int fotogramas = 0;
        while (d.getVida() == 3 && fotogramas < 60) {
            paso(a, d);
            fotogramas++;
        }

        assertEquals(2, d.getVida());
        assertEquals(3, a.getVida());
        assertTrue(a.acabaDeGolpear());
    }

    @Test
    void apuntarParaOtroLadoNoHiere() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(1150f, PISO);
        guionA.apuntar(500f, HOMBRO_Y);
        a.actualizar(DT, arena, blanco(3000f, PISO));

        pasos(a, d, 60);

        assertEquals(3, d.getVida());
    }

    @Test
    void apuntarCasiVerticalPasaLejosDelRival() {
        Jugador a = jugador(guionA, 1000f, PISO, false);
        Jugador d = blanco(1100f, PISO);
        pasos(a, d, 5);
        float angulo = (float) Math.toRadians(80);
        guionA.apuntar(1068f + (float) Math.cos(angulo) * 300f, HOMBRO_Y + (float) Math.sin(angulo) * 300f);

        pasos(a, d, 30);

        assertEquals(3, d.getVida());
        assertEquals(80f, (float) Math.toDegrees(a.getAngulo()), 0.5f);
    }

    @Test
    void laEspadaApuntaExactamenteAlCursor() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);
        pasos(a, d, 5);
        guionA.apuntar(1068f + 300f, HOMBRO_Y + 400f);

        pasos(a, d, 30);

        assertEquals(Math.atan2(4, 3), a.getAngulo(), 0.001);
        assertEquals(a.getAngulo(), a.getHoja().getAngulo(), 0.0001f);
    }

    @Test
    void elJugadorMiraHaciaDondeEstaElCursor() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);

        guionA.apuntar(200f, HOMBRO_Y);
        paso(a, d);
        assertFalse(a.isMirandoDerecha());

        guionA.apuntar(2000f, HOMBRO_Y);
        paso(a, d);
        assertTrue(a.isMirandoDerecha());
    }

    @Test
    void laEstocadaAlcanzaLoQueLaHojaQuietaNo() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(1190f, PISO);
        pasos(a, d, 5);
        guionA.apuntar(1400f, HOMBRO_Y);

        pasos(a, d, 30);
        assertEquals(3, d.getVida(), "sin estocada no llega");

        guionA.hacerClic();
        pasos(a, d, 12);
        assertEquals(2, d.getVida(), "con la estocada si");
    }

    @Test
    void laEstocadaSaleYVuelve() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);
        guionA.apuntar(1400f, HOMBRO_Y);
        pasos(a, d, 5);
        assertEquals(0f, a.getExtension());

        guionA.hacerClic();
        float maxima = 0f;
        for (int i = 0; i < 15; i++) {
            paso(a, d);
            maxima = Math.max(maxima, a.getExtension());
        }

        assertTrue(maxima > 0.95f, "llega a lo mas fuerte de la estocada");
        assertEquals(0f, a.getExtension(), "y la hoja vuelve a su lugar");
    }

    @Test
    void noSePuedeEstocarSeguidoPorqueHayRecarga() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);
        guionA.apuntar(1400f, HOMBRO_Y);
        pasos(a, d, 5);

        guionA.hacerClic();
        pasos(a, d, 12);
        assertEquals(0f, a.getExtension());

        guionA.hacerClic();
        paso(a, d);
        assertEquals(0f, a.getExtension(), "un clic durante la recarga no hace nada");

        pasos(a, d, 12);
        guionA.hacerClic();
        pasos(a, d, 3);
        assertTrue(a.getExtension() > 0f, "pasada la recarga, la estocada vuelve a salir");
    }

    @Test
    void despuesDeUnGolpeElRivalEsInvulnerableUnSegundo() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(1150f, PISO);
        pasos(a, d, 5);
        guionA.apuntar(1200f, HOMBRO_Y);

        paso(a, d);
        assertEquals(2, d.getVida());

        for (int i = 0; i < 40; i++) {
            guionA.apuntar(d.getPosicion().x + 40f, HOMBRO_Y);
            paso(a, d);
        }
        assertEquals(2, d.getVida());
    }

    @Test
    void unTajoRapidoCuentaLoQueLaHojaCruzoEnElCamino() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(1110f, PISO + 172f);
        guionA.apuntar(1068f, HOMBRO_Y + 500f);

        for (int i = 0; i < 30; i++) {
            a.actualizar(DT, arena, d);
        }

        Rectangle cuerpoDelRival = new Rectangle(d.getPosicion().x, d.getPosicion().y, Jugador.ANCHO, Jugador.ALTO);
        assertFalse(a.getHoja().solapa(cuerpoDelRival), "la hoja final apunta al cielo y no lo toca");
        assertEquals(2, d.getVida(), "pero en el giro paso por encima del rival y eso cuenta");
    }

    @Test
    void dosHojasQueSeCruzanEmpujanAMbosJugadores() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = jugador(guionD, 1100f, PISO, false);
        pasos(a, d, 5);
        float xA = a.getPosicion().x;
        float xD = d.getPosicion().x;
        guionA.apuntar(1400f, HOMBRO_Y);
        guionD.apuntar(700f, HOMBRO_Y);

        paso(a, d);

        assertTrue(a.getPosicion().x < xA, "el primero retrocede");
        assertTrue(d.getPosicion().x > xD, "el segundo retrocede");
    }

    @Test
    void elBlancoNoSeMueveNiApunta() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador blanco = blanco(2000f, PISO);
        pasos(a, blanco, 5);
        float x = blanco.getPosicion().x;
        float angulo = blanco.getAngulo();

        pasos(a, blanco, 120);

        assertEquals(x, blanco.getPosicion().x, 0.001f);
        assertEquals(angulo, blanco.getAngulo(), 0.0001f);
        assertEquals(3, blanco.getVida());
    }

    @Test
    void unJugadorMuertoYaNoApuntaNiEstoca() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);
        pasos(a, d, 5);
        a.morir();
        float angulo = a.getAngulo();

        guionA.apuntar(1068f, HOMBRO_Y + 500f);
        guionA.hacerClic();
        pasos(a, d, 10);

        assertEquals(angulo, a.getAngulo(), 0.0001f);
        assertEquals(0f, a.getExtension());
        assertFalse(a.isVivo());
    }

    @Test
    void laEspadaGiraPorElArcoMasCortoAlCruzarLosCientoOchentaGrados() {
        Jugador a = jugador(guionA, 1000f, PISO, false);
        Jugador d = blanco(3000f, PISO);
        float pivoteX = 1068f - 40f;
        double arriba = Math.toRadians(170);
        guionA.apuntar(pivoteX + (float) Math.cos(arriba) * 300f, HOMBRO_Y + (float) Math.sin(arriba) * 300f);
        pasos(a, d, 30);
        double abajo = Math.toRadians(-170);
        guionA.apuntar(pivoteX + (float) Math.cos(abajo) * 300f, HOMBRO_Y + (float) Math.sin(abajo) * 300f);

        for (int i = 0; i < 30; i++) {
            paso(a, d);
            assertTrue(Math.cos(a.getAngulo()) < -0.9, "la espada se fue por el lado equivocado en el fotograma " + i);
        }
    }

    @Test
    void laEspadaNoGiraMasRapidoQueSuVelocidad() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);
        guionA.apuntar(200f, HOMBRO_Y);

        float anterior = a.getAngulo();
        for (int i = 0; i < 6; i++) {
            paso(a, d);
            float giro = Math.abs(Hoja.diferenciaAngular(anterior, a.getAngulo()));
            assertTrue(giro <= 12f * DT + 0.001f, "giro de " + giro + " rad en un fotograma");
            anterior = a.getAngulo();
        }
        assertTrue(Math.abs(Hoja.diferenciaAngular(0f, a.getAngulo())) < Math.PI - 1f, "todavia no llego al otro lado");
    }

    @Test
    void elRetardoDeLaEspadaNoDependeDeLosFotogramasPorSegundo() {
        Jugador rapido = jugador(guionA, 1000f, PISO, true);
        Jugador lento = jugador(guionD, 1000f, PISO, true);
        Jugador lejos = blanco(3000f, PISO);
        Jugador lejos2 = blanco(3000f, PISO);
        guionA.apuntar(200f, HOMBRO_Y);
        guionD.apuntar(200f, HOMBRO_Y);

        for (int i = 0; i < 12; i++) {
            rapido.actualizar(1f / 60f, arena, lejos);
        }
        for (int i = 0; i < 6; i++) {
            lento.actualizar(1f / 30f, arena, lejos2);
        }

        assertEquals(rapido.getAngulo(), lento.getAngulo(), 0.02f);
    }

    @Test
    void despuesDeUnChoqueDeHojasLaEspadaGiraMasLenta() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = jugador(guionD, 1100f, PISO, false);
        pasos(a, d, 5);
        guionA.apuntar(1400f, HOMBRO_Y);
        guionD.apuntar(700f, HOMBRO_Y);
        paso(a, d);
        guionA.apuntar(a.getPosicion().x - 400f, HOMBRO_Y + 300f);
        float antes = a.getAngulo();

        paso(a, d);

        float giro = Math.abs(Hoja.diferenciaAngular(antes, a.getAngulo()));
        assertTrue(giro <= 3f * DT + 0.001f, "bloqueado giro de " + giro + " rad en un fotograma");
    }

    @Test
    void laExtensionDeLaEstocadaNuncaSeSaleDeCeroAUnoNiBloqueado() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = jugador(guionD, 1100f, PISO, false);
        pasos(a, d, 5);
        guionA.apuntar(1400f, HOMBRO_Y);
        guionD.apuntar(700f, HOMBRO_Y);
        paso(a, d);
        guionA.apuntar(2000f, HOMBRO_Y + 50f);

        guionA.hacerClic();
        float maxima = 0f;
        for (int i = 0; i < 40; i++) {
            paso(a, d);
            assertTrue(a.getExtension() >= 0f && a.getExtension() <= 1f,
                "extension fuera de 0 a 1: " + a.getExtension() + " (no se podria mandar por la red)");
            maxima = Math.max(maxima, a.getExtension());
        }
        assertTrue(maxima > 0.9f, "la estocada igual sale");
    }
}
