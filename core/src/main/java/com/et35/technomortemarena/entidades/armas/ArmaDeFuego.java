package com.et35.technomortemarena.entidades.armas;

import com.et35.technomortemarena.entidades.DefinicionProyectil;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entidades.Proyectil;
import com.et35.technomortemarena.mundo.Arena;
import java.util.List;
import java.util.Random;

public abstract class ArmaDeFuego extends Arma {
    private static final int DANIO_LANZADA = 2;

    private final int balasIniciales;
    private final float recarga;
    private final DefinicionProyectil disparo;
    private final float anchoEnLaMano;
    private final float altoEnLaMano;
    private final float anchoDibujado;

    protected ArmaDeFuego(String nombre, int balasIniciales, float recarga, DefinicionProyectil disparo,
                          float anchoEnLaMano, float altoEnLaMano, float anchoDibujado) {
        super(nombre, DANIO_LANZADA);
        this.balasIniciales = balasIniciales;
        this.recarga = recarga;
        this.disparo = disparo;
        this.anchoEnLaMano = anchoEnLaMano * Arena.ESCALA;
        this.altoEnLaMano = altoEnLaMano * Arena.ESCALA;
        this.anchoDibujado = anchoDibujado * Arena.ESCALA;
    }

    @Override
    public int getBalasIniciales() {
        return balasIniciales;
    }

    @Override
    public boolean esDeFuego() {
        return true;
    }

    @Override
    public float getRecarga() {
        return recarga;
    }

    public DefinicionProyectil getDisparo() {
        return disparo;
    }

    @Override
    public void disparar(Jugador quien, float x, float y, float angulo, Random azar, boolean punteria,
                         List<Proyectil> salida) {
        salida.add(new Proyectil(disparo, quien, x, y, angulo, disparo.getVelocidad()));
    }

    @Override
    public float getAnchoEnLaMano() {
        return anchoEnLaMano;
    }

    @Override
    public float getAltoEnLaMano() {
        return altoEnLaMano;
    }

    @Override
    public float getAnchoDibujado() {
        return anchoDibujado;
    }
}
