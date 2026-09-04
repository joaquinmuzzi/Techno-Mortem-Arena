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

/**
 * Menu principal: titulo del juego y dos botones placeholder, Jugar y Salir.
 *
 * <p>Usa el mismo mundo de 960x540 que {@link PantallaArena} para que la escala no "salte" al
 * cambiar de pantalla. Los botones son un pixel blanco de 1x1 estirado y tenido (la misma tecnica de
 * sprite blanco + {@code batch.setColor()} que ya usa {@link com.et35.technomortemarena.entidades.Jugador}
 * con sus texturas), y el texto usa la fuente que trae LibGDX incorporada de fabrica
 * ({@code new BitmapFont()}), asi que no hace falta cargar ningun archivo nuevo para tener algo
 * jugable en pantalla.
 */
public class PantallaMenu extends ScreenAdapter {

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

    private final Rectangle botonJugar = new Rectangle(ANCHO_MUNDO / 2f - 120f, 230f, 240f, 60f);
    private final Rectangle botonSalir = new Rectangle(ANCHO_MUNDO / 2f - 120f, 150f, 240f, 60f);

    public PantallaMenu(RecursosGraficos recursos) {
        this.recursos = recursos;
    }

    @Override
    public void render(float delta) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean sobreJugar = botonJugar.contains(mouse);
        boolean sobreSalir = botonSalir.contains(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (Gdx.input.justTouched() && sobreJugar)) {
            irAJugar();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreSalir)) {
            Gdx.app.exit();
            return;
        }

        dibujar(sobreJugar, sobreSalir);
    }

    /**
     * Igual que {@code PantallaArena.reiniciar()}: {@code setScreen} no libera esta pantalla, solo le
     * llama {@code hide()}, asi que hay que disponerla a mano o el pixel/la fuente quedan sin liberar.
     */
    private void irAJugar() {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(new PantallaArena(recursos));
        dispose();
    }

    private void dibujar(boolean sobreJugar, boolean sobreSalir) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarBoton(botonJugar, sobreJugar, "JUGAR");
        dibujarBoton(botonSalir, sobreSalir, "SALIR");
        dibujarTitulo();
        batch.end();
    }

    private void dibujarBoton(Rectangle boton, boolean resaltado, String texto) {
        batch.setColor(resaltado ? COLOR_BOTON_RESALTADO : COLOR_BOTON);
        batch.draw(pixel, boton.x, boton.y, boton.width, boton.height);
        batch.setColor(Color.WHITE);

        fuente.getData().setScale(1.6f);
        layout.setText(fuente, texto);
        float x = boton.x + (boton.width - layout.width) / 2f;
        float y = boton.y + (boton.height + layout.height) / 2f;
        fuente.draw(batch, layout, x, y);
    }

    private void dibujarTitulo() {
        fuente.getData().setScale(3f);
        layout.setText(fuente, "TECHNO MORTEM ARENA");
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
