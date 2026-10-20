package com.et35.technomortemarena.entrada;

/**
 * Lo que un jugador quiere hacer en un fotograma, sin importar de donde venga: el teclado y el mouse
 * locales o un paquete de red.
 *
 * <p>Izquierda y derecha son estados (verdaderos mientras la tecla esta hundida). Saltar y estocada
 * son acciones de una sola vez: valen solo en el fotograma de la pulsacion. El punto apuntado esta en
 * coordenadas del mundo (las mismas de la arena), no de la pantalla.
 */
public final class EntradaJugador {

    public boolean izquierda;
    public boolean derecha;

    public boolean saltar;
    public boolean estocada;

    /** Verdadero si hay un punto apuntado. Los blancos de prueba y los jugadores muertos no apuntan. */
    public boolean hayPuntero;
    public float apuntaX;
    public float apuntaY;

    public void limpiar() {
        izquierda = false;
        derecha = false;
        saltar = false;
        estocada = false;
        hayPuntero = false;
        apuntaX = 0f;
        apuntaY = 0f;
    }
}
