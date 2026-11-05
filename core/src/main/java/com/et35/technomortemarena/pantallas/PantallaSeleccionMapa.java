package com.et35.technomortemarena.pantallas;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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

import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.niveles.MapaGuardado;
import com.et35.technomortemarena.niveles.RepositorioMapas;

/**
 * Menu de seleccion de mapa, entre el menu principal y la ronda.
 *
 * <p>Esta en dos columnas: a la izquierda los mapas predefinidos (teclas 1 a 5) y a la derecha los
 * mapas guardados por el editor ("MIS MAPAS"), que se listan desde {@link RepositorioMapas}, con una
 * barra de desplazamiento cuando no entran todos. Asi
 * el editor deja de ser una isla: los niveles que arma el jugador se juegan desde el flujo normal.
 *
 * <p>Cada boton arma una {@link Arena} distinta y se la pasa a {@link PantallaArena}. Para agregar
 * un mapa predefinido alcanza con sumar un boton aca y un metodo de fabrica en {@link Arena}.
 */
public class PantallaSeleccionMapa extends ScreenAdapter {

    private static final float ANCHO_MUNDO = 960f;
    private static final float ALTO_MUNDO = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);

    private static final float ANCHO_BOTON = 300f;
    private static final float ALTO_BOTON = 46f;
    private static final float SEPARACION_BOTON = 56f;
    /**
     * La columna de la derecha ("MIS MAPAS") tiene filas mas bajas y texto mas chico, para que entren mas,
     * y una barra de desplazamiento cuando hay mas de las que entran.
     */
    private static final int MAPAS_VISIBLES = 7;
    private static final float X_LISTA = 520f;
    private static final float ALTO_FILA = 32f;
    private static final float PASO_FILA = 40f;
    /** Borde de abajo de la primera fila (la de arriba). */
    private static final float Y_PRIMERA_FILA = 384f;
    private static final float ESCALA_NOMBRE = 1.0f;
    private static final float ANCHO_BARRA = 12f;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewport = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, new OrthographicCamera());

    private final Rectangle botonArenaClasica = new Rectangle(140f, 400f, ANCHO_BOTON, ALTO_BOTON);
    private final Rectangle botonArenaAgujero = new Rectangle(140f, 344f, ANCHO_BOTON, ALTO_BOTON);
    private final Rectangle botonArenaPlataformas = new Rectangle(140f, 288f, ANCHO_BOTON, ALTO_BOTON);
    private final Rectangle botonArenaTrampolin = new Rectangle(140f, 232f, ANCHO_BOTON, ALTO_BOTON);
    private final Rectangle botonArenaCintas = new Rectangle(140f, 176f, ANCHO_BOTON, ALTO_BOTON);
    private final Rectangle botonVolver = new Rectangle(ANCHO_MUNDO / 2f - 150f, 40f, 300f, 50f);
    /** Toda la columna de los mapas guardados: con el mouse encima, la rueda la desplaza. */
    private final Rectangle areaLista = new Rectangle(X_LISTA, Y_PRIMERA_FILA - (MAPAS_VISIBLES - 1) * PASO_FILA,
        ANCHO_BOTON + 8f + ANCHO_BARRA, (MAPAS_VISIBLES - 1) * PASO_FILA + ALTO_FILA);
    private final BarraDeDesplazamiento barra = new BarraDeDesplazamiento(X_LISTA + ANCHO_BOTON + 8f,
        Y_PRIMERA_FILA - (MAPAS_VISIBLES - 1) * PASO_FILA, ANCHO_BARRA, (MAPAS_VISIBLES - 1) * PASO_FILA + ALTO_FILA);
    /** Rectangulo de trabajo para cada fila, que se arma de cero en cada fotograma. */
    private final Rectangle fila = new Rectangle();
    /** Filas que movio la rueda del mouse desde el ultimo fotograma. */
    private int ruedaPendiente;
    /** La rueda llega como evento, no se puede preguntar en el fotograma: la junta este procesador. */
    private final InputAdapter entrada = new InputAdapter() {
        @Override
        public boolean scrolled(float desplazamientoX, float desplazamientoY) {
            ruedaPendiente += desplazamientoY > 0 ? 1 : -1;
            return true;
        }
    };

    /** Mapas guardados por el editor, en el mismo orden en que los devuelve el repositorio. */
    private final List<MapaGuardado> mapasGuardados;

    /** Pantalla a la que se pasa con el mapa elegido, y a la que se vuelve con ESC o el boton Volver. */
    private final Function<Arena, Screen> alElegir;
    private final Supplier<Screen> alVolver;

    public PantallaSeleccionMapa(RecursosGraficos recursos, RecursosAudio audio) {
        this(recursos, audio,
            arena -> new PantallaArena(recursos, audio, arena),
            () -> new PantallaMenu(recursos, audio));
    }

    /**
     * @param alElegir pantalla que se muestra con el mapa elegido; la de multijugador abre la sala
     * @param alVolver pantalla a la que se vuelve sin elegir
     */
    public PantallaSeleccionMapa(RecursosGraficos recursos, RecursosAudio audio,
                                 Function<Arena, Screen> alElegir, Supplier<Screen> alVolver) {
        this.recursos = recursos;
        this.audio = audio;
        this.alElegir = alElegir;
        this.alVolver = alVolver;
        ui.setAudio(audio);
        this.mapasGuardados = RepositorioMapas.cargarTodos();
        barra.setContenido(mapasGuardados.size(), MAPAS_VISIBLES);
        Gdx.input.setInputProcessor(entrada);
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

        boolean usoLaBarra = actualizarScroll(mouse);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || (Gdx.input.justTouched() && sobreClasica)) {
            ui.clic();
            jugarEn(Arena.porDefecto());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || (Gdx.input.justTouched() && sobreAgujero)) {
            ui.clic();
            jugarEn(Arena.conAgujero());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || (Gdx.input.justTouched() && sobrePlataformas)) {
            ui.clic();
            jugarEn(Arena.conPlataformas());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) || (Gdx.input.justTouched() && sobreTrampolin)) {
            ui.clic();
            jugarEn(Arena.conTrampolin());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) || (Gdx.input.justTouched() && sobreCintas)) {
            ui.clic();
            jugarEn(Arena.conCintasYPinchos());
            return;
        }
        int indiceGuardado = usoLaBarra ? -1 : indiceMapaGuardadoEn(mouse);
        if (Gdx.input.justTouched() && indiceGuardado >= 0) {
            ui.clic();
            jugarEn(Arena.desdeGrilla(mapasGuardados.get(indiceGuardado).getFilas()));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreVolver)) {
            ui.clic();
            cambiarA(alVolver.get());
            return;
        }

        dibujar(sobreClasica, sobreAgujero, sobrePlataformas, sobreTrampolin, sobreCintas, sobreVolver,
            indiceGuardado, mouse);
    }

    /**
     * Mueve la lista de mapas guardados con la rueda del mouse (si esta encima de la lista) o con la barra.
     *
     * @return {@code true} si el mouse lo uso la barra: ese clic no elige ningun mapa
     */
    private boolean actualizarScroll(Vector2 mouse) {
        if (ruedaPendiente != 0 && areaLista.contains(mouse)) {
            barra.desplazar(ruedaPendiente);
        }
        ruedaPendiente = 0;
        return barra.actualizar(mouse, Gdx.input.isButtonPressed(Input.Buttons.LEFT), Gdx.input.justTouched());
    }

    /** El rectangulo de la fila visible numero {@code numero} (0 es la de arriba). */
    private Rectangle filaVisible(int numero) {
        return fila.set(X_LISTA, Y_PRIMERA_FILA - numero * PASO_FILA, ANCHO_BOTON, ALTO_FILA);
    }

    /**
     * @return el indice del mapa guardado bajo el mouse, o {@code -1} si no hay ninguno
     */
    private int indiceMapaGuardadoEn(Vector2 mouse) {
        for (int numero = 0; numero < MAPAS_VISIBLES; numero++) {
            int indice = barra.getPrimero() + numero;
            if (indice < mapasGuardados.size() && filaVisible(numero).contains(mouse)) {
                return indice;
            }
        }
        return -1;
    }

    private void jugarEn(Arena arena) {
        cambiarA(alElegir.apply(arena));
    }

    /** Igual que en el menu principal: {@code setScreen} no libera esta pantalla, hay que disponerla a mano. */
    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    private void dibujar(boolean sobreClasica, boolean sobreAgujero, boolean sobrePlataformas,
                         boolean sobreTrampolin, boolean sobreCintas, boolean sobreVolver,
                         int indiceResaltado, Vector2 mouse) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarPredefinidos(sobreClasica, sobreAgujero, sobrePlataformas, sobreTrampolin, sobreCintas);
        dibujarMapasGuardados(indiceResaltado, mouse);
        ui.boton(batch, botonVolver, sobreVolver, "VOLVER", 1.3f);
        String titulo = "ELEGI EL MAPA";
        dibujarTextoCentrado(titulo, 2f, ALTO_MUNDO - 60f, Color.WHITE);
        batch.end();
    }

    private void dibujarPredefinidos(boolean sobreClasica, boolean sobreAgujero, boolean sobrePlataformas,
                                     boolean sobreTrampolin, boolean sobreCintas) {
        String columna = "PREDEFINIDOS";
        // El encabezado se centra sobre su columna (290), no sobre toda la pantalla.
        float centroColumna = 290f - ui.anchoTexto(columna, 1.25f) / 2f;
        ui.texto(batch, columna, centroColumna, ALTO_MUNDO - 100f, 1.25f, Color.LIGHT_GRAY);
        ui.boton(batch, botonArenaClasica, sobreClasica, "1 - ARENA CLASICA", 1.3f);
        ui.boton(batch, botonArenaAgujero, sobreAgujero, "2 - ARENA CON AGUJERO", 1.3f);
        ui.boton(batch, botonArenaPlataformas, sobrePlataformas, "3 - ESCALERA", 1.3f);
        ui.boton(batch, botonArenaTrampolin, sobreTrampolin, "4 - TRAMPOLIN", 1.3f);
        ui.boton(batch, botonArenaCintas, sobreCintas, "5 - CINTAS Y PINCHOS", 1.3f);
    }

    /**
     * Lista los mapas guardados por el editor, con la rueda del mouse o la barra de la derecha para ver el
     * resto. Si no hay ninguno avisa, para que el jugador sepa que tiene que crearlos en el editor.
     */
    private void dibujarMapasGuardados(int indiceResaltado, Vector2 mouse) {
        String columna = "MIS MAPAS";
        float yColumna = ALTO_MUNDO - 100f;
        ui.texto(batch, columna, 670f - ui.anchoTexto(columna, 1.25f) / 2f, yColumna, 1.25f, Color.LIGHT_GRAY);
        if (mapasGuardados.isEmpty()) {
            String vacio = "SIN MAPAS GUARDADOS";
            ui.texto(batch, vacio, 670f - ui.anchoTexto(vacio, 1.3f) / 2f, 360f, 1.3f, Color.WHITE);
            String ayuda = "CREALOS EN EL EDITOR";
            ui.texto(batch, ayuda, 670f - ui.anchoTexto(ayuda, 1.1f) / 2f, 320f, 1.1f, Color.LIGHT_GRAY);
            return;
        }
        for (int numero = 0; numero < MAPAS_VISIBLES; numero++) {
            int indice = barra.getPrimero() + numero;
            if (indice >= mapasGuardados.size()) {
                break;
            }
            // El nombre se corta con puntos si es mas ancho que el boton, para no salirse del rectangulo.
            String nombre = nombreAcotado(mapasGuardados.get(indice).getNombre());
            // Las filas comparten el mismo Rectangle, por eso la identidad del sonido es el indice del mapa.
            ui.boton(batch, filaVisible(numero), indice == indiceResaltado, nombre, ESCALA_NOMBRE, indice);
        }
        barra.dibujar(batch, ui, mouse);
    }

    /**
     * El nombre del mapa tal cual entra al archivo de guardado, pero recortado para que entre en el
     * boton: el nombre lo escribe el jugador y puede ser cualquier cosa.
     */
    private String nombreAcotado(String nombre) {
        if (ui.anchoTexto(nombre, ESCALA_NOMBRE) <= ANCHO_BOTON - 20f) {
            return nombre;
        }
        String recortado = nombre;
        while (!recortado.isEmpty() && ui.anchoTexto(recortado + "...", ESCALA_NOMBRE) > ANCHO_BOTON - 20f) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }
        return recortado + "...";
    }

    /** Texto con el borde superior izquierdo centrado sobre {@code centroX}. */
    private void dibujarTextoCentrado(String texto, float escala, float yArriba, Color color) {
        ui.texto(batch, texto, (ANCHO_MUNDO - ui.anchoTexto(texto, escala)) / 2f, yArriba, escala, color);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        // Solo si sigue siendo el nuestro: la pantalla siguiente ya puede haber puesto el suyo.
        if (Gdx.input.getInputProcessor() == entrada) {
            Gdx.input.setInputProcessor(null);
        }
        batch.dispose();
        ui.dispose();
    }
}
