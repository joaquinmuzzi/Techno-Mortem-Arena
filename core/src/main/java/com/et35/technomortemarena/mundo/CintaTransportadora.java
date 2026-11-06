package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class CintaTransportadora extends Bloque {
    private static final float VELOCIDAD = 300f * Arena.ESCALA;

    private final float sentido;

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
