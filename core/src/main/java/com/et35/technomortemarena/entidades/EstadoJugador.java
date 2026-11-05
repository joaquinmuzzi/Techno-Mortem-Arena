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
    /** Vida en tercios de corazon. */
    public int vida;
    public boolean mirandoDerecha;
    public boolean vivo;
    public boolean corriendo;
    /** Verdadero mientras rueda: la caja de colision es mas baja. */
    public boolean rodando;
    /** Lo que tiene en la mano: 0 nada, 1 espada, 2 pistola (ver {@link TipoArma#codigoDe}). */
    public int arma = 1;
    /** Balas de la pistola. */
    public int balas;
    /** Segundos que le quedan a cada mejora, en el orden de {@link PowerUp}. */
    public final float[] tiempoPowerUp = new float[PowerUp.values().length];
    /** Hacia donde apunta la espada, en radianes. */
    public float anguloEspada;
    /** Cuanto esta adelantada la hoja por la estocada, de 0 a 1. */
    public float extension;
    public float tiempoRestanteFlash;
    /** Cuantos golpes dio y cuantos aterrizajes hizo, para que el cliente dispare los sonidos. */
    public int contadorGolpes;
    public int contadorAterrizajes;
    /** Disparos de pistola y de escopeta, para los sonidos del cliente. */
    public int contadorDisparosPistola;
    public int contadorDisparosEscopeta;
    /** Armas recogidas del suelo, separadas por el sonido que les corresponde. */
    public int contadorEspadasEquipadas;
    public int contadorArmasDeFuegoEquipadas;
}
