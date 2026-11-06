package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Color;

public class EspadaLaser extends Espada {
    public static final Color COLOR = new Color(1f, 0.25f, 0.25f, 1f);
    private static final int DANIO_HOJA = 8;
    private static final float GRAVEDAD_LANZADA_LASER = -500f;

    public EspadaLaser() {
        super("Espada laser", 8);
    }

    @Override
    public int getDanioHoja() {
        return DANIO_HOJA;
    }

    @Override
    public boolean sePuedeSoltar() {
        return false;
    }

    @Override
    public float getVelocidadReflejo() {
        return 1f;
    }

    @Override
    public float getDesvioReflejo() {
        return 0f;
    }

    @Override
    protected boolean esDeLuz() {
        return true;
    }

    @Override
    protected float getGravedadLanzada() {
        return GRAVEDAD_LANZADA_LASER;
    }

    @Override
    public Color getColor() {
        return COLOR;
    }
}
