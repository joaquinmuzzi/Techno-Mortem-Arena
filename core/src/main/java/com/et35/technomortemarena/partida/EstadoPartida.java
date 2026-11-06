package com.et35.technomortemarena.partida;

import java.util.ArrayList;
import java.util.List;

public final class EstadoPartida {
    public int fase;
    public int numeroRonda = 1;
    public int rondasGanadasUno;
    public int rondasGanadasDos;
    public int ganadorDeLaRonda = -1;
    public float tiempoRestante = RondasPartida.DURACION_RONDA;
    public float tiempoMuerteSubita = RondasPartida.DURACION_MUERTE_SUBITA;
    public int editor = -1;
    public float tiempoEdicion = RondasPartida.DURACION_EDICION;
    public final int[] opciones = {-1, -1, -1};
    public int eleccion = -1;
    public int cursorColumna;
    public int cursorFila;
    public final List<int[]> piezas = new ArrayList<>();

    public void copiarDe(EstadoPartida origen) {
        fase = origen.fase;
        numeroRonda = origen.numeroRonda;
        rondasGanadasUno = origen.rondasGanadasUno;
        rondasGanadasDos = origen.rondasGanadasDos;
        ganadorDeLaRonda = origen.ganadorDeLaRonda;
        tiempoRestante = origen.tiempoRestante;
        tiempoMuerteSubita = origen.tiempoMuerteSubita;
        editor = origen.editor;
        tiempoEdicion = origen.tiempoEdicion;
        System.arraycopy(origen.opciones, 0, opciones, 0, opciones.length);
        eleccion = origen.eleccion;
        cursorColumna = origen.cursorColumna;
        cursorFila = origen.cursorFila;
        piezas.clear();
        for (int[] pieza : origen.piezas) {
            piezas.add(pieza.clone());
        }
    }
}
