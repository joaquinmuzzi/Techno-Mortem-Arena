package com.et35.technomortemarena.red;

import com.et35.technomortemarena.mundo.Arena;
import java.io.ByteArrayOutputStream;

public final class CodecMapa {
    static final String CELDAS_VALIDAS = "#TLRXPEFC.12";

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
