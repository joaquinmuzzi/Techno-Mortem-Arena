package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.Jugador;

/**
 * Menu principal: titulo del juego, los dos duelistas en guardia a los costados y los botones Modo de
 * prueba, Multijugador, Editor de niveles y Salir.
 *
 * <p>Usa el mismo mundo de 960x540 que la interfaz de las demas pantallas para que la escala no "salte"
 * al cambiar de pantalla.
 */
public class PantallaMenu extends ScreenAdapter {

    private static final float ESCALA_TITULO = 3.2f;
    private static final float ESCALA_ARENA = 1.6f;
    private static final float SEPARACION_ARENA = 22f;
    private static final Color COLOR_MORTEM = new Color(0.90f, 0.22f, 0.22f, 1f);
    private static final Color COLOR_ARENA = new Color(0.35f, 0.72f, 1f, 1f);
    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    /** Los duelistas del menu: el doble de su sprite, parados sobre una franja de piso. */
    private static final float ESCALA_DUELISTA = 2f;
    private static final float Y_PISO_MENU = 70f;
    private static final float X_DUELISTA_UNO = 150f;
    private static final float X_DUELISTA_DOS = ANCHO_MUNDO - 150f - 48f * ESCALA_DUELISTA;

    /** Segundos desde que se abrio el menu: mueve un poco las espadas, como si estuvieran en guardia. */
    private float tiempo;

    private final Rectangle botonJugar = new Rectangle(ANCHO_MUNDO / 2f - 120f, 350f, 240f, 52f);
    private final Rectangle botonMultijugador = new Rectangle(ANCHO_MUNDO / 2f - 120f, 288f, 240f, 52f);
    private final Rectangle botonEditor = new Rectangle(ANCHO_MUNDO / 2f - 120f, 226f, 240f, 52f);
    private final Rectangle botonOpciones = new Rectangle(ANCHO_MUNDO / 2f - 120f, 164f, 240f, 52f);
    private final Rectangle botonSalir = new Rectangle(ANCHO_MUNDO / 2f - 120f, 102f, 240f, 52f);

    public PantallaMenu(RecursosGraficos recursos, RecursosAudio audio) {
        this.recursos = recursos;
        this.audio = audio;
        audio.pedirMusica(RecursosAudio.Musica.MENU);
        ui.setAudio(audio);
    }

    @Override
    public void render(float delta) {
        tiempo += delta;
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreJugar = botonJugar.contains(mouse);
        boolean sobreMultijugador = botonMultijugador.contains(mouse);
        boolean sobreEditor = botonEditor.contains(mouse);
        boolean sobreOpciones = botonOpciones.contains(mouse);
        boolean sobreSalir = botonSalir.contains(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (Gdx.input.justTouched() && sobreJugar)) {
            ui.clic();
            cambiarA(new PantallaSeleccionMapa(recursos, audio));
            return;
        }
        if (Gdx.input.justTouched() && sobreMultijugador) {
            ui.clic();
            cambiarA(new PantallaMultijugador(recursos, audio, ""));
            return;
        }
        if (Gdx.input.justTouched() && sobreEditor) {
            cambiarA(new PantallaEditor(recursos, audio));
            return;
        }
        if (Gdx.input.justTouched() && sobreOpciones) {
            ui.clic();
            cambiarA(new PantallaOpciones(recursos, audio));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreSalir)) {
            ui.clic();
            Gdx.app.exit();
            return;
        }

        dibujar(sobreJugar, sobreMultijugador, sobreEditor, sobreOpciones, sobreSalir);
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

