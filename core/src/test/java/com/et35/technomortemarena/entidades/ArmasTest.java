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

/**
 * Prueba las armas sin abrir una ventana: tirar el arma, la espada que rebota y queda en el mapa, la
 * pistola, la escopeta, el punetazo y las armas que aparecen en el mapa.
 */
class ArmasTest {

    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;
    private static final int CORAZON = Jugador.PARTES_POR_CORAZON;
    private static final int VIDA_LLENA = 3 * CORAZON;

    private static final class Guion implements FuenteEntrada {
        float apuntaX;
        float apuntaY;
        boolean clic;
        boolean lanzar;
        boolean derecha;
        boolean izquierda;

        @Override
        public void leer(EntradaJugador destino) {
            destino.limpiar();
            destino.hayPuntero = true;
            destino.apuntaX = apuntaX;
            destino.apuntaY = apuntaY;
            destino.estocada = clic;
            destino.lanzar = lanzar;
            destino.derecha = derecha;
            destino.izquierda = izquierda;
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

    /**
     * Un rival quieto con la espada en la mano, para probar el desarme. La apunta hacia arriba: hacia el
     * tirador, su hoja lo empujaria lejos y el puno no llegaria.
     */
    private Jugador rivalArmado(float x) {
        Jugador rival = new Jugador(new FuenteEntrada() {
            @Override
            public void leer(EntradaJugador destino) {
                destino.limpiar();
                destino.hayPuntero = true;
                destino.apuntaX = x + 60f;
                destino.apuntaY = 3000f;
            }
        }, x, PISO, true, Color.WHITE, 3); // arranca mirando para el otro lado: la hoja nunca pasa por el tirador
        return rival;
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

    // ---- vida

    @Test
    void laVidaSeCuentaEnSextos() {
        Jugador j = blanco(1000f);
        assertEquals(VIDA_LLENA, j.getPartes());
        assertEquals(3, j.getVida());

        assertTrue(j.recibirDanio(TipoArma.DANIO_BALA, 1f, true));

        assertEquals(VIDA_LLENA - 2, j.getPartes());
        assertEquals(3, j.getVida(), "un corazon a medias cuenta como entero");
    }

    @Test
    void elPunetazoHaceMenosQueUnaBala() {
        assertTrue(TipoArma.DANIO_PUNIO < TipoArma.DANIO_BALA);
        assertTrue(TipoArma.DANIO_BALA < CORAZON);
    }

    @Test
    void despuesDeUnGolpeHayUnRatoDeInvulnerabilidad() {
        Jugador j = blanco(1000f);
        assertTrue(j.recibirDanio(1, 1f, true));
        assertFalse(j.recibirDanio(1, 1f, true), "el siguiente llega dentro de la invulnerabilidad");
        assertEquals(VIDA_LLENA - 1, j.getPartes());
    }

    @Test
    void sinVidaMuere() {
        Jugador j = blanco(1000f);
        Jugador lejos = blanco(3000f);
        for (int i = 0; i < VIDA_LLENA; i++) {
            j.recibirDanio(1, 1f, true);
            for (int k = 0; k < 20; k++) {
                j.actualizar(DT, arena, lejos); // pasa la invulnerabilidad
            }
        }
        assertEquals(0, j.getPartes());
        assertFalse(j.isVivo());
    }

    // ---- balas

    @Test
    void lasBalasCaenUnPoco() {
        Proyectil bala = new Proyectil(Proyectil.Tipo.BALA, null, 100f, 1500f, 0f, Proyectil.VELOCIDAD_BALA);
        Arena vacia = Arena.desdeGrilla(repetir('.', Arena.COLUMNAS));
        for (int i = 0; i < 40; i++) {
            bala.actualizar(DT, vacia, null, null);
        }
        float caida = 1500f - bala.getY();
        assertTrue(caida > 20f, "la bala tendria que bajar algo, bajo " + caida);
        assertTrue(caida < Jugador.ALTO, "pero no tanto como un cuerpo entero, bajo " + caida);
    }

    @Test
    void unaBalaSacaUnTercioDeCorazon() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(1600f);
        a.equipar(TipoArma.PISTOLA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.clic = true;
        pasos(a, d, 60);

        assertEquals(VIDA_LLENA - TipoArma.DANIO_BALA, d.getPartes());
        assertEquals(TipoArma.PISTOLA.getBalasIniciales() - 1, a.getBalas());
    }

    @Test
    void unaBalaSeRompeContraUnBloque() {
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
        assertEquals(VIDA_LLENA, d.getPartes());
        assertTrue(armas.getProyectiles().isEmpty());
    }

    @Test
    void laPistolaDisparaSeisBalasYSinBalasQuedaConLasManosVacias() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(TipoArma.PISTOLA);
        apuntarALaDerecha();
        assertEquals(6, a.getBalas());

        List<Proyectil> salidas = disparoHastaVaciar(a, d);

        assertEquals(6, contar(salidas, Proyectil.Tipo.BALA));
        assertNull(a.getArma(), "sin balas no vuelve la espada: queda a las pinas");
        assertEquals(0, a.getBalas());
    }

    // ---- escopeta

    @Test
    void laEscopetaDisparaTresVecesCincoPerdigonesEnAbanico() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(TipoArma.ESCOPETA);
        apuntarALaDerecha();
        assertEquals(3, a.getBalas());

        List<Proyectil> salidas = disparoHastaVaciar(a, d);

        assertEquals(3 * TipoArma.PERDIGONES, contar(salidas, Proyectil.Tipo.PERDIGON));
        float minimo = Float.MAX_VALUE;
        float maximo = -Float.MAX_VALUE;
        for (int i = 0; i < TipoArma.PERDIGONES; i++) {
            minimo = Math.min(minimo, salidas.get(i).getAngulo());
            maximo = Math.max(maximo, salidas.get(i).getAngulo());
        }
        assertTrue(maximo - minimo > 0.3f, "los perdigones salen abiertos");
        assertNull(a.getArma());
    }

