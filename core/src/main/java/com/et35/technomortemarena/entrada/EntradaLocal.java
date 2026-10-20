package com.et35.technomortemarena.entrada;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * La entrada de quien juega en esta computadora: teclado para moverse y saltar, y mouse para apuntar
 * la espada (el cursor) y lanzar la estocada (clic izquierdo).
 *
 * <p>El cursor llega en pixeles de pantalla; se pasa a coordenadas del mundo con el {@link Viewport} de
 * la arena, asi el punto apuntado es el mismo en cualquier resolucion y tamano de ventana.
 */
public class EntradaLocal implements FuenteEntrada {

    private final Controles controles;
    private final Vector2 puntero = new Vector2();
    private Viewport viewport;
    private boolean activa = true;

    public EntradaLocal(Controles controles) {
        this.controles = controles;
    }

    /** El viewport de la arena, para convertir el cursor a coordenadas del mundo. */
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    /**
     * Si es {@code false} no se lee nada. En una partida en red la pausa no frena la simulacion, asi
     * que se usa para que el jugador no se mueva ni estoque mientras mira el menu.
     */
    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public void leer(EntradaJugador destino) {
        destino.limpiar();
        if (!activa) {
            return;
        }
        // isKeyPressed: verdadero mientras la tecla este hundida -> movimiento continuo.
        destino.izquierda = Gdx.input.isKeyPressed(controles.getIzquierda());
        destino.derecha = Gdx.input.isKeyPressed(controles.getDerecha());
        // isKeyJustPressed / isButtonJustPressed: verdadero solo en el fotograma del pulsado -> una
        // accion por pulsacion. Con "sostenida", mantener la tecla saltaria en cada fotograma.
        destino.saltar = Gdx.input.isKeyJustPressed(controles.getSaltar());
        destino.estocada = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (viewport != null) {
            puntero.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(puntero);
            destino.hayPuntero = true;
            destino.apuntaX = puntero.x;
            destino.apuntaY = puntero.y;
        }
    }
}
