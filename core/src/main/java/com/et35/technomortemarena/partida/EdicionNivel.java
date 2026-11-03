package com.et35.technomortemarena.partida;

import com.et35.technomortemarena.mundo.Arena;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * La edicion del nivel entre rondas, del lado del anfitrion: quien perdio la ronda recibe tres objetos al
 * azar, elige uno y lo coloca donde quiera dentro de lo que permite {@link ColocadorPiezas}.
 *
 * <p>Lleva la grilla del mapa con todas las piezas colocadas hasta ahora ({@link #getFilas}) y la lista de
 * piezas, que es lo que viaja por la red: el cliente rearma el mismo mapa aplicando esa lista sobre el
 * mapa original. Si el tiempo se agota, {@link #autocolocar} coloca la pieza elegida donde estaba el
 * cursor, o una al azar si no se eligio ninguna. Es logica pura, sin libGDX.
 */
public final class EdicionNivel {

    /** Cantidad de objetos entre los que elige el perdedor. */
    public static final int OPCIONES = 3;
    /** Tope de piezas colocadas en una partida: una por ronda perdida, y una partida no pasa de 5 rondas. */
    public static final int MAX_PIEZAS = 16;

    private final String[] filasOriginales;
    private final List<float[]> zonasProhibidas;
    private final Random azar;
    private final List<int[]> colocadas = new ArrayList<>();
    private final PiezaNivel[] opciones = new PiezaNivel[OPCIONES];

    private String[] filas;
    private int editor = -1;
    private int eleccion = -1;
    private boolean colocadaEstaVez;
    private int cursorColumna;
    private int cursorFila;

    /**
     * @param filasOriginales el mapa de la partida, sin piezas
     * @param zonasProhibidas zonas {@code {x0, y0, x1, y1}} en celdas donde no se puede colocar nada
     */
    public EdicionNivel(String[] filasOriginales, List<float[]> zonasProhibidas, Random azar) {
        this.filasOriginales = filasOriginales.clone();
        this.filas = filasOriginales.clone();
        this.zonasProhibidas = new ArrayList<>(zonasProhibidas);
        this.azar = azar;
    }

    /** Empieza una edicion: sortea tres objetos distintos para el jugador {@code editor} (0 o 1). */
    public void iniciar(int editor) {
        this.editor = editor;
        List<PiezaNivel> pool = new ArrayList<>(Arrays.asList(PiezaNivel.values()));
        for (int i = 0; i < OPCIONES; i++) {
            opciones[i] = pool.remove(azar.nextInt(pool.size()));
        }
        eleccion = -1;
        colocadaEstaVez = false;
    }

    /** Termina la edicion sin colocar nada mas. */
    public void terminar() {
        editor = -1;
        eleccion = -1;
    }

    /** Vuelve al mapa original, sin ninguna pieza: para empezar una partida nueva. */
    public void reiniciarNivel() {
        filas = filasOriginales.clone();
        colocadas.clear();
        terminar();
    }

    /** Elige uno de los objetos ofrecidos; un numero que no corresponde a ninguno deja sin elegir. */
    public void elegir(int indice) {
        if (editor < 0 || colocadaEstaVez) {
            return;
        }
        eleccion = indice >= 0 && indice < OPCIONES ? indice : -1;
    }

    /** Hacia donde apunta el cursor del editor: es la celda sobre la que se centra la pieza elegida. */
    public void moverCursor(int columna, int fila) {
        cursorColumna = Math.max(0, Math.min(Arena.COLUMNAS - 1, columna));
        cursorFila = Math.max(0, Math.min(Arena.FILAS - 1, fila));
    }

    /** Si la pieza elegida se puede colocar donde esta el cursor. */
    public boolean sePuedeColocar() {
        PiezaNivel pieza = getElegida();
        if (pieza == null || colocadaEstaVez || colocadas.size() >= MAX_PIEZAS) {
            return false;
        }
        int[] origen = ColocadorPiezas.origenPara(pieza, cursorColumna, cursorFila);
        return ColocadorPiezas.puedeColocar(filas, pieza, origen[0], origen[1], zonasProhibidas);
    }

    /**
     * Coloca la pieza elegida donde esta el cursor.
     *
     * @return {@code false} si no hay pieza elegida, ya se coloco una, o ahi no se puede: no pasa nada
     */
    public boolean colocar() {
        if (!sePuedeColocar()) {
            return false;
        }
        PiezaNivel pieza = getElegida();
        int[] origen = ColocadorPiezas.origenPara(pieza, cursorColumna, cursorFila);
        poner(pieza, origen[0], origen[1]);
        return true;
    }

    /**
     * Se acabo el tiempo: coloca la pieza elegida en el ultimo lugar del cursor (o en el lugar valido mas
     * cercano) y, si no habia elegido ninguna, una de las tres al azar en un lugar valido al azar.
     *
     * @return {@code true} si coloco algo; {@code false} si ya habia colocado o no hay lugar en el mapa
     */
    public boolean autocolocar() {
        if (editor < 0 || colocadaEstaVez || colocadas.size() >= MAX_PIEZAS) {
            return false;
        }
        PiezaNivel pieza = getElegida();
        int[] origen;
        if (pieza != null) {
            int[] deseado = ColocadorPiezas.origenPara(pieza, cursorColumna, cursorFila);
            origen = ColocadorPiezas.buscarValidaCerca(filas, pieza, deseado[0], deseado[1], zonasProhibidas);
        } else {
            pieza = opciones[azar.nextInt(OPCIONES)];
            origen = ColocadorPiezas.buscarValidaAlAzar(filas, pieza, azar, zonasProhibidas);
        }
        if (origen == null) {
            return false;
        }
        poner(pieza, origen[0], origen[1]);
        return true;
    }

    private void poner(PiezaNivel pieza, int columna, int fila) {
        filas = ColocadorPiezas.aplicar(filas, pieza, columna, fila);
        colocadas.add(new int[] {pieza.ordinal(), columna, fila});
        colocadaEstaVez = true;
    }

    public boolean haColocado() {
        return colocadaEstaVez;
    }

    /** Quien edita: 0 el Jugador 1, 1 el Jugador 2, -1 si no hay edicion en curso. */
    public int getEditor() {
        return editor;
    }

    public PiezaNivel getOpcion(int indice) {
        return opciones[indice];
    }

    /** El numero (0 a 2) del objeto elegido, o -1 si todavia no eligio. */
    public int getEleccion() {
        return eleccion;
    }

    public PiezaNivel getElegida() {
        return eleccion >= 0 && editor >= 0 ? opciones[eleccion] : null;
    }

    /** La grilla con todas las piezas colocadas, la primera fila la de arriba. */
    public String[] getFilas() {
        return filas.clone();
    }

    /** Las piezas colocadas, en orden, como {@code {tipo, columna, fila}}. */
    public List<int[]> getColocadas() {
        List<int[]> copia = new ArrayList<>();
        for (int[] pieza : colocadas) {
            copia.add(pieza.clone());
        }
        return copia;
    }

    public int getCursorColumna() {
        return cursorColumna;
    }

    public int getCursorFila() {
        return cursorFila;
    }

    /** Copia en {@code destino} lo de la edicion que viaja por la red. */
    public void exportar(EstadoPartida destino) {
        destino.editor = editor;
        for (int i = 0; i < OPCIONES; i++) {
            destino.opciones[i] = editor >= 0 && opciones[i] != null ? opciones[i].ordinal() : -1;
        }
        destino.eleccion = eleccion;
        destino.cursorColumna = cursorColumna;
        destino.cursorFila = cursorFila;
        destino.piezas.clear();
        destino.piezas.addAll(getColocadas());
    }

    /**
     * Rearma un mapa aplicando una lista de piezas sobre el original: es lo que hace el cliente con lo
     * que le llega del anfitrion.
     */
    public static String[] reconstruir(String[] filasOriginales, List<int[]> piezas) {
        String[] filas = filasOriginales.clone();
        for (int[] datos : piezas) {
            PiezaNivel pieza = PiezaNivel.porIndice(datos[0]);
            if (pieza != null) {
                filas = ColocadorPiezas.aplicar(filas, pieza, datos[1], datos[2]);
            }
        }
        return filas;
    }
}
