package com.et35.technomortemarena.entrada;

import com.badlogic.gdx.Gdx;

/** Lee la entrada del teclado local segun un juego de teclas {@link Controles}. */
public class EntradaTeclado implements FuenteEntrada {

    private final Controles controles;
    private boolean activa = true;

    public EntradaTeclado(Controles controles) {
        this.controles = controles;
    }

    /**
     * Si es {@code false} no se lee ninguna tecla. En una partida en red la pausa no frena la
     * simulacion, asi que se usa para que el jugador no se mueva mientras mira el menu.
     */
    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public void leer(EntradaJugador destino) {
        if (!activa) {
            destino.limpiar();
            return;
        }
        // isKeyPressed: verdadero mientras la tecla este hundida -> movimiento continuo.
        destino.izquierda = Gdx.input.isKeyPressed(controles.getIzquierda());
        destino.derecha = Gdx.input.isKeyPressed(controles.getDerecha());
        destino.estocada = Gdx.input.isKeyPressed(controles.getEstocada());
        // isKeyJustPressed: verdadero solo en el fotograma del pulsado -> una accion por pulsacion.
        // Con isKeyPressed, mantener la tecla saltaria en cada fotograma y cambiaria la guardia 60
        // veces por segundo.
        destino.saltar = Gdx.input.isKeyJustPressed(controles.getSaltar());
        destino.subirEspada = Gdx.input.isKeyJustPressed(controles.getSubirEspada());
        destino.bajarEspada = Gdx.input.isKeyJustPressed(controles.getBajarEspada());
    }
}
