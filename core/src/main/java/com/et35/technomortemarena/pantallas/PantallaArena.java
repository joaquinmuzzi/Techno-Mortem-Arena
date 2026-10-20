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
import com.et35.technomortemarena.entidades.EstadoJugador;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.EntradaLocal;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import com.et35.technomortemarena.partida.EstadoPartida;
import com.et35.technomortemarena.partida.RondasPartida;
import com.et35.technomortemarena.red.ClienteSala;
import com.et35.technomortemarena.red.ServidorSala;

/**
 * Ronda de duelo: arma la arena, los dos duelistas y corre el bucle de juego.
 *
 * <p>Tiene tres modos. En el modo de prueba (local) un jugador con teclado y mouse enfrenta a un blanco
 * quieto que reaparece cuando lo derrotan: sirve para probar mapas y armas. En red, cada instancia
 * controla un solo jugador (con el mismo teclado y mouse): el anfitrion ({@link ServidorSala}) simula
 * toda la partida y le manda el estado al cliente ({@link ClienteSala}), que solo manda sus teclas y
 * dibuja lo que recibe. En red la pausa no frena la simulacion: es solo un menu encima.
 *
 * <p>En red es una partida al mejor de 5 rondas: el anfitrion lleva la cuenta ({@link RondasPartida}) y
 * le manda el estado al cliente. Cuando un duelista cae termina la ronda, hay una pausa breve con el
 * cartel de resultado y la proxima arranca con los dos de vuelta en sus spawns. El primero en ganar 3
 * rondas se lleva la partida y aparece un cartel de victoria con revancha. En empate la ronda se
 * vuelve a jugar sin sumar nada. El HUD muestra las vidas de cada uno y el marcador.
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

    // Volumen de cada efecto. El de correr es mas bajo porque suena en bucle casi todo el tiempo y
    // taparia el resto; los golpes y aterrizajes son cortos y puntuales.
    private static final float VOLUMEN_CORRER = 0.6f;
    private static final float VOLUMEN_GOLPE = 1.0f;
    private static final float VOLUMEN_ATERRIZAR = 0.8f;

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
    private static final String[] LINEAS_AYUDA = {
        "MOVER: A / D",
        "SALTAR: W",
        "APUNTAR: MOUSE",
        "ESTOCADA: CLIC",
    };

    /** Vidas con las que arranca cada duelista en cada ronda. */
    private static final int VIDAS_INICIALES = 3;

    /**
     * Escalas del HUD: el cartel de ronda y el marcador van arriba al centro, y las vidas de cada
     * duelista a los costados del centro. Las vidas no van en las esquinas porque ahi esta el cartel
     * de controles y se pisarian.
     */
    private static final float ESCALA_RONDA_HUD = 1.2f;
    private static final float ESCALA_MARCADOR_HUD = 1.8f;
    private static final float MARGEN_HUD = 18f;
    private static final float TAMANO_VIDA = 20f;
    private static final float SEPARACION_VIDA = 8f;
    /** Distancia desde el centro de la pantalla hasta la primera vida de cada duelista. */
    private static final float SEPARACION_VIDAS_DEL_CENTRO = 70f;
    private static final Color COLOR_VIDA_PERDIDA = new Color(0.25f, 0.25f, 0.28f, 1f);

    /** Segundos que tarda en reaparecer el blanco del modo de prueba despues de caer. */
    private static final float REAPARICION_BLANCO = 2f;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final Arena arena;
    private final Supplier<Screen> alReiniciar;
    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Viewport viewportUi = new FitViewport(ANCHO_UI, ALTO_UI, new OrthographicCamera());
    private final Ui ui = new Ui();
    private Jugador jugadorUno;
    /** En modo anfitrion es nulo hasta que se une alguien. */
    private Jugador jugadorDos;

    /** No son nulos en modo anfitrion / cliente. Si los dos son nulos, es la partida local. */
    private final ServidorSala servidor;
    private final ClienteSala cliente;
    /** Teclado y mouse de quien juega en esta instancia; en red se apagan mientras el menu de pausa esta abierto. */
    private EntradaLocal entradaLocal;
    /** Oponente de mentira, lejos del mapa, para el anfitrion mientras nadie se une. */
    private Jugador fantasma;
    /** Segundos que lleva caido el blanco del modo de prueba. */
    private float tiempoBlancoCaido;
    private final Vector2 spawnUno;
    private final Vector2 spawnDos;
    private final EntradaJugador entradaAux = new EntradaJugador();
    private final EstadoJugador estadoUno = new EstadoJugador();
    private final EstadoJugador estadoDos = new EstadoJugador();
    /** Cuentas de golpes y aterrizajes ya sonados, para disparar cada sonido una sola vez. */
    private int golpesSonados;
    private int aterrizajesSonados;

    private final RondasPartida rondas = new RondasPartida();
    private final EstadoPartida estadoPartida = new EstadoPartida();

    private boolean pausado;
    /** Verdadero despues de dispose(): si un boton de la pausa nos cambio de pantalla, no hay que seguir dibujando. */
    private boolean liberada;
    /** Verdadero mientras el bucle del sonido de correr esta sonando, para no reiniciarlo cada fotograma. */
    private boolean suenaCorrer;
    /** Tiempo (segundos de juego) transcurrido desde que arranco la ronda, para el cartel de ayuda. */
    private float tiempoAyuda;
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
        this(recursos, audio, arena, alReiniciar, null, null);
    }

    /** Partida en red como anfitrion: controla al Jugador 1 y espera a que se una el otro. */
    public PantallaArena(RecursosGraficos recursos, RecursosAudio audio, Arena arena, ServidorSala servidor) {
        this(recursos, audio, arena, null, servidor, null);
    }

    /** Partida en red como cliente: el mapa es el que bajo de la sala. */
    public PantallaArena(RecursosGraficos recursos, RecursosAudio audio, ClienteSala cliente) {
        this(recursos, audio, Arena.desdeGrilla(cliente.getFilas()), null, null, cliente);
    }

    private PantallaArena(RecursosGraficos recursos, RecursosAudio audio, Arena arena,
                          Supplier<Screen> alReiniciar, ServidorSala servidor, ClienteSala cliente) {
        this.recursos = recursos;
        this.audio = audio;
        this.arena = arena;
        this.alReiniciar = alReiniciar;
        this.servidor = servidor;
        this.cliente = cliente;
        // FitViewport mantiene la relacion de aspecto agregando bandas negras si hace falta, en
        // lugar de estirar la imagen. Asi las distancias del duelo son identicas en cualquier
        // resolucion, que es imprescindible para que el enfrentamiento sea justo.
        this.viewport = new FitViewport(arena.getAncho(), arena.getAlto(), new OrthographicCamera());
        float yPiso = Arena.FILAS_PISO * Arena.CELDA;
        float xPorDefectoUno = 24f * Arena.CELDA;
        float xPorDefectoDos = arena.getAncho() - xPorDefectoUno - Jugador.ANCHO;
        Vector2 spawnUnoMapa = arena.getSpawnUno();
        Vector2 spawnDosMapa = arena.getSpawnDos();
        this.spawnUno = spawnUnoMapa != null ? spawnUnoMapa : new Vector2(xPorDefectoUno, yPiso);
        this.spawnDos = spawnDosMapa != null ? spawnDosMapa : new Vector2(xPorDefectoDos, yPiso);
        armarJugadores();
    }

    private boolean enRed() {
        return servidor != null || cliente != null;
    }

    /**
     * Crea a los jugadores de cero. Se usa al empezar y cuando el anfitrion reinicia la ronda.
     *
     * <p>En red, la instancia controla a un solo jugador con las teclas del Jugador 1, tanto si es el
     * anfitrion como si es el cliente. Las copias que solo se dibujan no leen ninguna entrada.
     */
    private void armarJugadores() {
        if (entradaLocal == null) {
            entradaLocal = new EntradaLocal(Controles.porDefecto());
            entradaLocal.setViewport(viewport);
        }
        if (cliente != null) {
            // Copias que solo se dibujan: el anfitrion les manda el estado.
            jugadorUno = new Jugador(FuenteEntrada.NINGUNA, spawnUno.x, spawnUno.y, true, TINTE_UNO, VIDAS_INICIALES);
            jugadorDos = new Jugador(FuenteEntrada.NINGUNA, spawnDos.x, spawnDos.y, false, TINTE_DOS, VIDAS_INICIALES);
        } else {
            jugadorUno = new Jugador(entradaLocal, spawnUno.x, spawnUno.y, true, TINTE_UNO, VIDAS_INICIALES);
            if (servidor == null) {
                jugadorDos = nuevoBlanco();
            } else {
                jugadorDos = servidor.clienteListo()
                    ? new Jugador(servidor.getEntradaRemota(), spawnDos.x, spawnDos.y, false, TINTE_DOS, VIDAS_INICIALES)
                    : null;
                if (fantasma == null) {
                    fantasma = new Jugador(FuenteEntrada.NINGUNA, -100000f, -100000f, false, TINTE_DOS, 1);
                }
            }
        }
        tiempoBlancoCaido = 0f;
        golpesSonados = 0;
        aterrizajesSonados = 0;
    }

    /** El blanco del modo de prueba: un jugador que no se mueve, no apunta y no tiene espada. */
    private Jugador nuevoBlanco() {
        Jugador blanco = new Jugador(FuenteEntrada.NINGUNA, spawnDos.x, spawnDos.y, false, TINTE_DOS, VIDAS_INICIALES);
        blanco.setDesarmado(true);
        return blanco;
    }

    @Override
    public void render(float delta) {
        // ESC pausa solo mientras la ronda esta en juego: entre rondas y en el cartel de victoria los
        // carteles tienen sus propios botones, y superponer dos menues solo confunde.
        boolean enRonda = rondas.getFase() == RondasPartida.Fase.RONDA;
        if (!enRonda) {
            pausado = false;
        }
        if (enRonda && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
            if (pausado && !enRed()) {
                // Al pausar deja de correr la simulacion, asi que el bucle quedaria sonando para
                // siempre: hay que apagarlo a mano. En red la simulacion sigue, y el sonido tambien.
                detenerSonidoCorrer();
            }
        }
        if (pausado) {
            if (atenderPausa()) {
                // Se cambio de pantalla: cambiarA() ya libero el SpriteBatch de esta instancia, asi que
                // hay que cortar el fotograma sin dibujar, o se dibujaria con recursos ya liberados.
                return;
            }
            if (!enRed()) {
                dibujar();
                return;
            }
            // En red el mundo sigue corriendo por debajo del menu de pausa.
        }
        if (rondas.getFase() == RondasPartida.Fase.GANADOR && atenderGanador()) {
            return;
        }
        if (servidor != null && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // El anfitrion reinicia la partida en el lugar: la sala y el socket siguen abiertos.
            reiniciarPartida();
        } else if (!enRed() && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Cortar aca todo el fotograma: reiniciar() ya libero el SpriteBatch de esta instancia
            // (ver dispose() mas abajo), asi que llamar a dibujar() despues crashearia.
            reiniciar();
            return;
        }
        actualizar(delta);
        if (liberada) {
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
        if (servidor != null) {
            actualizarComoAnfitrion(paso);
        } else if (cliente != null) {
            actualizarComoCliente();
            if (liberada) {
                return;
            }
        } else {
            jugadorUno.actualizar(paso, arena, jugadorDos);
            jugadorDos.actualizar(paso, arena, jugadorUno);
            reaparecerBlanco(paso);
        }
        actualizarSonidos();
    }

    /** Cuando el blanco cae, espera un momento y reaparece entero en su lugar. */
    private void reaparecerBlanco(float paso) {
        if (jugadorDos.isVivo()) {
            return;
        }
        tiempoBlancoCaido += paso;
        if (tiempoBlancoCaido >= REAPARICION_BLANCO) {
            jugadorDos = nuevoBlanco();
            tiempoBlancoCaido = 0f;
        }
    }

    /**
     * El anfitrion simula todo: su jugador con el teclado y el mouse y el del cliente con la entrada
     * que llega por la red. Lleva la cuenta de las rondas y le manda el estado resultante al cliente.
     */
    private void actualizarComoAnfitrion(float paso) {
        servidor.sondear();
        if (servidor.consumirDesconexion()) {
            // Si el cliente se va, la partida en curso termina: queda esperando a otro.
            jugadorDos = null;
            rondas.reiniciar();
            jugadorUno.reiniciar(spawnUno.x, spawnUno.y, true);
        }
        if (jugadorDos == null && servidor.clienteListo()) {
            // Llega alguien: empieza una partida nueva desde cero.
            rondas.reiniciar();
            jugadorUno.reiniciar(spawnUno.x, spawnUno.y, true);
            jugadorDos = new Jugador(servidor.getEntradaRemota(), spawnDos.x, spawnDos.y, false, TINTE_DOS, VIDAS_INICIALES);
        }
        boolean enRonda = rondas.getFase() == RondasPartida.Fase.RONDA;
        entradaLocal.setActiva(!pausado && enRonda);
        if (jugadorDos == null) {
            jugadorUno.actualizar(paso, arena, fantasma);
            return;
        }
        if (enRonda) {
            jugadorUno.actualizar(paso, arena, jugadorDos);
            jugadorDos.actualizar(paso, arena, jugadorUno);
        } else {
            // La simulacion queda congelada mientras se lee el resultado: se descartan las acciones
            // que el cliente haya mandado en el medio, para que no se disparen al arrancar la ronda.
            servidor.getEntradaRemota().leer(entradaAux);
        }
        if (rondas.actualizar(paso, jugadorUno.isVivo(), jugadorDos.isVivo())) {
            reiniciarRondaActual();
        }
        jugadorUno.exportar(estadoUno);
        jugadorDos.exportar(estadoDos);
        rondas.exportar(estadoPartida);
        servidor.enviarEstado(estadoUno, estadoDos, estadoPartida);
    }

    /**
     * El cliente no simula nada: manda sus teclas y copia el estado que le llega del anfitrion. Si se
     * corta la conexion vuelve a la pantalla de multijugador con el motivo.
     */
    private void actualizarComoCliente() {
        cliente.sondear();
        if (cliente.getFase() == ClienteSala.Fase.DESCONECTADO) {
            cambiarA(new PantallaMultijugador(recursos, audio, cliente.getMotivo()));
            return;
        }
        entradaLocal.setActiva(!pausado && rondas.getFase() == RondasPartida.Fase.RONDA);
        entradaLocal.leer(entradaAux);
        cliente.enviarEntrada(entradaAux);
        if (cliente.hayEstado()) {
            jugadorUno.importar(cliente.getEstado(0));
            jugadorDos.importar(cliente.getEstado(1));
            rondas.importar(cliente.getEstadoPartida());
        }
    }

    /**
     * Dispara los efectos segun lo que paso en el fotograma.
     *
     * <p>El sonido de correr es un bucle: se enciende la primera vez que algun jugador corre y se
     * apaga cuando ninguno lo hace. Los otros dos son golpes y aterrizajes puntuales, que cada jugador
     * marca con una bandera valida solo en el fotograma en que ocurren.
     */
    private void actualizarSonidos() {
        // Entre rondas el mundo esta congelado: el bucle de correr no puede seguir sonando.
        boolean corriendo = rondas.getFase() == RondasPartida.Fase.RONDA
            && (jugadorUno.estaCorriendo() || (jugadorDos != null && jugadorDos.estaCorriendo()));
        if (corriendo && !suenaCorrer) {
            audio.correr.loop(VOLUMEN_CORRER);
            suenaCorrer = true;
        } else if (!corriendo) {
            detenerSonidoCorrer();
        }
        // Se dispara cuando el contador sube. Asi funciona igual si el jugador lo simula esta
        // instancia o si es una copia que llega por la red.
        int golpes = jugadorUno.getContadorGolpes() + (jugadorDos != null ? jugadorDos.getContadorGolpes() : 0);
        if (golpes > golpesSonados) {
            audio.golpe.play(VOLUMEN_GOLPE);
        }
        golpesSonados = golpes;
        int aterrizajes = jugadorUno.getContadorAterrizajes()
            + (jugadorDos != null ? jugadorDos.getContadorAterrizajes() : 0);
        if (aterrizajes > aterrizajesSonados) {
            audio.aterrizar.play(VOLUMEN_ATERRIZAR);
        }
        aterrizajesSonados = aterrizajes;
    }

    /** Apaga el bucle del sonido de correr si estaba sonando. Se usa al pausar y al salir de la ronda. */
    private void detenerSonidoCorrer() {
        if (suenaCorrer) {
            audio.correr.stop();
            suenaCorrer = false;
        }
    }

    /** El cliente no puede reiniciar nada: lo decide el anfitrion. En el modo de prueba y como anfitrion si. */
    private boolean puedeReiniciar() {
        return cliente == null;
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
        } else if (puedeReiniciar() && botonReiniciarRonda.contains(mouse)) {
            // Reinicia solo la ronda actual: el marcador de la partida no se toca.
            pausado = false;
            reiniciarRondaActual();
        } else if (botonMenu.contains(mouse)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        } else if (botonSeleccion.contains(mouse)) {
            cambiarA(enRed()
                ? new PantallaMultijugador(recursos, audio, "")
                : new PantallaSeleccionMapa(recursos, audio));
            return true;
        } else if (botonSalir.contains(mouse)) {
            Gdx.app.exit();
        }
        return false;
    }

    /**
     * Atiende los botones del cartel de victoria: revancha en el mismo mapa (solo quien lleva la
     * partida) o salida hacia otro menu.
     *
     * @return {@code true} si se cambio de pantalla, con el mismo criterio que {@link #atenderPausa}
     */
    private boolean atenderGanador() {
        if (!Gdx.input.justTouched()) {
            return false;
        }
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (puedeReiniciar() && botonRevancha.contains(mouse)) {
            reiniciarPartida();
        } else if (botonGanadorSeleccion.contains(mouse)) {
            cambiarA(enRed()
                ? new PantallaMultijugador(recursos, audio, "")
                : new PantallaSeleccionMapa(recursos, audio));
            return true;
        } else if (botonGanadorMenu.contains(mouse)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        }
        return false;
    }

    /** Devuelve a los dos duelistas a su spawn, con las vidas completas, para jugar la ronda que sigue. */
    private void reiniciarRondaActual() {
        jugadorUno.reiniciar(spawnUno.x, spawnUno.y, true);
        if (jugadorDos != null) {
            jugadorDos.reiniciar(spawnDos.x, spawnDos.y, false);
        }
        tiempoBlancoCaido = 0f;
    }

    /** Empieza una partida nueva: marcador en cero, ronda 1 y todos de vuelta en su lugar. */
    private void reiniciarPartida() {
        rondas.reiniciar();
        armarJugadores();
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
        if (jugadorDos != null) {
            jugadorDos.dibujar(batch, recursos);
        }
        batch.end();

        dibujarAyudaControles();
        dibujarHud();
        dibujarEstadoRed();

        if (rondas.getFase() == RondasPartida.Fase.ENTRE_RONDAS) {
            dibujarEntreRondas();
        } else if (rondas.getFase() == RondasPartida.Fase.GANADOR) {
            dibujarGanador();
        }

        if (pausado) {
            dibujarPausa();
        }
    }

    /**
     * Dibuja las vidas de cada duelista a los costados del centro y, en red, el numero de ronda y el
     * marcador de la partida arriba al centro, con el viewport de interfaz para que queden ancladas a
     * la pantalla.
     */
    private void dibujarHud() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        float centro = ANCHO_UI / 2f;
        dibujarVidas(jugadorUno, TINTE_UNO, centro - SEPARACION_VIDAS_DEL_CENTRO, true);
        if (jugadorDos != null) {
            dibujarVidas(jugadorDos, TINTE_DOS, centro + SEPARACION_VIDAS_DEL_CENTRO, false);
            if (enRed()) {
                dibujarTextoCentrado("RONDA " + rondas.getNumeroRonda(), ESCALA_RONDA_HUD, ALTO_UI - MARGEN_HUD, Color.WHITE);
                String marcadorTexto = rondas.getRondasGanadasUno() + " - " + rondas.getRondasGanadasDos();
                dibujarTextoCentrado(marcadorTexto, ESCALA_MARCADOR_HUD, ALTO_UI - MARGEN_HUD - 20f, Color.WHITE);
            }
        }
        batch.end();
    }

    /**
     * Fila de vidas de un duelista: un cuadrado de su color por vida restante y otro apagado por cada
     * vida perdida. La fila empieza pegada al centro y crece hacia afuera.
     *
     * @param haciaLaIzquierda la fila crece hacia la izquierda desde {@code xAncla} (el Jugador 1); si no,
     *                         hacia la derecha (el Jugador 2)
     */
    private void dibujarVidas(Jugador jugador, Color tinte, float xAncla, boolean haciaLaIzquierda) {
        // Un duelista muerto muestra las vidas vacias: el contador es "lo que queda por perder".
        int vidas = jugador.isVivo() ? jugador.getVida() : 0;
        for (int i = 0; i < VIDAS_INICIALES; i++) {
            float x = haciaLaIzquierda
                ? xAncla - TAMANO_VIDA - i * (TAMANO_VIDA + SEPARACION_VIDA)
                : xAncla + i * (TAMANO_VIDA + SEPARACION_VIDA);
            rectAux.set(x, ALTO_UI - MARGEN_HUD - TAMANO_VIDA, TAMANO_VIDA, TAMANO_VIDA);
            ui.rectangulo(batch, rectAux, i < vidas ? tinte : COLOR_VIDA_PERDIDA);
        }
    }

    /** Cartel entre rondas: quien gano la ronda que acaba de terminar, o el aviso de empate. */
    private void dibujarEntreRondas() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        ui.rectangulo(batch, rectAux.set(0f, 0f, ANCHO_UI, ALTO_UI), VELO_PAUSA);
        int ganador = rondas.getGanadorDeLaRonda();
        if (ganador < 0) {
            dibujarTextoCentrado("EMPATE", 3f, 400f, Color.WHITE);
            dibujarTextoCentrado("LA RONDA SE VUELVE A JUGAR", 1.4f, 350f, Color.LIGHT_GRAY);
        } else {
            boolean ganoElUno = ganador == 0;
            dibujarTextoCentrado(ganoElUno ? "JUGADOR 1" : "JUGADOR 2", 3f, 400f,
                ganoElUno ? TINTE_UNO : TINTE_DOS);
            dibujarTextoCentrado("GANA LA RONDA " + rondas.getNumeroRonda(), 1.4f, 350f, Color.WHITE);
        }
        dibujarTextoCentrado("PROXIMA RONDA...", 1.1f, 260f, Color.LIGHT_GRAY);
        batch.end();
    }

    /** Cartel de victoria: anuncia al ganador de la partida y ofrece revancha (al anfitrion) o salir. */
    private void dibujarGanador() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean ganoElUno = rondas.ganoJugadorUno();
        batch.begin();
        ui.rectangulo(batch, rectAux.set(0f, 0f, ANCHO_UI, ALTO_UI), VELO_PAUSA);
        dibujarTextoCentrado("GANADOR DE LA PARTIDA", 1.6f, 440f, Color.WHITE);
        dibujarTextoCentrado(ganoElUno ? "JUGADOR 1" : "JUGADOR 2", 3f, 380f,
            ganoElUno ? TINTE_UNO : TINTE_DOS);
        dibujarTextoCentrado(rondas.getRondasGanadasUno() + " - " + rondas.getRondasGanadasDos(),
            1.6f, 325f, Color.WHITE);
        if (puedeReiniciar()) {
            ui.boton(batch, botonRevancha, botonRevancha.contains(mouse), "REVANCHA", 1.5f);
        } else {
            dibujarTextoCentrado("LA REVANCHA LA PIDE EL ANFITRION", 1.1f, 285f, Color.LIGHT_GRAY);
        }
        ui.boton(batch, botonGanadorSeleccion, botonGanadorSeleccion.contains(mouse),
            enRed() ? "SALIR DE LA SALA" : "SELECCION DE MAPA", 1.3f);
        ui.boton(batch, botonGanadorMenu, botonGanadorMenu.contains(mouse), "MENU PRINCIPAL", 1.3f);
        batch.end();
    }

    /** Linea de texto centrada en el ancho de la interfaz, para el HUD y los carteles de resultado. */
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
        String titulo = cliente != null ? "VOS: JUGADOR 2"
            : servidor != null ? "VOS: JUGADOR 1 (ANFITRION)" : "MODO DE PRUEBA";
        dibujarPanelAyuda(titulo, LINEAS_AYUDA, cliente != null ? TINTE_DOS : TINTE_UNO, false, alfa);
        batch.end();
    }

    /** Mensaje centrado mientras falta algo en red: el anfitrion espera a un jugador, el cliente al primer estado. */
    private void dibujarEstadoRed() {
        String texto = null;
        if (servidor != null && jugadorDos == null) {
            texto = "ESPERANDO JUGADOR... (PUERTO " + servidor.getPuerto() + ")";
        } else if (cliente != null && !cliente.hayEstado()) {
            texto = "ESPERANDO AL ANFITRION...";
        }
        if (texto == null) {
            return;
        }
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        float x = (ANCHO_UI - ui.anchoTexto(texto, 1.6f)) / 2f;
        ui.texto(batch, texto, x, 300f, 1.6f, Color.WHITE);
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
        if (puedeReiniciar()) {
            ui.boton(batch, botonReiniciarRonda, botonReiniciarRonda.contains(mouse), "REINICIAR RONDA", 1.3f);
        }
        ui.boton(batch, botonMenu, botonMenu.contains(mouse), "MENU PRINCIPAL", 1.3f);
        ui.boton(batch, botonSeleccion, botonSeleccion.contains(mouse),
            enRed() ? "SALIR DE LA SALA" : "SELECCION DE MAPA", 1.3f);
        ui.boton(batch, botonSalir, botonSalir.contains(mouse), "SALIR", 1.5f);
        String titulo = "PAUSA";
        float x = (ANCHO_UI - ui.anchoTexto(titulo, 2.5f)) / 2f;
        ui.texto(batch, titulo, x, 468f, 2.5f, Color.WHITE);
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
        liberada = true;
        // Cerrar la sala le avisa al otro lado, y suelta el puerto para poder crear otra.
        if (servidor != null) {
            servidor.close();
        }
        if (cliente != null) {
            cliente.close();
        }
        detenerSonidoCorrer();
        batch.dispose();
        ui.dispose();
    }
}
