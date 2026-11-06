package com.et35.technomortemarena.partida;

public final class MarcadorPartida {
    public static final int RONDAS_PARA_GANAR = 3;

    private int rondasGanadasUno;
    private int rondasGanadasDos;

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

    public boolean hayGanador() {
        return ganoJugadorUno() || ganoJugadorDos();
    }

    public boolean ganoJugadorUno() {
        return rondasGanadasUno >= RONDAS_PARA_GANAR;
    }

    public boolean ganoJugadorDos() {
        return rondasGanadasDos >= RONDAS_PARA_GANAR;
    }

    public void reiniciar() {
        rondasGanadasUno = 0;
        rondasGanadasDos = 0;
    }
}
