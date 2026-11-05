package com.et35.technomortemarena.entrada;

/**
 * Lo que un jugador quiere hacer en un fotograma, sin importar de donde venga: el teclado y el mouse
 * locales o un paquete de red.
 *
 * <p>Izquierda y derecha son estados (verdaderos mientras la tecla esta hundida). Saltar, estocada y rodar
 * son acciones de una sola vez: valen solo en el fotograma de la pulsacion. El punto apuntado esta en
 * coordenadas del mundo (las mismas de la arena), no de la pantalla.
 */
public final class EntradaJugador {

    public boolean izquierda;
    public boolean derecha;

    public boolean saltar;
    public boolean estocada;
    public boolean rodar;
    /** Tirar lo que tiene en la mano hacia donde apunta (clic derecho). */
    public boolean lanzar;

    /** Durante la edicion del nivel: el objeto elegido (0 a 2), o -1 si todavia no eligio. */
    public int eleccionPieza = -1;
    /** Durante la edicion del nivel: verdadero en el fotograma en que el editor pide colocar el objeto. */
    public boolean colocarPieza;

    /** Verdadero si hay un punto apuntado. Los blancos de prueba y los jugadores muertos no apuntan. */
    public boolean hayPuntero;
    public float apuntaX;
    public float apuntaY;

    public void limpiar() {
        izquierda = false;
        derecha = false;
        saltar = false;
        estocada = false;
        rodar = false;
        lanzar = false;
        eleccionPieza = -1;
        colocarPieza = false;
        hayPuntero = false;
        apuntaX = 0f;
        apuntaY = 0f;
    }
}
