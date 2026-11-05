package com.et35.technomortemarena.red;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.entidades.EstadoArmas;
import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.EstadoPartida;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CodecsTest {

    private static String fila(char caracter, int veces) {
        char[] celdas = new char[veces];
        Arrays.fill(celdas, caracter);
        return new String(celdas);
    }

    @Test
    void unaFilaVaciaOcupaDosBytes() {
        assertEquals(2, CodecMapa.codificarFila(fila('.', Arena.COLUMNAS)).length);
    }

    @Test
    void todosLosMapasPredefinidosSobrevivenAlViaje() {
        Arena[] mapas = {
            Arena.porDefecto(), Arena.conAgujero(), Arena.conPlataformas(),
            Arena.conTrampolin(), Arena.conCintasYPinchos()
        };
        for (Arena mapa : mapas) {
            for (String original : mapa.getFilas()) {
                byte[] bytes = CodecMapa.codificarFila(original);
                assertEquals(original, CodecMapa.decodificarFila(bytes, Arena.COLUMNAS));
            }
        }
    }

    @Test
    void unaFilaConSpawnsSeConserva() {
        String original = "1" + fila('.', Arena.COLUMNAS - 2) + "2";

        assertEquals(original, CodecMapa.decodificarFila(CodecMapa.codificarFila(original), Arena.COLUMNAS));
    }

    @Test
    void filasInvalidasSeRechazan() {
        // largo equivocado
        assertNull(CodecMapa.decodificarFila(CodecMapa.codificarFila(fila('#', Arena.COLUMNAS - 1)), Arena.COLUMNAS));
        assertNull(CodecMapa.decodificarFila(CodecMapa.codificarFila(fila('#', Arena.COLUMNAS + 1)), Arena.COLUMNAS));
        // caracter que el juego no entiende
        assertNull(CodecMapa.decodificarFila(new byte[] {'Z', (byte) Arena.COLUMNAS}, Arena.COLUMNAS));
        // cantidad cero, bytes impares y vacio
        assertNull(CodecMapa.decodificarFila(new byte[] {'#', 0, '.', (byte) Arena.COLUMNAS}, Arena.COLUMNAS));
        assertNull(CodecMapa.decodificarFila(new byte[] {'#'}, Arena.COLUMNAS));
        assertNull(CodecMapa.decodificarFila(new byte[0], Arena.COLUMNAS));
    }

    private static EstadoJugador estadoDeEjemplo() {
        EstadoJugador estado = new EstadoJugador();
        estado.x = 123.5f;
        estado.y = -4.25f;
        estado.vida = 2;
        estado.mirandoDerecha = true;
        estado.extension = 0.5f;
        estado.vivo = true;
        estado.corriendo = false;
        estado.anguloEspada = 3.5f;
        estado.tiempoRestanteFlash = 0.25f;
        estado.contadorGolpes = 7;
        estado.contadorAterrizajes = 9;
        return estado;
    }

    @Test
    void elEstadoRodandoSobreviveAlViaje() {
        EstadoJugador estado = estadoDeEjemplo();
        estado.rodando = true;
        ByteBuffer buffer = ByteBuffer.allocate(64);
        CodecEstado.escribir(buffer, estado);
        buffer.flip();

        EstadoJugador leido = new EstadoJugador();
        assertTrue(CodecEstado.leer(buffer, leido));

        assertTrue(leido.rodando);
        assertFalse(leido.corriendo);
    }

    @Test
    void unEstadoSobreviveAlViaje() {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        CodecEstado.escribir(buffer, estadoDeEjemplo());
        assertEquals(CodecEstado.BYTES, buffer.position());
        buffer.flip();

        EstadoJugador leido = new EstadoJugador();
        assertTrue(CodecEstado.leer(buffer, leido));

        assertEquals(123.5f, leido.x);
        assertEquals(-4.25f, leido.y);
        assertEquals(2, leido.vida);
        assertTrue(leido.mirandoDerecha);
        assertEquals(0.5f, leido.extension);
        assertTrue(leido.vivo);
        assertFalse(leido.corriendo);
        assertEquals(3.5f, leido.anguloEspada);
        assertEquals(0.25f, leido.tiempoRestanteFlash);
        assertEquals(7, leido.contadorGolpes);
        assertEquals(9, leido.contadorAterrizajes);
    }

    @Test
    void unEstadoCortoOConValoresAbsurdosSeRechaza() {
        ByteBuffer corto = ByteBuffer.allocate(CodecEstado.BYTES - 1);
        assertFalse(CodecEstado.leer(corto, new EstadoJugador()));

        EstadoJugador conNaN = estadoDeEjemplo();
        conNaN.x = Float.NaN;
        EstadoJugador conInfinito = estadoDeEjemplo();
        conInfinito.y = Float.POSITIVE_INFINITY;
        for (EstadoJugador malo : new EstadoJugador[] {conNaN, conInfinito}) {
            ByteBuffer buffer = ByteBuffer.allocate(64);
            CodecEstado.escribir(buffer, malo);
            buffer.flip();
            assertFalse(CodecEstado.leer(buffer, new EstadoJugador()));
        }

        // una extension de la estocada fuera de 0 a 1
        EstadoJugador conExtensionAbsurda = estadoDeEjemplo();
        conExtensionAbsurda.extension = 1.5f;
        ByteBuffer buffer = ByteBuffer.allocate(64);
        CodecEstado.escribir(buffer, conExtensionAbsurda);
        buffer.flip();
        EstadoJugador intacto = new EstadoJugador();
        intacto.x = 1f;
        assertFalse(CodecEstado.leer(buffer, intacto));
        assertEquals(1f, intacto.x, "un estado rechazado no debe tocar el destino");
    }

    @Test
    void elEstadoDeLaPartidaSobreviveAlViaje() {
        EstadoPartida partida = new EstadoPartida();
        partida.fase = 2;
        partida.numeroRonda = 5;
        partida.rondasGanadasUno = 3;
        partida.rondasGanadasDos = 1;
        partida.ganadorDeLaRonda = -1;
        ByteBuffer buffer = ByteBuffer.allocate(128);
        CodecEstado.escribir(buffer, partida);
        assertEquals(CodecEstado.BYTES_PARTIDA, buffer.position());
        buffer.flip();

        EstadoPartida leida = new EstadoPartida();
        assertTrue(CodecEstado.leer(buffer, leida));

        assertEquals(2, leida.fase);
        assertEquals(5, leida.numeroRonda);
        assertEquals(3, leida.rondasGanadasUno);
        assertEquals(1, leida.rondasGanadasDos);
        assertEquals(-1, leida.ganadorDeLaRonda);
    }

    @Test
    void unEstadoDePartidaConValoresAbsurdosSeRechaza() {
        int[][] malos = {
            {7, 1, 0, 0, 0},   // una etapa que no existe
            {0, 0, 0, 0, 0},   // ronda 0
            {0, 1, 9, 0, 0},   // mas rondas ganadas de las que alcanzan para ganar
            {0, 1, 0, 0, 5},   // un ganador que no es 0, 1 ni -1
        };
        for (int[] m : malos) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            for (int valor : m) {
                buffer.put((byte) valor);
            }
            buffer.flip();
            EstadoPartida intacta = new EstadoPartida();
            intacta.numeroRonda = 42;
            assertFalse(CodecEstado.leer(buffer, intacta));
            assertEquals(42, intacta.numeroRonda, "un estado rechazado no debe tocar el destino");
        }
        assertFalse(CodecEstado.leer(ByteBuffer.allocate(2), new EstadoPartida()));
    }

    @Test
    void laEdicionDelNivelSobreviveAlViaje() {
        EstadoPartida partida = new EstadoPartida();
        partida.fase = 4;
        partida.editor = 1;
        partida.opciones[0] = 2;
        partida.opciones[1] = 7;
        partida.opciones[2] = 0;
        partida.eleccion = 1;
        partida.cursorColumna = 45;
        partida.cursorFila = 12;
        partida.tiempoEdicion = 13.5f;
        partida.piezas.add(new int[] {3, 10, 3});
        partida.piezas.add(new int[] {5, 80, 20});
        ByteBuffer buffer = ByteBuffer.allocate(128);
        CodecEstado.escribir(buffer, partida);
        assertEquals(CodecEstado.BYTES_PARTIDA + 2 * CodecEstado.BYTES_POR_PIEZA, buffer.position());
        buffer.flip();

        EstadoPartida leida = new EstadoPartida();
        assertTrue(CodecEstado.leer(buffer, leida));

        assertEquals(4, leida.fase);
        assertEquals(1, leida.editor);
        assertArrayEquals(new int[] {2, 7, 0}, leida.opciones);
        assertEquals(1, leida.eleccion);
        assertEquals(45, leida.cursorColumna);
        assertEquals(12, leida.cursorFila);
        assertEquals(13.5f, leida.tiempoEdicion);
        assertEquals(2, leida.piezas.size());
        assertArrayEquals(new int[] {5, 80, 20}, leida.piezas.get(1));
    }

    @Test
    void unaEdicionConValoresAbsurdosSeRechaza() {
        Consumer<EstadoPartida>[] estropeadores = new Consumer[] {
            (Consumer<EstadoPartida>) e -> e.editor = 5,
            (Consumer<EstadoPartida>) e -> e.opciones[0] = 20,
            (Consumer<EstadoPartida>) e -> e.eleccion = 3,
            (Consumer<EstadoPartida>) e -> e.cursorColumna = Arena.COLUMNAS,
            (Consumer<EstadoPartida>) e -> e.cursorFila = Arena.FILAS,
            (Consumer<EstadoPartida>) e -> e.tiempoEdicion = Float.NaN,
            (Consumer<EstadoPartida>) e -> e.tiempoEdicion = -1f,
            (Consumer<EstadoPartida>) e -> e.piezas.add(new int[] {20, 0, 0}),
            (Consumer<EstadoPartida>) e -> e.piezas.add(new int[] {0, Arena.COLUMNAS, 0}),
            (Consumer<EstadoPartida>) e -> e.piezas.add(new int[] {0, 0, Arena.FILAS}),
        };
        for (Consumer<EstadoPartida> estropear : estropeadores) {
            EstadoPartida mala = new EstadoPartida();
            estropear.accept(mala);
            ByteBuffer buffer = ByteBuffer.allocate(128);
            CodecEstado.escribir(buffer, mala);
            buffer.flip();
            EstadoPartida intacta = new EstadoPartida();
            intacta.numeroRonda = 42;
            assertFalse(CodecEstado.leer(buffer, intacta));
            assertEquals(42, intacta.numeroRonda, "un estado rechazado no debe tocar el destino");
        }
    }

    @Test
    void unaListaDePiezasTruncadaSeRechaza() {
        EstadoPartida partida = new EstadoPartida();
        partida.piezas.add(new int[] {1, 5, 5});
        partida.piezas.add(new int[] {2, 9, 9});
        ByteBuffer buffer = ByteBuffer.allocate(128);
        CodecEstado.escribir(buffer, partida);
        buffer.flip();
        buffer.limit(buffer.limit() - 2);

        assertFalse(CodecEstado.leer(buffer, new EstadoPartida()));
    }

    @Test
    void lasArmasSobrevivenAlViaje() {
        EstadoArmas armas = new EstadoArmas();
        armas.proyectiles.add(new float[] {0, 100.5f, 200.25f, 1.5f});
        armas.proyectiles.add(new float[] {1, 300f, 400f, -2f});
        armas.enElSuelo.add(new float[] {2, 512f, 128f});
        ByteBuffer buffer = ByteBuffer.allocate(256);
        CodecEstado.escribir(buffer, armas);
        assertEquals(CodecEstado.BYTES_ARMAS + 2 * CodecEstado.BYTES_POR_PROYECTIL + CodecEstado.BYTES_POR_ARMA_EN_EL_SUELO,
            buffer.position());
        buffer.flip();

        EstadoArmas leidas = new EstadoArmas();
        assertTrue(CodecEstado.leer(buffer, leidas));
        assertEquals(2, leidas.proyectiles.size());
        assertArrayEquals(new float[] {1, 300f, 400f, -2f}, leidas.proyectiles.get(1));
        assertArrayEquals(new float[] {2, 512f, 128f}, leidas.enElSuelo.get(0));
    }

    @Test
    void unasArmasAbsurdasSeRechazan() {
        float[][][] malas = {
            {{9, 0, 0, 0}},          // un proyectil que no existe
            {{0, Float.NaN, 0, 0}},  // una posicion no finita
        };
        for (float[][] proyectiles : malas) {
            EstadoArmas armas = new EstadoArmas();
            armas.proyectiles.add(proyectiles[0]);
            ByteBuffer buffer = ByteBuffer.allocate(256);
            CodecEstado.escribir(buffer, armas);
            buffer.flip();
            assertFalse(CodecEstado.leer(buffer, new EstadoArmas()));
        }
        EstadoArmas conArmaInexistente = new EstadoArmas();
        conArmaInexistente.enElSuelo.add(new float[] {0, 1f, 1f}); // 0 es "sin arma": no se puede tirar en el suelo
        ByteBuffer buffer = ByteBuffer.allocate(256);
        CodecEstado.escribir(buffer, conArmaInexistente);
        buffer.flip();
        assertFalse(CodecEstado.leer(buffer, new EstadoArmas()));
    }

    @Test
    void elArmaDelJugadorViajaYUnaAbsurdaSeRechaza() {
        EstadoJugador estado = estadoDeEjemplo();
        estado.arma = 2;
        estado.balas = 4;
        ByteBuffer buffer = ByteBuffer.allocate(64);
        CodecEstado.escribir(buffer, estado);
        buffer.flip();
        EstadoJugador leido = new EstadoJugador();
        assertTrue(CodecEstado.leer(buffer, leido));
        assertEquals(2, leido.arma);
        assertEquals(4, leido.balas);

        estado.balas = 99;
        buffer = ByteBuffer.allocate(64);
        CodecEstado.escribir(buffer, estado);
        buffer.flip();
        assertFalse(CodecEstado.leer(buffer, new EstadoJugador()));
    }

    @Test
    void unaFilaConSpawnersDeArmasViajaPorLaRed() {
        String original = "PPP" + fila('.', 10) + "EEE" + fila('#', Arena.COLUMNAS - 16);
        assertEquals(original, CodecMapa.decodificarFila(CodecMapa.codificarFila(original), Arena.COLUMNAS));
    }

    @Test
    void laEdicionEnLasColumnasDeLaDerechaDelMapaGrandeViaja() {
        EstadoPartida partida = new EstadoPartida();
        partida.cursorColumna = Arena.COLUMNAS - 1;
        partida.cursorFila = Arena.FILAS - 1;
        partida.piezas.add(new int[] {1, Arena.COLUMNAS - 5, Arena.FILAS - 3});
        ByteBuffer buffer = ByteBuffer.allocate(128);
        CodecEstado.escribir(buffer, partida);
        buffer.flip();
        EstadoPartida leida = new EstadoPartida();
        assertTrue(CodecEstado.leer(buffer, leida));
        assertEquals(Arena.COLUMNAS - 1, leida.cursorColumna);
        assertEquals(Arena.FILAS - 1, leida.cursorFila);
        assertArrayEquals(new int[] {1, Arena.COLUMNAS - 5, Arena.FILAS - 3}, leida.piezas.get(0));
    }

    @Test
    void lasMejorasViajanPorLaRedYUnaAbsurdaSeRechaza() {
        EstadoArmas armas = new EstadoArmas();
        armas.powerUps.add(new float[] {2, 640f, 128f});
        ByteBuffer buffer = ByteBuffer.allocate(256);
        CodecEstado.escribir(buffer, armas);
        buffer.flip();
        EstadoArmas leidas = new EstadoArmas();
        assertTrue(CodecEstado.leer(buffer, leidas));
        assertArrayEquals(new float[] {2, 640f, 128f}, leidas.powerUps.get(0));

        EstadoArmas mala = new EstadoArmas();
        mala.powerUps.add(new float[] {9, 0f, 0f});
        buffer = ByteBuffer.allocate(256);
        CodecEstado.escribir(buffer, mala);
        buffer.flip();
        assertFalse(CodecEstado.leer(buffer, new EstadoArmas()));
    }

    @Test
    void elTiempoDeLasMejorasDelJugadorViaja() {
        EstadoJugador estado = estadoDeEjemplo();
        estado.tiempoPowerUp[0] = 7.3f;
        ByteBuffer buffer = ByteBuffer.allocate(64);
        CodecEstado.escribir(buffer, estado);
        assertEquals(CodecEstado.BYTES, buffer.position());
        buffer.flip();
        EstadoJugador leido = new EstadoJugador();
        assertTrue(CodecEstado.leer(buffer, leido));
        assertEquals(7.3f, leido.tiempoPowerUp[0], 0.051f);
        assertEquals(0f, leido.tiempoPowerUp[1], 0.0001f);
    }

    @Test
    void laCabeceraSoloAceptaPaquetesDeEsteProtocolo() {
        ByteBuffer bueno = Protocolo.nuevo(Protocolo.ENTRADA);
        bueno.flip();
        assertEquals(Protocolo.ENTRADA, Protocolo.leerTipo(bueno));

        assertEquals(-1, Protocolo.leerTipo(ByteBuffer.wrap(new byte[] {1, 2})));
        assertEquals(-1, Protocolo.leerTipo(ByteBuffer.wrap(new byte[] {0x00, 0x00, Protocolo.VERSION, 4})));
        assertEquals(-1, Protocolo.leerTipo(ByteBuffer.wrap(new byte[] {0x54, 0x4D, (byte) (Protocolo.VERSION + 1), 4})));
    }

    @Test
    void laSecuenciaSigueFuncionandoCuandoDaLaVuelta() {
        assertTrue(Protocolo.esMasNueva(2, 1));
        assertFalse(Protocolo.esMasNueva(1, 2));
        assertFalse(Protocolo.esMasNueva(5, 5));
        assertTrue(Protocolo.esMasNueva(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void lasFilasCodificadasNoSeCompartenPorReferencia() {
        byte[] a = CodecMapa.codificarFila(fila('#', Arena.COLUMNAS));
        byte[] b = CodecMapa.codificarFila(fila('#', Arena.COLUMNAS));
        assertArrayEquals(a, b);
    }
}
