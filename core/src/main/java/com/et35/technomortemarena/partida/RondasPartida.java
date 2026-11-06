package com.et35.technomortemarena.partida;

public final class RondasPartida {
    public enum Fase { RONDA, MUERTE_SUBITA, ENTRE_RONDAS, GANADOR }

    public static final float DURACION_ENTRE_RONDAS = 2.5f;
    public static final float DURACION_RONDA = 60f;
    public static final float DURACION_MUERTE_SUBITA = 5f;

    private final MarcadorPartida marcador = new MarcadorPartida();
    private Fase fase = Fase.RONDA;
    private int numeroRonda = 1;
    private float tiempoEntreRondas;
    private int ganadorDeLaRonda = -1;
    private float tiempoRestante = DURACION_RONDA;
    private float tiempoMuerteSubita = DURACION_MUERTE_SUBITA;

    public boolean actualizar(float delta, boolean unoVivo, boolean dosVivo) {
        switch (fase) {
            case RONDA:
                tiempoRestante -= delta;
                if (!unoVivo || !dosVivo) {
                    cerrarRonda(unoVivo, dosVivo);
                    return false;
                }
                if (tiempoRestante <= 0f) {
                    fase = Fase.MUERTE_SUBITA;
                    tiempoMuerteSubita = DURACION_MUERTE_SUBITA;
                    tiempoRestante = 0f;
                    return true;
                }
                return false;
            case MUERTE_SUBITA:
                tiempoMuerteSubita -= delta;
                if (!unoVivo || !dosVivo) {
                    cerrarRonda(unoVivo, dosVivo);
                    return false;
                }
                if (tiempoMuerteSubita <= 0f) {
                    cerrarRonda(unoVivo, dosVivo);
                }
                return false;
            case ENTRE_RONDAS:
                tiempoEntreRondas -= delta;
                if (tiempoEntreRondas <= 0f) {
                    empezarRondaNueva();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private void empezarRondaNueva() {
        numeroRonda++;
        fase = Fase.RONDA;
        tiempoRestante = DURACION_RONDA;
        tiempoMuerteSubita = DURACION_MUERTE_SUBITA;
    }

    private void cerrarRonda(boolean unoVivo, boolean dosVivo) {
        if (unoVivo && !dosVivo) {
            ganadorDeLaRonda = 0;
            marcador.registrarRondaGanada(true);
        } else if (dosVivo && !unoVivo) {
            ganadorDeLaRonda = 1;
            marcador.registrarRondaGanada(false);
        } else {
            ganadorDeLaRonda = -1;
        }
        if (marcador.hayGanador()) {
            fase = Fase.GANADOR;
        } else {
            fase = Fase.ENTRE_RONDAS;
            tiempoEntreRondas = DURACION_ENTRE_RONDAS;
        }
    }

    public void reiniciar() {
        marcador.reiniciar();
        fase = Fase.RONDA;
        numeroRonda = 1;
        tiempoEntreRondas = 0f;
        ganadorDeLaRonda = -1;
        tiempoRestante = DURACION_RONDA;
        tiempoMuerteSubita = DURACION_MUERTE_SUBITA;
    }

    public Fase getFase() {
        return fase;
    }

    public int getNumeroRonda() {
        return numeroRonda;
    }

    public int getRondasGanadasUno() {
        return marcador.getRondasGanadasUno();
    }

    public int getRondasGanadasDos() {
        return marcador.getRondasGanadasDos();
    }

    public float getTiempoRestante() {
        return tiempoRestante;
    }

    public float getTiempoMuerteSubita() {
        return tiempoMuerteSubita;
    }

    public boolean enMuerteSubita() {
        return fase == Fase.MUERTE_SUBITA;
    }

    public boolean ganoJugadorUno() {
        return marcador.ganoJugadorUno();
    }

    public int getGanadorDeLaRonda() {
        return ganadorDeLaRonda;
    }
}
