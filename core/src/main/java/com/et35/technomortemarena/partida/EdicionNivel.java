package com.et35.technomortemarena.partida;

import com.et35.technomortemarena.mundo.Arena;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class EdicionNivel {
    public static final int OPCIONES = 3;
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

    public EdicionNivel(String[] filasOriginales, List<float[]> zonasProhibidas, Random azar) {
        this.filasOriginales = filasOriginales.clone();
        this.filas = filasOriginales.clone();
        this.zonasProhibidas = new ArrayList<>(zonasProhibidas);
        this.azar = azar;
    }

    public void iniciar(int editor) {
        this.editor = editor;
        List<PiezaNivel> pool = new ArrayList<>(Arrays.asList(PiezaNivel.values()));
        for (int i = 0; i < OPCIONES; i++) {
            opciones[i] = pool.remove(azar.nextInt(pool.size()));
        }
        eleccion = -1;
        colocadaEstaVez = false;
    }

    public void terminar() {
        editor = -1;
        eleccion = -1;
    }

    public void reiniciarNivel() {
        filas = filasOriginales.clone();
        colocadas.clear();
        terminar();
    }

    public void elegir(int indice) {
        if (editor < 0 || colocadaEstaVez) {
            return;
        }
        eleccion = indice >= 0 && indice < OPCIONES ? indice : -1;
    }

    public void moverCursor(int columna, int fila) {
        cursorColumna = Math.max(0, Math.min(Arena.COLUMNAS - 1, columna));
        cursorFila = Math.max(0, Math.min(Arena.FILAS - 1, fila));
    }

    public boolean sePuedeColocar() {
        PiezaNivel pieza = getElegida();
        if (pieza == null || colocadaEstaVez || colocadas.size() >= MAX_PIEZAS) {
            return false;
        }
        int[] origen = ColocadorPiezas.origenPara(pieza, cursorColumna, cursorFila);
        return ColocadorPiezas.puedeColocar(filas, pieza, origen[0], origen[1], zonasProhibidas);
    }

    public boolean colocar() {
        if (!sePuedeColocar()) {
            return false;
        }
        PiezaNivel pieza = getElegida();
        int[] origen = ColocadorPiezas.origenPara(pieza, cursorColumna, cursorFila);
        poner(pieza, origen[0], origen[1]);
        return true;
    }

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

    public int getEditor() {
        return editor;
    }

    public PiezaNivel getOpcion(int indice) {
        return opciones[indice];
    }

    public int getEleccion() {
        return eleccion;
    }

    public PiezaNivel getElegida() {
        return eleccion >= 0 && editor >= 0 ? opciones[eleccion] : null;
    }

    public String[] getFilas() {
        return filas.clone();
    }

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
