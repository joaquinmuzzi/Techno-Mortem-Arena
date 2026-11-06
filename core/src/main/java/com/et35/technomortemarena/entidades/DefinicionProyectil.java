package com.et35.technomortemarena.entidades;

import com.et35.technomortemarena.mundo.Arena;

public final class DefinicionProyectil {
    public enum Aspecto { BALA, PERDIGON, LASER, COHETE, EXPLOSION, ARMA }

    private static final float ESCALA = Arena.ESCALA;

    private final Aspecto aspecto;
    private float velocidad;
    private float gravedad;
    private float lado = 8f * ESCALA;
    private int danio;
    private float alcance;
    private boolean seRefleja;
    private boolean ignoraInvulnerabilidad;
    private boolean explota;

    public DefinicionProyectil(Aspecto aspecto) {
        this.aspecto = aspecto;
    }

    public DefinicionProyectil velocidad(float velocidad) {
        this.velocidad = velocidad * ESCALA;
        return this;
    }

    public DefinicionProyectil gravedad(float gravedad) {
        this.gravedad = gravedad * ESCALA;
        return this;
    }

    public DefinicionProyectil lado(float lado) {
        this.lado = lado * ESCALA;
        return this;
    }

    public DefinicionProyectil danio(int danio) {
        this.danio = danio;
        return this;
    }

    public DefinicionProyectil alcance(float segundos) {
        this.alcance = segundos;
        return this;
    }

    public DefinicionProyectil seRefleja() {
        this.seRefleja = true;
        return this;
    }

    public DefinicionProyectil ignoraInvulnerabilidad() {
        this.ignoraInvulnerabilidad = true;
        return this;
    }

    public DefinicionProyectil explota() {
        this.explota = true;
        return this;
    }

    public Aspecto getAspecto() {
        return aspecto;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public float getGravedad() {
        return gravedad;
    }

    public float getLado() {
        return lado;
    }

    public int getDanio() {
        return danio;
    }

    public float getAlcance() {
        return alcance;
    }

    public boolean isSeRefleja() {
        return seRefleja;
    }

    public boolean isIgnoraInvulnerabilidad() {
        return ignoraInvulnerabilidad;
    }

    public boolean isExplota() {
        return explota;
    }
}
