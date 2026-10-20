package com.et35.technomortemarena.pantallas;

import java.util.List;

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

import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.niveles.MapaGuardado;
import com.et35.technomortemarena.niveles.RepositorioMapas;

/**
 * Menu de seleccion de mapa, entre el menu principal y la ronda.
 *
 * <p>Esta en dos columnas: a la izquierda los mapas predefinidos (teclas 1 a 5) y a la derecha los
 * mapas guardados por el editor ("MIS MAPAS"), que se listan desde {@link RepositorioMapas}. Asi
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
    /** Cantidad de mapas de la columna de la derecha que entran en pantalla a la vez. */
    private static final int MAPAS_VISIBLES = 5;

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
    /** Area de clic de cada fila de la columna derecha; se recoloca por fotograma segun el scroll. */
    private final Rectangle areaMapaGuardado = new Rectangle(520f, 400f, ANCHO_BOTON, ALTO_BOTON);

    /** Mapas guardados por el editor, en el mismo orden en que los devuelve el repositorio. */
    private final List<MapaGuardado> mapasGuardados;
    /** Primera fila de {@link #mapasGuardados} visible en pantalla; se mueve con la rueda del mouse. */
    private int desplazamientoGuardados;

    public PantallaSeleccionMapa(RecursosGraficos recursos, RecursosAudio audio) {
        this.recursos = recursos;
        this.audio = audio;
        this.mapasGuardados = RepositorioMapas.cargarTodos();
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

        actualizarScroll();

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
        int indiceGuardado = indiceMapaGuardadoEn(mouse);
        if (Gdx.input.justTouched() && indiceGuardado >= 0) {
            jugarEn(Arena.desdeGrilla(mapasGuardados.get(indiceGuardado).getFilas()));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && sobreVolver)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return;
        }

        dibujar(sobreClasica, sobreAgujero, sobrePlataformas, sobreTrampolin, sobreCintas, sobreVolver,
            indiceGuardadoEn(indiceGuardado));
    }

    /**
     * Mueve la lista de mapas guardados con la rueda del mouse, pero solo cuando hay mas mapas que
     * filas en pantalla. {@code getDeltaY()} es positivo al subir la rueda.
     */
    private void actualizarScroll() {
        int desplazamientoMaximo = Math.max(0, mapasGuardados.size() - MAPAS_VISIBLES);
        float rueda = Gdx.input.getDeltaY();
        if (rueda > 0f) {
            desplazamientoGuardados = Math.max(0, desplazamientoGuardados - 1);
        } else if (rueda < 0f) {
            desplazamientoGuardados = Math.min(desplazamientoMaximo, desplazamientoGuardados + 1);
        }
    }

    /**
     * @return el indice del mapa guardado bajo el mouse, o {@code -1} si no hay ninguno
     */
    private int indiceMapaGuardadoEn(Vector2 mouse) {
        if (!areaMapaGuardado.contains(mouse)) {
            return -1;
        }
        float y = areaMapaGuardado.y;
        for (int fila = 0; fila < MAPAS_VISIBLES; fila++) {
            if (mouse.y >= y && mouse.y <= y + ALTO_BOTON) {
                int indice = desplazamientoGuardados + fila;
                return indice < mapasGuardados.size() ? indice : -1;
            }
            y -= SEPARACION_BOTON;
        }
        return -1;
    }

    /** Convierte el indice de mapa guardado en el indice de fila visible, o {@code -1}. */
    private int indiceGuardadoEn(int indiceGuardado) {
        return indiceGuardado < 0 ? -1 : indiceGuardado - desplazamientoGuardados;
    }

    private void jugarEn(Arena arena) {
        cambiarA(new PantallaArena(recursos, audio, arena));
    }

    /** Igual que en el menu principal: {@code setScreen} no libera esta pantalla, hay que disponerla a mano. */
    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    private void dibujar(boolean sobreClasica, boolean sobreAgujero, boolean sobrePlataformas,
                         boolean sobreTrampolin, boolean sobreCintas, boolean sobreVolver,
                         int filaGuardadoResaltada) {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarPredefinidos(sobreClasica, sobreAgujero, sobrePlataformas, sobreTrampolin, sobreCintas);
        dibujarMapasGuardados(filaGuardadoResaltada);
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
     * Lista los mapas guardados por el editor, con scroll por la rueda del mouse. Si no hay ninguno
     * avisa, para que el jugador sepa que tiene que crearlos en el editor de niveles.
     */
    private void dibujarMapasGuardados(int filaGuardadoResaltada) {
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
        float y = areaMapaGuardado.y;
        for (int fila = 0; fila < MAPAS_VISIBLES; fila++) {
            int indice = desplazamientoGuardados + fila;
            if (indice >= mapasGuardados.size()) {
                break;
            }
            // El nombre se corta con puntos si es mas ancho que el boton, para no salirse del rectangulo.
            areaMapaGuardado.set(520f, y, ANCHO_BOTON, ALTO_BOTON);
            String nombre = nombreAcotado(mapasGuardados.get(indice).getNombre());
            ui.boton(batch, areaMapaGuardado, fila == filaGuardadoResaltada, nombre, 1.3f);
            y -= SEPARACION_BOTON;
        }
        if (mapasGuardados.size() > MAPAS_VISIBLES) {
            String pista = "RUEDA DEL MOUSE PARA VER MAS";
            ui.texto(batch, pista, 670f - ui.anchoTexto(pista, 0.9f) / 2f, 130f, 0.9f, Color.LIGHT_GRAY);
        }
    }

    /**
     * El nombre del mapa tal cual entra al archivo de guardado, pero recortado para que entre en el
     * boton: el nombre lo escribe el jugador y puede ser cualquier cosa.
     */
    private String nombreAcotado(String nombre) {
        if (ui.anchoTexto(nombre, 1.3f) <= ANCHO_BOTON - 20f) {
            return nombre;
        }
        String recortado = nombre;
        while (!recortado.isEmpty() && ui.anchoTexto(recortado + "...", 1.3f) > ANCHO_BOTON - 20f) {
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
        batch.dispose();
        ui.dispose();
    }
}
