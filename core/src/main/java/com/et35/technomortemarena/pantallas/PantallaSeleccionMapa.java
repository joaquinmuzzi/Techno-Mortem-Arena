package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.mundo.Arena;

/**
 * Menu de seleccion de mapa, entre el menu principal y la ronda.
 *
 * <p>Cada boton arma una {@link Arena} distinta y se la pasa a {@link PantallaArena}. Para agregar un
 * mapa nuevo alcanza con sumar un boton aca y un metodo de fabrica en {@link Arena}.
 */
public class PantallaSeleccionMapa extends ScreenAdapter {

    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);

    private final RecursosGraficos recursos;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    private final Rectangle botonArenaClasica = new Rectangle(ANCHO_MUNDO / 2f - 150f, 360f, 300f, 50f);
    private final Rectangle botonArenaAgujero = new Rectangle(ANCHO_MUNDO / 2f - 150f, 300f, 300f, 50f);
    private final Rectangle botonArenaPlataformas = new Rectangle(ANCHO_MUNDO / 2f - 150f, 240f, 300f, 50f);
    private final Rectangle botonArenaTrampolin = new Rectangle(ANCHO_MUNDO / 2f - 150f, 180f, 300f, 50f);
    private final Rectangle botonArenaCintas = new Rectangle(ANCHO_MUNDO / 2f - 150f, 120f, 300f, 50f);
    private final Rectangle botonVolver = new Rectangle(ANCHO_MUNDO / 2f - 150f, 40f, 300f, 50f);

    public PantallaSeleccionMapa(RecursosGraficos recursos) {
        this.recursos = recursos;
    }

    @Override
    public void render(float delta) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreClasica = botonArenaClasica.contains(mouse);
        boolean sobreAgujero = botonArenaAgujero.contains(mouse);
        boolean sobrePlataformas = botonArenaPlataformas.contains(mouse);
        boolean sobreTrampolin = botonArenaTrampolin.contains(mouse);
        boolean sobreCintas = botonArenaCintas.contains(mouse);
        boolean sobreVolver = botonVolver.contains(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || (Gdx.input.justTouched() && sobreClasica)) {
            jugarEn(Arena.porDefecto());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || (Gdx.input.justTouched() && sobreAgujero)) {
            jugarEn(Arena.conAgujero());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || (Gdx.input.justTouched() && sobrePlataformas)) {
            jugarEn(Arena.conPlataformas());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) || (Gdx.input.justTouched() && sobreTrampolin)) {
            jugarEn(Arena.conTrampolin());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) || (Gdx.input.justTouched() && sobreCintas)) {
            jugarEn(Arena.conCintasYPinchos());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreVolver)) {
            cambiarA(new PantallaMenu(recursos));
            return;
        }

        dibujar(sobreClasica, sobreAgujero, sobrePlataformas, sobreTrampolin, sobreCintas, sobreVolver);
    }

    private void jugarEn(Arena arena) {
        cambiarA(new PantallaArena(recursos, arena));
    }

    /** Igual que en el menu principal: {@code setScreen} no libera esta pantalla, hay que disponerla a mano. */
    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    private void dibujar(boolean sobreClasica, boolean sobreAgujero, boolean sobrePlataformas,
                         boolean sobreTrampolin, boolean sobreCintas, boolean sobreVolver) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        ui.boton(batch, botonArenaClasica, sobreClasica, "1 - ARENA CLASICA", 1.3f);
        ui.boton(batch, botonArenaAgujero, sobreAgujero, "2 - ARENA CON AGUJERO", 1.3f);
        ui.boton(batch, botonArenaPlataformas, sobrePlataformas, "3 - ESCALERA", 1.3f);
        ui.boton(batch, botonArenaTrampolin, sobreTrampolin, "4 - TRAMPOLIN", 1.3f);
        ui.boton(batch, botonArenaCintas, sobreCintas, "5 - CINTAS Y PINCHOS", 1.3f);
        ui.boton(batch, botonVolver, sobreVolver, "VOLVER", 1.3f);
        String titulo = "ELEGI EL MAPA";
        float x = (ANCHO_MUNDO - ui.anchoTexto(titulo, 2.2f)) / 2f;
        ui.texto(batch, titulo, x, ALTO_MUNDO - 100f, 2.2f, Color.WHITE);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        ui.dispose();
    }
}
