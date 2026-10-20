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
    public enum Fase { RONDA, ENTRE_RONDAS, GANADOR }

    /** Segundos entre que termina una ronda y arranca la siguiente, para alcanzar a leer el resultado. */
    public static final float DURACION_ENTRE_RONDAS = 2.5f;

    private final MarcadorPartida marcador = new MarcadorPartida();
    private Fase fase = Fase.RONDA;
    private int numeroRonda = 1;
    private float tiempoEntreRondas;
    private int ganadorDeLaRonda = -1;

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
                if (!unoVivo || !dosVivo) {
                    cerrarRonda(unoVivo, dosVivo);
                }
                return false;
            case ENTRE_RONDAS:
                tiempoEntreRondas -= delta;
                if (tiempoEntreRondas <= 0f) {
                    numeroRonda++;
                    fase = Fase.RONDA;
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /** Anota el resultado: la ronda de quien siga vivo, o ninguna si cayeron los dos. */
    private void cerrarRonda(boolean unoVivo, boolean dosVivo) {
        if (unoVivo) {
            ganadorDeLaRonda = 0;
            marcador.registrarRondaGanada(true);
        } else if (dosVivo) {
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
    }

    /** Copia en {@code destino} lo que hay que mostrar: es lo que el anfitrion le manda a los clientes. */
    public void exportar(EstadoPartida destino) {
        destino.fase = fase.ordinal();
        destino.numeroRonda = numeroRonda;
        destino.rondasGanadasUno = marcador.getRondasGanadasUno();
        destino.rondasGanadasDos = marcador.getRondasGanadasDos();
        destino.ganadorDeLaRonda = ganadorDeLaRonda;
    }

    /** Pisa esta partida con el estado que mando el anfitrion. Lo usa el cliente, que no lleva la cuenta. */
    public void importar(EstadoPartida origen) {
        fase = Fase.values()[origen.fase];
        numeroRonda = origen.numeroRonda;
        marcador.restaurar(origen.rondasGanadasUno, origen.rondasGanadasDos);
        ganadorDeLaRonda = origen.ganadorDeLaRonda;
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

    /** Verdadero si el Jugador 1 se llevo la partida. Solo tiene sentido en la fase {@link Fase#GANADOR}. */
    public boolean ganoJugadorUno() {
        return marcador.ganoJugadorUno();
    }

    /** Quien gano la ultima ronda: 0 el Jugador 1, 1 el Jugador 2, -1 si fue empate o todavia no hubo. */
    public int getGanadorDeLaRonda() {
        return ganadorDeLaRonda;
    }
}
