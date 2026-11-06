package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.TipoArma;
public class SpawnerArma extends Bloque {
    private static final Color COLOR_PISTOLA = new Color(0.45f, 0.58f, 0.80f, 1f);
    private static final Color COLOR_ESCOPETA = new Color(0.78f, 0.55f, 0.32f, 1f);
    private static final Color COLOR_RIFLE_LASER = new Color(0.35f, 0.80f, 0.85f, 1f);
    private static final Color COLOR_LANZACOHETES = new Color(0.55f, 0.68f, 0.30f, 1f);

    private final TipoArma arma;
    private final Color color;

    public SpawnerArma(Rectangle area, char celda) {
        super(area);
        this.arma = armaDe(celda);
        this.color = colorDe(celda);
    }

    public static TipoArma armaDe(char celda) {
        switch (celda) {
            case 'P':
                return TipoArma.PISTOLA;
            case 'E':
                return TipoArma.ESCOPETA;
            case 'F':
                return TipoArma.RIFLE_LASER;
            case 'C':
                return TipoArma.LANZACOHETES;
            default:
                return null;
        }
    }

    private static Color colorDe(char celda) {
        switch (celda) {
            case 'E':
                return COLOR_ESCOPETA;
            case 'F':
                return COLOR_RIFLE_LASER;
            case 'C':
                return COLOR_LANZACOHETES;
            default:
                return COLOR_PISTOLA;
        }
    }

    public TipoArma getArma() {
        return arma;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
