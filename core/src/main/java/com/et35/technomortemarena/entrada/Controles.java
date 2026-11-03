package com.et35.technomortemarena.entrada;

import com.badlogic.gdx.Input.Keys;

/**
 * Teclas del teclado local: moverse, saltar y rodar. Apuntar y estocar se hacen con el mouse.
 *
 * <p>Sacar las teclas afuera de {@code EntradaLocal} permite cambiarlas sin tocar la logica. Cuando se
 * implemente el menu de ajustes de controles, alcanza con construir instancias de esta clase con las
 * teclas que elija el usuario.
 */
public class Controles {

    private final int izquierda;
    private final int derecha;
    private final int saltar;
    private final int rodar;

    public Controles(int izquierda, int derecha, int saltar, int rodar) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.saltar = saltar;
        this.rodar = rodar;
    }

    /** A y D para moverse, W para saltar y Shift izquierdo para rodar. */
    public static Controles porDefecto() {
        return new Controles(Keys.A, Keys.D, Keys.W, Keys.SHIFT_LEFT);
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

    public int getRodar() {
        return rodar;
    }
}
