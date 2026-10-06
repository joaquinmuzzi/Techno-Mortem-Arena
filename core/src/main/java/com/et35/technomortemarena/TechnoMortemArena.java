package com.et35.technomortemarena;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;

import com.et35.technomortemarena.pantallas.PantallaMenu;

/**
 * Aplicacion compartida por todas las plataformas.
 *
 * <p>Extiende {@link Game} en lugar de {@code ApplicationAdapter} porque {@code Game} administra
 * pantallas: mas adelante van a convivir el menu principal, la seleccion de mapa y la arena, y
 * cambiar entre ellas se reduce a llamar a {@code setScreen}.
 */
public class TechnoMortemArena extends Game {

    private RecursosGraficos recursos;

    @Override
    public void create() {
        // Las texturas se cargan una sola vez, aca, y se comparten entre todas las pantallas.
        recursos = new RecursosGraficos();
        setScreen(new PantallaMenu(recursos));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            alternarPantallaCompleta();
        }
        super.render();
    }

    /** Pantalla completa a la resolucion nativa del monitor; al salir, vuelve a una ventana del mismo tamano. */
    private void alternarPantallaCompleta() {
        if (Gdx.graphics.isFullscreen()) {
            Graphics.DisplayMode escritorio = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setWindowedMode(Math.min(1920, escritorio.width), Math.min(1080, escritorio.height));
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    @Override
    public void dispose() {
        // Game.dispose() solo llama a hide() en la pantalla activa: NO la libera. Hay que hacerlo a
        // mano o el SpriteBatch de la pantalla queda sin liberar.
        if (getScreen() != null) {
            getScreen().dispose();
        }
        recursos.dispose();
        super.dispose();
    }
}
