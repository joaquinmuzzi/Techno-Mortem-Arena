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
import java.util.function.Supplier;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;

/**
 * Ronda de duelo: arma la arena, los dos duelistas y corre el bucle de juego.
 *
 * <p>ESC abre el menu de pausa encima de la ronda, con opciones para volver al menu principal, a la
 * seleccion de mapa o cerrar el juego.
 *
 * <p>Extiende {@link ScreenAdapter} en lugar de implementar {@code Screen} para no tener que
 * escribir los metodos del ciclo de vida que todavia no se usan ({@code pause}, {@code resume},
 * {@code hide}).
 */
public class PantallaArena extends ScreenAdapter {

    private static final float DELTA_MAXIMO = 1f / 30f;
    private static final float ANCHO_UI = 960f;
    private static final float ALTO_UI = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color TINTE_UNO = Jugador.TINTE_UNO;
    private static final Color TINTE_DOS = Jugador.TINTE_DOS;
    private static final Color VELO_PAUSA = new Color(0f, 0f, 0f, 0.6f);

    private final RecursosGraficos recursos;
    private final Arena arena;
    private final Supplier<Screen> alReiniciar;
    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Viewport viewportUi = new FitViewport(ANCHO_UI, ALTO_UI, new OrthographicCamera());
    private final Ui ui = new Ui();
    private final Jugador jugadorUno;
    private final Jugador jugadorDos;

    private boolean pausado;
    private final Rectangle botonSeguir = new Rectangle(330f, 320f, 300f, 50f);
    private final Rectangle botonMenu = new Rectangle(330f, 250f, 300f, 50f);
    private final Rectangle botonSeleccion = new Rectangle(330f, 180f, 300f, 50f);
    private final Rectangle botonSalir = new Rectangle(330f, 110f, 300f, 50f);

    public PantallaArena(RecursosGraficos recursos, Arena arena) {
        this(recursos, arena, () -> new PantallaArena(recursos, arena));
    }

    /**
     * @param alReiniciar pantalla que se muestra al apretar R. Por defecto reinicia la ronda; el editor la
     *                    cambia para volver a edicion con el mismo mapa.
     */
    public PantallaArena(RecursosGraficos recursos, Arena arena, Supplier<Screen> alReiniciar) {
        this.recursos = recursos;
        this.arena = arena;
        this.alReiniciar = alReiniciar;
        // FitViewport mantiene la relacion de aspecto agregando bandas negras si hace falta, en
        // lugar de estirar la imagen. Asi las distancias del duelo son identicas en cualquier
        // resolucion, que es imprescindible para que el enfrentamiento sea justo.
        this.viewport = new FitViewport(arena.getAncho(), arena.getAlto(), new OrthographicCamera());
        float yPiso = Arena.FILAS_PISO * Arena.CELDA;
        float xPorDefectoUno = 24f * Arena.CELDA;
        float xPorDefectoDos = arena.getAncho() - xPorDefectoUno - Jugador.ANCHO;
        Vector2 spawnUno = arena.getSpawnUno();
        Vector2 spawnDos = arena.getSpawnDos();
        Vector2 pisoUno = spawnUno != null ? spawnUno : new Vector2(xPorDefectoUno, yPiso);
        Vector2 pisoDos = spawnDos != null ? spawnDos : new Vector2(xPorDefectoDos, yPiso);
        this.jugadorUno = new Jugador(Controles.jugadorUno(), pisoUno.x, pisoUno.y, true, TINTE_UNO, 3);
        this.jugadorDos = new Jugador(Controles.jugadorDos(), pisoDos.x, pisoDos.y, false, TINTE_DOS, 3);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
        }
        if (pausado) {
            atenderPausa();
            dibujar();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Cortar aca todo el fotograma: reiniciar() ya libero el SpriteBatch de esta instancia
            // (ver dispose() mas abajo), asi que llamar a dibujar() despues crashearia.
            reiniciar();
            return;
        }
        actualizar(delta);
        dibujar();
    }

    private void actualizar(float delta) {
        // Un delta grande (por ejemplo al arrastrar la ventana, que congela el bucle) haria que los
        // jugadores se teletransporten y atraviesen el piso. Acotarlo degrada la simulacion en lugar
        // de romperla.
        float paso = Math.min(delta, DELTA_MAXIMO);
        jugadorUno.actualizar(paso, arena, jugadorDos);
        jugadorDos.actualizar(paso, arena, jugadorUno);
    }

    private void atenderPausa() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (botonSeguir.contains(mouse)) {
            pausado = false;
        } else if (botonMenu.contains(mouse)) {
            cambiarA(new PantallaMenu(recursos));
        } else if (botonSeleccion.contains(mouse)) {
            cambiarA(new PantallaSeleccionMapa(recursos));
        } else if (botonSalir.contains(mouse)) {
            Gdx.app.exit();
        }
    }

    /**
     * Vuelve a armar la ronda desde cero, en el mismo mapa: arma una {@code PantallaArena} nueva y la
     * reemplaza en el {@link Game}.
     *
     * <p>{@code setScreen} no libera la pantalla anterior (solo le llama {@code hide()}), asi que hay
     * que disponer el {@code SpriteBatch} de esta instancia a mano, o cada reinicio dejaria uno sin
     * liberar.
     */
    private void reiniciar() {
        cambiarA(alReiniciar.get());
    }

    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    private void dibujar() {
        ScreenUtils.clear(FONDO);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarBloques();
        jugadorUno.dibujar(batch, recursos);
        jugadorDos.dibujar(batch, recursos);
        batch.end();

        if (pausado) {
            dibujarPausa();
        }
    }

    private void dibujarPausa() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        batch.begin();
        ui.rectangulo(batch, new Rectangle(0f, 0f, ANCHO_UI, ALTO_UI), VELO_PAUSA);
        ui.boton(batch, botonSeguir, botonSeguir.contains(mouse), "SEGUIR", 1.5f);
        ui.boton(batch, botonMenu, botonMenu.contains(mouse), "MENU PRINCIPAL", 1.3f);
        ui.boton(batch, botonSeleccion, botonSeleccion.contains(mouse), "SELECCION DE MAPA", 1.3f);
        ui.boton(batch, botonSalir, botonSalir.contains(mouse), "SALIR", 1.5f);
        String titulo = "PAUSA";
        float x = (ANCHO_UI - ui.anchoTexto(titulo, 2.5f)) / 2f;
        ui.texto(batch, titulo, x, 470f, 2.5f, Color.WHITE);
        batch.end();
    }

    private void dibujarBloques() {
        for (Bloque bloque : arena.getBloques()) {
            Rectangle caja = bloque.getArea();
            batch.setColor(bloque.getColor());
            batch.draw(recursos.piso, caja.x, caja.y, caja.width, caja.height);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        viewportUi.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        ui.dispose();
    }
}
