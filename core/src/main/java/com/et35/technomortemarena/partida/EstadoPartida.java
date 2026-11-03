package com.et35.technomortemarena.partida;

import java.util.ArrayList;
import java.util.List;

/**
 * El estado de la partida al mejor de 5 que el anfitrion le manda a los clientes: en que etapa esta,
 * que ronda es, como va el marcador y quien gano la ultima ronda.
 *
 * <p>Son datos planos a proposito, igual que {@code EstadoJugador}: el paquete {@code red} los
 * convierte a bytes y esta clase no sabe nada de la red.
 */
public final class EstadoPartida {

    /** Etapa de la partida: ver {@link RondasPartida.Fase#ordinal()}. */
    public int fase;
    public int numeroRonda = 1;
    public int rondasGanadasUno;
    public int rondasGanadasDos;
    /** Quien gano la ultima ronda: 0 el Jugador 1, 1 el Jugador 2, -1 si fue empate o todavia no hubo. */
    public int ganadorDeLaRonda = -1;
    /** Tiempo restante en la ronda (segundos). */
    public float tiempoRestante = RondasPartida.DURACION_RONDA;
    /** Tiempo restante en muerte subita (segundos). */
    public float tiempoMuerteSubita = RondasPartida.DURACION_MUERTE_SUBITA;
    /** Quien edita el nivel entre rondas: 0 el Jugador 1, 1 el Jugador 2, -1 si no hay edicion. */
    public int editor = -1;
    /** Segundos que le quedan al editor para colocar su objeto. */
    public float tiempoEdicion = RondasPartida.DURACION_EDICION;
    /** Los tres objetos ofrecidos ({@link PiezaNivel#ordinal()}), o -1 si no hay edicion. */
    public final int[] opciones = {-1, -1, -1};
    /** El objeto elegido (0 a 2), o -1 si todavia no eligio. */
    public int eleccion = -1;
    /** La celda sobre la que esta el cursor del editor. */
    public int cursorColumna;
    public int cursorFila;
    /** Las piezas colocadas en la partida, en orden, como {@code {tipo, columna, fila}}. */
    public final List<int[]> piezas = new ArrayList<>();

    /** Copia todo el estado de {@code origen}. */
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
