package com.et35.technomortemarena.partida;

import com.et35.technomortemarena.mundo.Arena;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Las reglas para colocar una {@link PiezaNivel} sobre la grilla de una arena. Todo en celdas: la columna
 * 0 es la de la izquierda y la fila 0 es la del piso (de abajo hacia arriba), al reves que el texto de la
 * grilla, donde la primera fila es la de arriba.
 *
 * <p>Una pieza se puede colocar si entra en la grilla, no pisa ninguna celda ocupada y no toca una zona
 * prohibida (el entorno de los spawns, para que nadie arme una trampa de pinchos sobre el punto de
 * aparicion). Es logica pura, sin libGDX.
 */
public final class ColocadorPiezas {

    /** Celdas libres que se dejan alrededor del cuerpo de un jugador al aparecer. */
    public static final int MARGEN_SPAWN = 4;

    private ColocadorPiezas() {
    }

    /**
     * La zona prohibida alrededor de un spawn, como {@code {x0, y0, x1, y1}} en celdas: el cuerpo del
     * jugador con {@link #MARGEN_SPAWN} celdas de margen en cada lado.
     *
     * @param x, y posicion de los pies del jugador en la arena, en pixeles del mundo
     * @param ancho, alto medidas del cuerpo del jugador, en pixeles del mundo
     */
    public static float[] zonaDeSpawn(float x, float y, float ancho, float alto) {
        return new float[] {
            x / Arena.CELDA - MARGEN_SPAWN, y / Arena.CELDA - MARGEN_SPAWN,
            (x + ancho) / Arena.CELDA + MARGEN_SPAWN, (y + alto) / Arena.CELDA + MARGEN_SPAWN};
    }

    /** La celda (columna) en la que cae una coordenada x del mundo, sin salirse de la grilla. */
    public static int columnaDe(float x) {
        return Math.max(0, Math.min(Arena.COLUMNAS - 1, (int) Math.floor(x / Arena.CELDA)));
    }

    /** La celda (fila, desde el piso) en la que cae una coordenada y del mundo, sin salirse de la grilla. */
    public static int filaDe(float y) {
        return Math.max(0, Math.min(Arena.FILAS - 1, (int) Math.floor(y / Arena.CELDA)));
    }

    /**
     * Esquina inferior izquierda de la pieza cuando el cursor esta sobre la celda {@code (cursorColumna,
     * cursorFila)}: la pieza queda centrada en el cursor y se corre para no salirse de la grilla.
     *
     * @return {@code {columna, fila}}
     */
    public static int[] origenPara(PiezaNivel pieza, int cursorColumna, int cursorFila) {
        int columna = Math.max(0, Math.min(Arena.COLUMNAS - pieza.getAncho(), cursorColumna - pieza.getAncho() / 2));
        int fila = Math.max(0, Math.min(Arena.FILAS - pieza.getAlto(), cursorFila - pieza.getAlto() / 2));
        return new int[] {columna, fila};
    }

    /**
     * Si la pieza con la esquina inferior izquierda en {@code (columna, fila)} se puede colocar.
     *
     * @param filas la grilla actual, la primera fila la de arriba; las filas que no existen (por encima
     *              de la ultima) cuentan como aire
     * @param zonasProhibidas zonas {@code {x0, y0, x1, y1}} en celdas
     */
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
        for (int dy = 0; dy < pieza.getAlto(); dy++) {
            for (int dx = 0; dx < pieza.getAncho(); dx++) {
                if (pieza.celda(dx, dy) != '.' && celdaDe(filas, columna + dx, fila + dy) != '.') {
                    return false;
                }
            }
        }
        return true;
    }

    /** El caracter de la grilla en {@code (columna, fila)}, con la fila contada desde el piso. */
    static char celdaDe(String[] filas, int columna, int fila) {
        int indice = filas.length - 1 - fila;
        return indice >= 0 && indice < filas.length ? filas[indice].charAt(columna) : '.';
    }

    /**
     * Una grilla nueva con la pieza dibujada. Si la pieza llega mas arriba que la grilla, se agregan filas
     * de aire arriba. No valida nada: hay que llamar antes a {@link #puedeColocar}.
     */
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

    /**
     * La posicion valida mas cercana a {@code (columna, fila)}, buscando en anillos cada vez mas grandes.
     *
     * @return {@code {columna, fila}}, o {@code null} si no hay lugar en toda la grilla
     */
    public static int[] buscarValidaCerca(String[] filas, PiezaNivel pieza, int columna, int fila,
                                          List<float[]> zonasProhibidas) {
        int maximo = Math.max(Arena.COLUMNAS, Arena.FILAS);
        for (int radio = 0; radio <= maximo; radio++) {
            for (int dy = -radio; dy <= radio; dy++) {
                for (int dx = -radio; dx <= radio; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != radio) {
                        continue; // solo el borde del anillo: el interior ya se probo
                    }
                    if (puedeColocar(filas, pieza, columna + dx, fila + dy, zonasProhibidas)) {
                        return new int[] {columna + dx, fila + dy};
                    }
                }
            }
        }
        return null;
    }

    /**
     * Una posicion valida al azar: prueba posiciones al azar y, si no tiene suerte, busca la primera
     * libre desde un punto al azar.
     *
     * @return {@code {columna, fila}}, o {@code null} si no hay lugar en toda la grilla
     */
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
