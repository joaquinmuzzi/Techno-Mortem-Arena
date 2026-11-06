package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class SpawnerArma extends Bloque {
    private static final Color COLOR_PISTOLA = new Color(0.45f, 0.58f, 0.80f, 1f);
    private static final Color COLOR_ESCOPETA = new Color(0.78f, 0.55f, 0.32f, 1f);

    private final boolean deEscopetas;

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
