package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.armas.Arma;
import com.et35.technomortemarena.entidades.armas.Armas;
public class SpawnerArma extends Bloque {
    private static final Color COLOR_PISTOLA = new Color(0.45f, 0.58f, 0.80f, 1f);
    private static final Color COLOR_ESCOPETA = new Color(0.78f, 0.55f, 0.32f, 1f);
    private static final Color COLOR_RIFLE_LASER = new Color(0.35f, 0.80f, 0.85f, 1f);
    private static final Color COLOR_LANZACOHETES = new Color(0.55f, 0.68f, 0.30f, 1f);

    private final Arma arma;
    private final Color color;

    public SpawnerArma(Rectangle area, char celda) {
        super(area);
        this.arma = armaDe(celda);
        this.color = colorDe(celda);
    }

    public static Arma armaDe(char celda) {
        switch (celda) {
            case 'P':
                return Armas.PISTOLA;
            case 'E':
                return Armas.ESCOPETA;
            case 'F':
                return Armas.RIFLE_LASER;
            case 'C':
                return Armas.LANZACOHETES;
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

    public Arma getArma() {
        return arma;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
