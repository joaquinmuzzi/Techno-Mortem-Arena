package com.et35.technomortemarena.entidades;

public final class EstadoJugador {
    public float x;
    public float y;
    public int vida;
    public boolean mirandoDerecha;
    public boolean vivo;
    public boolean corriendo;
    public boolean rodando;
    public int arma = 1;
    public int balas;
    public final float[] tiempoPowerUp = new float[PowerUp.values().length];
    public float anguloEspada;
    public float extension;
    public float tiempoRestanteFlash;
    public int contadorGolpes;
    public int contadorAterrizajes;
    public int contadorDisparosPistola;
    public int contadorDisparosEscopeta;
    public int contadorEspadasEquipadas;
    public int contadorArmasDeFuegoEquipadas;
    public int contadorDisparosLaser;
    public int contadorDisparosBazuca;
    public int contadorPowerUps;
}
