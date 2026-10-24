package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.partida.EstadoPartida;
import com.et35.technomortemarena.partida.MarcadorPartida;
import com.et35.technomortemarena.partida.RondasPartida;
import java.nio.ByteBuffer;

/**
 * Pasa un {@link EstadoJugador} a bytes y de vuelta ({@link #BYTES} bytes), y lo mismo con el
 * {@link EstadoPartida} ({@link #BYTES_PARTIDA} bytes).
 */
final class CodecEstado {

    static final int BYTES = 4 + 4 + 1 + 1 + 4 + 4 + 4 + 4 + 4;

    /** Etapa, numero de ronda, rondas ganadas de cada uno y ganador de la ultima ronda: un byte cada uno. */
    static final int BYTES_PARTIDA = 5 + 4 + 4;

    private static final int BIT_MIRANDO = 1;
    private static final int BIT_VIVO = 2;
    private static final int BIT_CORRIENDO = 4;

    private CodecEstado() {
    }

    static void escribir(ByteBuffer buffer, EstadoJugador estado) {
        buffer.putFloat(estado.x);
        buffer.putFloat(estado.y);
        buffer.put((byte) estado.vida);
        int banderas = (estado.mirandoDerecha ? BIT_MIRANDO : 0)
            | (estado.vivo ? BIT_VIVO : 0)
            | (estado.corriendo ? BIT_CORRIENDO : 0);
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
        return true;
    }

    private static boolean finito(float valor) {
        return !Float.isNaN(valor) && !Float.isInfinite(valor);
    }
}
