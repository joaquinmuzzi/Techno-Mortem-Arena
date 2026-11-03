package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.partida.EdicionNivel;
import com.et35.technomortemarena.partida.EstadoPartida;
import com.et35.technomortemarena.partida.PiezaNivel;
import com.et35.technomortemarena.partida.MarcadorPartida;
import com.et35.technomortemarena.partida.RondasPartida;
import java.nio.ByteBuffer;

/**
 * Pasa un {@link EstadoJugador} a bytes y de vuelta ({@link #BYTES} bytes), y lo mismo con el
 * {@link EstadoPartida} ({@link #BYTES_PARTIDA} bytes).
 */
final class CodecEstado {

    static final int BYTES = 4 + 4 + 1 + 1 + 4 + 4 + 4 + 4 + 4;

    /**
     * Lo fijo del estado de la partida: etapa, numero de ronda, rondas ganadas de cada uno y ganador de la
     * ultima ronda (un byte cada uno), los dos tiempos, y la edicion del nivel (editor, tres opciones,
     * eleccion, celda del cursor, tiempo y cantidad de piezas). Cada pieza colocada suma
     * {@link #BYTES_POR_PIEZA}.
     */
    static final int BYTES_PARTIDA = 5 + 4 + 4 + (1 + 3 + 1 + 1 + 1 + 4 + 1);
    /** Tipo, columna y fila de una pieza colocada. */
    static final int BYTES_POR_PIEZA = 3;

    private static final int BIT_MIRANDO = 1;
    private static final int BIT_VIVO = 2;
    private static final int BIT_CORRIENDO = 4;
    private static final int BIT_RODANDO = 8;

    private CodecEstado() {
    }

    static void escribir(ByteBuffer buffer, EstadoJugador estado) {
        buffer.putFloat(estado.x);
        buffer.putFloat(estado.y);
        buffer.put((byte) estado.vida);
        int banderas = (estado.mirandoDerecha ? BIT_MIRANDO : 0)
            | (estado.vivo ? BIT_VIVO : 0)
            | (estado.corriendo ? BIT_CORRIENDO : 0)
            | (estado.rodando ? BIT_RODANDO : 0);
        buffer.put((byte) banderas);
        buffer.putFloat(estado.anguloEspada);
        buffer.putFloat(estado.extension);
        buffer.putFloat(estado.tiempoRestanteFlash);
        buffer.putInt(estado.contadorGolpes);
        buffer.putInt(estado.contadorAterrizajes);
    }

    /**
     * Lee un estado del buffer.
     *
     * @return {@code false} si no hay bytes suficientes o algun valor no tiene sentido (un numero no
     *         finito, una extension fuera de 0 a 1): en ese caso {@code destino} no se toca
     */
    static boolean leer(ByteBuffer buffer, EstadoJugador destino) {
        if (buffer.remaining() < BYTES) {
            return false;
        }
        float x = buffer.getFloat();
        float y = buffer.getFloat();
        int vida = buffer.get();
        int banderas = buffer.get();
        float angulo = buffer.getFloat();
        float extension = buffer.getFloat();
        float flash = buffer.getFloat();
        int golpes = buffer.getInt();
        int aterrizajes = buffer.getInt();
        if (!finito(x) || !finito(y) || !finito(angulo) || !finito(extension) || !finito(flash)
            || vida < 0 || extension < 0f || extension > 1f) {
            return false;
        }
        destino.x = x;
        destino.y = y;
        destino.vida = vida;
        destino.mirandoDerecha = (banderas & BIT_MIRANDO) != 0;
        destino.vivo = (banderas & BIT_VIVO) != 0;
        destino.corriendo = (banderas & BIT_CORRIENDO) != 0;
        destino.rodando = (banderas & BIT_RODANDO) != 0;
        destino.anguloEspada = angulo;
        destino.extension = extension;
        destino.tiempoRestanteFlash = flash;
        destino.contadorGolpes = golpes;
        destino.contadorAterrizajes = aterrizajes;
        return true;
    }

