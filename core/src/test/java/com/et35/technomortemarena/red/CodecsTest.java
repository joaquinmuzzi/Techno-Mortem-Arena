package com.et35.technomortemarena.red;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.mundo.Arena;
import java.nio.ByteBuffer;
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