    @Test
    void losPerdigonesTienenAlcanceCorto() {
        Proyectil perdigon = new Proyectil(Proyectil.Tipo.PERDIGON, null, 100f, 1500f, 0f, Proyectil.VELOCIDAD_PERDIGON);
        Arena vacia = Arena.desdeGrilla(repetir('.', Arena.COLUMNAS));
        for (int i = 0; i < 60 && perdigon.isActivo(); i++) {
            perdigon.actualizar(DT, vacia, null, null);
        }
        assertFalse(perdigon.isActivo());
        assertTrue(perdigon.getX() < Arena.ANCHO_MUNDO / 3f, "se pierde antes de cruzar un tercio de la arena");
    }

    @Test
    void deCercaLaEscopetaPegaVariosPerdigones() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(1350f);
        a.equipar(TipoArma.ESCOPETA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.clic = true;
        pasos(a, d, 30);

        // la invulnerabilidad corta deja pasar un perdigon por golpe: de cerca se lleva al menos uno
        assertTrue(d.getPartes() < VIDA_LLENA);
        assertTrue(d.getPartes() >= VIDA_LLENA - TipoArma.PERDIGONES);
    }

    // ---- tirar la espada

    @Test
    void laEspadaTiradaNoReapareceEnLaMano() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.lanzar = true;
        pasos(a, d, 1);

        assertNull(a.getArma());
        assertEquals(Proyectil.Tipo.ESPADA_LANZADA, armas.getProyectiles().get(0).getTipo());

        pasos(a, d, 180); // tres segundos
        assertNull(a.getArma(), "ya no vuelve sola");
    }

