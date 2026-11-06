package com.et35.technomortemarena.entrada;

import com.badlogic.gdx.Input.Keys;

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
