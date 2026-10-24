package com.et35.technomortemarena.partida;

/**
 * El desarrollo de una partida al mejor de 5: ronda en juego, espera entre rondas y victoria.
 *
 * <p>Es logica pura, sin libGDX, para poder probarla sin abrir una ventana. Quien la usa le avisa en
 * cada fotograma si los duelistas siguen vivos y ella decide que sigue. La ronda la gana quien siga en
 * pie; un empate (los dos caen en el mismo fotograma) no suma nada y la misma ronda se vuelve a jugar.
 * El primero en ganar {@link MarcadorPartida#RONDAS_PARA_GANAR} rondas se lleva la partida.
 *
 * <p>En una partida en red la lleva el anfitrion y los clientes solo copian su estado con
 * {@link #importar}.
 */
public final class RondasPartida {

    /** Etapa de la partida: duelo activo, espera entre rondas o cartel de victoria. */
    public enum Fase { RONDA, MUERTE_SUBITA, ENTRE_RONDAS, GANADOR }

    /** Segundos entre que termina una ronda y arranca la siguiente, para alcanzar a leer el resultado. */
    public static final float DURACION_ENTRE_RONDAS = 2.5f;
    /** Duracion de una ronda en segundos. Al agotarse el tiempo entra en muerte subita. */
    public static final float DURACION_RONDA = 60f;
    /** Tiempo extra en muerte subita antes de forzar una decision. */
    public static final float DURACION_MUERTE_SUBITA = 5f;

    private final MarcadorPartida marcador = new MarcadorPartida();
    private Fase fase = Fase.RONDA;
    private int numeroRonda = 1;
    private float tiempoEntreRondas;
    private int ganadorDeLaRonda = -1;
    private float tiempoRestante = DURACION_RONDA;
    private float tiempoMuerteSubita = DURACION_MUERTE_SUBITA;

    /**
     * Avanza la partida un fotograma.
     *
     * @param unoVivo si el Jugador 1 sigue en pie
     * @param dosVivo si el Jugador 2 sigue en pie
     * @return {@code true} cuando arranca una ronda nueva: quien llama tiene que devolver a los dos
     *         duelistas a sus spawns
     */
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
                    // Si siguen los dos vivos al agotarse la muerte subita, se declara empate y se repite la ronda
                    cerrarRonda(unoVivo, dosVivo);
                }
                return false;
            case ENTRE_RONDAS:
                tiempoEntreRondas -= delta;
                if (tiempoEntreRondas <= 0f) {
                    numeroRonda++;
                    fase = Fase.RONDA;
                    tiempoRestante = DURACION_RONDA;
                    tiempoMuerteSubita = DURACION_MUERTE_SUBITA;
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /** Anota el resultado: la ronda de quien siga vivo, o ninguna si cayeron los dos o siguen ambos. */
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

    /** Empieza una partida nueva: marcador en cero, ronda 1. Sirve para la revancha. */
    public void reiniciar() {
        marcador.reiniciar();
        fase = Fase.RONDA;
        numeroRonda = 1;
        tiempoEntreRondas = 0f;
        ganadorDeLaRonda = -1;
        tiempoRestante = DURACION_RONDA;
        tiempoMuerteSubita = DURACION_MUERTE_SUBITA;
    }

    /** Copia en {@code destino} lo que hay que mostrar: es lo que el anfitrion le manda a los clientes. */
    public void exportar(EstadoPartida destino) {
        destino.fase = fase.ordinal();
        destino.numeroRonda = numeroRonda;
        destino.rondasGanadasUno = marcador.getRondasGanadasUno();
        destino.rondasGanadasDos = marcador.getRondasGanadasDos();
        destino.ganadorDeLaRonda = ganadorDeLaRonda;
        destino.tiempoRestante = tiempoRestante;
        destino.tiempoMuerteSubita = tiempoMuerteSubita;
    }

    /** Pisa esta partida con el estado que mando el anfitrion. Lo usa el cliente, que no lleva la cuenta. */
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

    /** Verdadero si el Jugador 1 se llevo la partida. Solo tiene sentido en la fase {@link Fase#GANADOR}. */
    public boolean ganoJugadorUno() {
        return marcador.ganoJugadorUno();
    }

    /** Quien gano la ultima ronda: 0 el Jugador 1, 1 el Jugador 2, -1 si fue empate o todavia no hubo. */
    public int getGanadorDeLaRonda() {
        return ganadorDeLaRonda;
    }
}
