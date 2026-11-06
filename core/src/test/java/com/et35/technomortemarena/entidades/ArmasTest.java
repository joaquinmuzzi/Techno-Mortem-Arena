package com.et35.technomortemarena.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.et35.technomortemarena.entidades.armas.Arma;
import com.et35.technomortemarena.entidades.armas.Armas;
import com.et35.technomortemarena.entidades.armas.Escopeta;
import com.et35.technomortemarena.entidades.armas.Pistola;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

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

    private Jugador rivalArmado(float x) {
        Jugador rival = new Jugador(new FuenteEntrada() {
            @Override
            public void leer(EntradaJugador destino) {
                destino.limpiar();
                destino.hayPuntero = true;
                destino.apuntaX = x + 60f;
                destino.apuntaY = 3000f;
            }
        }, x, PISO, true, Color.WHITE, 3);
        return rival;
    }

    private void pasos(Jugador a, Jugador b, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            a.actualizar(DT, arena, b);
            b.actualizar(DT, arena, a);
            armas.actualizar(DT, arena, a, b);
        }
    }

    private void apuntarALaDerecha() {
        guion.apuntaX = 3500f;
        guion.apuntaY = PISO + 112f;
    }

    @Test
    void laVidaSeCuentaEnSextos() {
        Jugador j = blanco(1000f);
        assertEquals(VIDA_LLENA, j.getPartes());
        assertEquals(3, j.getVida());

        assertTrue(j.recibirDanio(Pistola.BALA.getDanio(), 1f, true));

        assertEquals(VIDA_LLENA - 2, j.getPartes());
        assertEquals(3, j.getVida(), "un corazon a medias cuenta como entero");
    }

    @Test
    void elPunetazoHaceMenosQueUnaBala() {
        assertTrue(Jugador.DANIO_PUNIO < Pistola.BALA.getDanio());
        assertTrue(Pistola.BALA.getDanio() < CORAZON);
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
                j.actualizar(DT, arena, lejos);
            }
        }
        assertEquals(0, j.getPartes());
        assertFalse(j.isVivo());
    }

    @Test
    void lasBalasCaenUnPoco() {
        Proyectil bala = new Proyectil(Pistola.BALA, null, 100f, 1500f, 0f, Pistola.BALA.getVelocidad());
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
        a.equipar(Armas.PISTOLA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.clic = true;
        pasos(a, d, 60);

        assertEquals(VIDA_LLENA - Pistola.BALA.getDanio(), d.getPartes());
        assertEquals(Armas.PISTOLA.getBalasIniciales() - 1, a.getBalas());
    }

    @Test
    void unaBalaSeRompeContraUnBloque() {
        String aire = repetir('.', Arena.COLUMNAS);
        String pared = repetir('.', 35) + "##" + repetir('.', Arena.COLUMNAS - 37);
        String piso = repetir('#', Arena.COLUMNAS);
        Arena conPared = Arena.desdeGrilla(aire, pared, pared, pared, pared, pared, pared, piso, piso, piso);
        Jugador a = tirador(1000f);
        Jugador d = blanco(2500f);
        a.equipar(Armas.PISTOLA);
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
        a.equipar(Armas.PISTOLA);
        apuntarALaDerecha();
        assertEquals(6, a.getBalas());

        List<Proyectil> salidas = disparoHastaVaciar(a, d);

        assertEquals(6, contar(salidas, Pistola.BALA));
        assertNull(a.getArma(), "sin balas no vuelve la espada: queda a las pinas");
        assertEquals(0, a.getBalas());
    }

    @Test
    void laEscopetaDisparaTresVecesCincoPerdigonesEnAbanico() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(Armas.ESCOPETA);
        apuntarALaDerecha();
        assertEquals(3, a.getBalas());

        List<Proyectil> salidas = disparoHastaVaciar(a, d);

        assertEquals(3 * Escopeta.PERDIGONES, contar(salidas, Escopeta.PERDIGON));
        float minimo = Float.MAX_VALUE;
        float maximo = -Float.MAX_VALUE;
        for (int i = 0; i < Escopeta.PERDIGONES; i++) {
            minimo = Math.min(minimo, salidas.get(i).getAngulo());
            maximo = Math.max(maximo, salidas.get(i).getAngulo());
        }
        assertTrue(maximo - minimo > 0.15f, "los perdigones salen abiertos");
        assertTrue(maximo - minimo < Escopeta.ABANICO + 2f * Escopeta.DISPERSION + 0.01f,
            "pero en un abanico cerrado");
        assertNull(a.getArma());
    }

    @Test
    void losPerdigonesTienenAlcanceCorto() {
        Proyectil perdigon = new Proyectil(Escopeta.PERDIGON, null, 100f, 1500f, 0f, Escopeta.PERDIGON.getVelocidad());
        Arena vacia = Arena.desdeGrilla(repetir('.', Arena.COLUMNAS));
        for (int i = 0; i < 60 && perdigon.isActivo(); i++) {
            perdigon.actualizar(DT, vacia, null, null);
        }
        assertFalse(perdigon.isActivo());
        assertTrue(perdigon.getX() < Arena.ANCHO_VISTA / 2f, "se pierde antes de cruzar media pantalla");
        assertTrue(perdigon.getX() > Arena.ANCHO_VISTA / 4f, "pero llega mas lejos que un cuarto de la pantalla");
    }

    @Test
    void deCercaLaEscopetaPegaVariosPerdigones() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(1350f);
        a.equipar(Armas.ESCOPETA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.clic = true;
        pasos(a, d, 30);

        assertEquals(VIDA_LLENA - Escopeta.PERDIGONES * Escopeta.PERDIGON.getDanio(), d.getPartes(),
            "de cerca pegan los cinco");
    }

    @Test
    void laEspadaTiradaNoReapareceEnLaMano() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        apuntarALaDerecha();
        pasos(a, d, 3);
        guion.lanzar = true;
        pasos(a, d, 1);

        assertNull(a.getArma());
        assertEquals(Armas.ESPADA, armas.getProyectiles().get(0).getArma());

        pasos(a, d, 180);
        assertNull(a.getArma(), "ya no vuelve sola");
    }

    @Test
    void laEspadaTiradaReboteaYQuedaEnElMapa() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3500f);
        guion.apuntaX = 3500f;
        guion.apuntaY = PISO + 400f;
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
        assertEquals(Armas.codigoDe(Armas.ESPADA), (int) enElSuelo[0]);
        assertEquals(PISO, enElSuelo[2], 2f, "apoyada sobre el piso");
    }

    @Test
    void laEspadaEnElSueloLaAgarraQuienTieneLasManosVacias() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3500f);
        a.equipar(Armas.PISTOLA);
        armas.dejarEnElSuelo(Armas.ESPADA, 1000f, PISO, 0f);
        pasos(a, d, 2);
        assertEquals(Armas.PISTOLA, a.getArma(), "con algo en la mano no la levanta");
        assertEquals(1, armas.getEnElSuelo().size());

        a.equipar(Armas.PISTOLA);
        for (int i = 0; i < Armas.PISTOLA.getBalasIniciales(); i++) {
            pasos(a, d, 20);
            guion.clic = true;
        }
        pasos(a, d, 20);
        assertEquals(Armas.ESPADA, a.getArma(), "con las manos vacias la agarra");
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
        guion.apuntaY = PISO + 112f;
        pasos(a, lejos, 30);
        guion.lanzar = true;
        pasos(a, lejos, 1);
        assertNull(a.getArma());

        Jugador d = blanco(1150f);
        apuntarALaDerecha();
        pasos(a, d, 30);

        assertEquals(VIDA_LLENA, d.getPartes(), "barrer con el brazo vacio no hiere");
    }

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

        assertEquals(VIDA_LLENA - Jugador.DANIO_PUNIO, d.getPartes());
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
                return 0;
            }
        });
        apuntarALaDerecha();
        pasos(a, d, 20);
        guion.clic = true;
        pasos(a, d, 5);

        assertNull(d.getArma(), "se le cayo la espada");
        assertEquals(1, armas.getEnElSuelo().size(), "queda en el suelo, a sus pies");
        assertEquals(Armas.codigoDe(Armas.ESPADA), (int) armas.getEnElSuelo().get(0)[0]);
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

        assertEquals(Armas.ESPADA, d.getArma());
        assertEquals(VIDA_LLENA - Jugador.DANIO_PUNIO, d.getPartes());
    }

    @Test
    void elArmaRecienCaidaNoSeAgarraEnseguida() {
        Jugador a = desarmado(1000f);
        Jugador d = blanco(3000f);
        armas.dejarEnElSuelo(Armas.ESPADA, 1000f, PISO, ArmasEnJuego.ESPERA_PARA_AGARRAR);
        pasos(a, d, 5);
        assertNull(a.getArma());
        pasos(a, d, (int) (ArmasEnJuego.ESPERA_PARA_AGARRAR / DT) + 2);
        assertEquals(Armas.ESPADA, a.getArma());
    }

    @Test
    void cadaTantoApareceUnArmaDeFuegoSobreElPisoYSeAgarraPasandoPorEncima() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        pasos(a, d, (int) (ArmasEnJuego.INTERVALO_APARICION / DT) + 2);

        assertEquals(1, armas.getEnElSuelo().size());
        float[] arma = armas.getEnElSuelo().get(0);
        Arma tipo = Armas.porCodigo((int) arma[0]);
        assertTrue(tipo == Armas.PISTOLA || tipo == Armas.ESCOPETA);
        assertTrue(arma[2] > Arena.ALTO_MUNDO / 2f, "aparece arriba, en el cielo");
        pasos(a, d, 240);
        assertEquals(PISO, arma[2], 0.01f, "y cae hasta apoyarse en el piso");

        arma[1] = a.getPosicion().x;
        pasos(a, d, 1);
        assertEquals(Armas.ESPADA, a.getArma(), "con la espada en la mano no la agarra");
        assertEquals(1, armas.getEnElSuelo().size());

        armas.getEnElSuelo().clear();
        Jugador vacio = desarmado(1000f);
        armas.dejarEnElSuelo(tipo, vacio.getPosicion().x, PISO, 0f);
        pasos(vacio, d, 1);
        assertEquals(tipo, vacio.getArma(), "con la mano vacia si");
        assertEquals(tipo.getBalasIniciales(), vacio.getBalas());
        assertTrue(armas.getEnElSuelo().isEmpty());
    }

    @Test
    void conUnArmaDeFuegoEnLaManoNoSeAgarraOtra() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(Armas.PISTOLA);
        armas.dejarEnElSuelo(Armas.ESCOPETA, 1000f, PISO, 0f);
        pasos(a, d, 5);
        assertEquals(Armas.PISTOLA, a.getArma());
        assertEquals(1, armas.getEnElSuelo().size());
    }

    @Test
    void aparecenPistolasYEscopetas() {
        boolean pistola = false;
        boolean escopeta = false;
        for (long semilla = 0; semilla < 30; semilla++) {
            ArmasEnJuego otras = new ArmasEnJuego(new Random(semilla), new ArrayList<>());
            otras.aparecerArma(arena);
            Arma tipo = Armas.porCodigo((int) otras.getEnElSuelo().get(0)[0]);
            pistola |= tipo == Armas.PISTOLA;
            escopeta |= tipo == Armas.ESCOPETA;
        }
        assertTrue(pistola && escopeta);
    }

    @Test
    void noAparecenArmasEnLasZonasProhibidas() {
        List<float[]> zonas = new ArrayList<>();
        zonas.add(ArmasEnJuego.zonaDeSpawn(24f * Arena.CELDA, PISO, Jugador.ANCHO, Jugador.ALTO));
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
        a.equipar(Armas.ESCOPETA);
        a.recibirDanio(2, 1f, true);
        a.reiniciar(1000f, PISO, true);
        assertEquals(Armas.ESPADA, a.getArma());
        assertEquals(0, a.getBalas());
        assertEquals(VIDA_LLENA, a.getPartes());
    }

    @Test
    void losContadoresDeArmasSubenAlEquiparYNoSeReinician() {
        Jugador a = tirador(1000f);
        assertEquals(0, a.getContadorEspadasEquipadas());
        assertEquals(0, a.getContadorArmasDeFuegoEquipadas());

        a.equipar(Armas.PISTOLA);
        assertEquals(1, a.getContadorArmasDeFuegoEquipadas());
        assertEquals(0, a.getContadorEspadasEquipadas());

        a.equipar(Armas.ESCOPETA);
        assertEquals(2, a.getContadorArmasDeFuegoEquipadas());

        a.equipar(Armas.ESPADA);
        assertEquals(1, a.getContadorEspadasEquipadas());

        a.reiniciar(1000f, PISO, true);
        assertEquals(1, a.getContadorEspadasEquipadas(), "el reinicio no borra el contador");
        assertEquals(2, a.getContadorArmasDeFuegoEquipadas(), "el reinicio no borra el contador");
        assertEquals(Armas.ESPADA, a.getArma(), "la espada inicial del reinicio no cuenta como recogida");
    }

    @Test
    void cadaDisparoSumaElContadorDeSuArma() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(Armas.PISTOLA);
        apuntarALaDerecha();
        guion.clic = true;
        pasos(a, d, 1);
        assertEquals(1, a.getContadorDisparosPistola());
        assertEquals(0, a.getContadorDisparosEscopeta());

        Jugador b = tirador(1000f);
        b.equipar(Armas.ESCOPETA);
        guion.clic = true;
        pasos(b, d, 1);
        assertEquals(0, b.getContadorDisparosPistola(), "la escopeta no suma al contador de la pistola");
        assertEquals(1, b.getContadorDisparosEscopeta());
    }

    @Test
    void elContadorDeEspadasAlCaerSumaCadaToqueDelPiso() {
        Proyectil espada = Proyectil.inofensiva(Armas.ESPADA, null, 1500f, PISO + 400f, 0f, 0f);
        armas.getProyectiles().add(espada);
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        for (int i = 0; i < 600 && !armas.getProyectiles().isEmpty(); i++) {
            armas.actualizar(DT, arena, a, d);
        }
        assertEquals(1, armas.getEnElSuelo().size(), "termina apoyada en el mapa");
        assertTrue(armas.getContadorEspadasAlCaer() >= 2,
            "cuenta el primer golpe y los rebotes: " + armas.getContadorEspadasAlCaer());
    }

    @Test
    void tirarUnArmaDeFuegoNoSuenaHastaQueTocaElPisoYLaEspadaNoSuena() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(Armas.PISTOLA);
        apuntarALaDerecha();
        pasos(a, d, 1);
        guion.lanzar = true;
        pasos(a, d, 1);
        assertEquals(0, armas.getContadorArmasDeFuegoAlCaer(), "todavia no toco el piso");

        pasos(a, d, 120);
        assertTrue(armas.getContadorArmasDeFuegoAlCaer() >= 1, "suena recien al tocar el piso");
        int conLaPistola = armas.getContadorArmasDeFuegoAlCaer();

        Jugador b = tirador(1000f);
        apuntarALaDerecha();
        pasos(b, d, 1);
        guion.lanzar = true;
        pasos(b, d, 120);
        assertEquals(conLaPistola, armas.getContadorArmasDeFuegoAlCaer(), "la espada no suena con Gun-drop");
    }

    @Test
    void elContadorDeArmasDeFuegoAlCaerSumaCadaReboteYNoSeMezclaConLasEspadas() {
        Proyectil pistola = Proyectil.inofensiva(Armas.PISTOLA, null, 1500f, PISO + 400f, 0f, 0f).conBalas(1);
        armas.getProyectiles().add(pistola);
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        for (int i = 0; i < 600 && !armas.getProyectiles().isEmpty(); i++) {
            armas.actualizar(DT, arena, a, d);
        }
        assertTrue(armas.getContadorArmasDeFuegoAlCaer() >= 2,
            "cuenta el primer golpe y los rebotes: " + armas.getContadorArmasDeFuegoAlCaer());
        assertEquals(0, armas.getContadorEspadasAlCaer(), "la pistola no suma al contador de las espadas");
    }

    @Test
    void laEspadaNuncaSeQuedaFlotandoEnElAire() {
        Proyectil espada = Proyectil.inofensiva(Armas.ESPADA, null, 1500f, 1400f, 300f, 900f);
        for (int i = 0; i < 600 && espada.isActivo(); i++) {
            espada.actualizar(DT, arena, null, null);
        }
        assertTrue(espada.dejaArmaEnElSuelo());
        assertEquals(PISO + Proyectil.LADO_LANZADA / 2f, espada.getY(), 5f, "se frena apoyada en el piso, no en el aire");
    }

    @Test
    void laEspadaReboteaPoco() {
        float altura = 600f;
        Proyectil espada = Proyectil.inofensiva(Armas.ESPADA, null, 1500f, PISO + altura, 0f, 0f);
        boolean pico = false;
        float maxima = 0f;
        float anterior = espada.getY();
        for (int i = 0; i < 600 && espada.isActivo(); i++) {
            espada.actualizar(DT, arena, null, null);
            if (espada.getY() > anterior) {
                pico = true;
            }
            if (pico) {
                maxima = Math.max(maxima, espada.getY() - PISO);
            }
            anterior = espada.getY();
        }
        assertTrue(pico, "rebota algo");
        assertTrue(maxima < 0.15f * altura, "pero sube menos del 15% de lo que cayo: subio " + maxima);
    }

    @Test
    void unArmaDeFuegoTiradaConBalasQuedaEnElPisoConSusBalas() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3500f);
        a.equipar(Armas.PISTOLA);
        guion.apuntaX = 0f;
        guion.apuntaY = PISO + 112f;
        pasos(a, d, 30);
        guion.clic = true;
        pasos(a, d, 20);
        guion.clic = true;
        pasos(a, d, 20);
        assertEquals(4, a.getBalas());
        guion.lanzar = true;
        pasos(a, d, 1);
        pasos(a, d, 240);

        assertEquals(1, armas.getEnElSuelo().size(), "no se rompe: queda en el piso");
        float[] arma = armas.getEnElSuelo().get(0);
        assertEquals(Armas.codigoDe(Armas.PISTOLA), (int) arma[0]);
        assertEquals(4, (int) arma[4], "con las balas que le quedaban");

        arma[1] = a.getPosicion().x;
        arma[2] = a.getPosicion().y;
        pasos(a, d, 1);
        assertEquals(Armas.PISTOLA, a.getArma());
        assertEquals(4, a.getBalas());
    }

    @Test
    void unArmaDeFuegoSinBalasSeRompeAlChocar() {
        Proyectil vacia = Proyectil.lanzada(Armas.PISTOLA, null, 1500f, PISO + 200f, -1f, 500f);
        for (int i = 0; i < 120 && vacia.isActivo(); i++) {
            vacia.actualizar(DT, arena, null, null);
        }
        assertFalse(vacia.isActivo());
        assertFalse(vacia.dejaArmaEnElSuelo());
    }

    @Test
    void apareceElTipoQueFaltaEnElSuelo() {
        for (int semilla = 0; semilla < 20; semilla++) {
            ArmasEnJuego otras = new ArmasEnJuego(new Random(semilla), new ArrayList<>());
            otras.dejarEnElSuelo(Armas.PISTOLA, 500f, PISO, 0f);
            otras.dejarEnElSuelo(Armas.RIFLE_LASER, 700f, PISO, 0f);
            otras.dejarEnElSuelo(Armas.LANZACOHETES, 900f, PISO, 0f);
            otras.aparecerArma(arena);
            Arma nueva = Armas.porCodigo((int) otras.getEnElSuelo().get(3)[0]);
            assertTrue(nueva == Armas.ESCOPETA || nueva == Armas.ESPADA_LASER, "aparecio " + nueva);
        }
    }

    @Test
    void laEspadaLaserApareceMuyDeVezEnCuandoYNuncaDosALaVez() {
        int laser = 0;
        int total = 1200;
        for (int semilla = 0; semilla < total; semilla++) {
            ArmasEnJuego otras = new ArmasEnJuego(new Random(semilla), new ArrayList<>());
            otras.aparecerArma(arena);
            if (Armas.porCodigo((int) otras.getEnElSuelo().get(0)[0]) == Armas.ESPADA_LASER) {
                laser++;
                for (int i = 0; i < 30; i++) {
                    otras.aparecerArma(arena);
                }
                int espadas = 0;
                for (float[] arma : otras.getEnElSuelo()) {
                    espadas += Armas.porCodigo((int) arma[0]) == Armas.ESPADA_LASER ? 1 : 0;
                }
                assertTrue(espadas <= 1, "nunca dos espadas laser a la vez");
            }
        }
        float esperada = total / (float) ArmasEnJuego.CHANCE_ESPADA_LASER;
        assertTrue(laser > esperada * 0.6f && laser < esperada * 1.4f, "salieron " + laser);
    }

    @Test
    void elPunetazoDesarmaYElArmaCaeConSusBalas() {
        Jugador a = desarmado(1000f);
        Jugador d = rivalArmado(1130f);
        d.equipar(Armas.ESCOPETA, 2);
        d.setAzar(new Random() {
            @Override
            public int nextInt(int limite) {
                return 0;
            }
        });
        apuntarALaDerecha();
        pasos(a, d, 20);
        guion.clic = true;
        pasos(a, d, 5);

        assertNull(d.getArma());
        float[] arma = armas.getEnElSuelo().get(0);
        assertEquals(Armas.codigoDe(Armas.ESCOPETA), (int) arma[0]);
        assertEquals(2, (int) arma[4]);
    }

    private static Hoja suya() {
        Hoja hoja = new Hoja();
        hoja.colocar(1000f, 500f, 0f, 0f);
        return hoja;
    }

    private static Hoja miaCruzandoEn(float x, float largo) {
        Hoja hoja = new Hoja();
        hoja.colocar(x, 500f + largo, (float) -Math.PI / 2f, 0f);
        return hoja;
    }

    @Test
    void laPuntaDeMiHojaContraLaBaseDeLaSuyaDesarma() {
        assertTrue(Jugador.esDesarme(miaCruzandoEn(1020f, 100f), suya(), true));
    }

    @Test
    void sinMoverLaEspadaNoDesarma() {
        assertFalse(Jugador.esDesarme(miaCruzandoEn(1020f, 100f), suya(), false));
    }

    @Test
    void contraElMedioDeLaSuyaTambienDesarmaPeroContraLaPuntaNo() {
        assertTrue(Jugador.esDesarme(miaCruzandoEn(1060f, 100f), suya(), true), "le pegue en el medio");
        assertFalse(Jugador.esDesarme(miaCruzandoEn(1100f, 100f), suya(), true), "le pegue en la punta");
    }

    @Test
    void conLaMitadDeAfueraDeMiHojaDesarmaYConLaDeAdentroNo() {
        assertTrue(Jugador.esDesarme(miaCruzandoEn(1020f, 60f), suya(), true), "pegue con el medio de mi hoja");
        assertFalse(Jugador.esDesarme(miaCruzandoEn(1020f, 45f), suya(), true), "pegue cerca de mi mano");
    }

    @Test
    void laEspadaDesarmadaVuelaHaciaArribaSinHerirYQuedaEnElMapa() {
        Jugador a = blanco(1300f);
        Jugador d = rivalArmado(1000f);
        pasos(d, a, 3);
        d.soltarEspadaAlAire(1f);
        assertNull(d.getArma());
        pasos(d, a, 1);
        Proyectil espada = armas.getProyectiles().get(0);
        float yInicial = espada.getY();
        pasos(d, a, 10);
        assertTrue(espada.getY() > yInicial, "sale hacia arriba");

        pasos(d, a, 300);
        assertEquals(VIDA_LLENA, a.getPartes(), "no lastima a nadie");
        assertEquals(VIDA_LLENA, d.getPartes());
        assertEquals(1, armas.getEnElSuelo().size());
        assertEquals(Armas.codigoDe(Armas.ESPADA), (int) armas.getEnElSuelo().get(0)[0]);
    }

    @Test
    void losPerdigonesPeganAunqueElRivalEsteInvulnerable() {
        Jugador d = blanco(1000f);
        assertTrue(d.recibirDanio(CORAZON, 1f, false), "un espadazo: un segundo de invulnerabilidad");
        assertFalse(d.recibirDanio(Pistola.BALA.getDanio(), 1f, true), "una bala no pasa");
        for (int i = 0; i < Escopeta.PERDIGONES; i++) {
            assertTrue(d.recibirPerdigon(Escopeta.PERDIGON.getDanio(), 1f), "pero cada perdigon si");
        }
        assertEquals(VIDA_LLENA - CORAZON - Escopeta.PERDIGONES, d.getPartes());
    }

    @Test
    void unEscopetazoJustoDespuesDeUnEspadazoPegaLosCinco() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(1350f);
        a.equipar(Armas.ESCOPETA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        d.recibirDanio(Pistola.BALA.getDanio(), 0f, true);
        guion.clic = true;
        pasos(a, d, 30);
        assertEquals(VIDA_LLENA - Pistola.BALA.getDanio() - Escopeta.PERDIGONES * Escopeta.PERDIGON.getDanio(), d.getPartes());
    }

    private static Arena conSpawner(String celdas) {
        String aire = repetir('.', Arena.COLUMNAS);
        String spawner = repetir('.', 40) + celdas + repetir('.', Arena.COLUMNAS - 40 - celdas.length());
        String piso = repetir('#', Arena.COLUMNAS);
        return Arena.desdeGrilla(aire, aire, spawner, spawner, spawner, piso, piso, piso);
    }

    @Test
    void elSpawnerEsUnBloqueSolido() {
        Arena mapa = conSpawner("PPP");
        assertTrue(mapa.getBloques().stream().anyMatch(b -> b instanceof com.et35.technomortemarena.mundo.SpawnerArma));
    }

    @Test
    void elSpawnerHaceAparecerSuArmaEncimaCadaCincoSegundosYNoApila() {
        Arena mapa = conSpawner("PPP");
        Jugador a = blanco(200f);
        Jugador d = blanco(3000f);
        int cinco = (int) (ArmasEnJuego.INTERVALO_SPAWNER / DT) + 2;
        for (int i = 0; i < cinco - 10; i++) {
            armas.actualizar(DT, mapa, a, d);
        }
        assertTrue(armas.getEnElSuelo().isEmpty(), "todavia no pasaron 5 s");
        for (int i = 0; i < 12; i++) {
            armas.actualizar(DT, mapa, a, d);
        }
        assertEquals(1, armas.getEnElSuelo().size());
        float[] arma = armas.getEnElSuelo().get(0);
        assertEquals(Armas.codigoDe(Armas.PISTOLA), (int) arma[0]);
        assertEquals((3 + 3) * Arena.CELDA, arma[2], 0.01f, "encima del spawner");
        float centro = arma[1] + Arma.ANCHO_EN_EL_SUELO / 2f;
        assertEquals(41.5f * Arena.CELDA, centro, 1f, "al medio del spawner");

        for (int i = 0; i < 3 * cinco; i++) {
            armas.actualizar(DT, mapa, a, d);
        }
        assertEquals(1, deSpawner(), "con una arriba no apila otra");

        armas.getEnElSuelo().clear();
        for (int i = 0; i < cinco; i++) {
            armas.actualizar(DT, mapa, a, d);
        }
        assertEquals(1, deSpawner(), "cuando se la llevan, aparece otra");
    }

    private int deSpawner() {
        int cantidad = 0;
        for (float[] arma : armas.getEnElSuelo()) {
            if (arma[5] == 1f) {
                cantidad++;
            }
        }
        return cantidad;
    }

    @Test
    void cadaGrupoDeCeldasEsUnSpawnerDeSuTipo() {
        Arena mapa = conSpawner("PPP..........EEE");
        java.util.List<float[]> spawners = ArmasEnJuego.buscarSpawners(mapa);
        assertEquals(2, spawners.size());
        boolean pistola = false;
        boolean escopeta = false;
        for (float[] s : spawners) {
            pistola |= (int) s[0] == Armas.codigoDe(Armas.PISTOLA);
            escopeta |= (int) s[0] == Armas.codigoDe(Armas.ESCOPETA);
        }
        assertTrue(pistola && escopeta);
    }

    @Test
    void losSpawnersDeRifleLaserYLanzacohetesHacenAparecerLaSuya() {
        Arena mapa = conSpawner("FFF..........CCC");
        java.util.List<float[]> spawners = ArmasEnJuego.buscarSpawners(mapa);
        assertEquals(2, spawners.size());
        boolean rifle = false;
        boolean cohetes = false;
        for (float[] s : spawners) {
            rifle |= (int) s[0] == Armas.codigoDe(Armas.RIFLE_LASER);
            cohetes |= (int) s[0] == Armas.codigoDe(Armas.LANZACOHETES);
        }
        assertTrue(rifle && cohetes);
        assertTrue(mapa.getBloques().stream().anyMatch(b -> b instanceof com.et35.technomortemarena.mundo.SpawnerArma
            && ((com.et35.technomortemarena.mundo.SpawnerArma) b).getArma() == Armas.RIFLE_LASER));
    }

    @Test
    void lasArmasDeLosSpawnersNoFrenanLasQueAparecenSueltas() {
        Arena mapa = conSpawner("PPP..........EEE");
        Jugador a = blanco(200f);
        Jugador d = blanco(3000f);
        int ocho = (int) (ArmasEnJuego.INTERVALO_APARICION / DT) + 2;
        for (int i = 0; i < ocho; i++) {
            armas.actualizar(DT, mapa, a, d);
        }
        assertEquals(3, armas.getEnElSuelo().size());
    }

    @Test
    void cadaDisparoDeEscopetaTieneSuPropiaDispersion() {
        Jugador a = tirador(1000f);
        Jugador d = blanco(3000f);
        a.equipar(Armas.ESCOPETA);
        apuntarALaDerecha();
        pasos(a, d, 3);
        List<Proyectil> salidas = disparoHastaVaciar(a, d);
        boolean distintos = false;
        for (int i = 0; i < Escopeta.PERDIGONES; i++) {
            float primero = salidas.get(i).getAngulo();
            float segundo = salidas.get(i + Escopeta.PERDIGONES).getAngulo();
            distintos |= Math.abs(primero - segundo) > 0.001f;
        }
        assertTrue(distintos, "dos disparos no salen exactamente igual");
    }

    @Test
    void unArmaQueCaeNoSeAgarraEnElAire() {
        Jugador vacio = desarmado(1000f);
        Jugador d = blanco(3500f);
        armas.dejarCayendo(Armas.PISTOLA, vacio.getPosicion().x, PISO + 400f, 6);
        boolean laTuvoEnElAire = false;
        for (int i = 0; i < 120 && vacio.getArma() == null; i++) {
            float[] arma = armas.getEnElSuelo().isEmpty() ? null : armas.getEnElSuelo().get(0);
            pasos(vacio, d, 1);
            if (vacio.getArma() != null && arma != null && arma[2] > PISO + 1f) {
                laTuvoEnElAire = true;
            }
        }
        assertFalse(laTuvoEnElAire, "la agarro antes de que tocara el piso");
        assertEquals(Armas.PISTOLA, vacio.getArma(), "cuando toca el piso, si");
    }

    @Test
    void unArmaQueCaeSeApoyaEnLaPrimeraPlataformaQueEncuentra() {
        String aire = repetir('.', Arena.COLUMNAS);
        String plataforma = repetir('.', 40) + repetir('#', 10) + repetir('.', Arena.COLUMNAS - 50);
        String piso = repetir('#', Arena.COLUMNAS);
        Arena conPlataforma = Arena.desdeGrilla(plataforma, aire, aire, aire, aire, piso, piso, piso);
        armas.dejarCayendo(Armas.ESCOPETA, 42f * Arena.CELDA, Arena.ALTO_MUNDO - 100f, 3);
        Jugador a = blanco(200f);
        Jugador d = blanco(3500f);
        for (int i = 0; i < 300; i++) {
            armas.actualizar(DT, conPlataforma, a, d);
        }
        assertEquals(8f * Arena.CELDA, armas.getEnElSuelo().get(0)[2], 0.01f, "sobre la plataforma, no en el piso");
    }

    @Test
    void unArmaQueCaePorUnAgujeroDesaparece() {
        Arena conAgujero = Arena.conAgujero();
        float centro = Arena.ANCHO_MUNDO / 2f;
        armas.dejarCayendo(Armas.PISTOLA, centro - 20f, PISO + 200f, 6);
        Jugador a = blanco(200f);
        Jugador d = blanco(7000f);
        for (int i = 0; i < 300; i++) {
            armas.actualizar(DT, conAgujero, a, d);
        }
        assertTrue(armas.getEnElSuelo().isEmpty());
    }

    @Test
    void elArmaQueSeCaeDeLaManoEnElAireCaeAlPiso() {
        Jugador a = desarmado(1000f);
        Jugador d = rivalArmado(1130f);
        d.setAzar(new Random() {
            @Override
            public int nextInt(int limite) {
                return 0;
            }
        });
        apuntarALaDerecha();
        pasos(a, d, 20);
        d.impulsar(Jugador.IMPULSO_SALTO);
        pasos(a, d, 3);
        guion.clic = true;
        for (int i = 0; i < 10 && d.getArma() != null; i++) {
            pasos(a, d, 1);
        }
        pasos(a, d, 180);
        if (!armas.getEnElSuelo().isEmpty()) {
            assertEquals(PISO, armas.getEnElSuelo().get(0)[2], 0.01f, "termina en el piso, no flotando");
        }
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

    private static int contar(List<Proyectil> proyectiles, DefinicionProyectil tipo) {
        int cantidad = 0;
        for (Proyectil p : proyectiles) {
            if (p.getDefinicion() == tipo) {
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
