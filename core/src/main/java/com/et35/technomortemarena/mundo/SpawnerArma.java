package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

/**
 * Celda de un bloque spawner de armas: es solido como un bloque comun y, entre todas las celdas pegadas del
 * mismo tipo, hace aparecer encima un arma cada tanto (lo lleva {@code entidades.ArmasEnJuego}). En la
 * grilla es {@code P} para el de pistolas y {@code E} para el de escopetas; el editor los pone de a 3x3.
 */
public class SpawnerArma extends Bloque {

    private static final Color COLOR_PISTOLA = new Color(0.45f, 0.58f, 0.80f, 1f);
    private static final Color COLOR_ESCOPETA = new Color(0.78f, 0.55f, 0.32f, 1f);

    private final boolean deEscopetas;

    /** @param deEscopetas {@code true} para el de escopetas ({@code E}), {@code false} para el de pistolas ({@code P}) */
    public SpawnerArma(Rectangle area, boolean deEscopetas) {
        super(area);
        this.deEscopetas = deEscopetas;
    }

    public boolean isDeEscopetas() {
        return deEscopetas;
    }

    @Override
    public Color getColor() {
        return deEscopetas ? COLOR_ESCOPETA : COLOR_PISTOLA;
    }
}
