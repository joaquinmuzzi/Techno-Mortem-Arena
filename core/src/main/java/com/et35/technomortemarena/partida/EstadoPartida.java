package com.et35.technomortemarena.partida;

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
}
