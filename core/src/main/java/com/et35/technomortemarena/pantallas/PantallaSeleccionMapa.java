package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
    private static final Color COLOR_BOTON = new Color(0.20f, 0.22f, 0.30f, 1f);
    private static final Color COLOR_BOTON_RESALTADO = new Color(0.32f, 0.36f, 0.50f, 1f);

    private final RecursosGraficos recursos;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fuente = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());
    private final Texture pixel = crearPixelBlanco();

    private final Rectangle botonArenaClasica = new Rectangle(ANCHO_MUNDO / 2f - 150f, 280f, 300f, 60f);
    private final Rectangle botonArenaAgujero = new Rectangle(ANCHO_MUNDO / 2f - 150f, 200f, 300f, 60f);
    private final Rectangle botonVolver = new Rectangle(ANCHO_MUNDO / 2f - 150f, 100f, 300f, 60f);

    public PantallaSeleccionMapa(RecursosGraficos recursos) {
        this.recursos = recursos;
    }

    @Override
    public void render(float delta) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreClasica = botonArenaClasica.contains(mouse);
        boolean sobreAgujero = botonArenaAgujero.contains(mouse);
        boolean sobreVolver = botonVolver.contains(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || (Gdx.input.justTouched() && sobreClasica)) {
            jugarEn(Arena.porDefecto());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || (Gdx.input.justTouched() && sobreAgujero)) {
            jugarEn(Arena.conAgujero());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreVolver)) {
            volverAlMenu();
            return;
        }

        dibujar(sobreClasica, sobreAgujero, sobreVolver);
    }

    private void jugarEn(Arena arena) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(new PantallaArena(recursos, arena));
        dispose();
    }

    private void volverAlMenu() {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(new PantallaMenu(recursos));
        dispose();
    }

    private void dibujar(boolean sobreClasica, boolean sobreAgujero, boolean sobreVolver) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarBoton(botonArenaClasica, sobreClasica, "1 - ARENA CLASICA");
        dibujarBoton(botonArenaAgujero, sobreAgujero, "2 - ARENA CON AGUJERO");
        dibujarBoton(botonVolver, sobreVolver, "VOLVER");
        dibujarTitulo();
        batch.end();
    }

    private void dibujarBoton(Rectangle boton, boolean resaltado, String texto) {
        batch.setColor(resaltado ? COLOR_BOTON_RESALTADO : COLOR_BOTON);
        batch.draw(pixel, boton.x, boton.y, boton.width, boton.height);
        batch.setColor(Color.WHITE);

        fuente.getData().setScale(1.3f);
        layout.setText(fuente, texto);
        float x = boton.x + (boton.width - layout.width) / 2f;
        float y = boton.y + (boton.height + layout.height) / 2f;
        fuente.draw(batch, layout, x, y);
    }

    private void dibujarTitulo() {
        fuente.getData().setScale(2.2f);
        layout.setText(fuente, "ELEGI EL MAPA");
        float x = (ANCHO_MUNDO - layout.width) / 2f;
        float y = ALTO_MUNDO - 100f;
        fuente.draw(batch, layout, x, y);
    }

    /** Textura de 1x1 blanca: estirada y tenida con {@code batch.setColor()}, sirve como rectangulo placeholder. */
    private static Texture crearPixelBlanco() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        fuente.dispose();
        pixel.dispose();
    }
}
