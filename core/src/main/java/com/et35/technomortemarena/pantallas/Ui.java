package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import com.et35.technomortemarena.RecursosAudio;

public class Ui {
    public static final Color COLOR_BOTON = new Color(0.20f, 0.22f, 0.30f, 1f);
    public static final Color COLOR_BOTON_RESALTADO = new Color(0.32f, 0.36f, 0.50f, 1f);
    public static final Color COLOR_BOTON_SELECCIONADO = new Color(0.55f, 0.45f, 0.15f, 1f);

    private static final float VOLUMEN_INTERFAZ = 0.6f;

    private final Texture pixel;
    private static final int[] TAMANIOS = {12, 14, 16, 18, 20, 24, 28, 32, 40, 48};
    private static final float ESCALA_TITULO = 1.5f;
    private static final float ALTO_BASE = 15f;
    public static final Color COLOR_LINEA_BOTON = new Color(0.36f, 0.40f, 0.50f, 1f);
    public static final Color COLOR_LINEA_BOTON_RESALTADO = new Color(0.35f, 0.72f, 1f, 1f);
    private static final Color FONDO_BOTON = new Color(0.20f, 0.22f, 0.30f, 0.55f);
    private static final Color FONDO_BOTON_RESALTADO = new Color(0.24f, 0.30f, 0.42f, 0.75f);
    private static final float GROSOR_LINEA_BOTON = 3f;
    private static final float ESCALA_TEXTO_BOTON = 0.85f;
    private static final float ESCALA_MINIMA_TITULO = 0.95f;

    private final BitmapFont[] titulo = new BitmapFont[TAMANIOS.length];
    private final BitmapFont[] mono = new BitmapFont[TAMANIOS.length];
    private BitmapFont fuente;
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle rectLinea = new Rectangle();

    private RecursosAudio audio;

    private Object idResaltado;

    public Ui() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        for (int i = 0; i < TAMANIOS.length; i++) {
            titulo[i] = new BitmapFont(Gdx.files.internal("fuentes/titulo-" + TAMANIOS[i] + ".fnt"));
            mono[i] = new BitmapFont(Gdx.files.internal("fuentes/mono-" + TAMANIOS[i] + ".fnt"));
        }
    }

    private BitmapFont elegir(float escala, boolean esTitulo) {
        BitmapFont[] familia = esTitulo ? titulo : mono;
        float alto = ALTO_BASE * escala;
        int mejor = 0;
        for (int i = 1; i < TAMANIOS.length; i++) {
            if (Math.abs(TAMANIOS[i] - alto) < Math.abs(TAMANIOS[mejor] - alto)) {
                mejor = i;
            }
        }
        fuente = familia[mejor];
        fuente.getData().setScale(alto / TAMANIOS[mejor]);
        return fuente;
    }

    private BitmapFont elegir(float escala) {
        return elegir(escala, escala >= ESCALA_TITULO);
    }

    public void setAudio(RecursosAudio audio) {
        this.audio = audio;
    }

    private void sonarHover(Object id, boolean resaltado) {
        if (audio == null) {
            return;
        }
        if (resaltado && !id.equals(idResaltado)) {
            audio.reproducir(audio.boton, VOLUMEN_INTERFAZ);
            idResaltado = id;
        } else if (!resaltado && id.equals(idResaltado)) {
            idResaltado = null;
        }
    }

    public void clic() {
        if (audio != null) {
            audio.reproducir(audio.botonSeleccionado, VOLUMEN_INTERFAZ);
        }
    }

    public void rectangulo(SpriteBatch batch, Rectangle area, Color color) {
        batch.setColor(color);
        batch.draw(pixel, area.x, area.y, area.width, area.height);
        batch.setColor(Color.WHITE);
    }

    public void boton(SpriteBatch batch, Rectangle area, boolean resaltado, String texto, float escala) {
        boton(batch, area, resaltado, texto, escala, area);
    }

    public void boton(SpriteBatch batch, Rectangle area, boolean resaltado, String texto, float escala,
                      Object id) {
        sonarHover(id, resaltado);
        rectangulo(batch, area, resaltado ? FONDO_BOTON_RESALTADO : FONDO_BOTON);
        rectangulo(batch, rectLinea.set(area.x, area.y, area.width, GROSOR_LINEA_BOTON),
            resaltado ? COLOR_LINEA_BOTON_RESALTADO : COLOR_LINEA_BOTON);
        boolean conTitulo = escala * ESCALA_TEXTO_BOTON >= ESCALA_MINIMA_TITULO;
        textoCentrado(batch, area, texto, conTitulo ? escala * ESCALA_TEXTO_BOTON : escala, Color.WHITE, conTitulo);
    }

    public void textoCentrado(SpriteBatch batch, Rectangle area, String texto, float escala, Color color) {
        textoCentrado(batch, area, texto, escala, color, escala >= ESCALA_TITULO);
    }

    private void textoCentrado(SpriteBatch batch, Rectangle area, String texto, float escala, Color color, boolean esTitulo) {
        elegir(escala, esTitulo);
        fuente.setColor(color);
        layout.setText(fuente, texto);
        float x = area.x + (area.width - layout.width) / 2f;
        float y = area.y + (area.height + layout.height) / 2f;
        fuente.draw(batch, layout, x, y);
    }

    public void texto(SpriteBatch batch, String texto, float x, float yArriba, float escala, Color color) {
        elegir(escala);
        fuente.setColor(color);
        layout.setText(fuente, texto);
        fuente.draw(batch, layout, x, yArriba);
    }

    public float anchoTexto(String texto, float escala) {
        elegir(escala);
        layout.setText(fuente, texto);
        return layout.width;
    }

    public void dispose() {
        pixel.dispose();
        for (int i = 0; i < TAMANIOS.length; i++) {
            titulo[i].dispose();
            mono[i].dispose();
        }
    }
}