    @Test
    void laEspadaTiradaReboteaYQuedaEnElMapa() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3500f);
        guion.apuntaX = 3500f;
        guion.apuntaY = PISO + 400f; // en arco
        pasos(a, d, 20);
        guion.lanzar = true;
        pasos(a, d, 1);
        apuntarALaDerecha();

        boolean reboto = false;
        float vyAnterior = 0f;
        Proyectil espada = armas.getProyectiles().get(0);
        float yAnterior = espada.getY();
        for (int i = 0; i < 300 && !armas.getProyectiles().isEmpty(); i++) {
            pasos(a, d, 1);
            if (!armas.getProyectiles().isEmpty()) {
                float vy = espada.getY() - yAnterior;
                if (vyAnterior < 0f && vy > 0f) {
                    reboto = true;
                }
                vyAnterior = vy;
                yAnterior = espada.getY();
            }
        }
        assertTrue(reboto, "tendria que rebotar en el piso");
        assertTrue(armas.getProyectiles().isEmpty(), "termina de rebotar");
        assertEquals(1, armas.getEnElSuelo().size(), "y queda en el mapa");
        float[] enElSuelo = armas.getEnElSuelo().get(0);
        assertEquals(TipoArma.codigoDe(TipoArma.ESPADA), (int) enElSuelo[0]);
        assertEquals(PISO, enElSuelo[2], 2f, "apoyada sobre el piso");
    }

    @Test
    void laEspadaEnElSueloLaAgarraQuienTieneLasManosVacias() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3500f);
        a.equipar(TipoArma.PISTOLA);
        armas.dejarEnElSuelo(TipoArma.ESPADA, 1000f, PISO, 0f);
        pasos(a, d, 2);
        assertEquals(TipoArma.PISTOLA, a.getArma(), "con algo en la mano no la levanta");
        assertEquals(1, armas.getEnElSuelo().size());

        a.equipar(TipoArma.PISTOLA);
        for (int i = 0; i < TipoArma.PISTOLA.getBalasIniciales(); i++) {
            pasos(a, d, 20);
            guion.clic = true;
        }
        pasos(a, d, 20);
        assertEquals(TipoArma.ESPADA, a.getArma(), "con las manos vacias la agarra");
        assertTrue(armas.getEnElSuelo().isEmpty());
    }

    @Test
    void laEspadaTiradaHiereUnCorazon() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(1500f);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.lanzar = true;
        int golpesAntes = a.getContadorGolpes();
        pasos(a, d, 60);

        assertEquals(VIDA_LLENA - CORAZON, d.getPartes());
        assertEquals(golpesAntes + 1, a.getContadorGolpes(), "suena el golpe");
    }

    @Test
    void sinArmaLaHojaNoHiere() {
        Jugador a = tirador(1000f);
        Jugador lejos = blanco(3000f);
        guion.apuntaX = 0f;
        guion.apuntaY = PISO + 112f; // gira la hoja hacia la izquierda y la tira para ese lado
        pasos(a, lejos, 30);
        guion.lanzar = true;
        pasos(a, lejos, 1);
        assertNull(a.getArma());

        Jugador d = blanco(1150f);
        apuntarALaDerecha();
        pasos(a, d, 30);

        assertEquals(VIDA_LLENA, d.getPartes(), "barrer con el brazo vacio no hiere");
    }

    // ---- punetazo

    /** Deja al tirador con las manos vacias tirando la espada hacia la izquierda, lejos del rival. */
    private Jugador desarmado(float x) {
        Jugador a = tirador(x);
        Jugador lejos = blanco(3500f);
        guion.apuntaX = 0f;
        guion.apuntaY = PISO + 112f;
        pasos(a, lejos, 30);
        guion.lanzar = true;
        pasos(a, lejos, 1);
        armas.limpiar();
        return a;
    }

    @Test
    void sinArmaElClicPegaUnPunetazoQueHierePocoYEmpuja() {
        Jugador a = desarmado(1000f);
        Jugador d = blanco(1130f);
        apuntarALaDerecha();
        pasos(a, d, 20);
        float xAntes = d.getPosicion().x;
        guion.clic = true;
        pasos(a, d, 20);

        assertEquals(VIDA_LLENA - TipoArma.DANIO_PUNIO, d.getPartes());
        assertTrue(d.getPosicion().x > xAntes + 20f, "lo empuja hacia atras");
    }

    @Test
    void elPunetazoNoLlegaLejos() {
        Jugador a = desarmado(1000f);
        Jugador d = blanco(1400f);
        apuntarALaDerecha();
        pasos(a, d, 20);
        guion.clic = true;
        pasos(a, d, 20);
        assertEquals(VIDA_LLENA, d.getPartes());
    }

    @Test
    void unPunetazoPuedeHacerleSoltarElArmaAlRival() {
        Jugador a = desarmado(1000f);
        Jugador d = rivalArmado(1130f);
        d.setAzar(new Random() {
            @Override
            public int nextInt(int limite) {
                return 0; // siempre sale el desarme
            }
        });
        apuntarALaDerecha();
        pasos(a, d, 20);
        guion.clic = true;
        pasos(a, d, 5);

        assertNull(d.getArma(), "se le cayo la espada");
        assertEquals(1, armas.getEnElSuelo().size(), "queda en el suelo, a sus pies");
        assertEquals(TipoArma.codigoDe(TipoArma.ESPADA), (int) armas.getEnElSuelo().get(0)[0]);
    }

    @Test
    void sinSuerteElPunetazoNoDesarma() {
        Jugador a = desarmado(1000f);
        Jugador d = rivalArmado(1130f);
        d.setAzar(new Random() {
            @Override
            public int nextInt(int limite) {
                return 1;
            }
        });
        apuntarALaDerecha();
        pasos(a, d, 20);
        guion.clic = true;
        pasos(a, d, 5);

        assertEquals(TipoArma.ESPADA, d.getArma());
        assertEquals(VIDA_LLENA - TipoArma.DANIO_PUNIO, d.getPartes());
    }

    @Test
    void elArmaRecienCaidaNoSeAgarraEnseguida() {
        Jugador a = desarmado(1000f);
        Jugador d = blanco(3000f);
        armas.dejarEnElSuelo(TipoArma.ESPADA, 1000f, PISO, ArmasEnJuego.ESPERA_PARA_AGARRAR);
        pasos(a, d, 5);
        assertNull(a.getArma());
        pasos(a, d, (int) (ArmasEnJuego.ESPERA_PARA_AGARRAR / DT) + 2);
        assertEquals(TipoArma.ESPADA, a.getArma());
    }

    // ---- armas en el mapa

    @Test
    void cadaTantoApareceUnArmaDeFuegoSobreElPisoYSeAgarraPasandoPorEncima() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        pasos(a, d, (int) (ArmasEnJuego.INTERVALO_APARICION / DT) + 2);

        assertEquals(1, armas.getEnElSuelo().size());
        float[] arma = armas.getEnElSuelo().get(0);
        TipoArma tipo = TipoArma.porCodigo((int) arma[0]);
        assertTrue(tipo == TipoArma.PISTOLA || tipo == TipoArma.ESCOPETA);
        assertEquals(PISO, arma[2], 0.01f, "aparece apoyada sobre el piso");

        arma[1] = a.getPosicion().x;
        pasos(a, d, 1);
        assertEquals(tipo, a.getArma());
        assertEquals(tipo.getBalasIniciales(), a.getBalas());
        assertEquals(1, armas.getEnElSuelo().size(), "la espada que tenia queda en el suelo");
        assertEquals(TipoArma.codigoDe(TipoArma.ESPADA), (int) armas.getEnElSuelo().get(0)[0]);
    }

    @Test
    void aparecenPistolasYEscopetas() {
        boolean pistola = false;
        boolean escopeta = false;
        for (long semilla = 0; semilla < 30; semilla++) {
            ArmasEnJuego otras = new ArmasEnJuego(new Random(semilla), new ArrayList<>());
            otras.aparecerArma(arena);
            TipoArma tipo = TipoArma.porCodigo((int) otras.getEnElSuelo().get(0)[0]);
            pistola |= tipo == TipoArma.PISTOLA;
            escopeta |= tipo == TipoArma.ESCOPETA;
        }
        assertTrue(pistola && escopeta);
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
        a.equipar(TipoArma.ESCOPETA);
        a.recibirDanio(2, 1f, true);
        a.reiniciar(1000f, PISO, true);
        assertEquals(TipoArma.ESPADA, a.getArma());
        assertEquals(0, a.getBalas());
        assertEquals(VIDA_LLENA, a.getPartes());
    }

    @Test
    void elArmaYLasBalasViajanAlCliente() {
        Jugador a = tirador(1000f);
        a.equipar(TipoArma.ESCOPETA);
        EstadoJugador estado = new EstadoJugador();
        a.exportar(estado);
        Jugador copia = new Jugador(FuenteEntrada.NINGUNA, 0f, PISO, true, Color.WHITE, 3);
        copia.importar(estado);
        assertEquals(TipoArma.ESCOPETA, copia.getArma());
        assertEquals(3, copia.getBalas());
        assertEquals(VIDA_LLENA, copia.getPartes());
    }

    private List<Proyectil> disparoHastaVaciar(Jugador a, Jugador d) {
        List<Proyectil> salidas = new ArrayList<>();
        for (int i = 0; i < 400 && a.getArma() != null; i++) {
            guion.clic = true;
            a.actualizar(DT, arena, d);
            a.tomarLanzados(salidas);
        }
        return salidas;
    }

    private static int contar(List<Proyectil> proyectiles, Proyectil.Tipo tipo) {
        int cantidad = 0;
        for (Proyectil p : proyectiles) {
            if (p.getTipo() == tipo) {
                cantidad++;
            }
        }
        return cantidad;
    }

    private static String repetir(char c, int veces) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            texto.append(c);
        }
        return texto.toString();
    }
}
