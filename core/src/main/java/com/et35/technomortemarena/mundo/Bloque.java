package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.Jugador;

public class Bloque {
    private final Rectangle area;

    public Bloque(Rectangle area) {
        this.area = area;
    }

    public Rectangle getArea() {
        return area;
    }

    public Color getColor() {
        return Color.WHITE;
    }

    public float getVelocidadHorizontal() {
        return 0f;
    }

    public void alAterrizar(Jugador jugador) {
    }

    public void alTocar(Jugador jugador) {
    }
}
