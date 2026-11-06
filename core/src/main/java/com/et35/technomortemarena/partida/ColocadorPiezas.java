package com.et35.technomortemarena.partida;

import com.et35.technomortemarena.mundo.Arena;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ColocadorPiezas {
    public static final int MARGEN_SPAWN = 4;

    private ColocadorPiezas() {
    }

    public static float[] zonaDeSpawn(float x, float y, float ancho, float alto) {
        return new float[] {
            x / Arena.CELDA - MARGEN_SPAWN, y / Arena.CELDA - MARGEN_SPAWN,
            (x + ancho) / Arena.CELDA + MARGEN_SPAWN, (y + alto) / Arena.CELDA + MARGEN_SPAWN};
    }

    public static int columnaDe(float x) {
        return Math.max(0, Math.min(Arena.COLUMNAS - 1, (int) Math.floor(x / Arena.CELDA)));
    }

    public static int filaDe(float y) {
        return Math.max(0, Math.min(Arena.FILAS - 1, (int) Math.floor(y / Arena.CELDA)));
    }

    public static int[] origenPara(PiezaNivel pieza, int cursorColumna, int cursorFila) {
        int columna = Math.max(0, Math.min(Arena.COLUMNAS - pieza.getAncho(), cursorColumna - pieza.getAncho() / 2));
        int fila = Math.max(0, Math.min(Arena.FILAS - pieza.getAlto(), cursorFila - pieza.getAlto() / 2));
        return new int[] {columna, fila};
    }

    public static boolean puedeColocar(String[] filas, PiezaNivel pieza, int columna, int fila,
                                       List<float[]> zonasProhibidas) {
        if (columna < 0 || fila < 0 || columna + pieza.getAncho() > Arena.COLUMNAS
            || fila + pieza.getAlto() > Arena.FILAS) {
            return false;
        }
        for (float[] zona : zonasProhibidas) {
            boolean seCruzan = columna < zona[2] && columna + pieza.getAncho() > zona[0]
                && fila < zona[3] && fila + pieza.getAlto() > zona[1];
            if (seCruzan) {
                return false;
            }
        }
        return true;
    }

    static char celdaDe(String[] filas, int columna, int fila) {
        int indice = filas.length - 1 - fila;
        return indice >= 0 && indice < filas.length ? filas[indice].charAt(columna) : '.';
    }

    public static String[] aplicar(String[] filas, PiezaNivel pieza, int columna, int fila) {
        int altoNecesario = Math.max(filas.length, fila + pieza.getAlto());
        char[][] grilla = new char[altoNecesario][];
        for (int i = 0; i < altoNecesario; i++) {
            int original = i - (altoNecesario - filas.length);
            grilla[i] = original >= 0 ? filas[original].toCharArray() : puroAire();
        }
        for (int dy = 0; dy < pieza.getAlto(); dy++) {
            for (int dx = 0; dx < pieza.getAncho(); dx++) {
                char celda = pieza.celda(dx, dy);
                if (celda != '.') {
                    grilla[altoNecesario - 1 - (fila + dy)][columna + dx] = celda;
                }
            }
        }
        String[] resultado = new String[altoNecesario];
        for (int i = 0; i < altoNecesario; i++) {
            resultado[i] = new String(grilla[i]);
        }
        return resultado;
    }

    private static char[] puroAire() {
        char[] aire = new char[Arena.COLUMNAS];
        Arrays.fill(aire, '.');
        return aire;
    }

    public static int[] buscarValidaCerca(String[] filas, PiezaNivel pieza, int columna, int fila,
                                          List<float[]> zonasProhibidas) {
        int maximo = Math.max(Arena.COLUMNAS, Arena.FILAS);
        for (int radio = 0; radio <= maximo; radio++) {
            for (int dy = -radio; dy <= radio; dy++) {
                for (int dx = -radio; dx <= radio; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != radio) {
                        continue;
                    }
                    if (puedeColocar(filas, pieza, columna + dx, fila + dy, zonasProhibidas)) {
                        return new int[] {columna + dx, fila + dy};
                    }
                }
            }
        }
        return null;
    }

    public static int[] buscarValidaAlAzar(String[] filas, PiezaNivel pieza, Random azar,
                                           List<float[]> zonasProhibidas) {
        for (int intento = 0; intento < 200; intento++) {
            int columna = azar.nextInt(Arena.COLUMNAS - pieza.getAncho() + 1);
            int fila = azar.nextInt(Arena.FILAS - pieza.getAlto() + 1);
            if (puedeColocar(filas, pieza, columna, fila, zonasProhibidas)) {
                return new int[] {columna, fila};
            }
        }
        return buscarValidaCerca(filas, pieza, azar.nextInt(Arena.COLUMNAS), azar.nextInt(Arena.FILAS), zonasProhibidas);
    }
}
