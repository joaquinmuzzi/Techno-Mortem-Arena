package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.ColocadorPiezas;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Prueba las armas sin abrir una ventana: tirar el arma, la pistola y las armas que aparecen en el mapa. */
class ArmasTest {

    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;

    private static final class Guion implements FuenteEntrada {
        float apuntaX;
        float apuntaY;
        boolean clic;
        boolean lanzar;
        boolean derecha;

        @Override
        public void leer(EntradaJugador destino) {
            destino.limpiar();
            destino.hayPuntero = true;
            destino.apuntaX = apuntaX;
            destino.apuntaY = apuntaY;
            destino.estocada = clic;
            destino.lanzar = lanzar;
            destino.derecha = derecha;
            clic = false;
            lanzar = false;
        }
    }

    private final Arena arena = Arena.porDefecto();
    private final Guion guion = new Guion();
    private final ArmasEnJuego armas = new ArmasEnJuego(new Random(1), new ArrayList<>());

    private Jugador tirador(float x) {
        return new Jugador(guion, x, PISO, true, Color.WHITE, 3);
    }

    private Jugador blanco(float x) {
        Jugador blanco = new Jugador(FuenteEntrada.NINGUNA, x, PISO, false, Color.WHITE, 3);
        blanco.setDesarmado(true);
        return blanco;
    }

