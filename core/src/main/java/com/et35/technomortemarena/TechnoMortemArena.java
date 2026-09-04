package com.et35.technomortemarena;

import com.badlogic.gdx.Game;

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
