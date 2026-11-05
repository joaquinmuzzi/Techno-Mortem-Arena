package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Prueba las armas nuevas sin abrir una ventana: el rifle laser, el lanzacohetes y su explosion, el reflejo
 * de las balas en la hoja y la espada laser.
 */
class ArmasNuevasTest {

    private static final float DT = 1f / 60f;
    private static final float PISO = Arena.FILAS_PISO * Arena.CELDA;
    private static final float HOMBRO_Y = PISO + 112f;
    private static final int CORAZON = Jugador.PARTES_POR_CORAZON;
    private static final int VIDA_LLENA = 3 * CORAZON;

    private static final class Guion implements FuenteEntrada {
        float apuntaX = 7000f;
        float apuntaY = HOMBRO_Y;
        boolean clic;

        @Override
        public void leer(EntradaJugador destino) {
            destino.limpiar();
            destino.hayPuntero = true;
            destino.apuntaX = apuntaX;
            destino.apuntaY = apuntaY;
            destino.estocada = clic;
            clic = false;
        }
    }

    private final Arena arena = Arena.porDefecto();
    /** Un rival lejos de todo, para actualizar a uno solo. */
    private static final Jugador LEJOS = blanco(7000f);

    private static Jugador jugador(FuenteEntrada fuente, float x, boolean derecha) {
        return new Jugador(fuente, x, PISO, derecha, Color.WHITE, 3);
    }

    private static Jugador blanco(float x) {
        Jugador blanco = jugador(FuenteEntrada.NINGUNA, x, false);
        blanco.setDesarmado(true);
        return blanco;
    }

    private static List<Proyectil> disparar(Jugador j, Guion guion, Jugador otro, Arena arena) {
        guion.clic = true;
        j.actualizar(DT, arena, otro);
        List<Proyectil> salidas = new ArrayList<>();
        j.tomarLanzados(salidas);
        return salidas;
    }

    // ---- rifle laser

    @Test
    void elRifleLaserTraeVeinteRayosRapidosQueNoCaen() {
        Guion guion = new Guion();
        Jugador j = jugador(guion, 1000f, true);
        Jugador otro = blanco(7000f);
        j.equipar(TipoArma.RIFLE_LASER);
        assertEquals(20, j.getBalas());
        List<Proyectil> salidas = disparar(j, guion, otro, arena);
        assertEquals(1, salidas.size());
        Proyectil rayo = salidas.get(0);
        assertEquals(Proyectil.Tipo.LASER, rayo.getTipo());
        assertTrue(rayo.getVx() > Proyectil.VELOCIDAD_BALA, "mas rapido que una bala");
        float y0 = rayo.getY();
        for (int i = 0; i < 20; i++) {
            rayo.actualizar(DT, arena, null, null);
        }
        assertEquals(y0, rayo.getY(), 0.5f, "sin caida");
    }

    @Test
    void cadaRayoSacaUnSextoYNoRespetaLaInvulnerabilidad() {
        Jugador otro = blanco(2000f);
        for (int i = 0; i < 3; i++) {
            Proyectil rayo = new Proyectil(Proyectil.Tipo.LASER, null, 1900f, PISO + 80f, 0f, Proyectil.VELOCIDAD_LASER);
            for (int k = 0; k < 10 && rayo.isActivo(); k++) {
                rayo.actualizar(DT, arena, otro, null);
            }
        }
        assertEquals(VIDA_LLENA - 3 * TipoArma.DANIO_LASER, otro.getPartes());
    }

    @Test
    void elRayoLaserNoSeReflejaEnLaHoja() {
        Jugador espadachin = jugador(FuenteEntrada.NINGUNA, 2000f, false);
        espadachin.actualizar(DT, arena, LEJOS);
        float hojaX = espadachin.getHoja().getPuntaX();
        Proyectil rayo = new Proyectil(Proyectil.Tipo.LASER, null, hojaX - 200f, espadachin.getHoja().getPuntaY(), 0f,
            Proyectil.VELOCIDAD_LASER);
        rayo.actualizar(DT, arena, espadachin, null);
        assertTrue(rayo.getVx() > 0f);
    }

    // ---- lanzacohetes

