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

/**
 * Prueba el combate sin abrir una ventana: dos jugadores con entrada guionada sobre la arena plana.
 *
 * <p>Medidas (escala 2): el cuerpo mide 96 x 192, el hombro esta 112 sobre los pies y a 20 del centro,
 * y la hoja mide 112, asi que un jugador en x = 1000 mirando a la derecha tiene el hombro en x = 1068 y la
 * punta en x = 1180. El piso sube hasta y = 128.
 */
class JugadorCombateTest {

    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;
    private static final float HOMBRO_Y = PISO + 112f;

    /** Entrada que el test controla: el punto apuntado y los clics. */
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

    /** Un blanco de prueba: sin entrada y sin espada, como el del modo de prueba. */
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
        // empieza mirando para el otro lado: la hoja no toca al rival hasta que se apunta hacia el
        Jugador a = jugador(guionA, 1000f, PISO, false);
        Jugador d = blanco(1150f, PISO);
        pasos(a, d, 5);
        assertEquals(3, d.getVida());
        guionA.apuntar(1200f, HOMBRO_Y);

        paso(a, d);

        assertEquals(2, d.getVida());
        assertEquals(3, a.getVida());
        assertTrue(a.acabaDeGolpear());
    }

    @Test
    void apuntarParaOtroLadoNoHiere() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(1150f, PISO);
        // el primer fotograma gira la hoja hacia la izquierda; se hace lejos del rival para que el giro no
        // cuente como un tajo sobre el
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
        // 80 grados: la punta queda en x = 1068 + 112 * cos(80) = 1087, antes del cuerpo del rival (1100)
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
        // un punto a 3 de ancho y 4 de alto del hombro (ver pivote: 1068, 240): angulo de 53.13 grados
        guionA.apuntar(1068f + 300f, HOMBRO_Y + 400f);

        paso(a, d);

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
        // la punta quieta llega a x = 1180; el rival empieza en 1190, y la estocada suma hasta 48
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
        pasos(a, d, 12); // la estocada (0,18 s) ya termino, la recarga (0,30 s) no
        assertEquals(0f, a.getExtension());

        guionA.hacerClic();
        paso(a, d);
        assertEquals(0f, a.getExtension(), "un clic durante la recarga no hace nada");

        pasos(a, d, 12); // ya paso la recarga
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

        // se mantiene la hoja sobre el rival: durante la invulnerabilidad no baja mas
        for (int i = 0; i < 40; i++) {
            guionA.apuntar(d.getPosicion().x + 40f, HOMBRO_Y);
            paso(a, d);
        }
        assertEquals(2, d.getVida());
    }

    @Test
    void unTajoRapidoCuentaLoQueLaHojaCruzoEnElCamino() {
        // el rival esta en el aire, entre la hoja horizontal (abajo) y la vertical (a la izquierda)
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(1110f, PISO + 172f);
        guionA.apuntar(1068f, HOMBRO_Y + 500f); // arriba, en un solo fotograma

        a.actualizar(DT, arena, d);

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
    void elEstadoExportadoYImportadoDibujaLaMismaHoja() {
        Jugador a = jugador(guionA, 1000f, PISO, true);
        Jugador d = blanco(3000f, PISO);
        pasos(a, d, 5);
        guionA.apuntar(1300f, HOMBRO_Y + 250f);
        guionA.hacerClic();
        pasos(a, d, 4);
        EstadoJugador estado = new EstadoJugador();
        a.exportar(estado);

        Jugador copia = jugador(FuenteEntrada.NINGUNA, 0f, 0f, false);
        copia.importar(estado);

        assertEquals(a.getPosicion().x, copia.getPosicion().x, 0.001f);
        assertEquals(a.getPosicion().y, copia.getPosicion().y, 0.001f);
        assertEquals(a.isMirandoDerecha(), copia.isMirandoDerecha());
        assertEquals(a.getExtension(), copia.getExtension(), 0.0001f);
        assertTrue(copia.getExtension() > 0f, "la copia tambien ve la hoja adelantada");
        assertArrayEquals(a.getHoja().getVertices(), copia.getHoja().getVertices(), 0.01f);
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
}
