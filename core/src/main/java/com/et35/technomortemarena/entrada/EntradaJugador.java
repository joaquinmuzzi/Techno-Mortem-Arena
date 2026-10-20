package com.et35.technomortemarena.entrada;

/**
 * Lo que un jugador quiere hacer en un fotograma, sin importar de donde venga: el teclado local o
 * un paquete de red.
 *
 * <p>Las tres primeras son estados (verdaderas mientras la tecla esta hundida). Las otras tres son
 * acciones de una sola vez: valen solo en el fotograma de la pulsacion.
 */
public final class EntradaJugador {

    public boolean izquierda;
    public boolean derecha;
    public boolean estocada;

    public boolean saltar;
    public boolean subirEspada;
    public boolean bajarEspada;

    public void limpiar() {
        izquierda = false;
        derecha = false;
        estocada = false;
        saltar = false;
        subirEspada = false;
        bajarEspada = false;
    }
}
