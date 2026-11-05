package com.et35.technomortemarena.partida;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.RondasPartida.Fase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EdicionNivelTest {

    private static String aire() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < Arena.COLUMNAS; i++) {
            texto.append('.');
        }
        return texto.toString();
    }

    private static String piso() {
        return aire().replace('.', '#');
    }

    /** Mapa de 9 filas de aire y 3 de piso (alto 12), como los mapas predefinidos. */
    private static String[] mapa() {
        String[] filas = new String[12];
        for (int i = 0; i < 12; i++) {
            filas[i] = i < 9 ? aire() : piso();
        }
        return filas;
    }

    /** Zonas prohibidas de un spawn en la columna 24 sobre el piso, como en los mapas sin spawn. */
    private static List<float[]> zonas() {
        List<float[]> zonas = new ArrayList<>();
        zonas.add(ColocadorPiezas.zonaDeSpawn(24f * Arena.CELDA, 3f * Arena.CELDA, 96f, 192f));
        zonas.add(ColocadorPiezas.zonaDeSpawn(Arena.ANCHO_MUNDO - 24f * Arena.CELDA - 96f, 3f * Arena.CELDA, 96f, 192f));
        return zonas;
    }

    private EdicionNivel edicion(long semilla) {
        return new EdicionNivel(mapa(), zonas(), new Random(semilla));
    }

    @Test
    void lasOchoPiezasTienenLasMedidasPedidas() {
        assertEquals(6, PiezaNivel.FILA_DE_PINCHOS.getAncho());
        assertEquals(2, PiezaNivel.FILA_DE_PINCHOS.getAlto());
        assertEquals(4, PiezaNivel.TRAMPOLIN.getAncho());
        assertEquals(2, PiezaNivel.TRAMPOLIN.getAlto());
        assertEquals(8, PiezaNivel.BLOQUE.getAncho());
        assertEquals(8, PiezaNivel.BLOQUE.getAlto());
        assertEquals(10, PiezaNivel.RECTANGULO.getAncho());
        assertEquals(4, PiezaNivel.RECTANGULO.getAlto());
        assertEquals(4, PiezaNivel.ESQUINA.getAncho());
        assertEquals(10, PiezaNivel.ESQUINA.getAlto());
        assertEquals(6, PiezaNivel.CINTA_IZQUIERDA.getAncho());
        assertEquals(2, PiezaNivel.CINTA_IZQUIERDA.getAlto());
        assertEquals(6, PiezaNivel.CINTA_DERECHA.getAncho());
        assertEquals(2, PiezaNivel.CINTA_DERECHA.getAlto());
        assertEquals(6 + 4 + 6, PiezaNivel.PINCHOS_CON_CINTAS.getAncho());
        assertEquals(2, PiezaNivel.PINCHOS_CON_CINTAS.getAlto());
        assertEquals(3, PiezaNivel.SPAWNER_PISTOLA.getAncho());
        assertEquals(3, PiezaNivel.SPAWNER_PISTOLA.getAlto());
        assertEquals('P', PiezaNivel.SPAWNER_PISTOLA.celda(1, 1));
        assertEquals(3, PiezaNivel.SPAWNER_ESCOPETA.getAncho());
        assertEquals('E', PiezaNivel.SPAWNER_ESCOPETA.celda(2, 2));
        assertEquals(10, PiezaNivel.values().length);
    }

    @Test
    void laEsquinaEsUnaLConLosBrazosDe2() {
        PiezaNivel e = PiezaNivel.ESQUINA;
        for (int dy = 0; dy < 10; dy++) {
            assertEquals('#', e.celda(0, dy));
            assertEquals('#', e.celda(1, dy));
        }
        assertEquals('#', e.celda(3, 0));
        assertEquals('#', e.celda(3, 1));
        assertEquals('.', e.celda(3, 2), "el brazo de abajo mide 2 de grosor");
        assertEquals('.', e.celda(2, 5), "el brazo vertical mide 2 de ancho");
    }

    @Test
    void losPinchosConCintasEmpujanHaciaElCentro() {
        PiezaNivel p = PiezaNivel.PINCHOS_CON_CINTAS;
        assertEquals('R', p.celda(0, 0));
        assertEquals('R', p.celda(5, 1));
        assertEquals('X', p.celda(6, 0));
        assertEquals('X', p.celda(9, 1));
        assertEquals('L', p.celda(10, 0));
        assertEquals('L', p.celda(15, 1));
    }

    @Test
    void sortea3ObjetosDistintos() {
        for (long semilla = 0; semilla < 50; semilla++) {
            EdicionNivel e = edicion(semilla);
            e.iniciar(1);
            Set<PiezaNivel> vistas = new HashSet<>();
            for (int i = 0; i < EdicionNivel.OPCIONES; i++) {
                assertNotNull(e.getOpcion(i));
                vistas.add(e.getOpcion(i));
            }
            assertEquals(3, vistas.size());
            assertEquals(1, e.getEditor());
            assertEquals(-1, e.getEleccion());
        }
    }

    @Test
    void conVariasSemillasSalenTodasLasPiezas() {
        Set<PiezaNivel> vistas = new HashSet<>();
        for (long semilla = 0; semilla < 200; semilla++) {
            EdicionNivel e = edicion(semilla);
            e.iniciar(0);
            for (int i = 0; i < 3; i++) {
                vistas.add(e.getOpcion(i));
            }
        }
        assertEquals(PiezaNivel.values().length, vistas.size(), "salen todas, los spawners tambien");
    }

    @Test
    void colocaLaPiezaElegidaCentradaEnElCursor() {
        EdicionNivel e = edicion(1);
        e.iniciar(0);
        e.elegir(0);
        PiezaNivel pieza = e.getElegida();
        e.moverCursor(45, 20);

        assertTrue(e.colocar());

        int[] origen = ColocadorPiezas.origenPara(pieza, 45, 20);
        String[] filas = e.getFilas();
        assertEquals(pieza.celda(0, 0), ColocadorPiezas.celdaDe(filas, origen[0], origen[1]));
        assertEquals(1, e.getColocadas().size());
        assertTrue(e.haColocado());
        assertFalse(e.colocar(), "una sola pieza por edicion");
    }

    @Test
    void sinElegirNoSeColoca() {
        EdicionNivel e = edicion(1);
        e.iniciar(0);
        e.moverCursor(45, 20);
        assertFalse(e.colocar());
        assertTrue(e.getColocadas().isEmpty());
    }

    @Test
    void sePuedeColocarSobreElPisoYSobreOtraPieza() {
        String[] filas = mapa();
        List<float[]> sin = new ArrayList<>();
        // el piso mide 3 filas: una pieza apoyada en la fila 0 lo pisa, y se puede
        assertTrue(ColocadorPiezas.puedeColocar(filas, PiezaNivel.FILA_DE_PINCHOS, 40, 0, sin));
        String[] con = ColocadorPiezas.aplicar(filas, PiezaNivel.FILA_DE_PINCHOS, 40, 1);
        assertEquals('X', ColocadorPiezas.celdaDe(con, 40, 1), "los pinchos reemplazan al piso");
        assertEquals('X', ColocadorPiezas.celdaDe(con, 45, 2));

        String[] conBloque = ColocadorPiezas.aplicar(filas, PiezaNivel.BLOQUE, 40, 3);
        assertTrue(ColocadorPiezas.puedeColocar(conBloque, PiezaNivel.TRAMPOLIN, 41, 5, sin), "y sobre otra pieza");
        String[] encima = ColocadorPiezas.aplicar(conBloque, PiezaNivel.TRAMPOLIN, 41, 5);
        assertEquals('T', ColocadorPiezas.celdaDe(encima, 42, 6));
    }

    @Test
    void elHuecoDeLaEsquinaNoBorraLoQueHabia() {
        String[] conBloque = ColocadorPiezas.aplicar(mapa(), PiezaNivel.BLOQUE, 40, 3);
        String[] conEsquina = ColocadorPiezas.aplicar(conBloque, PiezaNivel.ESQUINA, 40, 3);
        assertEquals('#', ColocadorPiezas.celdaDe(conEsquina, 43, 8), "el hueco de la L deja el bloque");
    }

    @Test
    void noSePuedeColocarFueraDeLaGrilla() {
        String[] filas = mapa();
        List<float[]> sin = new ArrayList<>();
        assertFalse(ColocadorPiezas.puedeColocar(filas, PiezaNivel.BLOQUE, -1, 10, sin));
        assertFalse(ColocadorPiezas.puedeColocar(filas, PiezaNivel.BLOQUE, Arena.COLUMNAS - 7, 10, sin));
        assertFalse(ColocadorPiezas.puedeColocar(filas, PiezaNivel.BLOQUE, 10, Arena.FILAS - 7, sin));
        assertTrue(ColocadorPiezas.puedeColocar(filas, PiezaNivel.BLOQUE, Arena.COLUMNAS - 8, Arena.FILAS - 8, sin));
    }

    @Test
    void elCursorNoSacaLaPiezaDeLaGrilla() {
        int[] origen = ColocadorPiezas.origenPara(PiezaNivel.RECTANGULO, 0, 0);
        assertArrayEquals(new int[] {0, 0}, origen);
        origen = ColocadorPiezas.origenPara(PiezaNivel.RECTANGULO, Arena.COLUMNAS - 1, Arena.FILAS - 1);
        assertArrayEquals(new int[] {Arena.COLUMNAS - 10, Arena.FILAS - 4}, origen);
    }

    @Test
    void noSePuedeColocarCercaDeLosSpawns() {
        EdicionNivel e = edicion(2);
        e.iniciar(0);
        // ponemos una fila de pinchos justo sobre el spawn del jugador 1 (columna 24, sobre el piso)
        PiezaNivel pinchos = PiezaNivel.FILA_DE_PINCHOS;
        for (int i = 0; i < 3; i++) {
            if (e.getOpcion(i) == pinchos) {
                e.elegir(i);
            }
        }
        // si no salio la fila de pinchos, se prueba directo con el colocador
        List<float[]> zonas = zonas();
        String[] filas = mapa();
        assertFalse(ColocadorPiezas.puedeColocar(filas, pinchos, 24, 3, zonas), "sobre el spawn");
        assertFalse(ColocadorPiezas.puedeColocar(filas, pinchos, 28, 3, zonas), "pegada al spawn");
        assertFalse(ColocadorPiezas.puedeColocar(filas, pinchos, 24, 8, zonas), "arriba del spawn, dentro del margen");
        assertFalse(ColocadorPiezas.puedeColocar(filas, pinchos, 17, 3, zonas), "a la izquierda, dentro del margen");
        assertTrue(ColocadorPiezas.puedeColocar(filas, pinchos, 40, 3, zonas), "lejos del spawn");
        // y la del jugador 2, del otro lado
        int columnaSpawnDos = (int) ((Arena.ANCHO_MUNDO - 24f * Arena.CELDA - 96f) / Arena.CELDA);
        assertFalse(ColocadorPiezas.puedeColocar(filas, pinchos, columnaSpawnDos, 3, zonas));
    }

    @Test
    void elAutocolocadoNuncaCaeEnUnaZonaProhibida() {
        for (long semilla = 0; semilla < 100; semilla++) {
            EdicionNivel e = edicion(semilla);
            e.iniciar(1);
            // sin elegir: una al azar
            assertTrue(e.autocolocar());
            int[] pieza = e.getColocadas().get(0);
            PiezaNivel tipo = PiezaNivel.porIndice(pieza[0]);
            for (float[] zona : zonas()) {
                boolean seCruzan = pieza[1] < zona[2] && pieza[1] + tipo.getAncho() > zona[0]
                    && pieza[2] < zona[3] && pieza[2] + tipo.getAlto() > zona[1];
                assertFalse(seCruzan, "semilla " + semilla + " cayo en un spawn");
            }
        }
    }

    @Test
    void siHabiaElegidoSeColocaEnElUltimoLugarDelCursor() {
        EdicionNivel e = edicion(3);
        e.iniciar(0);
        e.elegir(1);
        PiezaNivel pieza = e.getElegida();
        e.moverCursor(60, 25);

        assertTrue(e.autocolocar());

        int[] colocada = e.getColocadas().get(0);
        int[] esperado = ColocadorPiezas.origenPara(pieza, 60, 25);
        assertEquals(pieza.ordinal(), colocada[0]);
        assertEquals(esperado[0], colocada[1]);
        assertEquals(esperado[1], colocada[2]);
    }

    @Test
    void siElUltimoLugarNoServiaSeColocaEnElMasCercano() {
        EdicionNivel e = edicion(4);
        e.iniciar(0);
        e.elegir(0);
        PiezaNivel pieza = e.getElegida();
        e.moverCursor(24, 4); // justo sobre el spawn
        assertFalse(e.sePuedeColocar());

        assertTrue(e.autocolocar());

        int[] colocada = e.getColocadas().get(0);
        assertEquals(pieza.ordinal(), colocada[0]);
        assertTrue(ColocadorPiezas.puedeColocar(mapa(), pieza, colocada[1], colocada[2], zonas()));
    }

    @Test
    void laColocadaAgrandaLaGrillaSiLlegaMasArribaDeLoQueHay() {
        String[] filas = mapa();
        String[] con = ColocadorPiezas.aplicar(filas, PiezaNivel.BLOQUE, 40, 20);
        assertEquals(28, con.length);
        assertEquals('#', ColocadorPiezas.celdaDe(con, 40, 27));
        assertEquals(piso(), con[con.length - 1], "el piso sigue abajo");
        Arena.desdeGrilla(con); // la grilla resultante sigue siendo valida
    }

    @Test
    void elClienteReconstruyeElMismoMapaConLaListaDePiezas() {
        EdicionNivel e = edicion(5);
        for (int ronda = 0; ronda < 3; ronda++) {
            e.iniciar(ronda % 2);
            assertTrue(e.autocolocar());
            e.terminar();
        }
        EstadoPartida estado = new EstadoPartida();
        e.exportar(estado);

        assertArrayEquals(e.getFilas(), EdicionNivel.reconstruir(mapa(), estado.piezas));
        assertEquals(3, estado.piezas.size());
    }

    @Test
    void reiniciarElNivelBorraLasPiezas() {
        EdicionNivel e = edicion(6);
        e.iniciar(0);
        e.autocolocar();
        e.reiniciarNivel();

        assertArrayEquals(mapa(), e.getFilas());
        assertTrue(e.getColocadas().isEmpty());
        assertEquals(-1, e.getEditor());
    }

    @Test
    void sinLugarFueraDeLasZonasNoColocaNada() {
        List<float[]> todo = new ArrayList<>();
        todo.add(new float[] {-1f, -1f, Arena.COLUMNAS + 1f, Arena.FILAS + 1f}); // una zona que tapa todo el mapa
        EdicionNivel e = new EdicionNivel(mapa(), todo, new Random(1));
        e.iniciar(0);
        assertFalse(e.autocolocar());
        assertNull(ColocadorPiezas.buscarValidaCerca(mapa(), PiezaNivel.TRAMPOLIN, 5, 5, todo));
    }

    // ---- la fase de edicion en la partida

    private static void perderLaRonda(RondasPartida rondas, boolean gana1) {
        rondas.actualizar(0.016f, gana1, !gana1);
    }

    @Test
    void trasElResultadoEditaElPerdedorYLuegoArrancaLaRonda() {
        RondasPartida rondas = new RondasPartida(true);
        perderLaRonda(rondas, true);
        assertEquals(Fase.ENTRE_RONDAS, rondas.getFase());

        assertFalse(rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true));
        assertEquals(Fase.EDICION, rondas.getFase());
        assertEquals(1, rondas.getEditor(), "edita el que perdio (el Jugador 2)");
        assertEquals(RondasPartida.DURACION_EDICION, rondas.getTiempoEdicion(), 0.001f);

        assertFalse(rondas.actualizar(1f, true, true));
        assertEquals(Fase.EDICION, rondas.getFase());
        rondas.terminarEdicion();
        assertTrue(rondas.actualizar(0.016f, true, true), "arranca la ronda nueva: hay que devolver a los duelistas");
        assertEquals(Fase.RONDA, rondas.getFase());
        assertEquals(2, rondas.getNumeroRonda());
    }

    @Test
    void siGanoElDosEditaElUno() {
        RondasPartida rondas = new RondasPartida(true);
        perderLaRonda(rondas, false);
        rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true);
        assertEquals(0, rondas.getEditor());
    }

    @Test
    void elTiempoDeEdicionSeAgotaPeroLaRondaNoArrancaSola() {
        RondasPartida rondas = new RondasPartida(true);
        perderLaRonda(rondas, true);
        rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true);

        rondas.actualizar(RondasPartida.DURACION_EDICION + 1f, true, true);

        assertTrue(rondas.edicionAgotada());
        assertEquals(Fase.EDICION, rondas.getFase(), "espera a que quien la lleva coloque y termine");
        assertEquals(0f, rondas.getTiempoEdicion(), 0.0001f);
    }

    @Test
    void unEmpateNoTieneEdicion() {
        RondasPartida rondas = new RondasPartida(true);
        rondas.actualizar(0.016f, false, false);
        assertTrue(rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true));
        assertEquals(Fase.RONDA, rondas.getFase());
    }

    @Test
    void trasLaUltimaRondaNoHayEdicion() {
        RondasPartida rondas = new RondasPartida(true);
        for (int i = 0; i < 3; i++) {
            perderLaRonda(rondas, true);
            if (i < 2) {
                rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true);
                rondas.terminarEdicion();
                rondas.actualizar(0.016f, true, true);
            }
        }
        assertEquals(Fase.GANADOR, rondas.getFase());
    }

    @Test
    void sinEdicionActivadaElComportamientoNoCambia() {
        RondasPartida rondas = new RondasPartida();
        perderLaRonda(rondas, true);
        assertTrue(rondas.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true));
        assertEquals(Fase.RONDA, rondas.getFase());
    }

    @Test
    void elClienteCopiaLaEdicionDelAnfitrion() {
        RondasPartida anfitrion = new RondasPartida(true);
        perderLaRonda(anfitrion, true);
        anfitrion.actualizar(RondasPartida.DURACION_ENTRE_RONDAS + 0.01f, true, true);
        anfitrion.actualizar(3f, true, true);
        EstadoPartida estado = new EstadoPartida();
        anfitrion.exportar(estado);

        RondasPartida cliente = new RondasPartida();
        cliente.importar(estado);

        assertEquals(Fase.EDICION, cliente.getFase());
        assertEquals(1, cliente.getEditor());
        assertEquals(17f, cliente.getTiempoEdicion(), 0.001f);
        assertNotEquals(-1, estado.editor);
    }
}
