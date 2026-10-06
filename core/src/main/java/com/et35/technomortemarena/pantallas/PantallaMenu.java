package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

/**
 * Menu principal: titulo del juego y los botones Jugar, Editor de niveles y Salir.
 *
 * <p>Usa el mismo mundo de 960x540 que la interfaz de las demas pantallas para que la escala no "salte"
 * al cambiar de pantalla.
 */
public class PantallaMenu extends ScreenAdapter {

    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);

    private final RecursosGraficos recursos;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    private final Rectangle botonJugar = new Rectangle(ANCHO_MUNDO / 2f - 120f, 300f, 240f, 60f);
    private final Rectangle botonEditor = new Rectangle(ANCHO_MUNDO / 2f - 120f, 220f, 240f, 60f);
    private final Rectangle botonSalir = new Rectangle(ANCHO_MUNDO / 2f - 120f, 140f, 240f, 60f);

    public PantallaMenu(RecursosGraficos recursos) {
        this.recursos = recursos;
    }

    @Override
    public void render(float delta) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreJugar = botonJugar.contains(mouse);
        boolean sobreEditor = botonEditor.contains(mouse);
        boolean sobreSalir = botonSalir.contains(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (Gdx.input.justTouched() && sobreJugar)) {
            cambiarA(new PantallaSeleccionMapa(recursos));
            return;
        }
        if (Gdx.input.justTouched() && sobreEditor) {
            cambiarA(new PantallaEditor(recursos));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreSalir)) {
            Gdx.app.exit();
            return;
        }

        dibujar(sobreJugar, sobreEditor, sobreSalir);
    }

    /**
     * {@code setScreen} no libera la pantalla anterior, solo le llama {@code hide()}: hay que disponerla a
     * mano o el batch y la fuente quedan sin liberar.
     */
    private void cambiarA(com.badlogic.gdx.Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    private void dibujar(boolean sobreJugar, boolean sobreEditor, boolean sobreSalir) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        ui.boton(batch, botonJugar, sobreJugar, "JUGAR", 1.6f);
        ui.boton(batch, botonEditor, sobreEditor, "EDITOR DE NIVELES", 1.3f);
        ui.boton(batch, botonSalir, sobreSalir, "SALIR", 1.6f);
        String titulo = "TECHNO MORTEM ARENA";
        float x = (ANCHO_MUNDO - ui.anchoTexto(titulo, 3f)) / 2f;
        ui.texto(batch, titulo, x, ALTO_MUNDO - 100f, 3f, Color.WHITE);
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
