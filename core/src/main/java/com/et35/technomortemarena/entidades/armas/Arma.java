package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.DefinicionProyectil;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entidades.Proyectil;
import com.et35.technomortemarena.mundo.Arena;
import java.util.List;
import java.util.Random;

public abstract class Arma {
    public static final float ANCHO_EN_EL_SUELO = 56f * Arena.ESCALA;
    protected static final float ALTO_DIBUJADO = 18f * Arena.ESCALA;
    protected static final float VELOCIDAD_LANZADA = 1200f;
    protected static final float GRAVEDAD_LANZADA = -1100f;

    private final String nombre;
    private final int danioLanzada;
    private DefinicionProyectil definicionLanzada;

    protected Arma(String nombre, int danioLanzada) {
        this.nombre = nombre;
        this.danioLanzada = danioLanzada;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDanioLanzada() {
        return danioLanzada;
    }

    public int getBalasIniciales() {
        return 0;
    }

    public boolean esDeFuego() {
        return false;
    }

    public boolean esEspada() {
        return false;
    }

    public boolean sePuedeSoltar() {
        return true;
    }

    public void disparar(Jugador quien, float x, float y, float angulo, Random azar, boolean punteria,
                         List<Proyectil> salida) {
    }

    public float getRecarga() {
        return 0f;
    }

    public SonidoDisparo getSonidoDisparo() {
        return SonidoDisparo.NINGUNO;
    }

    public enum SonidoDisparo { NINGUNO, PISTOLA, ESCOPETA, LASER, BAZUCA }

    public DefinicionProyectil getDefinicionLanzada() {
        if (definicionLanzada == null) {
            definicionLanzada = new DefinicionProyectil(DefinicionProyectil.Aspecto.ARMA)
                .velocidad(VELOCIDAD_LANZADA).gravedad(getGravedadLanzada()).lado(28f).danio(danioLanzada);
        }
        return definicionLanzada;
    }

    protected float getGravedadLanzada() {
        return GRAVEDAD_LANZADA;
    }

    public boolean quedaEnElMapa(int balasQueLeQuedan) {
        return balasQueLeQuedan > 0;
    }

    public abstract Texture getTextura(RecursosGraficos recursos);

    public Color getColor() {
        return Color.WHITE;
    }

    public abstract float getAnchoEnLaMano();

    public abstract float getAltoEnLaMano();

    public abstract float getAnchoDibujado();

    public float getAltoDibujado() {
        return ALTO_DIBUJADO;
    }

    public float getAnchoEnElSuelo() {
        return ANCHO_EN_EL_SUELO;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
