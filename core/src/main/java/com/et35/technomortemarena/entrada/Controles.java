package com.et35.technomortemarena.entrada;

import com.badlogic.gdx.Input.Keys;

/**
 * Mapeo de teclas de un jugador.
 *
 * <p>Sacar las teclas afuera de {@code Jugador} es lo que permite que la misma clase sirva para los
 * dos duelistas: se construyen dos jugadores identicos y cada uno recibe su propio juego de
 * controles. Sin esto habria que duplicar la clase {@code Jugador} o llenarla de {@code if}.
 *
 * <p>Cuando se implemente el menu de ajustes de controles de la propuesta, alcanzara con construir
 * instancias de esta clase con las teclas que elija el usuario: no hay que tocar {@code Jugador}.
 */
public class Controles {

    private final int izquierda;
    private final int derecha;
    private final int saltar;
    private final int subirEspada;
    private final int bajarEspada;

    public Controles(int izquierda, int derecha, int saltar, int subirEspada, int bajarEspada) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.saltar = saltar;
        this.subirEspada = subirEspada;
        this.bajarEspada = bajarEspada;
    }

    /** Jugador 1: A y D para moverse, W para saltar, E sube la guardia y Q la baja. */
    public static Controles jugadorUno() {
        return new Controles(Keys.A, Keys.D, Keys.W, Keys.E, Keys.Q);
    }

    /** Jugador 2: flechas para moverse y saltar, O sube la guardia y L la baja. */
    public static Controles jugadorDos() {
        return new Controles(Keys.LEFT, Keys.RIGHT, Keys.UP, Keys.O, Keys.L);
    }

    public int getIzquierda() {
        return izquierda;
    }

    public int getDerecha() {
        return derecha;
    }

    public int getSaltar() {
        return saltar;
    }

    public int getSubirEspada() {
        return subirEspada;
    }

    public int getBajarEspada() {
        return bajarEspada;
    }
}
