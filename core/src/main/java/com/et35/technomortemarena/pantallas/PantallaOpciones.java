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

import com.et35.technomortemarena.OpcionesAudio;
import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;

/**
 * Las opciones, desde el menu principal: prender y apagar la musica y los efectos de sonido. Cada clic
 * cambia la opcion al momento y queda guardada para la proxima vez que se abra el juego.
 */
public class PantallaOpciones extends ScreenAdapter {

    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    private final Rectangle botonMusica = new Rectangle(ANCHO_MUNDO / 2f - 150f, 310f, 300f, 56f);
    private final Rectangle botonEfectos = new Rectangle(ANCHO_MUNDO / 2f - 150f, 240f, 300f, 56f);
    private final Rectangle botonVolver = new Rectangle(ANCHO_MUNDO / 2f - 150f, 110f, 300f, 50f);

    public PantallaOpciones(RecursosGraficos recursos, RecursosAudio audio) {
        this.recursos = recursos;
        this.audio = audio;
        audio.pedirMusica(RecursosAudio.Musica.MENU);
        ui.setAudio(audio);
    }

    @Override
    public void render(float delta) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreMusica = botonMusica.contains(mouse);
        boolean sobreEfectos = botonEfectos.contains(mouse);
        boolean sobreVolver = botonVolver.contains(mouse);
        boolean toque = Gdx.input.justTouched();
        OpcionesAudio opciones = audio.getOpciones();

        if (toque && sobreMusica) {
            ui.clic();
            audio.setMusica(!opciones.isMusica());
        } else if (toque && sobreEfectos) {
            // primero se cambia: al prenderlos, el clic ya suena; al apagarlos, ya no
            audio.setEfectos(!opciones.isEfectos());
            ui.clic();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (toque && sobreVolver)) {
            ui.clic();
            cambiarA(new PantallaMenu(recursos, audio));
            return;
        }

        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        String titulo = "OPCIONES";
        ui.texto(batch, titulo, (ANCHO_MUNDO - ui.anchoTexto(titulo, 2.5f)) / 2f, ALTO_MUNDO - 70f, 2.5f, Color.WHITE);
        ui.boton(batch, botonMusica, sobreMusica, "MUSICA: " + (opciones.isMusica() ? "SI" : "NO"), 1.3f);
        ui.boton(batch, botonEfectos, sobreEfectos, "EFECTOS: " + (opciones.isEfectos() ? "SI" : "NO"), 1.3f);
        ui.boton(batch, botonVolver, sobreVolver, "VOLVER", 1.3f);
        batch.end();
    }

    /** Igual que en las demas pantallas: {@code setScreen} no libera la anterior, hay que disponerla a mano. */
    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
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
