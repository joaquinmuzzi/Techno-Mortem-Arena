package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entidades.EstadoJugador;
import java.nio.ByteBuffer;

/** Pasa un {@link EstadoJugador} a bytes y de vuelta. Ocupa {@link #BYTES} bytes. */
final class CodecEstado {

    static final int BYTES = 4 + 4 + 1 + 1 + 4 + 4 + 4 + 4 + 4;

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

    private static boolean finito(float valor) {
        return !Float.isNaN(valor) && !Float.isInfinite(valor);
    }
}
