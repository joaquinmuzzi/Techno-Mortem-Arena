package com.et35.technomortemarena.entrada;

/**
 * De donde lee un {@link com.et35.technomortemarena.entidades.Jugador} lo que tiene que hacer.
 *
 * <p>Separar la fuente del jugador es lo que permite que la misma clase sirva para un duelista con
 * teclado local y para uno controlado por un paquete de red.
 */
public interface FuenteEntrada {

    /** Fuente que nunca hace nada: para jugadores que no simulan su propia entrada. */
    FuenteEntrada NINGUNA = destino -> destino.limpiar();

    /** Escribe en {@code destino} lo que el jugador quiere hacer en este fotograma. */
    void leer(EntradaJugador destino);
}
