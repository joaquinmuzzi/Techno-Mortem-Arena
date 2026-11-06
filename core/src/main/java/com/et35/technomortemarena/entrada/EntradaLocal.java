package com.et35.technomortemarena.entrada;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class EntradaLocal implements FuenteEntrada {
    private final Controles controles;
    private final Vector2 puntero = new Vector2();
    private Viewport viewport;
    private boolean activa = true;

    public EntradaLocal(Controles controles) {
        this.controles = controles;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public void leer(EntradaJugador destino) {
        destino.limpiar();
        if (!activa) {
            return;
        }
        destino.izquierda = Gdx.input.isKeyPressed(controles.getIzquierda());
        destino.derecha = Gdx.input.isKeyPressed(controles.getDerecha());
        destino.saltar = Gdx.input.isKeyJustPressed(controles.getSaltar());
        destino.rodar = Gdx.input.isKeyJustPressed(controles.getRodar());
        destino.lanzar = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
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
