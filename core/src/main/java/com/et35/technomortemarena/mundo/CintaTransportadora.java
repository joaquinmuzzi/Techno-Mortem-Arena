package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

/** Bloque que arrastra a quien esta parado sobre el, en una direccion fija. */
public class CintaTransportadora extends Bloque {

    private static final float VELOCIDAD = 300f * Arena.ESCALA;

    private final float sentido;

    /** @param sentido -1 para empujar hacia la izquierda, 1 para empujar hacia la derecha */
    public CintaTransportadora(Rectangle area, float sentido) {
        super(area);
        this.sentido = sentido;
    }

    @Override
    public Color getColor() {
        return new Color(0.45f, 0.55f, 0.65f, 1f);
    }

    @Override
    public float getVelocidadHorizontal() {
        return sentido * VELOCIDAD;
    }
}
