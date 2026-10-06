package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.Jugador;

/** Bloque mortal: cualquier jugador que lo toca muere al instante. */
public class Pincho extends Bloque {

    public Pincho(Rectangle area) {
        super(area);
    }

    @Override
    public Color getColor() {
        return new Color(0.85f, 0.2f, 0.2f, 1f);
    }

    @Override
    public void alTocar(Jugador jugador) {
        jugador.morir();
    }
}
