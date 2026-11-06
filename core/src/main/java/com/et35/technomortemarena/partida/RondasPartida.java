package com.et35.technomortemarena.partida;

public final class RondasPartida {
    public enum Fase { RONDA, MUERTE_SUBITA, ENTRE_RONDAS, GANADOR, EDICION }

    public static final float DURACION_ENTRE_RONDAS = 2.5f;
    public static final float DURACION_RONDA = 60f;
    public static final float DURACION_MUERTE_SUBITA = 5f;
    public static final float DURACION_EDICION = 20f;

    private final MarcadorPartida marcador = new MarcadorPartida();
    private Fase fase = Fase.RONDA;
    private int numeroRonda = 1;
    private float tiempoEntreRondas;
    private int ganadorDeLaRonda = -1;
    private float tiempoRestante = DURACION_RONDA;
    private float tiempoMuerteSubita = DURACION_MUERTE_SUBITA;
    private final boolean conEdicion;
    private float tiempoEdicion = DURACION_EDICION;
    private int editor = -1;
    private boolean edicionTerminada;

    public RondasPartida() {
        this(false);
    }

    public RondasPartida(boolean conEdicion) {
        this.conEdicion = conEdicion;
    }

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
                    if (conEdicion && ganadorDeLaRonda >= 0) {
                        fase = Fase.EDICION;
                        editor = 1 - ganadorDeLaRonda;
                        tiempoEdicion = DURACION_EDICION;
                        edicionTerminada = false;
                        return false;
                    }
                    empezarRondaNueva();
                    return true;
                }
                return false;
            case EDICION:
                tiempoEdicion = Math.max(0f, tiempoEdicion - delta);
                if (edicionTerminada) {
                    editor = -1;
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
        tiempoEdicion = DURACION_EDICION;
        editor = -1;
        edicionTerminada = false;
    }

    public void exportar(EstadoPartida destino) {
        destino.fase = fase.ordinal();
        destino.numeroRonda = numeroRonda;
        destino.rondasGanadasUno = marcador.getRondasGanadasUno();
        destino.rondasGanadasDos = marcador.getRondasGanadasDos();
        destino.ganadorDeLaRonda = ganadorDeLaRonda;
        destino.tiempoRestante = tiempoRestante;
        destino.tiempoMuerteSubita = tiempoMuerteSubita;
        destino.editor = editor;
        destino.tiempoEdicion = tiempoEdicion;
    }

    public void importar(EstadoPartida origen) {
        if (origen.fase >= 0 && origen.fase < Fase.values().length) {
            fase = Fase.values()[origen.fase];
        }
        numeroRonda = origen.numeroRonda;
        marcador.restaurar(origen.rondasGanadasUno, origen.rondasGanadasDos);
        ganadorDeLaRonda = origen.ganadorDeLaRonda;
        if (origen.tiempoRestante >= 0) {
            tiempoRestante = origen.tiempoRestante;
        }
        if (origen.tiempoMuerteSubita >= 0) {
            tiempoMuerteSubita = origen.tiempoMuerteSubita;
        }
        editor = origen.editor;
        if (origen.tiempoEdicion >= 0) {
            tiempoEdicion = origen.tiempoEdicion;
        }
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

    public int getEditor() {
        return editor;
    }

    public float getTiempoEdicion() {
        return tiempoEdicion;
    }

    public boolean edicionAgotada() {
        return fase == Fase.EDICION && tiempoEdicion <= 0f;
    }

    public void terminarEdicion() {
        edicionTerminada = true;
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
