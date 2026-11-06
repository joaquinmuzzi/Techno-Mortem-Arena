package com.et35.technomortemarena.mundo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Arena {
    public static final float ESCALA = 2f;

    public static final int COLUMNAS = 180;

    public static final int FILAS_PISO = 3;

    private static final float ESCALA_ARENA = 2f * ESCALA;

    public static final float ANCHO_MUNDO = 2f * 960f * ESCALA_ARENA;
    public static final float ALTO_MUNDO = 2f * 540f * ESCALA_ARENA;
    public static final float ANCHO_VISTA = ANCHO_MUNDO / 2f;
    public static final float ALTO_VISTA = ALTO_MUNDO / 2f;

    public static final float CELDA = ANCHO_MUNDO / COLUMNAS;

    public static final int FILAS = (int) (ALTO_MUNDO / CELDA);

    private final float ancho;
    private final float alto;
    private final List<Bloque> bloques;
    private final Vector2 spawnUno;
    private final Vector2 spawnDos;
    private final String[] filas;

    public Arena(float ancho, float alto, List<Bloque> bloques, Vector2 spawnUno, Vector2 spawnDos) {
        this(ancho, alto, bloques, spawnUno, spawnDos, new String[0]);
    }

    private Arena(float ancho, float alto, List<Bloque> bloques, Vector2 spawnUno, Vector2 spawnDos, String[] filas) {
        this.filas = filas.clone();
        this.ancho = ancho;
        this.alto = alto;
        this.bloques = Collections.unmodifiableList(new ArrayList<>(bloques));
        this.spawnUno = spawnUno == null ? null : new Vector2(spawnUno);
        this.spawnDos = spawnDos == null ? null : new Vector2(spawnDos);
    }

    public static Arena porDefecto() {
        return desdeGrilla(
            repetir('#', COLUMNAS),
            repetir('#', COLUMNAS),
            repetir('#', COLUMNAS));
    }

    public static Arena conAgujero() {
        int lado = (COLUMNAS - 18) / 2;
        String fila = repetir('#', lado) + repetir('.', 18) + repetir('#', COLUMNAS - lado - 18);
        return desdeGrilla(fila, fila, fila);
    }

    public static Arena conPlataformas() {
        String alto = repetir('#', 12) + repetir('.', COLUMNAS - 24) + repetir('#', 12);
        String bajo = repetir('.', 12) + repetir('#', 12) + repetir('.', COLUMNAS - 48) + repetir('#', 12) + repetir('.', 12);
        String piso = repetir('#', COLUMNAS);
        return desdeGrilla(
            alto, alto, alto,
            bajo, bajo, bajo,
            piso, piso, piso);
    }

    public static Arena conTrampolin() {
        int[] centros = {COLUMNAS / 2 - 60, COLUMNAS / 2, COLUMNAS / 2 + 60};
        char[] plataforma = repetir('.', COLUMNAS).toCharArray();
        char[] piso = repetir('#', COLUMNAS).toCharArray();
        for (int centro : centros) {
            for (int c = centro - 9; c < centro + 9; c++) {
                plataforma[c] = '#';
            }
            for (int c = centro - 3; c < centro + 3; c++) {
                piso[c] = 'T';
            }
        }
        String alta = new String(plataforma);
        String vacia = repetir('.', COLUMNAS);
        String pisoConTrampolines = new String(piso);
        return desdeGrilla(
            alta, alta, alta,
            vacia, vacia, vacia,
            vacia, vacia, vacia,
            pisoConTrampolines, pisoConTrampolines, pisoConTrampolines);
    }

    public static Arena conCintasYPinchos() {
        String vacia = repetir('.', COLUMNAS);
        String trampa = repetir('L', 9) + repetir('.', 9) + repetir('X', 3) + repetir('.', 6) + repetir('R', 9);
        int antes = (COLUMNAS - trampa.length()) / 2;
        String cintas = repetir('.', antes) + trampa + repetir('.', COLUMNAS - antes - trampa.length());
        String piso = repetir('#', COLUMNAS);
        return desdeGrilla(
            vacia, vacia, vacia, vacia, vacia, vacia, vacia, vacia,
            cintas,
            piso, piso, piso);
    }

    private static String repetir(char caracter, int veces) {
        StringBuilder texto = new StringBuilder(veces);
        for (int i = 0; i < veces; i++) {
            texto.append(caracter);
        }
        return texto.toString();
    }

    public static Arena desdeGrilla(String... filas) {
        List<Bloque> bloques = new ArrayList<>();
        Vector2 spawnUno = null;
        Vector2 spawnDos = null;
        for (int fila = 0; fila < filas.length; fila++) {
            String texto = filas[fila];
            if (texto.length() != COLUMNAS) {
                throw new IllegalArgumentException("Cada fila de la grilla debe tener " + COLUMNAS
                    + " celdas, y la fila " + fila + " tiene " + texto.length());
            }
            float y = (filas.length - 1 - fila) * CELDA;
            for (int columna = 0; columna < COLUMNAS; columna++) {
                Rectangle area = new Rectangle(columna * CELDA, y, CELDA, CELDA);
                char celda = texto.charAt(columna);
                if (celda == '1') {
                    spawnUno = new Vector2(columna * CELDA, y);
                } else if (celda == '2') {
                    spawnDos = new Vector2(columna * CELDA, y);
                }
                Bloque bloque = bloquePara(celda, area);
                if (bloque != null) {
                    bloques.add(bloque);
                }
            }
        }
        return new Arena(ANCHO_MUNDO, ALTO_MUNDO, bloques, spawnUno, spawnDos, filas);
    }

    public Arena sinCeldas(List<int[]> celdas) {
        if (filas.length == 0 || celdas.isEmpty()) {
            return this;
        }
        char[][] grilla = new char[filas.length][];
        for (int i = 0; i < filas.length; i++) {
            grilla[i] = filas[i].toCharArray();
        }
        for (int[] celda : celdas) {
            int indice = filas.length - 1 - celda[1];
            if (celda[0] >= 0 && celda[0] < COLUMNAS && indice >= 0 && indice < filas.length) {
                grilla[indice][celda[0]] = '.';
            }
        }
        String[] nuevas = new String[filas.length];
        for (int i = 0; i < filas.length; i++) {
            nuevas[i] = new String(grilla[i]);
        }
        return desdeGrilla(nuevas);
    }

    public String[] getFilas() {
        return filas.clone();
    }

    public Vector2 getSpawnUno() {
        return spawnUno == null ? null : new Vector2(spawnUno);
    }

    public Vector2 getSpawnDos() {
        return spawnDos == null ? null : new Vector2(spawnDos);
    }

    public static Bloque bloquePara(char celda, Rectangle area) {
        switch (celda) {
            case '#':
                return new Bloque(area);
            case 'T':
                return new Trampolin(area);
            case 'L':
                return new CintaTransportadora(area, -1f);
            case 'R':
                return new CintaTransportadora(area, 1f);
            case 'X':
                return new Pincho(area);
            case 'P':
                return new SpawnerArma(area, false);
            case 'E':
                return new SpawnerArma(area, true);
            default:
                return null;
        }
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }

    public List<Bloque> getBloques() {
        return bloques;
    }

    public float limitarX(float x, float anchoEntidad) {
        float maximo = ancho - anchoEntidad;
        if (x < 0f) {
            return 0f;
        }
        if (x > maximo) {
            return maximo;
        }
        return x;
    }
}