    private void pasos(Jugador a, Jugador b, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            a.actualizar(DT, arena, b);
            b.actualizar(DT, arena, a);
            armas.actualizar(DT, arena, a, b);
        }
    }

    /** Apunta horizontal hacia la derecha, a la altura del pecho del rival. */
    private void apuntarALaDerecha() {
        guion.apuntaX = 3500f;
        guion.apuntaY = PISO + 112f;
    }

    @Test
    void laVidaSeCuentaEnTercios() {
        Jugador j = blanco(1000f);
        assertEquals(9, j.getTercios());
        assertEquals(3, j.getVida());

        assertTrue(j.recibirDanio(1, 1f, true));

        assertEquals(8, j.getTercios());
        assertEquals(3, j.getVida(), "un corazon por la mitad cuenta como entero");
    }

    @Test
    void despuesDeUnGolpeHayUnRatoDeInvulnerabilidad() {
        Jugador j = blanco(1000f);
        assertTrue(j.recibirDanio(1, 1f, true));
        assertFalse(j.recibirDanio(1, 1f, true), "la bala siguiente llega dentro de la invulnerabilidad");
        assertEquals(8, j.getTercios());
    }

    @Test
    void conCeroTerciosMuere() {
        Jugador j = blanco(1000f);
        for (int i = 0; i < 9; i++) {
            j.recibirDanio(1, 1f, true);
            Jugador lejos = blanco(3000f);
            for (int k = 0; k < 20; k++) {
                j.actualizar(DT, arena, lejos); // pasa la invulnerabilidad
            }
        }
        assertEquals(0, j.getTercios());
        assertFalse(j.isVivo());
    }

    @Test
    void tirarLaEspadaLaSacaDeLaManoYReapareceOtra() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.lanzar = true;
        pasos(a, d, 1);

        assertNull(a.getArma());
        assertEquals(1, armas.getProyectiles().size());
        assertEquals(Proyectil.Tipo.ESPADA_LANZADA, armas.getProyectiles().get(0).getTipo());

        pasos(a, d, (int) (Jugador.REAPARICION_ARMA / DT) + 2);
        assertEquals(TipoArma.ESPADA, a.getArma());
    }

    @Test
    void laEspadaTiradaHiereUnCorazon() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(1500f);
        apuntarALaDerecha();
        guion.apuntaY = PISO + 200f; // un poco hacia arriba: cae por la gravedad
        pasos(a, d, 3);
        guion.lanzar = true;
        int golpesAntes = a.getContadorGolpes();
        pasos(a, d, 60);

        assertEquals(6, d.getTercios());
        assertEquals(golpesAntes + 1, a.getContadorGolpes(), "suena el golpe");
    }

    @Test
    void sinArmaEnLaManoLaHojaNoHiere() {
        Jugador a = tirador(1000f);
        Jugador lejos = blanco(3000f);
        guion.apuntaX = 0f;
        guion.apuntaY = PISO + 112f; // gira la hoja hacia la izquierda y la tira para ese lado
        pasos(a, lejos, 30);
        guion.lanzar = true;
        pasos(a, lejos, 1);
        assertNull(a.getArma());

        // ahora un rival pegado, y el tirador barre con el brazo vacio por encima de el
        Jugador d = blanco(1150f);
        apuntarALaDerecha();
        pasos(a, d, 30);

        assertNull(a.getArma());
        assertEquals(9, d.getTercios());
    }

    @Test
    void laPistolaDisparaSeisBalasYDespuesVuelveLaEspada() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(TipoArma.PISTOLA);
        apuntarALaDerecha();
        assertEquals(TipoArma.BALAS_PISTOLA, a.getBalas());

        int disparadas = 0;
        List<Proyectil> salidas = new ArrayList<>();
        for (int i = 0; i < 200 && a.getArma() == TipoArma.PISTOLA; i++) {
            guion.clic = true;
            a.actualizar(DT, arena, d);
            a.tomarLanzados(salidas);
        }
        for (Proyectil p : salidas) {
            if (p.getTipo() == Proyectil.Tipo.BALA) {
                disparadas++;
            }
        }
        assertEquals(6, disparadas);
        assertEquals(TipoArma.ESPADA, a.getArma());
        assertEquals(0, a.getBalas());
    }

    @Test
    void unaBalaSacaUnTercio() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(2000f);
        a.equipar(TipoArma.PISTOLA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.clic = true;
        pasos(a, d, 60);

        assertEquals(8, d.getTercios());
        assertEquals(TipoArma.BALAS_PISTOLA - 1, a.getBalas());
    }

    @Test
    void unaBalaSeRompeContraUnBloque() {
        // una pared entre los dos
        String aire = repetir('.', Arena.COLUMNAS);
        String pared = repetir('.', 35) + "##" + repetir('.', Arena.COLUMNAS - 37);
        String piso = repetir('#', Arena.COLUMNAS);
        Arena conPared = Arena.desdeGrilla(aire, pared, pared, pared, pared, pared, pared, piso, piso, piso);
        Jugador a = tirador(1000f);
        Jugador d = blanco(2500f);
        a.equipar(TipoArma.PISTOLA);
        apuntarALaDerecha();
        for (int i = 0; i < 3; i++) {
            a.actualizar(DT, conPared, d);
        }
        guion.clic = true;
        for (int i = 0; i < 90; i++) {
            a.actualizar(DT, conPared, d);
            d.actualizar(DT, conPared, a);
            armas.actualizar(DT, conPared, a, d);
        }
        assertEquals(9, d.getTercios());
        assertTrue(armas.getProyectiles().isEmpty());
    }

    @Test
    void cadaTantoApareceUnaPistolaSobreElPisoYSeAgarraPasandoPorEncima() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        pasos(a, d, (int) (ArmasEnJuego.INTERVALO_APARICION / DT) + 2);

        assertEquals(1, armas.getEnElSuelo().size());
        float[] arma = armas.getEnElSuelo().get(0);
        assertEquals(PISO, arma[2], 0.01f, "aparece apoyada sobre el piso");

        // la llevamos al lado del jugador para no depender de donde cayo
        arma[1] = a.getPosicion().x;
        pasos(a, d, 1);
        assertEquals(TipoArma.PISTOLA, a.getArma());
        assertEquals(TipoArma.BALAS_PISTOLA, a.getBalas());
        assertTrue(armas.getEnElSuelo().isEmpty());
    }

    @Test
    void noAparecenArmasEnLasZonasProhibidas() {
        List<float[]> zonas = new ArrayList<>();
        zonas.add(ColocadorPiezas.zonaDeSpawn(24f * Arena.CELDA, PISO, Jugador.ANCHO, Jugador.ALTO));
        ArmasEnJuego conZonas = new ArmasEnJuego(new Random(3), zonas);
        for (float[] lugar : conZonas.superficies(arena)) {
            float columna = lugar[0] / Arena.CELDA;
            float[] zona = zonas.get(0);
            assertFalse(columna + 2 > zona[0] && columna < zona[2], "aparecio dentro de la zona del spawn");
        }
        assertFalse(conZonas.superficies(arena).isEmpty());
    }

    @Test
    void elBlancoDePruebaNoAgarraArmas() {
        Jugador a = tirador(100f);
        Jugador d = blanco(2000f);
        pasos(a, d, (int) (ArmasEnJuego.INTERVALO_APARICION / DT) + 2);
        armas.getEnElSuelo().get(0)[1] = d.getPosicion().x;
        pasos(a, d, 1);
        assertEquals(1, armas.getEnElSuelo().size());
    }

    @Test
    void limpiarSacaTodoDelMapa() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        pasos(a, d, (int) (ArmasEnJuego.INTERVALO_APARICION / DT) + 2);
        guion.lanzar = true;
        pasos(a, d, 1);
        armas.limpiar();
        assertTrue(armas.getProyectiles().isEmpty());
        assertTrue(armas.getEnElSuelo().isEmpty());
    }

    @Test
    void reiniciarDevuelveLaEspadaYLaVida() {
        Jugador a = tirador(1000f);
        a.equipar(TipoArma.PISTOLA);
        a.recibirDanio(2, 1f, true);
        a.reiniciar(1000f, PISO, true);
        assertEquals(TipoArma.ESPADA, a.getArma());
        assertEquals(0, a.getBalas());
        assertEquals(9, a.getTercios());
    }

    @Test
    void elArmaYLasBalasViajanAlCliente() {
        Jugador a = tirador(1000f);
        a.equipar(TipoArma.PISTOLA);
        EstadoJugador estado = new EstadoJugador();
        a.exportar(estado);
        Jugador copia = new Jugador(FuenteEntrada.NINGUNA, 0f, PISO, true, Color.WHITE, 3);
        copia.importar(estado);
        assertEquals(TipoArma.PISTOLA, copia.getArma());
        assertEquals(TipoArma.BALAS_PISTOLA, copia.getBalas());
        assertEquals(9, copia.getTercios());
    }

    private static String repetir(char c, int veces) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            texto.append(c);
        }
        return texto.toString();
    }
}