    static void escribir(ByteBuffer buffer, EstadoPartida partida) {
        buffer.put((byte) partida.fase);
        buffer.put((byte) Math.min(partida.numeroRonda, 255));
        buffer.put((byte) partida.rondasGanadasUno);
        buffer.put((byte) partida.rondasGanadasDos);
        buffer.put((byte) partida.ganadorDeLaRonda);
        buffer.putFloat(partida.tiempoRestante);
        buffer.putFloat(partida.tiempoMuerteSubita);
        buffer.put((byte) partida.editor);
        for (int opcion : partida.opciones) {
            buffer.put((byte) opcion);
        }
        buffer.put((byte) partida.eleccion);
        buffer.put((byte) partida.cursorColumna);
        buffer.put((byte) partida.cursorFila);
        buffer.putFloat(partida.tiempoEdicion);
        int cantidad = Math.min(partida.piezas.size(), EdicionNivel.MAX_PIEZAS);
        buffer.put((byte) cantidad);
        for (int i = 0; i < cantidad; i++) {
            int[] pieza = partida.piezas.get(i);
            buffer.put((byte) pieza[0]);
            buffer.put((byte) pieza[1]);
            buffer.put((byte) pieza[2]);
        }
    }

    /**
     * Lee el estado de la partida.
     *
     * @return {@code false} si faltan bytes o algun valor no tiene sentido (una etapa que no existe, mas
     *         rondas ganadas de las que alcanzan para ganar, un ganador que no es 0, 1 ni -1): en ese
     *         caso {@code destino} no se toca
     */
    static boolean leer(ByteBuffer buffer, EstadoPartida destino) {
        if (buffer.remaining() < BYTES_PARTIDA) {
            return false;
        }
        int fase = buffer.get();
        int ronda = buffer.get() & 0xFF;
        int uno = buffer.get();
        int dos = buffer.get();
        int ganador = buffer.get();
        float tiempoRestante = buffer.getFloat();
        float tiempoMuerteSubita = buffer.getFloat();
        int editor = buffer.get();
        int[] opciones = {buffer.get(), buffer.get(), buffer.get()};
        int eleccion = buffer.get();
        int cursorColumna = buffer.get();
        int cursorFila = buffer.get();
        float tiempoEdicion = buffer.getFloat();
        int cantidad = buffer.get();
        if (cantidad < 0 || cantidad > EdicionNivel.MAX_PIEZAS || buffer.remaining() < cantidad * BYTES_POR_PIEZA) {
            return false;
        }
        int[][] piezas = new int[cantidad][];
        for (int i = 0; i < cantidad; i++) {
            piezas[i] = new int[] {buffer.get(), buffer.get(), buffer.get()};
            if (PiezaNivel.porIndice(piezas[i][0]) == null || piezas[i][1] < 0 || piezas[i][1] >= Arena.COLUMNAS
                || piezas[i][2] < 0 || piezas[i][2] >= Arena.FILAS) {
                return false;
            }
        }
        for (int opcion : opciones) {
            if (opcion < -1 || opcion >= PiezaNivel.values().length) {
                return false;
            }
        }
        if (editor < -1 || editor > 1 || eleccion < -1 || eleccion >= EdicionNivel.OPCIONES
            || cursorColumna < 0 || cursorColumna >= Arena.COLUMNAS || cursorFila < 0 || cursorFila >= Arena.FILAS
            || !finito(tiempoEdicion) || tiempoEdicion < 0f) {
            return false;
        }
        int maximo = MarcadorPartida.RONDAS_PARA_GANAR;
        if (fase < 0 || fase >= RondasPartida.Fase.values().length || ronda < 1
            || uno < 0 || uno > maximo || dos < 0 || dos > maximo || ganador < -1 || ganador > 1
            || Float.isNaN(tiempoRestante) || Float.isInfinite(tiempoRestante) || tiempoRestante < 0f
            || Float.isNaN(tiempoMuerteSubita) || Float.isInfinite(tiempoMuerteSubita) || tiempoMuerteSubita < 0f) {
            return false;
        }
        destino.fase = fase;
        destino.numeroRonda = ronda;
        destino.rondasGanadasUno = uno;
        destino.rondasGanadasDos = dos;
        destino.ganadorDeLaRonda = ganador;
        destino.tiempoRestante = tiempoRestante;
        destino.tiempoMuerteSubita = tiempoMuerteSubita;
        destino.editor = editor;
        System.arraycopy(opciones, 0, destino.opciones, 0, opciones.length);
        destino.eleccion = eleccion;
        destino.cursorColumna = cursorColumna;
        destino.cursorFila = cursorFila;
        destino.tiempoEdicion = tiempoEdicion;
        destino.piezas.clear();
        for (int[] pieza : piezas) {
            destino.piezas.add(pieza);
        }
        return true;
    }

    private static boolean finito(float valor) {
        return !Float.isNaN(valor) && !Float.isInfinite(valor);
    }
}
