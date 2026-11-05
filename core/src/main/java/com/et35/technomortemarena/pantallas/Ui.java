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

/**
 * Dibujo basico de interfaz: rectangulos, botones y texto. Lo usan los menus, la pausa y el editor.
 *
 * <p>Los botones son un pixel blanco de 1x1 estirado y tenido con {@code batch.setColor()}, y el texto usa
 * dos fuentes pixeladas, sin suavizado (Orbitron para titulos y botones, Share Tech Mono para el resto; ver
 * {@code assets/fuentes}). Cada pantalla tiene su propia instancia y la libera al salir.
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
    /**
     * Tamanos en que estan horneadas las fuentes (herramientas/hornear_fuentes.py): una fuente de tamano N tiene
     * la mayuscula del alto que tenia la fuente de 15 px de antes a escala N / 15.
     */
    private static final int[] TAMANIOS = {12, 14, 16, 18, 20, 24, 28, 32, 40, 48};
    /** Desde esta escala el texto es un titulo y va con la fuente de titulos; mas chico, con la monoespaciada. */
    private static final float ESCALA_TITULO = 1.5f;
    /** Alto de referencia: el de la fuente de antes, a la que se refieren todas las escalas. */
    private static final float ALTO_BASE = 15f;
    public static final Color COLOR_LINEA_BOTON = new Color(0.36f, 0.40f, 0.50f, 1f);
    public static final Color COLOR_LINEA_BOTON_RESALTADO = new Color(0.35f, 0.72f, 1f, 1f);
    private static final Color FONDO_BOTON = new Color(0.20f, 0.22f, 0.30f, 0.55f);
    private static final Color FONDO_BOTON_RESALTADO = new Color(0.24f, 0.30f, 0.42f, 0.75f);
    private static final float GROSOR_LINEA_BOTON = 3f;
    /** La fuente de titulos es mas ancha que la de antes: en los botones va un poco mas chica para que entre. */
    private static final float ESCALA_TEXTO_BOTON = 0.85f;
    /** Mas chica que esto, la fuente de titulos ya no se lee bien. */
    private static final float ESCALA_MINIMA_TITULO = 0.95f;

    /** Orbitron en negrita, para titulos y botones. */
    private final BitmapFont[] titulo = new BitmapFont[TAMANIOS.length];
    /** Share Tech Mono, para el resto: el HUD, la ayuda, las listas. */
    private final BitmapFont[] mono = new BitmapFont[TAMANIOS.length];
    private BitmapFont fuente;
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle rectLinea = new Rectangle();

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
        for (int i = 0; i < TAMANIOS.length; i++) {
            titulo[i] = new BitmapFont(Gdx.files.internal("fuentes/titulo-" + TAMANIOS[i] + ".fnt"));
            mono[i] = new BitmapFont(Gdx.files.internal("fuentes/mono-" + TAMANIOS[i] + ".fnt"));
        }
    }

    /**
     * Elige la fuente para una escala (la de los titulos o la monoespaciada) en el tamano horneado mas cercano,
     * y la deja escalada a lo justo. Las letras no tienen suavizado y se dibujan sin filtrar: quedan pixeladas.
     */
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
            audio.reproducir(audio.boton, VOLUMEN_INTERFAZ);
            idResaltado = id;
        } else if (!resaltado && id.equals(idResaltado)) {
            idResaltado = null;
        }
    }

    /** Reproduce el sonido de clic cuando un boton se activa. No hace nada si la pantalla esta en silencio. */
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
        // fondo apenas marcado y una raya abajo, azul cuando el mouse esta encima
        rectangulo(batch, area, resaltado ? FONDO_BOTON_RESALTADO : FONDO_BOTON);
        rectangulo(batch, rectLinea.set(area.x, area.y, area.width, GROSOR_LINEA_BOTON),
            resaltado ? COLOR_LINEA_BOTON_RESALTADO : COLOR_LINEA_BOTON);
        // la de titulos en chico se empasta: los botones de texto chico van con la monoespaciada
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

    /** Texto con el borde superior izquierdo en {@code (x, yArriba)}. */
    public void texto(SpriteBatch batch, String texto, float x, float yArriba, float escala, Color color) {
        elegir(escala);
        fuente.setColor(color);
        layout.setText(fuente, texto);
        fuente.draw(batch, layout, x, yArriba);
    }

    /** Ancho en pixeles del texto a la escala dada, para centrarlo en pantalla. */
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
