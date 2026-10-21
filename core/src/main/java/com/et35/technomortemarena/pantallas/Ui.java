package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import com.et35.technomortemarena.RecursosAudio;

/**
 * Dibujo basico de interfaz: rectangulos, botones y texto. Lo usan los menus, la pausa y el editor.
 *
 * <p>Los botones son un pixel blanco de 1x1 estirado y tenido con {@code batch.setColor()}, y el texto usa
 * la fuente que trae LibGDX incorporada. Cada pantalla tiene su propia instancia y la libera al salir.
 *
 * <p>Los sonidos de interfaz (pasar por encima y hacer clic) se disparan desde aca para no repetir la
 * logica en cada pantalla: la pantalla solo pasa su {@link RecursosAudio} con {@link #setAudio} y, si
 * no lo hace, la interfaz queda en silencio (es lo que usa el editor de niveles).
 */
public class Ui {

    public static final Color COLOR_BOTON = new Color(0.20f, 0.22f, 0.30f, 1f);
    public static final Color COLOR_BOTON_RESALTADO = new Color(0.32f, 0.36f, 0.50f, 1f);
    public static final Color COLOR_BOTON_SELECCIONADO = new Color(0.55f, 0.45f, 0.15f, 1f);

    /** Volumen de los sonidos de interfaz: son cortos y frecuentes, por eso suenan mas bajo que los del juego. */
    private static final float VOLUMEN_INTERFAZ = 0.6f;

    private final Texture pixel;
    private final BitmapFont fuente = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    /** Sonidos de interfaz; {@code null} cuando la pantalla no debe sonar, como el editor de niveles. */
    private RecursosAudio audio;

    /** Identidad del boton resaltado en el fotograma anterior, para avisar una sola vez cuando el cursor entra en otro. */
    private Object idResaltado;

    public Ui() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    /** Habilita los sonidos de interfaz de esta pantalla. Sin llamarlo, o con {@code null}, no suena nada. */
    public void setAudio(RecursosAudio audio) {
        this.audio = audio;
    }

    /**
     * Reproduce el sonido de pasar por encima, solo cuando el cursor entra en un boton distinto al anterior.
     *
     * <p>{@code id} identifica al boton: dos botones distintos deben tener identificadores distintos, y
     * el mismo boton siempre el mismo. La mayoria de las pantallas pasan su propio {@link Rectangle} (que
     * es unico por boton), pero las filas que reutilizan un rectangulo, como los mapas guardados, pasan
     * una identidad estable por fila.
     */
    private void sonarHover(Object id, boolean resaltado) {
        if (audio == null) {
            return;
        }
        if (resaltado && !id.equals(idResaltado)) {
            audio.boton.play(VOLUMEN_INTERFAZ);
            idResaltado = id;
        } else if (!resaltado && id.equals(idResaltado)) {
            idResaltado = null;
        }
    }

    /** Reproduce el sonido de clic cuando un boton se activa. No hace nada si la pantalla esta en silencio. */
    public void clic() {
        if (audio != null) {
            audio.botonSeleccionado.play(VOLUMEN_INTERFAZ);
        }
    }

    public void rectangulo(SpriteBatch batch, Rectangle area, Color color) {
        batch.setColor(color);
        batch.draw(pixel, area.x, area.y, area.width, area.height);
        batch.setColor(Color.WHITE);
    }

    /** Boton con el texto centrado. {@code resaltado} lo pinta mas claro, por ejemplo al pasar el mouse. */
    public void boton(SpriteBatch batch, Rectangle area, boolean resaltado, String texto, float escala) {
        boton(batch, area, resaltado, texto, escala, area);
    }

    /**
     * Igual que {@link #boton}, pero con una identidad propia para el sonido de pasar por encima.
     *
     * <p>Se usa cuando varias filas comparten el mismo {@link Rectangle}: ahi {@code id} distingue una
     * fila de otra para que el sonido suene al pasar de una a la siguiente.
     */
    public void boton(SpriteBatch batch, Rectangle area, boolean resaltado, String texto, float escala,
                      Object id) {
        sonarHover(id, resaltado);
        rectangulo(batch, area, resaltado ? COLOR_BOTON_RESALTADO : COLOR_BOTON);
        textoCentrado(batch, area, texto, escala, Color.WHITE);
    }

    public void textoCentrado(SpriteBatch batch, Rectangle area, String texto, float escala, Color color) {
        fuente.getData().setScale(escala);
        fuente.setColor(color);
        layout.setText(fuente, texto);
        float x = area.x + (area.width - layout.width) / 2f;
        float y = area.y + (area.height + layout.height) / 2f;
        fuente.draw(batch, layout, x, y);
    }

    /** Texto con el borde superior izquierdo en {@code (x, yArriba)}. */
    public void texto(SpriteBatch batch, String texto, float x, float yArriba, float escala, Color color) {
        fuente.getData().setScale(escala);
        fuente.setColor(color);
        layout.setText(fuente, texto);
        fuente.draw(batch, layout, x, yArriba);
    }

    /** Ancho en pixeles del texto a la escala dada, para centrarlo en pantalla. */
    public float anchoTexto(String texto, float escala) {
        fuente.getData().setScale(escala);
        layout.setText(fuente, texto);
        return layout.width;
    }

    public void dispose() {
        pixel.dispose();
        fuente.dispose();
    }
}
