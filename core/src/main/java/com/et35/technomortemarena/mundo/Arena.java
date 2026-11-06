package com.et35.technomortemarena.mundo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Escenario cerrado donde se desarrolla el duelo.
 *
 * <p>Las medidas base son 960x540 y se multiplican por la escala de la arena (4), asi que mide el doble
 * que la version anterior. Los jugadores usan {@link #ESCALA}, mas chica, asi que quedan a la mitad de su
 * tamano relativo a la arena.
 *
 * <p>Los bloques de construccion se describen con texto sobre una grilla de {@link #COLUMNAS} columnas.
 * Cada caracter es una celda: {@code #} bloque solido, {@code T} trampolin, {@code L} y {@code R} cinta
 * transportadora hacia la izquierda y hacia la derecha, {@code X} pincho, {@code P} y {@code E} spawner de
 * pistolas y de escopetas y {@code .} aire. La primera fila
 * del texto es la de arriba y la ultima es la del piso.
 */
public class Arena {

    /** Escala de jugadores, espada y fisica. */
    public static final float ESCALA = 2f;

    /** Cantidad de columnas de la grilla de construccion. */
    public static final int COLUMNAS = 180;

    /** Cantidad de filas que ocupa el piso de todos los mapas. */
    public static final int FILAS_PISO = 3;

    private static final float ESCALA_ARENA = 2f * ESCALA;

    /**
     * El mapa mide el doble de ancho y de alto que la vista de la camara mas cerca ({@link #ANCHO_VISTA} x
     * {@link #ALTO_VISTA}): la camara sigue a los duelistas y se aleja hasta mostrarlo entero.
     */
    public static final float ANCHO_MUNDO = 2f * 960f * ESCALA_ARENA;
    public static final float ALTO_MUNDO = 2f * 540f * ESCALA_ARENA;
    /** Lo que muestra la camara con los duelistas cerca: la mitad del mapa en cada sentido. */
    public static final float ANCHO_VISTA = ANCHO_MUNDO / 2f;
    public static final float ALTO_VISTA = ALTO_MUNDO / 2f;

    /** Lado de una celda de construccion. */
    public static final float CELDA = ANCHO_MUNDO / COLUMNAS;

    /** Filas completas de la grilla que entran en la altura de la arena. */
    public static final int FILAS = (int) (ALTO_MUNDO / CELDA);

    private final float ancho;
    private final float alto;
    private final List<Bloque> bloques;
    private final Vector2 spawnUno;
    private final Vector2 spawnDos;
    /** Las filas de texto de las que salio la arena, para mandarla por la red. Vacio si no salio de una grilla. */
    private final String[] filas;

    /** @param spawnUno, spawnDos posicion de los pies de cada jugador, o {@code null} si el mapa no la define */
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

    /** Arena plana con el piso como unica construccion. */
    public static Arena porDefecto() {
        return desdeGrilla(
            repetir('#', COLUMNAS),
            repetir('#', COLUMNAS),
            repetir('#', COLUMNAS));
    }

    /** Igual que {@link #porDefecto()}, pero con un agujero de 18 celdas en el centro del piso, entre los spawns. */
    public static Arena conAgujero() {
        int lado = (COLUMNAS - 18) / 2;
        String fila = repetir('#', lado) + repetir('.', 18) + repetir('#', COLUMNAS - lado - 18);
        return desdeGrilla(fila, fila, fila);
    }

    /**
     * Una escalera de dos escalones en cada punta del mapa, que sube hacia la pared. Cada escalon toca al
     * siguiente, asi que se sube saltando contra la pared de cada uno.
     */
    public static Arena conPlataformas() {
        String alto = repetir('#', 12) + repetir('.', COLUMNAS - 24) + repetir('#', 12);
        String bajo = repetir('.', 12) + repetir('#', 12) + repetir('.', COLUMNAS - 48) + repetir('#', 12) + repetir('.', 12);
        String piso = repetir('#', COLUMNAS);
        return desdeGrilla(
            alto, alto, alto,
            bajo, bajo, bajo,
            piso, piso, piso);
    }

    /**
     * Tres trampolines en el piso, uno al centro y uno a cada lado, cada uno con su plataforma alta que
     * solo se alcanza rebotando.
     */
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

    /** Cintas que empujan en direcciones opuestas y pinchos entre ellas, en el centro. Hay que saltarlos o caer. */
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

    /**
     * Arma una arena desde filas de texto, la primera la de arriba y la ultima la del piso. Cada fila debe
     * tener exactamente {@link #COLUMNAS} caracteres. Es la misma forma que usan los mapas predefinidos y
     * los niveles guardados por el editor.
     */
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

    /**
     * Una copia de esta arena con esas celdas vacias, como {@code {columna, fila}} contando las filas desde
     * abajo: asi quedan los bloques que rompio un cohete. Si la arena no salio de una grilla, devuelve esta.
     */
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

    /** Copia de las filas de texto de la grilla, la primera la de arriba. Vacia si la arena no salio de una grilla. */
    public String[] getFilas() {
        return filas.clone();
    }

    /** Posicion de los pies del jugador 1, o {@code null} si el mapa no define el spawn. */
    public Vector2 getSpawnUno() {
        return spawnUno == null ? null : new Vector2(spawnUno);
    }

    /** Posicion de los pies del jugador 2, o {@code null} si el mapa no define el spawn. */
    public Vector2 getSpawnDos() {
        return spawnDos == null ? null : new Vector2(spawnDos);
    }

    /** Bloque que corresponde a un caracter de la grilla, o {@code null} si la celda queda vacia. */
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
            case 'E':
            case 'F':
            case 'C':
                return new SpawnerArma(area, celda);
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

    /** Bloques solidos del escenario. La lista es de solo lectura. */
    public List<Bloque> getBloques() {
        return bloques;
    }

    /**
     * Devuelve {@code x} corregido para que una entidad de ancho {@code anchoEntidad} no atraviese
     * las paredes laterales.
     *
     * <p>Es la implementacion del "espacio cerrado y delimitado" de la propuesta: en lugar de
     * permitir el avance territorial de Nidhogg, la arena encierra a los dos jugadores.
     */
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
