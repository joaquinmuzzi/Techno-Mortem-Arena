package com.et35.technomortemarena.red;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.LongSupplier;

/**
 * Constantes y formato de los datagramas UDP del multijugador.
 *
 * <p>Todos los paquetes empiezan con la misma cabecera de 4 bytes: {@link #MAGIA} (2), {@link #VERSION}
 * (1) y el tipo (1). Un paquete con otra magia, otra version o demasiado corto se descarta sin
 * responder, asi que cualquier otro programa que mande basura a este puerto no hace nada.
 *
 * <p>UDP no garantiza entrega ni orden. Lo que importa de verdad se repite o se numera:
 * <ul>
 *   <li>el cliente repite {@link #HELLO} hasta tener el mapa completo;
 *   <li>las entradas y los estados llevan un numero de secuencia y los viejos se descartan;
 *   <li>las acciones de una sola vez (saltar, subir o bajar la guardia) viajan como contadores, asi
 *       un paquete perdido o duplicado no pierde ni repite la accion.
 * </ul>
 */
public final class Protocolo {

    /** "TM" de Techno Mortem. */
    public static final short MAGIA = 0x544D;
    public static final byte VERSION = 3;

    public static final int PUERTO_POR_DEFECTO = 7777;

    /**
     * Jugadores por sala, el anfitrion incluido. El duelo es 1 contra 1: subir este numero es solo
     * una parte del trabajo, porque {@code Jugador} todavia resuelve las colisiones contra un unico oponente.
     */
    public static final int MAX_JUGADORES = 2;

    /** Tamano maximo de un datagrama. Por debajo del MTU tipico de internet, para que no se fragmente. */
    public static final int MAX_DATAGRAMA = 1200;

    /** Cantidad maxima de filas de un mapa. Cabe en la mascara de 64 bits del {@link #HELLO}. */
    public static final int MAX_FILAS = 64;

    public static final byte HELLO = 1;
    public static final byte BIENVENIDA = 2;
    public static final byte FILA_MAPA = 3;
    public static final byte ENTRADA = 4;
    public static final byte ESTADO = 5;
    public static final byte ADIOS = 6;
    public static final byte SALA_LLENA = 7;

    /** Cada cuanto el cliente repite el {@link #HELLO} mientras conecta. */
    public static final long REINTENTO_MS = 250;
    /** Tiempo maximo para conectar y bajar el mapa. */
    public static final long TIEMPO_CONEXION_MS = 5000;
    /** Si no llega nada en este tiempo, se da la conexion por perdida. */
    public static final long TIEMPO_SIN_NOTICIAS_MS = 3000;
    /** El anfitrion no responde mas de un {@link #HELLO} por este tiempo: limita el trafico que puede provocar. */
    public static final long PAUSA_ENTRE_RESPUESTAS_MS = 150;

    /** Milisegundos de un reloj monotono: el que usa el juego. Los tests usan uno falso. */
    public static final LongSupplier RELOJ_SISTEMA = () -> System.nanoTime() / 1_000_000L;

    private static final int LARGO_CABECERA = 4;

    private Protocolo() {
    }

    /** Arma un buffer con la cabecera ya escrita, listo para agregarle el cuerpo. */
    static ByteBuffer nuevo(byte tipo) {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_DATAGRAMA);
        buffer.putShort(MAGIA);
        buffer.put(VERSION);
        buffer.put(tipo);
        return buffer;
    }

    /**
     * Lee la cabecera de un paquete recibido, con el buffer ya en modo lectura.
     *
     * @return el tipo del paquete, o -1 si no es un paquete de este protocolo
     */
    static int leerTipo(ByteBuffer buffer) {
        if (buffer.remaining() < LARGO_CABECERA) {
            return -1;
        }
        if (buffer.getShort() != MAGIA || buffer.get() != VERSION) {
            return -1;
        }
        return buffer.get();
    }

    /** Manda el buffer tal como esta (hasta su posicion). Es de "mejor esfuerzo": en UDP un fallo al enviar no se reintenta. */
    static void enviar(DatagramChannel canal, ByteBuffer buffer, SocketAddress destino) {
        buffer.flip();
        try {
            canal.send(buffer, destino);
        } catch (IOException e) {
            // Un paquete que no sale es lo mismo que uno que se pierde en el camino: el protocolo ya lo tolera.
        }
    }

    /** Verdadero si {@code nueva} es posterior a {@code vieja}, aunque el contador haya dado la vuelta. */
    static boolean esMasNueva(int nueva, int vieja) {
        return (nueva - vieja) > 0;
    }
}
