package com.et35.technomortemarena.partida;

/**
 * Marcador de una partida al mejor de 5 rondas: lleve el conteo de rondas ganadas de cada duelistas
 * y dice cuando hay un ganador.
 *
 * <p>Es logica pura, sin dependencias de libGDX, para poder testearla sin abrir una ventana. La
 * ronda es ganada por quien siga vivo al final; un empate (murieron los dos en el mismo fotograma)
 * no suma ninguna ronda y la misma ronda se vuelve a jugar.
 */
public final class MarcadorPartida {

    /** Rondas que hay que ganar para llevarse la partida: al mejor de 5. */
    public static final int RONDAS_PARA_GANAR = 3;

    private int rondasGanadasUno;
    private int rondasGanadasDos;

    /** Suma una ronda ganada al duelistas indicado. */
    public void registrarRondaGanada(boolean ganoElUno) {
        if (ganoElUno) {
            rondasGanadasUno++;
        } else {
            rondasGanadasDos++;
        }
    }

    public int getRondasGanadasUno() {
        return rondasGanadasUno;
    }

    public int getRondasGanadasDos() {
        return rondasGanadasDos;
    }

    /** Verdadero cuando alguien ya gano las rondas necesarias para llevarse la partida. */
    public boolean hayGanador() {
        return ganoJugadorUno() || ganoJugadorDos();
    }

    /** Verdadero si el duelistas uno se llevo la partida. Solo tiene sentido con {@link #hayGanador()}. */
    public boolean ganoJugadorUno() {
        return rondasGanadasUno >= RONDAS_PARA_GANAR;
    }

    /** Verdadero si el duelistas dos se llevo la partida. Solo tiene sentido con {@link #hayGanador()}. */
    public boolean ganoJugadorDos() {
        return rondasGanadasDos >= RONDAS_PARA_GANAR;
    }

    /**
     * Pone el marcador en un valor dado. Lo usan los clientes de una partida en red, que no llevan la
     * cuenta: copian la que lleva el anfitrion.
     */
    public void restaurar(int rondasGanadasUno, int rondasGanadasDos) {
        this.rondasGanadasUno = rondasGanadasUno;
        this.rondasGanadasDos = rondasGanadasDos;
    }

    /** Vuelve el marcador a cero, para una revancha en el mismo mapa. */
    public void reiniciar() {
        rondasGanadasUno = 0;
        rondasGanadasDos = 0;
    }
}
