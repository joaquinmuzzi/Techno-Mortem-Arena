package com.et35.technomortemarena.mundo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.entidades.Jugador;

/**
 * Caja solida del escenario con la que colisionan los jugadores.
 *
 * <p>Para crear un tipo de bloque nuevo (trampolin, plataforma movil, pinchos...) se extiende esta clase
 * y se sobreescriben los ganchos: {@link #alAterrizar} para lo que pasa cuando un jugador cae sobre el
 * bloque, y {@link #getColor()} para como se dibuja. La colision en si no cambia.
 */
public class Bloque {

    private final Rectangle area;

    public Bloque(Rectangle area) {
        this.area = area;
    }

    public Rectangle getArea() {
        return area;
    }

    /** Color con el que se tiñe el sprite del bloque. */
    public Color getColor() {
        return Color.WHITE;
    }

    /** Velocidad horizontal que el bloque le imprime a quien esta parado sobre el. Las cintas la usan. */
    public float getVelocidadHorizontal() {
        return 0f;
    }

    /** Se llama una vez cada vez que un jugador aterriza sobre el bloque, no mientras sigue parado. */
    public void alAterrizar(Jugador jugador) {
    }

    /** Se llama en cada fotograma en que un jugador esta tocando el bloque, de cualquier lado. */
    public void alTocar(Jugador jugador) {
    }
}
