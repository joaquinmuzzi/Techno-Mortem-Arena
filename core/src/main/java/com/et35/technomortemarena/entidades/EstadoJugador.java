package com.et35.technomortemarena.entidades;

/**
 * Todo lo que hace falta para dibujar a un jugador: es lo que el anfitrion le manda a los clientes.
 *
 * <p>Son datos planos a proposito. El paquete {@code red} los convierte a bytes; esta clase no sabe
 * nada de la red.
 */
public final class EstadoJugador {

    public float x;
    public float y;
    public int vida;
    public AlturaEspada alturaEspada = AlturaEspada.PECHO;
    public boolean mirandoDerecha;
    public boolean estocada;
    public boolean vivo;
    public boolean corriendo;
    public float anguloEspada;
    public float tiempoRestanteFlash;
    /** Cuantos golpes dio y cuantos aterrizajes hizo, para que el cliente dispare los sonidos. */
    public int contadorGolpes;
    public int contadorAterrizajes;
}
