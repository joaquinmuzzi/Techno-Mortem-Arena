package com.et35.technomortemarena.red;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.LongSupplier;

public final class Protocolo {
    public static final short MAGIA = 0x544D;
    public static final byte VERSION = 12;

    public static final int PUERTO_POR_DEFECTO = 7777;

    public static final int MAX_JUGADORES = 2;

    public static final int MAX_DATAGRAMA = 1200;

    public static final int MAX_FILAS = 128;

    public static final byte HELLO = 1;
    public static final byte BIENVENIDA = 2;
    public static final byte FILA_MAPA = 3;
    public static final byte ENTRADA = 4;
    public static final byte ESTADO = 5;
    public static final byte ADIOS = 6;
    public static final byte SALA_LLENA = 7;

    public static final long REINTENTO_MS = 250;
    public static final long TIEMPO_CONEXION_MS = 5000;
    public static final long TIEMPO_SIN_NOTICIAS_MS = 3000;
    public static final long PAUSA_ENTRE_RESPUESTAS_MS = 150;

    public static final LongSupplier RELOJ_SISTEMA = () -> System.nanoTime() / 1_000_000L;

    private static final int LARGO_CABECERA = 4;

    private Protocolo() {
    }

    static ByteBuffer nuevo(byte tipo) {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_DATAGRAMA);
        buffer.putShort(MAGIA);
        buffer.put(VERSION);
        buffer.put(tipo);
        return buffer;
    }

    static int leerTipo(ByteBuffer buffer) {
        if (buffer.remaining() < LARGO_CABECERA) {
            return -1;
        }
        if (buffer.getShort() != MAGIA || buffer.get() != VERSION) {
            return -1;
        }
        return buffer.get();
    }

    static void enviar(DatagramChannel canal, ByteBuffer buffer, SocketAddress destino) {
        buffer.flip();
        try {
            canal.send(buffer, destino);
        } catch (IOException e) {
        }
    }

    static boolean esMasNueva(int nueva, int vieja) {
        return (nueva - vieja) > 0;
    }
}
