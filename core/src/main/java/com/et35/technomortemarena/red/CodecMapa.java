package com.et35.technomortemarena.red;

import com.et35.technomortemarena.mundo.Arena;
import java.io.ByteArrayOutputStream;

/**
 * Pasa las filas de texto de un mapa a bytes y de vuelta, comprimidas con repeticiones.
 *
 * <p>Cada fila ocupa {@link Arena#COLUMNAS} caracteres y casi todos se repiten (un piso entero de
 * {@code #}), asi que se guardan como pares {@code [caracter][cantidad]}. Una fila vacia pesa 2 bytes
 * en lugar de 90 y todo el mapa entra en un par de datagramas.
 *
 * <p>Lo que llega de la red no es de fiar: al leer se comprueba que la fila tenga exactamente el
 * largo esperado y solo caracteres que el juego entiende.
 */
public final class CodecMapa {

    /** Caracteres que puede tener una fila (ver {@link Arena#desdeGrilla}). */
    static final String CELDAS_VALIDAS = "#TLRX.12";

    private CodecMapa() {
    }

    public static byte[] codificarFila(String fila) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        int i = 0;
        while (i < fila.length()) {
            char caracter = fila.charAt(i);
            int cantidad = 1;
            while (i + cantidad < fila.length() && fila.charAt(i + cantidad) == caracter && cantidad < 255) {
                cantidad++;
            }
            salida.write(caracter);
            salida.write(cantidad);
            i += cantidad;
        }
        return salida.toByteArray();
    }

    /**
     * @return la fila, o {@code null} si los bytes no son una fila valida de {@code columnas} celdas
     */
    public static String decodificarFila(byte[] datos, int columnas) {
        if (datos.length == 0 || datos.length % 2 != 0) {
            return null;
        }
        StringBuilder fila = new StringBuilder(columnas);
        for (int i = 0; i < datos.length; i += 2) {
            char caracter = (char) (datos[i] & 0xFF);
            int cantidad = datos[i + 1] & 0xFF;
            if (cantidad == 0 || CELDAS_VALIDAS.indexOf(caracter) < 0 || fila.length() + cantidad > columnas) {
                return null;
            }
            for (int k = 0; k < cantidad; k++) {
                fila.append(caracter);
            }
        }
        return fila.length() == columnas ? fila.toString() : null;
    }
}
