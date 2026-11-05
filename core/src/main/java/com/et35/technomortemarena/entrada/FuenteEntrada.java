package com.et35.technomortemarena.entrada;

/**
 * De donde lee un {@link com.et35.technomortemarena.entidades.Jugador} lo que tiene que hacer.
 *
 * <p>Separar la fuente del jugador es lo que permite que la misma clase sirva para el duelista que se
 * maneja con el teclado y para el blanco quieto, y pasar el teclado de uno al otro.
 */
public interface FuenteEntrada {

    /** Fuente que nunca hace nada: para jugadores que no simulan su propia entrada. */
    FuenteEntrada NINGUNA = destino -> destino.limpiar();

    /** Escribe en {@code destino} lo que el jugador quiere hacer en este fotograma. */
    void leer(EntradaJugador destino);
}