    private void dibujar(boolean sobreJugar, boolean sobreMultijugador, boolean sobreEditor, boolean sobreOpciones,
                         boolean sobreSalir) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarDuelistas();
        ui.boton(batch, botonJugar, sobreJugar, "MODO DE PRUEBA", 1.3f);
        ui.boton(batch, botonMultijugador, sobreMultijugador, "MULTIJUGADOR", 1.3f);
        ui.boton(batch, botonEditor, sobreEditor, "EDITOR DE NIVELES", 1.3f);
        ui.boton(batch, botonOpciones, sobreOpciones, "OPCIONES", 1.3f);
        ui.boton(batch, botonSalir, sobreSalir, "SALIR", 1.6f);
        dibujarTitulo();
        batch.end();
    }

    /** "TECHNO MORTEM" grande, con MORTEM en rojo, y abajo "ARENA" chico, azul y con las letras separadas. */
    private void dibujarTitulo() {
        float arriba = ALTO_MUNDO - 38f;
        String techno = "TECHNO ";
        String mortem = "MORTEM";
        float anchoTechno = ui.anchoTexto(techno, ESCALA_TITULO);
        float x = (ANCHO_MUNDO - anchoTechno - ui.anchoTexto(mortem, ESCALA_TITULO)) / 2f;
        ui.texto(batch, techno, x, arriba, ESCALA_TITULO, Color.WHITE);
        ui.texto(batch, mortem, x + anchoTechno, arriba, ESCALA_TITULO, COLOR_MORTEM);
        String arena = "ARENA";
        float anchoLetras = 0f;
        for (int i = 0; i < arena.length(); i++) {
            anchoLetras += ui.anchoTexto(arena.substring(i, i + 1), ESCALA_ARENA);
        }
        float xLetra = (ANCHO_MUNDO - anchoLetras - (arena.length() - 1) * SEPARACION_ARENA) / 2f;
        for (int i = 0; i < arena.length(); i++) {
            String letra = arena.substring(i, i + 1);
            ui.texto(batch, letra, xLetra, arriba - 54f, ESCALA_ARENA, COLOR_ARENA);
            xLetra += ui.anchoTexto(letra, ESCALA_ARENA) + SEPARACION_ARENA;
        }
    }

    /** Los dos duelistas a los costados, mirandose con la espada en guardia hacia el centro. */
    private void dibujarDuelistas() {
        batch.setColor(0.35f, 0.38f, 0.45f, 1f);
        batch.draw(recursos.piso, 0f, Y_PISO_MENU - 24f, ANCHO_MUNDO, 24f);
        float vaiven = 0.12f * MathUtils.sin(tiempo * 1.6f);
        dibujarDuelista(X_DUELISTA_UNO, true, Jugador.TINTE_UNO, 0.45f + vaiven);
        dibujarDuelista(X_DUELISTA_DOS, false, Jugador.TINTE_DOS, MathUtils.PI - 0.45f + vaiven);
        batch.setColor(Color.WHITE);
    }

    private void dibujarDuelista(float x, boolean mirandoDerecha, Color tinte, float angulo) {
        float ancho = 48f * ESCALA_DUELISTA;
        float alto = 96f * ESCALA_DUELISTA;
        Texture cuerpo = recursos.jugador;
        batch.setColor(tinte);
        batch.draw(cuerpo, x, Y_PISO_MENU, ancho, alto, 0, 0, cuerpo.getWidth(), cuerpo.getHeight(), !mirandoDerecha, false);
        // el hombro, como en el juego: corrido hacia donde mira, a 56 del piso en la escala del sprite
        float hombroX = x + ancho / 2f + (mirandoDerecha ? 10f : -10f) * ESCALA_DUELISTA;
        float hombroY = Y_PISO_MENU + 56f * ESCALA_DUELISTA;
        dibujarGirado(recursos.brazo, hombroX, hombroY, 22f * ESCALA_DUELISTA, 8f * ESCALA_DUELISTA, angulo);
        batch.setColor(Color.WHITE);
        float manoX = hombroX + MathUtils.cos(angulo) * 16f * ESCALA_DUELISTA;
        float manoY = hombroY + MathUtils.sin(angulo) * 16f * ESCALA_DUELISTA;
        dibujarGirado(recursos.espada, manoX, manoY, 56f * ESCALA_DUELISTA, 10f * ESCALA_DUELISTA, angulo);
    }

    /** Una textura que mira a la derecha, girada alrededor de su extremo izquierdo (espejada si apunta a la izquierda). */
    private void dibujarGirado(Texture textura, float x, float y, float ancho, float alto, float angulo) {
        boolean haciaLaIzquierda = MathUtils.cos(angulo) < 0f;
        batch.draw(textura, x, y - alto / 2f, 0f, alto / 2f, ancho, alto, 1f, 1f, angulo * MathUtils.radiansToDegrees,
            0, 0, textura.getWidth(), textura.getHeight(), false, haciaLaIzquierda);
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