    @Test
    void elLanzacohetesTraeUnCoheteMedioLento() {
        Guion guion = new Guion();
        Jugador j = jugador(guion, 1000f, true);
        j.equipar(TipoArma.LANZACOHETES);
        assertEquals(1, j.getBalas());
        List<Proyectil> salidas = disparar(j, guion, blanco(7000f), arena);
        assertEquals(Proyectil.Tipo.COHETE, salidas.get(0).getTipo());
        assertTrue(salidas.get(0).getVx() < Proyectil.VELOCIDAD_BALA);
        assertEquals(0, j.getBalas());
    }

    @Test
    void elCoheteExplotaAlChocarConUnBloque() {
        Proyectil cohete = new Proyectil(Proyectil.Tipo.COHETE, null, 1000f, PISO + 60f, -1.2f, Proyectil.VELOCIDAD_COHETE);
        for (int i = 0; i < 120 && cohete.isActivo(); i++) {
            cohete.actualizar(DT, arena, null, null);
        }
        assertFalse(cohete.isActivo());
        assertTrue(cohete.exploto());
    }

    @Test
    void laExplosionSacaUnCorazonEnElRadioTambienAlQueLaTiro() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(1), new ArrayList<>());
        Jugador tirador = blanco(1000f);
        Jugador cerca = blanco(1000f + Jugador.ANCHO + 2f * Arena.CELDA);
        float x = tirador.getCaja().x + tirador.getCaja().width + Arena.CELDA;
        armas.explotar(x, PISO + 10f, tirador, arena, tirador, cerca);
        assertEquals(VIDA_LLENA - CORAZON, tirador.getPartes());
        assertEquals(VIDA_LLENA - CORAZON, cerca.getPartes());

        Jugador lejos = blanco(4000f);
        armas.explotar(4000f - ArmasEnJuego.RADIO_EXPLOSION - 10f, PISO + 10f, tirador, arena, lejos, null);
        assertEquals(VIDA_LLENA, lejos.getPartes());
    }

    @Test
    void laExplosionSeVeYRompeLosBloquesDeAlrededorMenosLaFilaDeAbajo() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(1), new ArrayList<>());
        // el impacto en la fila de arriba del piso (fila 2): rompe las filas 1 a 3, y la 3 es aire
        int columna = 50;
        float x = (columna + 0.5f) * Arena.CELDA;
        float y = PISO - 0.5f * Arena.CELDA;
        armas.explotar(x, y, null, arena, blanco(5000f), null);
        assertEquals(6, armas.getCeldasRotas().size());
        for (int[] celda : armas.getCeldasRotas()) {
            assertTrue(celda[1] >= 1 && celda[1] <= 2, "fila " + celda[1]);
            assertTrue(Math.abs(celda[0] - columna) <= 1);
        }
        // la misma explosion otra vez no repite celdas
        armas.explotar(x, y, null, arena, blanco(5000f), null);
        assertEquals(6, armas.getCeldasRotas().size());

        Arena rota = arena.sinCeldas(armas.getCeldasRotas());
        String filaDelMedio = rota.getFilas()[1];
        assertEquals('.', filaDelMedio.charAt(columna));
        assertEquals('#', filaDelMedio.charAt(columna + 2));
        assertEquals('#', rota.getFilas()[2].charAt(columna), "la de abajo de todo no se rompe");
        assertTrue(rota.getBloques().size() < arena.getBloques().size());

        // y la explosion se dibuja un rato
        armas.actualizar(DT, arena, blanco(5000f), null);
        assertTrue(armas.getProyectiles().stream().anyMatch(p -> p.getTipo() == Proyectil.Tipo.EXPLOSION));
        armas.limpiar();
        assertTrue(armas.getCeldasRotas().isEmpty(), "al empezar la ronda los bloques vuelven");
    }

    @Test
    void lasParedesDeLosCostadosNoSeRompen() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(1), new ArrayList<>());
        armas.explotar(0.5f * Arena.CELDA, PISO - 0.5f * Arena.CELDA, null, arena, blanco(5000f), null);
        for (int[] celda : armas.getCeldasRotas()) {
            assertTrue(celda[0] >= 1, "columna " + celda[0]);
        }
    }

    @Test
    void elCoheteQueVuelaEnLaPartidaExplotaYHiere() {
        ArmasEnJuego armas = new ArmasEnJuego(new Random(1), new ArrayList<>());
        Guion guion = new Guion();
        Jugador j = jugador(guion, 1000f, true);
        Jugador otro = blanco(1800f);
        j.equipar(TipoArma.LANZACOHETES);
        guion.apuntaX = 1850f;
        guion.apuntaY = PISO + 60f;
        guion.clic = true;
        for (int i = 0; i < 240; i++) {
            j.actualizar(DT, arena, otro);
            otro.actualizar(DT, arena, j);
            armas.actualizar(DT, arena, j, otro);
        }
        assertEquals(VIDA_LLENA - CORAZON, otro.getPartes());
    }

    // ---- reflejo

    /** Un espadachin quieto mirando a la izquierda: su hoja queda horizontal, a la altura del hombro. */
    private Jugador espadachinQuieto(float x) {
        Jugador espadachin = jugador(new FuenteEntrada() {
            @Override
            public void leer(EntradaJugador destino) {
                destino.limpiar();
                destino.hayPuntero = true;
                destino.apuntaX = x - 600f;
                destino.apuntaY = HOMBRO_Y;
            }
        }, x, false);
        for (int i = 0; i < 30; i++) {
            espadachin.actualizar(DT, arena, LEJOS);
        }
        return espadachin;
    }

    @Test
    void unaBalaQueCaeSobreLaHojaRebotaEspejadaYCambiaDeDuenio() {
        Jugador espadachin = espadachinQuieto(3000f);
        Jugador tirador = blanco(1000f);
        Hoja hoja = espadachin.getHoja();
        float medioX = (hoja.getBaseX() + hoja.getPuntaX()) / 2f;
        float medioY = (hoja.getBaseY() + hoja.getPuntaY()) / 2f;
        // cae en diagonal sobre una hoja casi horizontal: sale para arriba, siguiendo para el mismo lado
        Proyectil bala = new Proyectil(Proyectil.Tipo.BALA, tirador, medioX - 40f, medioY + 40f, -0.785f, Proyectil.VELOCIDAD_BALA);
        float vx = bala.getVx();
        for (int i = 0; i < 3 && bala.getVy() < 0f; i++) {
            bala.actualizar(DT / 4f, arena, tirador, espadachin);
        }
        assertTrue(bala.getVy() > 0f, "rebota hacia arriba");
        assertTrue(bala.getVx() > 0f && bala.getVx() <= vx, "sigue para el mismo lado, algo mas lenta");
        assertSame(espadachin, bala.getDuenio());
    }

    @Test
    void laEspadaLaserReflejaPerfecto() {
        Jugador espadachin = espadachinQuieto(3000f);
        espadachin.equipar(TipoArma.ESPADA_LASER);
        for (int i = 0; i < 5; i++) {
            espadachin.actualizar(DT, arena, LEJOS);
        }
        Hoja hoja = espadachin.getHoja();
        float a = hoja.getAngulo();
        float medioX = (hoja.getBaseX() + hoja.getPuntaX()) / 2f;
        float medioY = (hoja.getBaseY() + hoja.getPuntaY()) / 2f;
        Proyectil bala = new Proyectil(Proyectil.Tipo.BALA, null, medioX - 40f, medioY + 40f, -0.785f, Proyectil.VELOCIDAD_BALA);
        float rapidez = (float) Math.hypot(bala.getVx(), bala.getVy());
        for (int i = 0; i < 3 && bala.getVy() < 0f; i++) {
            bala.actualizar(DT / 4f, arena, null, espadachin);
        }
        assertTrue(bala.getVy() > 0f);
        // la gravedad de un cuarto de fotograma cambia un poco la rapidez, no la mitad
        assertEquals(rapidez, (float) Math.hypot(bala.getVx(), bala.getVy()), rapidez * 0.02f);
        // espejo exacto: el componente a lo largo de la hoja se conserva
        float tangente = (float) (bala.getVx() * Math.cos(a) + bala.getVy() * Math.sin(a));
        float tangenteAntes = (float) (Math.cos(-0.785f) * Math.cos(a) + Math.sin(-0.785f) * Math.sin(a)) * rapidez;
        assertEquals(tangenteAntes, tangente, rapidez * 0.03f);
    }

    // ---- espada laser

    @Test
    void laEspadaLaserPegaUnCorazonYUnTercio() {
        assertEquals(CORAZON + CORAZON / 3, TipoArma.ESPADA_LASER.getDanioHoja());
        Guion guion = new Guion();
        Jugador a = jugador(guion, 1000f, false);
        a.equipar(TipoArma.ESPADA_LASER);
        Jugador d = blanco(1150f);
        guion.apuntaX = 1200f;
        for (int i = 0; i < 60 && d.getPartes() == VIDA_LLENA; i++) {
            a.actualizar(DT, arena, d);
            d.actualizar(DT, arena, a);
        }
        assertEquals(VIDA_LLENA - TipoArma.ESPADA_LASER.getDanioHoja(), d.getPartes());
    }

    @Test
    void laEspadaLaserAtraviesaALaComun() {
        Guion guionA = new Guion();
        Guion guionD = new Guion();
        Jugador a = jugador(guionA, 1000f, true);
        Jugador d = jugador(guionD, 1100f, false);
        a.equipar(TipoArma.ESPADA_LASER);
        guionA.apuntaX = 900f;
        guionD.apuntaX = 1400f;
        for (int i = 0; i < 5; i++) {
            a.actualizar(DT, arena, d);
            d.actualizar(DT, arena, a);
        }
        float xA = a.getPosicion().x;
        float xD = d.getPosicion().x;
        guionA.apuntaX = 1400f;
        guionD.apuntaX = 700f;
        a.actualizar(DT, arena, d);
        d.actualizar(DT, arena, a);
        // con dos espadas comunes los dos retroceden (JugadorCombateTest); aca no hay choque
        assertFalse(a.getPosicion().x < xA - 1f && d.getPosicion().x > xD + 1f, "no se empujan");
    }

    @Test
    void laEspadaLaserNoSeSueltaPorUnPunetazo() {
        Jugador j = jugador(FuenteEntrada.NINGUNA, 1000f, true);
        j.equipar(TipoArma.ESPADA_LASER);
        j.setAzar(new Random(0) {
            @Override
            public int nextInt(int limite) {
                return 0; // el punetazo siempre desarmaria
            }
        });
        for (int i = 0; i < 5; i++) {
            j.recibirPunetazo(1f);
        }
        assertEquals(TipoArma.ESPADA_LASER, j.getArma());
    }

    @Test
    void laEspadaLaserTiradaCaeMenosQueLaComun() {
        Proyectil comun = Proyectil.inofensiva(Proyectil.Tipo.ESPADA_LANZADA, null, 1000f, 2000f, 1000f, 0f);
        Proyectil laser = Proyectil.inofensiva(Proyectil.Tipo.ESPADA_LASER_LANZADA, null, 1000f, 2000f, 1000f, 0f);
        for (int i = 0; i < 20; i++) {
            comun.actualizar(DT, arena, null, null);
            laser.actualizar(DT, arena, null, null);
        }
        assertTrue(laser.getY() > comun.getY() + 50f);
        assertEquals(TipoArma.ESPADA_LASER, laser.getArma());
    }

    @Test
    void lasArmasNuevasViajanPorLaRed() {
        Jugador j = jugador(FuenteEntrada.NINGUNA, 1000f, true);
        j.equipar(TipoArma.RIFLE_LASER);
        EstadoJugador estado = new EstadoJugador();
        j.exportar(estado);
        Jugador copia = jugador(FuenteEntrada.NINGUNA, 0f, true);
        copia.importar(estado);
        assertEquals(TipoArma.RIFLE_LASER, copia.getArma());
        assertEquals(20, copia.getBalas());

        ArmasEnJuego armas = new ArmasEnJuego(new Random(1), new ArrayList<>());
        armas.explotar(50.5f * Arena.CELDA, PISO - 0.5f * Arena.CELDA, null, arena, blanco(5000f), null);
        EstadoArmas exportado = new EstadoArmas();
        armas.exportar(exportado);
        ArmasEnJuego delCliente = new ArmasEnJuego(new Random(1), new ArrayList<>());
        delCliente.importar(exportado, DT);
        assertEquals(armas.getCeldasRotas().size(), delCliente.getCeldasRotas().size());
        assertTrue(Arrays.equals(armas.getCeldasRotas().get(0), delCliente.getCeldasRotas().get(0)));
    }
}
