package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.mundo.Arena;

/**
 * Ronda de duelo: arma la arena, los dos duelistas y corre el bucle de juego.
 *
 * <p>Extiende {@link ScreenAdapter} en lugar de implementar {@code Screen} para no tener que
 * escribir los metodos del ciclo de vida que todavia no se usan ({@code pause}, {@code resume},
 * {@code hide}).
 */
public class PantallaArena extends ScreenAdapter {

    private static final float DELTA_MAXIMO = 1f / 30f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color TINTE_UNO = new Color(0.42f, 0.78f, 1f, 1f);
    private static final Color TINTE_DOS = new Color(1f, 0.48f, 0.42f, 1f);

    private final RecursosGraficos recursos;
    private final Arena arena = Arena.porDefecto();
    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Jugador jugadorUno;
    private final Jugador jugadorDos;

    public PantallaArena(RecursosGraficos recursos) {
        this.recursos = recursos;
        // FitViewport mantiene la relacion de aspecto agregando bandas negras si hace falta, en
        // lugar de estirar la imagen. Asi las distancias del duelo son identicas en cualquier
        // resolucion, que es imprescindible para que el enfrentamiento sea justo.
        this.viewport = new FitViewport(arena.getAncho(), arena.getAlto(), new OrthographicCamera());
        this.jugadorUno = new Jugador(Controles.jugadorUno(), 180f, true, TINTE_UNO);
        this.jugadorDos = new Jugador(Controles.jugadorDos(), 730f, false, TINTE_DOS);
    }

    @Override
    public void render(float delta) {
        actualizar(delta);
        dibujar();
    }

    private void actualizar(float delta) {
        // Un delta grande (por ejemplo al arrastrar la ventana, que congela el bucle) haria que los
        // jugadores se teletransporten y atraviesen el piso. Acotarlo degrada la simulacion en lugar
        // de romperla.
        float paso = Math.min(delta, DELTA_MAXIMO);
        jugadorUno.actualizar(paso, arena);
        jugadorDos.actualizar(paso, arena);
    }

    private void dibujar() {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarPiso();
        jugadorUno.dibujar(batch, recursos);
        jugadorDos.dibujar(batch, recursos);
        batch.end();
    }

    private void dibujarPiso() {
        float lado = recursos.piso.getWidth();
        for (float x = 0f; x < arena.getAncho(); x += lado) {
            for (float y = arena.getAlturaPiso() - lado; y > -lado; y -= lado) {
                batch.draw(recursos.piso, x, y, lado, lado);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
