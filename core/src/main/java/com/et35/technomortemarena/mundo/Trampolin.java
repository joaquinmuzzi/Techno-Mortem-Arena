package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.Jugador;

/** Bloque que rebota: quien aterriza sobre el sale disparado hacia arriba con mas fuerza que un salto. */
public class Trampolin extends Bloque {

    private static final float IMPULSO = 1.6f * Jugador.IMPULSO_SALTO;

    public Trampolin(Rectangle area) {
        super(area);
    }

    @Override
    public Color getColor() {
        return new Color(1f, 0.85f, 0.2f, 1f);
    }

    @Override
    public void alAterrizar(Jugador jugador) {
        jugador.impulsar(IMPULSO);
    }
}
