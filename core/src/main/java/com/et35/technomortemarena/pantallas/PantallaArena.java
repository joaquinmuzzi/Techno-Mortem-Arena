package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.function.Supplier;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import com.et35.technomortemarena.partida.MarcadorPartida;

/**
 * Partida de duelo al mejor de 5 rondas: arma la arena, los dos duelistas y corre el bucle de
 * juego, lleva el marcador y muestra las vidas de cada duelistas en el HUD.
 *
 * <p>Cuando un duelistas queda fuera de combate termina la ronda: se anota el resultado, hay una
 * pausa breve con el cartel y la proxima ronda arranca con los dos de vuelta en sus spawns. El
 * primero en ganar 3 rondas se lleva la partida y aparece un cartel de victoria con revancha. En
 * empate (murieron los dos en el mismo fotograma) la ronda se vuelve a jugar sin sumar nada.
 *
 * <p>ESC abre el menu de pausa encima de la ronda, con opciones para reiniciar la ronda actual,
 * volver al menu principal, a la seleccion de mapa o cerrar el juego.
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

    // Volumen de cada efecto. El de correr es mas bajo porque suena en bucle casi todo el tiempo y
    // taparia el resto; los golpes y aterrizajes son cortos y puntuales.
    private static final float VOLUMEN_CORRER = 0.6f;
    private static final float VOLUMEN_GOLPE = 1.0f;
    private static final float VOLUMEN_ATERRIZAR = 0.8f;

    /** Vidas con las que arranca cada duelistas en cada ronda. */
    private static final int VIDAS_INICIALES = 3;
    /** Segundos entre que termina una ronda y arranca la siguiente, para alcanzar a leer el resultado. */
    private static final float DURACION_ENTRE_RONDAS = 2.5f;

    /** Escalas del HUD: la fila de vidas, el cartel de ronda y el marcador arriba al centro. */
    private static final float ESCALA_RONDA_HUD = 1.2f;
    private static final float ESCALA_MARCADOR_HUD = 1.8f;
    private static final float MARGEN_HUD = 18f;
    private static final float TAMANO_VIDA = 20f;
    private static final float SEPARACION_VIDA = 8f;
    private static final Color COLOR_VIDA_PERDIDA = new Color(0.25f, 0.25f, 0.28f, 1f);

    // Cartel de controles que aparece al empezar la ronda. Se muestran los del Jugador 1 arriba a
    // la izquierda y los del Jugador 2 arriba a la derecha, para que nadie arranque perdido.
    /** Segundos que los controles quedan totalmente visibles antes de empezar a desvanecerse. */
    private static final float DURACION_AYUDA = 7f;
    /** Segundos que tarda el fundido final, al terminar {@link #DURACION_AYUDA}. */
    private static final float DESVANECIDO_AYUDA = 1.5f;
    private static final float MARGEN_AYUDA = 18f;
    private static final float RELLENO_AYUDA = 12f;
    private static final float SEPARACION_TITULO_AYUDA = 26f;
    private static final float INTERLINEA_AYUDA = 22f;
    private static final float ESCALA_TITULO_AYUDA = 1.4f;
    private static final float ESCALA_LINEA_AYUDA = 1.15f;
    private static final Color FONDO_AYUDA = new Color(0f, 0f, 0f, 0.55f);
    private static final String TITULO_AYUDA_UNO = "JUGADOR 1";
    private static final String[] LINEAS_AYUDA_UNO = {
        "MOVER: A / D",
        "SALTAR: W",
        "GUARDIA: E / Q",
        "ESTOCADA: F",
    };
    private static final String TITULO_AYUDA_DOS = "JUGADOR 2";
    private static final String[] LINEAS_AYUDA_DOS = {
        "MOVER: FLECHA IZQ / DER",
        "SALTAR: FLECHA ARRIBA",
        "GUARDIA: O / L",
        "ESTOCADA: K",
    };

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final Arena arena;
    private final Supplier<Screen> alReiniciar;
    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Viewport viewportUi = new FitViewport(ANCHO_UI, ALTO_UI, new OrthographicCamera());
    private final Ui ui = new Ui();
    private final Jugador jugadorUno;
    private final Jugador jugadorDos;

    private boolean pausado;
    /** Verdadero mientras el bucle del sonido de correr esta sonando, para no reiniciarlo cada fotograma. */
    private boolean suenaCorrer;
    /** Tiempo (segundos de juego) transcurrido desde que arranco la ronda, para el cartel de ayuda. */
    private float tiempoAyuda;

    /** Etapa de la partida: duelo activo, espera entre rondas o cartel de victoria. */
    private enum Fase { RONDA, ENTRE_RONDAS, GANADOR }

    private final MarcadorPartida marcador = new MarcadorPartida();
    private Fase fase = Fase.RONDA;
    /** Numero de ronda dentro de la partida, para el HUD y los carteles de resultado. */
    private int numeroRonda = 1;
    /** Cuenta regresiva de {@link #DURACION_ENTRE_RONDAS} antes de arrancar la proxima ronda. */
    private float tiempoEntreRondas;
    /** Duelista que gano la ultima ronda, o {@code null} si murieron los dos a la vez (empate). */
    private Jugador ganadorDeLaRonda;
    /** Posiciones de spawn de cada duelistas, para volver a colocarlos en cada ronda. */
    private final Vector2 posInicialUno;
    private final Vector2 posInicialDos;

    /** Color y rectangulo de trabajo: evitan crear objetos nuevos en cada fotograma del cartel. */
    private final Color colorAux = new Color();
    private final Rectangle rectAux = new Rectangle();
    private final Rectangle botonSeguir = new Rectangle(330f, 390f, 300f, 50f);
    private final Rectangle botonReiniciarRonda = new Rectangle(330f, 320f, 300f, 50f);
    private final Rectangle botonMenu = new Rectangle(330f, 250f, 300f, 50f);
    private final Rectangle botonSeleccion = new Rectangle(330f, 180f, 300f, 50f);
    private final Rectangle botonSalir = new Rectangle(330f, 110f, 300f, 50f);
    /** Cartel de victoria: revancha en el mismo mapa o salida hacia otro menu. */
    private final Rectangle botonRevancha = new Rectangle(330f, 250f, 300f, 50f);
    private final Rectangle botonGanadorSeleccion = new Rectangle(330f, 180f, 300f, 50f);
    private final Rectangle botonGanadorMenu = new Rectangle(330f, 110f, 300f, 50f);

    public PantallaArena(RecursosGraficos recursos, RecursosAudio audio, Arena arena) {
        this(recursos, audio, arena, () -> new PantallaArena(recursos, audio, arena));
    }

    /**
     * @param alReiniciar pantalla que se muestra al apretar R. Por defecto reinicia la ronda; el editor la
     *                    cambia para volver a edicion con el mismo mapa.
     */
    public PantallaArena(RecursosGraficos recursos, RecursosAudio audio, Arena arena, Supplier<Screen> alReiniciar) {
        this.recursos = recursos;
        this.audio = audio;
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
        this.jugadorUno = new Jugador(Controles.jugadorUno(), pisoUno.x, pisoUno.y, true, TINTE_UNO, VIDAS_INICIALES);
        this.jugadorDos = new Jugador(Controles.jugadorDos(), pisoDos.x, pisoDos.y, false, TINTE_DOS, VIDAS_INICIALES);
        this.posInicialUno = new Vector2(pisoUno);
        this.posInicialDos = new Vector2(pisoDos);
    }

    @Override
    public void render(float delta) {
        // ESC pausa solo mientras la ronda esta en juego: entre rondas y en el cartel de victoria
        // los carteles tienen sus propios botones, y superponer dos menues solo confunde.
        if (fase == Fase.RONDA && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
            if (pausado) {
                // Al pausar deja de correr la simulacion, asi que el bucle quedaria sonando para
                // siempre: hay que apagarlo a mano.
                detenerSonidoCorrer();
            }
        }
        if (pausado) {
            if (atenderPausa()) {
                // Se cambio de pantalla: atenderPausa() ya libero el SpriteBatch de esta instancia
                // (ver cambiarA mas abajo), asi que hay que cortar el fotograma sin dibujar, o se
                // dibujaria con recursos ya liberados.
                return;
            }
            dibujar();
            return;
        }
        if (fase == Fase.RONDA) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                // Cortar aca todo el fotograma: reiniciar() ya libero el SpriteBatch de esta instancia
                // (ver dispose() mas abajo), asi que llamar a dibujar() despues crashearia.
                reiniciar();
                return;
            }
            actualizar(delta);
            verificarFinDeRonda();
        } else if (fase == Fase.ENTRE_RONDAS) {
            // La simulacion queda congelada mientras se lee el resultado: solo corre la espera.
            tiempoEntreRondas -= delta;
            if (tiempoEntreRondas <= 0f) {
                avanzarRonda();
            }
        } else if (atenderGanador()) {
            return;
        }
        dibujar();
    }

    private void actualizar(float delta) {
        // Un delta grande (por ejemplo al arrastrar la ventana, que congela el bucle) haria que los
        // jugadores se teletransporten y atraviesen el piso. Acotarlo degrada la simulacion en lugar
        // de romperla.
        float paso = Math.min(delta, DELTA_MAXIMO);
        // El reloj de la ayuda corre con la simulacion, no con la pausa: mientras el juego esta
        // pausado no se llama a actualizar(), asi el cartel se queda quieto y no se pierde.
        tiempoAyuda += paso;
        jugadorUno.actualizar(paso, arena, jugadorDos);
        jugadorDos.actualizar(paso, arena, jugadorUno);
        actualizarSonidos();
    }

    /**
     * Dispara los efectos segun lo que paso en el fotograma.
     *
     * <p>El sonido de correr es un bucle: se enciende la primera vez que algun jugador corre y se
     * apaga cuando ninguno lo hace. Los otros dos son golpes y aterrizajes puntuales, que cada jugador
     * marca con una bandera valida solo en el fotograma en que ocurren.
     */
    private void actualizarSonidos() {
        boolean corriendo = jugadorUno.estaCorriendo() || jugadorDos.estaCorriendo();
        if (corriendo && !suenaCorrer) {
            audio.correr.loop(VOLUMEN_CORRER);
            suenaCorrer = true;
        } else if (!corriendo) {
            detenerSonidoCorrer();
        }
        if (jugadorUno.acabaDeGolpear() || jugadorDos.acabaDeGolpear()) {
            audio.golpe.play(VOLUMEN_GOLPE);
        }
        if (jugadorUno.acabaDeAterrizar() || jugadorDos.acabaDeAterrizar()) {
            audio.aterrizar.play(VOLUMEN_ATERRIZAR);
        }
    }

    /** Apaga el bucle del sonido de correr si estaba sonando. Se usa al pausar y al salir de la ronda. */
    private void detenerSonidoCorrer() {
        if (suenaCorrer) {
            audio.correr.stop();
            suenaCorrer = false;
        }
    }

    /**
     * Atiende los clics del menu de pausa.
     *
     * @return {@code true} si se cambio de pantalla: en ese caso hay que cortar el fotograma sin
     *         dibujar, porque {@link #cambiarA} ya libero el {@code SpriteBatch} de esta instancia.
     */
    private boolean atenderPausa() {
        if (!Gdx.input.justTouched()) {
            return false;
        }
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (botonSeguir.contains(mouse)) {
            pausado = false;
        } else if (botonReiniciarRonda.contains(mouse)) {
            // Reinicia solo la ronda actual: el marcador de la partida no se toca.
            pausado = false;
            reiniciarRondaActual();
        } else if (botonMenu.contains(mouse)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        } else if (botonSeleccion.contains(mouse)) {
            cambiarA(new PantallaSeleccionMapa(recursos, audio));
            return true;
        } else if (botonSalir.contains(mouse)) {
            Gdx.app.exit();
        }
        return false;
    }

    /**
     * Atiende los botones del cartel de victoria: revancha en el mismo mapa o salida hacia otro menu.
     *
     * @return {@code true} si se cambio de pantalla, con el mismo criterio que {@link #atenderPausa}
     */
    private boolean atenderGanador() {
        if (!Gdx.input.justTouched()) {
            return false;
        }
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (botonRevancha.contains(mouse)) {
            marcador.reiniciar();
            numeroRonda = 1;
            reiniciarRondaActual();
            fase = Fase.RONDA;
        } else if (botonGanadorSeleccion.contains(mouse)) {
            cambiarA(new PantallaSeleccionMapa(recursos, audio));
            return true;
        } else if (botonGanadorMenu.contains(mouse)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        }
        return false;
    }

    /**
     * Cierra la ronda cuando alguien queda fuera de combate: anota el resultado en el marcador y
     * pasa a la espera entre rondas, o al cartel de victoria si esa ronda definio la partida.
     *
     * <p>Un empate (murieron los dos en el mismo fotograma) no suma rondas: la misma ronda se
     * vuelve a jugar.
     */
    private void verificarFinDeRonda() {
        if (jugadorUno.isVivo() && jugadorDos.isVivo()) {
            return;
        }
        detenerSonidoCorrer();
        if (jugadorUno.isVivo()) {
            ganadorDeLaRonda = jugadorUno;
            marcador.registrarRondaGanada(true);
        } else if (jugadorDos.isVivo()) {
            ganadorDeLaRonda = jugadorDos;
            marcador.registrarRondaGanada(false);
        } else {
            ganadorDeLaRonda = null;
        }
        if (marcador.hayGanador()) {
            fase = Fase.GANADOR;
        } else {
            fase = Fase.ENTRE_RONDAS;
            tiempoEntreRondas = DURACION_ENTRE_RONDAS;
        }
    }

    /** Pasa a la proxima ronda: los duelistas vuelven a sus spawns y el marcador queda intacto. */
    private void avanzarRonda() {
        numeroRonda++;
        reiniciarRondaActual();
        fase = Fase.RONDA;
    }

    /** Devuelve a los dos duelistas a su spawn, con las vidas completas, para jugar la ronda que sigue. */
    private void reiniciarRondaActual() {
        jugadorUno.reiniciar(posInicialUno.x, posInicialUno.y, true);
        jugadorDos.reiniciar(posInicialDos.x, posInicialDos.y, false);
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

        dibujarAyudaControles();
        dibujarHud();

        if (fase == Fase.ENTRE_RONDAS) {
            dibujarEntreRondas();
        } else if (fase == Fase.GANADOR) {
            dibujarGanador();
        }

        if (pausado) {
            dibujarPausa();
        }
    }

    /**
     * Dibuja las vidas de cada duelistas en sus esquinas y el marcador de la partida arriba al
     * centro, con el viewport de interfaz para que queden ancladas a la pantalla.
     */
    private void dibujarHud() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        dibujarVidas(jugadorUno, TINTE_UNO, MARGEN_HUD, false);
        dibujarVidas(jugadorDos, TINTE_DOS, ANCHO_UI - MARGEN_HUD, true);
        String ronda = "RONDA " + numeroRonda;
        dibujarTextoCentrado(ronda, ESCALA_RONDA_HUD, ALTO_UI - MARGEN_HUD, Color.WHITE);
        String marcadorTexto = marcador.getRondasGanadasUno() + " - " + marcador.getRondasGanadasDos();
        dibujarTextoCentrado(marcadorTexto, ESCALA_MARCADOR_HUD, ALTO_UI - MARGEN_HUD - 20f, Color.WHITE);
        batch.end();
    }

    /**
     * Fila de vidas de un duelistas: un cuadrado de su color por vida restante y otro apagado por
     * cada vida perdida. La fila se ancla a la esquina indicada para que no se pisen las dos.
     *
     * @param ancladaALaDerecha la fila crece hacia la izquierda, pegada al borde derecho
     */
    private void dibujarVidas(Jugador jugador, Color tinte, float xAncla, boolean ancladaALaDerecha) {
        // Un duelistas muerto muestra las vidas vacias: el contador es "lo que queda por perder".
        int vidas = jugador.isVivo() ? jugador.getVida() : 0;
        for (int i = 0; i < VIDAS_INICIALES; i++) {
            float x = ancladaALaDerecha
                ? xAncla - TAMANO_VIDA - i * (TAMANO_VIDA + SEPARACION_VIDA)
                : xAncla + i * (TAMANO_VIDA + SEPARACION_VIDA);
            rectAux.set(x, ALTO_UI - MARGEN_HUD - TAMANO_VIDA, TAMANO_VIDA, TAMANO_VIDA);
            ui.rectangulo(batch, rectAux, i < vidas ? tinte : COLOR_VIDA_PERDIDA);
        }
    }

    /** Cartel entre rondas: quien gano la ronda que acabo de terminar, o el aviso de empate. */
    private void dibujarEntreRondas() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        ui.rectangulo(batch, rectAux.set(0f, 0f, ANCHO_UI, ALTO_UI), VELO_PAUSA);
        if (ganadorDeLaRonda == null) {
            dibujarTextoCentrado("EMPATE", 3f, 400f, Color.WHITE);
            dibujarTextoCentrado("LA RONDA SE VUELVE A JUGAR", 1.4f, 350f, Color.LIGHT_GRAY);
        } else {
            boolean ganoElUno = ganadorDeLaRonda == jugadorUno;
            dibujarTextoCentrado(ganoElUno ? "JUGADOR 1" : "JUGADOR 2", 3f, 400f,
                ganoElUno ? TINTE_UNO : TINTE_DOS);
            dibujarTextoCentrado("GANA LA RONDA " + numeroRonda, 1.4f, 350f, Color.WHITE);
        }
        dibujarTextoCentrado("PROXIMA RONDA...", 1.1f, 260f, Color.LIGHT_GRAY);
        batch.end();
    }

    /** Cartel de victoria: anuncia al ganador de la partida y ofrece revancha o salir. */
    private void dibujarGanador() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean ganoElUno = marcador.ganoJugadorUno();
        batch.begin();
        ui.rectangulo(batch, rectAux.set(0f, 0f, ANCHO_UI, ALTO_UI), VELO_PAUSA);
        dibujarTextoCentrado("GANADOR DE LA PARTIDA", 1.6f, 440f, Color.WHITE);
        dibujarTextoCentrado(ganoElUno ? "JUGADOR 1" : "JUGADOR 2", 3f, 380f,
            ganoElUno ? TINTE_UNO : TINTE_DOS);
        dibujarTextoCentrado(marcador.getRondasGanadasUno() + " - " + marcador.getRondasGanadasDos(),
            1.6f, 325f, Color.WHITE);
        ui.boton(batch, botonRevancha, botonRevancha.contains(mouse), "REVANCHA", 1.5f);
        ui.boton(batch, botonGanadorSeleccion, botonGanadorSeleccion.contains(mouse), "SELECCION DE MAPA", 1.3f);
        ui.boton(batch, botonGanadorMenu, botonGanadorMenu.contains(mouse), "MENU PRINCIPAL", 1.3f);
        batch.end();
    }

    /** Linea de texto centrada en el ancho de la interfaz, para los carteles de resultado. */
    private void dibujarTextoCentrado(String texto, float escala, float yArriba, Color color) {
        ui.texto(batch, texto, (ANCHO_UI - ui.anchoTexto(texto, escala)) / 2f, yArriba, escala, color);
    }

    /**
     * Dibuja el cartel de controles de los dos jugadores mientras no se haya terminado de desvanecer.
     *
     * <p>Se dibuja con el viewport de interfaz ({@link #viewportUi}) para que las esquinas queden
     * ancladas a la pantalla y no se muevan con la camara de la arena.
     */
    private void dibujarAyudaControles() {
        float alfa = alfaAyuda();
        if (alfa <= 0f) {
            return;
        }
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        dibujarPanelAyuda(TITULO_AYUDA_UNO, LINEAS_AYUDA_UNO, TINTE_UNO, false, alfa);
        dibujarPanelAyuda(TITULO_AYUDA_DOS, LINEAS_AYUDA_DOS, TINTE_DOS, true, alfa);
        batch.end();
    }

    /**
     * Opacidad del cartel: entero mientras dura {@link #DURACION_AYUDA}, y despues baja de 1 a 0 en
     * {@link #DESVANECIDO_AYUDA} segundos. Devuelve 0 cuando ya no hay que dibujarlo.
     */
    private float alfaAyuda() {
        if (tiempoAyuda < DURACION_AYUDA) {
            return 1f;
        }
        float restante = DURACION_AYUDA + DESVANECIDO_AYUDA - tiempoAyuda;
        if (restante <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(restante / DESVANECIDO_AYUDA, 0f, 1f);
    }

    /**
     * Dibuja un panel de controles: fondo semitransparente, titulo con el color del jugador y una
     * linea por accion. El ancho se calcula segun el texto mas largo para que el fondo quede ajustado.
     *
     * @param aLaDerecha ancla el panel contra el borde derecho de la pantalla (Jugador 2); si es
     *                   {@code false}, lo ancla contra el izquierdo (Jugador 1)
     * @param alfa       opacidad global, para el fundido de salida
     */
    private void dibujarPanelAyuda(String titulo, String[] lineas, Color colorTitulo,
                                   boolean aLaDerecha, float alfa) {
        float anchoContenido = ui.anchoTexto(titulo, ESCALA_TITULO_AYUDA);
        for (String linea : lineas) {
            anchoContenido = Math.max(anchoContenido, ui.anchoTexto(linea, ESCALA_LINEA_AYUDA));
        }
        float anchoPanel = anchoContenido + 2f * RELLENO_AYUDA;
        float altoPanel = 2f * RELLENO_AYUDA + SEPARACION_TITULO_AYUDA + lineas.length * INTERLINEA_AYUDA;
        float x = aLaDerecha ? ANCHO_UI - MARGEN_AYUDA - anchoPanel : MARGEN_AYUDA;
        float arriba = ALTO_UI - MARGEN_AYUDA;
        float abajo = arriba - altoPanel;

        colorAux.set(FONDO_AYUDA);
        colorAux.a *= alfa;
        rectAux.set(x, abajo, anchoPanel, altoPanel);
        ui.rectangulo(batch, rectAux, colorAux);

        ui.texto(batch, titulo, x + RELLENO_AYUDA, arriba - RELLENO_AYUDA,
            ESCALA_TITULO_AYUDA, conAlfa(colorTitulo, alfa));

        float yLinea = arriba - RELLENO_AYUDA - SEPARACION_TITULO_AYUDA;
        for (String linea : lineas) {
            ui.texto(batch, linea, x + RELLENO_AYUDA, yLinea, ESCALA_LINEA_AYUDA,
                conAlfa(Color.WHITE, alfa));
            yLinea -= INTERLINEA_AYUDA;
        }
    }

    /** Copia {@code base} en {@link #colorAux} con su opacidad multiplicada por {@code alfa}. */
    private Color conAlfa(Color base, float alfa) {
        colorAux.set(base);
        colorAux.a *= alfa;
        return colorAux;
    }

    private void dibujarPausa() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        batch.begin();
        ui.rectangulo(batch, new Rectangle(0f, 0f, ANCHO_UI, ALTO_UI), VELO_PAUSA);
        ui.boton(batch, botonSeguir, botonSeguir.contains(mouse), "SEGUIR", 1.5f);
        ui.boton(batch, botonReiniciarRonda, botonReiniciarRonda.contains(mouse), "REINICIAR RONDA", 1.3f);
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
        detenerSonidoCorrer();
        batch.dispose();
        ui.dispose();
    }
}
