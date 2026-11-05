package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.et35.technomortemarena.RecursosAudio;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.ArmasEnJuego;
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.entidades.PowerUp;
import com.et35.technomortemarena.entidades.TipoArma;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.entrada.EntradaLocal;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import com.et35.technomortemarena.partida.RondasPartida;

/**
 * Ronda de duelo: arma la arena, los dos duelistas y corre el bucle de juego.
 *
 * <p>Es el modo de prueba: un duelista se maneja con el teclado y el mouse y el otro es un blanco quieto,
 * sin arma. Con TAB se cambia de duelista: el que se manejaba queda como blanco y el otro toma el teclado
 * y el mouse, asi se prueban los dos (el azul y el rojo).
 *
 * <p>Es una partida al mejor de 5 rondas ({@link RondasPartida}). Cuando un duelista cae termina la ronda,
 * hay una pausa breve con el cartel de resultado y la proxima arranca con los dos de vuelta en sus spawns.
 * El primero en ganar 3 rondas se lleva la partida y aparece un cartel de victoria con revancha. En empate
 * la ronda se vuelve a jugar sin sumar nada. El HUD muestra las vidas de cada uno y el marcador.
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
    private static final float VOLUMEN_DISPARO_PISTOLA = 0.8f;
    private static final float VOLUMEN_DISPARO_ESCOPETA = 0.8f;
    private static final float VOLUMEN_EQUIPAR_ESPADA = 0.7f;
    private static final float VOLUMEN_EQUIPAR_ARMA = 0.7f;
    private static final float VOLUMEN_ESPADA_CAE = 0.8f;

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
        "RODAR: SHIFT",
        "APUNTAR: MOUSE",
        "ATACAR: CLIC (SIN ARMA, GOLPE)",
        "TIRAR EL ARMA: CLIC DERECHO",
        "CAMBIAR DE JUGADOR: TAB",
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
    /** El cuadro del arma en la esquina de cada duelista, y las balas debajo. */
    private static final float ANCHO_CUADRO_ARMA = 120f;
    private static final float ALTO_CUADRO_ARMA = 44f;
    private static final Color FONDO_CUADRO_ARMA = new Color(0f, 0f, 0f, 0.45f);
    private static final float ANCHO_BALA_HUD = 8f;
    private static final float ALTO_BALA_HUD = 18f;
    private static final float SEPARACION_BALA_HUD = 5f;
    private static final float LADO_POWER_UP_HUD = 24f;
    private static final Color COLOR_TIMER = new Color(1f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_MUERTE_SUBITA = new Color(1f, 0.3f, 0.2f, 1f);

    /** A cuantas celdas de su pared aparece cada duelista en un mapa que no marca los spawns. */
    static final float DISTANCIA_SPAWN_A_LA_PARED = 15f;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    /** Cambia cuando un cohete rompe bloques: se vuelve a armar desde la grilla. */
    private Arena arena;
    private final Supplier<Screen> alReiniciar;
    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Viewport viewportUi = new FitViewport(ANCHO_UI, ALTO_UI, new OrthographicCamera());
    private final Ui ui = new Ui();
    /** El duelista azul y el rojo. Uno se maneja con el teclado y el mouse y el otro es el blanco quieto. */
    private Jugador jugadorUno;
    private Jugador jugadorDos;
    /** Verdadero si el teclado y el mouse manejan al rojo (el Jugador 2): se cambia con TAB. */
    private boolean controlaAlDos;
    /** Teclado y mouse de quien juega. */
    private EntradaLocal entradaLocal;
    private final Vector2 spawnUno;
    private final Vector2 spawnDos;
    /** Cuentas de golpes y aterrizajes ya sonados, para disparar cada sonido una sola vez. */
    private int golpesSonados;
    private int aterrizajesSonados;
    /** Cuentas de disparos y armas recogidas ya sonadas. */
    private int disparosPistolaSonados;
    private int disparosEscopetaSonados;
    private int espadasEquipadasSonadas;
    private int armasDeFuegoEquipadasSonadas;
    /** Veces que ya sono la espada tirada al caer. */
    private int espadasCaidasSonadas;

    private final RondasPartida rondas = new RondasPartida();

    /** El mapa tal como se eligio, sin los bloques que rompen los cohetes. */
    private final String[] filasOriginales;
    /** Zonas {@code {x0, y0, x1, y1}} en celdas donde no caen armas: los spawns y el centro de la muerte subita. */
    private final List<float[]> zonasProhibidas;
    /** Cuantas celdas rotas por los cohetes tiene ya sacadas el mapa: cuando cambia la lista, se rearma. */
    private int celdasRotasAplicadas;
    /** Sigue a los duelistas y se aleja cuando se separan. */
    private final CamaraDuelo camara = new CamaraDuelo();
    /** Balas, armas tiradas y armas en el mapa. */
    private ArmasEnJuego armas;

    private boolean pausado;
    /** Verdadero despues de dispose(): si un boton de la pausa nos cambio de pantalla, no hay que seguir dibujando. */
    private boolean liberada;
    /** Verdadero mientras el bucle del sonido de correr esta sonando, para no reiniciarlo cada fotograma. */
    private boolean suenaCorrer;
    /** Tiempo (segundos de juego) transcurrido desde que arranco la ronda, para el cartel de ayuda. */
    private float tiempoAyuda;
    private boolean muerteSubitaActivada;
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
        audio.pedirMusicaMenu(false);
        this.arena = arena;
        this.filasOriginales = arena.getFilas();
        this.alReiniciar = alReiniciar;
        ui.setAudio(audio);
        // FitViewport mantiene la relacion de aspecto agregando bandas negras si hace falta, en
        // lugar de estirar la imagen. Asi las distancias del duelo son identicas en cualquier
        // resolucion, que es imprescindible para que el enfrentamiento sea justo. Muestra la vista de
        // la camara (la mitad del mapa en cada sentido): la camara sigue a los duelistas (CamaraDuelo).
        this.viewport = new FitViewport(Arena.ANCHO_VISTA, Arena.ALTO_VISTA, new OrthographicCamera());
        // Sin spawns en el mapa, cada uno aparece a 15 celdas de su pared, parado sobre lo que haya ahi
        // (el piso, o un escalon en el mapa de la escalera).
        float xPorDefectoUno = DISTANCIA_SPAWN_A_LA_PARED * Arena.CELDA;
        float xPorDefectoDos = arena.getAncho() - xPorDefectoUno - Jugador.ANCHO;
        Vector2 spawnUnoMapa = arena.getSpawnUno();
        Vector2 spawnDosMapa = arena.getSpawnDos();
        this.spawnUno = spawnUnoMapa != null ? spawnUnoMapa : new Vector2(xPorDefectoUno, alturaDelPiso(arena, xPorDefectoUno));
        this.spawnDos = spawnDosMapa != null ? spawnDosMapa : new Vector2(xPorDefectoDos, alturaDelPiso(arena, xPorDefectoDos));
        this.zonasProhibidas = crearZonasProhibidas();
        this.armas = new ArmasEnJuego(new Random(), zonasProhibidas);
        armarJugadores();
    }

    /** Crea a los dos duelistas de cero, con el teclado y el mouse en el que se maneja. Se usa al empezar y en la revancha. */
    private void armarJugadores() {
        if (entradaLocal == null) {
            entradaLocal = new EntradaLocal(Controles.porDefecto());
            entradaLocal.setViewport(viewport);
        }
        jugadorUno = new Jugador(FuenteEntrada.NINGUNA, spawnUno.x, spawnUno.y, true, TINTE_UNO, VIDAS_INICIALES);
        jugadorDos = new Jugador(FuenteEntrada.NINGUNA, spawnDos.x, spawnDos.y, false, TINTE_DOS, VIDAS_INICIALES);
        asignarControl();
        if (armas != null) {
            armas.limpiar();
        }
        golpesSonados = 0;
        aterrizajesSonados = 0;
        disparosPistolaSonados = 0;
        disparosEscopetaSonados = 0;
        espadasEquipadasSonadas = 0;
        armasDeFuegoEquipadasSonadas = 0;
        // El contador de espadas caidas es de las armas, que no se recrean: se toma el valor actual
        // para no sonar por caidas de la partida anterior.
        espadasCaidasSonadas = armas.getContadorEspadasAlCaer();
        muerteSubitaActivada = false;
    }

    /**
     * Le da el teclado y el mouse al duelista que se maneja, y deja al otro como blanco: sin entrada y sin
     * arma, no se mueve, no apunta y no agarra nada.
     */
    private void asignarControl() {
        Jugador manejado = controlaAlDos ? jugadorDos : jugadorUno;
        Jugador blanco = controlaAlDos ? jugadorUno : jugadorDos;
        manejado.setFuente(entradaLocal);
        manejado.setDesarmado(false);
        blanco.setFuente(FuenteEntrada.NINGUNA);
        blanco.setDesarmado(true);
    }

    /** TAB: el teclado y el mouse pasan al otro duelista, y el que se manejaba queda como blanco. */
    private void cambiarDeJugador() {
        controlaAlDos = !controlaAlDos;
        asignarControl();
        // el cartel de controles vuelve a aparecer con el color del duelista que se maneja ahora
        tiempoAyuda = 0f;
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
            if (pausado) {
                // Al pausar deja de correr la simulacion, asi que el bucle quedaria sonando para
                // siempre: hay que apagarlo a mano.
                detenerSonidoCorrer();
            }
        }
        if (pausado) {
            if (atenderPausa()) {
                // Se cambio de pantalla: cambiarA() ya libero el SpriteBatch de esta instancia, asi que
                // hay que cortar el fotograma sin dibujar, o se dibujaria con recursos ya liberados.
                return;
            }
            dibujar();
            return;
        }
        if (rondas.getFase() == RondasPartida.Fase.GANADOR && atenderGanador()) {
            return;
        }
        if (enRonda && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            cambiarDeJugador();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
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
        boolean enRonda = rondas.getFase() == RondasPartida.Fase.RONDA
            || rondas.getFase() == RondasPartida.Fase.MUERTE_SUBITA;
        entradaLocal.setActiva(!pausado && enRonda);
        if (enRonda) {
            // entre rondas y en el cartel de victoria el mundo queda congelado
            jugadorUno.actualizar(paso, arena, jugadorDos);
            jugadorDos.actualizar(paso, arena, jugadorUno);
            armas.actualizar(paso, arena, jugadorUno, jugadorDos);
        }
        if (rondas.getFase() == RondasPartida.Fase.MUERTE_SUBITA && !muerteSubitaActivada) {
            forzarMuerteSubita();
            muerteSubitaActivada = true;
        } else if (rondas.getFase() == RondasPartida.Fase.RONDA) {
            muerteSubitaActivada = false;
        }
        if (rondas.actualizar(paso, jugadorUno.isVivo(), jugadorDos.isVivo())) {
            reiniciarRondaActual();
        }
        aplicarCeldasRotas();
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
        // Entre rondas el mundo esta congelado: el bucle de correr no puede seguir sonando.
        boolean corriendo = rondas.getFase() == RondasPartida.Fase.RONDA
            && (jugadorUno.estaCorriendo() || jugadorDos.estaCorriendo());
        if (corriendo && !suenaCorrer) {
            audio.repetir(audio.correr, VOLUMEN_CORRER);
            suenaCorrer = true;
        } else if (!corriendo) {
            detenerSonidoCorrer();
        }
        // Se dispara cuando el contador sube.
        int golpes = jugadorUno.getContadorGolpes() + (jugadorDos != null ? jugadorDos.getContadorGolpes() : 0);
        if (golpes > golpesSonados) {
            audio.reproducir(audio.golpe, VOLUMEN_GOLPE);
        }
        golpesSonados = golpes;
        int aterrizajes = jugadorUno.getContadorAterrizajes()
            + (jugadorDos != null ? jugadorDos.getContadorAterrizajes() : 0);
        if (aterrizajes > aterrizajesSonados) {
            audio.reproducir(audio.aterrizar, VOLUMEN_ATERRIZAR);
        }
        aterrizajesSonados = aterrizajes;
        int disparosPistola = jugadorUno.getContadorDisparosPistola()
            + (jugadorDos != null ? jugadorDos.getContadorDisparosPistola() : 0);
        if (disparosPistola > disparosPistolaSonados) {
            audio.pistolazo.play(VOLUMEN_DISPARO_PISTOLA);
        }
        disparosPistolaSonados = disparosPistola;
        int disparosEscopeta = jugadorUno.getContadorDisparosEscopeta()
            + (jugadorDos != null ? jugadorDos.getContadorDisparosEscopeta() : 0);
        if (disparosEscopeta > disparosEscopetaSonados) {
            audio.escopetazo.play(VOLUMEN_DISPARO_ESCOPETA);
        }
        disparosEscopetaSonados = disparosEscopeta;
        int espadasEquipadas = jugadorUno.getContadorEspadasEquipadas()
            + (jugadorDos != null ? jugadorDos.getContadorEspadasEquipadas() : 0);
        if (espadasEquipadas > espadasEquipadasSonadas) {
            audio.espadaEquipada.play(VOLUMEN_EQUIPAR_ESPADA);
        }
        espadasEquipadasSonadas = espadasEquipadas;
        int armasDeFuegoEquipadas = jugadorUno.getContadorArmasDeFuegoEquipadas()
            + (jugadorDos != null ? jugadorDos.getContadorArmasDeFuegoEquipadas() : 0);
        if (armasDeFuegoEquipadas > armasDeFuegoEquipadasSonadas) {
            audio.armaEquipada.play(VOLUMEN_EQUIPAR_ARMA);
        }
        armasDeFuegoEquipadasSonadas = armasDeFuegoEquipadas;
        int espadasCaidas = armas.getContadorEspadasAlCaer();
        if (espadasCaidas > espadasCaidasSonadas) {
            audio.espadaCae.play(VOLUMEN_ESPADA_CAE);
        }
        espadasCaidasSonadas = espadasCaidas;
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
            ui.clic();
            pausado = false;
        } else if (botonReiniciarRonda.contains(mouse)) {
            // Reinicia solo la ronda actual: el marcador de la partida no se toca.
            ui.clic();
            pausado = false;
            reiniciarRondaActual();
        } else if (botonMenu.contains(mouse)) {
            ui.clic();
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        } else if (botonSeleccion.contains(mouse)) {
            ui.clic();
            cambiarA(new PantallaSeleccionMapa(recursos, audio));
            return true;
        } else if (botonSalir.contains(mouse)) {
            ui.clic();
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
            ui.clic();
            reiniciarPartida();
        } else if (botonGanadorSeleccion.contains(mouse)) {
            ui.clic();
            cambiarA(new PantallaSeleccionMapa(recursos, audio));
            return true;
        } else if (botonGanadorMenu.contains(mouse)) {
            ui.clic();
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        }
        return false;
    }

    /** Devuelve a los dos duelistas a su spawn, con las vidas completas, para jugar la ronda que sigue. */
    private void reiniciarRondaActual() {
        jugadorUno.reiniciar(spawnUno.x, spawnUno.y, true);
        jugadorDos.reiniciar(spawnDos.x, spawnDos.y, false);
        // reiniciar le devuelve la espada a los dos: el blanco tiene que seguir sin arma
        asignarControl();
        muerteSubitaActivada = false;
        armas.limpiar();
    }

    private void forzarMuerteSubita() {
        float centroX = arena.getAncho() / 2f;
        float yPiso = Arena.FILAS_PISO * Arena.CELDA;
        jugadorUno.reiniciar(centroX - 80f, yPiso, true);
        jugadorDos.reiniciar(centroX + 80f - Jugador.ANCHO, yPiso, false);
        asignarControl();
    }

    /** Marcador en cero, ronda 1 y sin armas tiradas: una partida nueva de verdad. Se usa en la revancha. */
    private void reiniciarRondasYNivel() {
        rondas.reiniciar();
        armas.limpiar();
    }

    /**
     * La altura de lo mas alto que hay debajo de un cuerpo parado en {@code x}: ahi apoya los pies. Sin nada
     * debajo, la altura del piso de siempre.
     */
    static float alturaDelPiso(Arena arena, float x) {
        float altura = 0f;
        boolean hayAlgo = false;
        for (Bloque bloque : arena.getBloques()) {
            Rectangle area = bloque.getArea();
            if (area.x < x + Jugador.ANCHO && area.x + area.width > x) {
                altura = Math.max(altura, area.y + area.height);
                hayAlgo = true;
            }
        }
        return hayAlgo ? altura : Arena.FILAS_PISO * Arena.CELDA;
    }

    /**
     * Las zonas donde no caen armas: alrededor de cada spawn y del centro, donde la muerte subita pone a los
     * dos duelistas.
     */
    private List<float[]> crearZonasProhibidas() {
        List<float[]> zonas = new ArrayList<>();
        zonas.add(ArmasEnJuego.zonaDeSpawn(spawnUno.x, spawnUno.y, Jugador.ANCHO, Jugador.ALTO));
        zonas.add(ArmasEnJuego.zonaDeSpawn(spawnDos.x, spawnDos.y, Jugador.ANCHO, Jugador.ALTO));
        float yPiso = Arena.FILAS_PISO * Arena.CELDA;
        zonas.add(ArmasEnJuego.zonaDeSpawn(arena.getAncho() / 2f - 80f, yPiso, 160f, Jugador.ALTO));
        return zonas;
    }

    /** Empieza una partida nueva: marcador en cero, ronda 1 y todos de vuelta en su lugar. */
    private void reiniciarPartida() {
        reiniciarRondasYNivel();
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
        actualizarCamara(Gdx.graphics.getDeltaTime());
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        dibujarBloques();
        jugadorUno.dibujar(batch, recursos);
        jugadorDos.dibujar(batch, recursos);
        armas.dibujar(batch, recursos);
        batch.end();

        dibujarAyudaControles();
        dibujarHud();
        if ((rondas.getFase() == RondasPartida.Fase.RONDA || rondas.getFase() == RondasPartida.Fase.MUERTE_SUBITA)) {
            if (rondas.getFase() == RondasPartida.Fase.MUERTE_SUBITA) {
                int seg = Math.max(0, (int) Math.ceil(rondas.getTiempoMuerteSubita()));
                batch.begin();
                dibujarTextoCentrado("MUERTE SUBITA " + seg, 1.6f, ALTO_UI - MARGEN_HUD - 68f, COLOR_MUERTE_SUBITA);
                batch.end();
            } else {
                int seg = Math.max(0, (int) Math.ceil(rondas.getTiempoRestante()));
                batch.begin();
                dibujarTextoCentrado(seg + "s", 1.4f, ALTO_UI - MARGEN_HUD - 68f, COLOR_TIMER);
                batch.end();
            }
        }

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
     * Dibuja las vidas de cada duelista a los costados del centro y el numero de ronda y el marcador de la
     * partida arriba al centro, con el viewport de interfaz para que queden ancladas a
     * la pantalla.
     */
    private void dibujarHud() {
        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        float centro = ANCHO_UI / 2f;
        dibujarVidas(jugadorUno, TINTE_UNO, centro - SEPARACION_VIDAS_DEL_CENTRO, true);
        dibujarArma(jugadorUno, TINTE_UNO, true);
        dibujarVidas(jugadorDos, TINTE_DOS, centro + SEPARACION_VIDAS_DEL_CENTRO, false);
        dibujarArma(jugadorDos, TINTE_DOS, false);
        dibujarTextoCentrado("RONDA " + rondas.getNumeroRonda(), ESCALA_RONDA_HUD, ALTO_UI - MARGEN_HUD, Color.WHITE);
        String marcadorTexto = rondas.getRondasGanadasUno() + " - " + rondas.getRondasGanadasDos();
        dibujarTextoCentrado(marcadorTexto, ESCALA_MARCADOR_HUD, ALTO_UI - MARGEN_HUD - 20f, Color.WHITE);
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
        // Cada corazon se llena por sextos: la espada saca uno entero, una bala un tercio y un punetazo un sexto.
        int partes = jugador.isVivo() ? jugador.getPartes() : 0;
        for (int i = 0; i < VIDAS_INICIALES; i++) {
            float x = haciaLaIzquierda
                ? xAncla - TAMANO_VIDA - i * (TAMANO_VIDA + SEPARACION_VIDA)
                : xAncla + i * (TAMANO_VIDA + SEPARACION_VIDA);
            float y = ALTO_UI - MARGEN_HUD - TAMANO_VIDA;
            ui.rectangulo(batch, rectAux.set(x, y, TAMANO_VIDA, TAMANO_VIDA), COLOR_VIDA_PERDIDA);
            int enEste = MathUtils.clamp(partes - i * Jugador.PARTES_POR_CORAZON, 0, Jugador.PARTES_POR_CORAZON);
            if (enEste > 0) {
                // se vacia desde afuera hacia el centro: la del Jugador 1 de izquierda a derecha, la del 2 al reves
                float ancho = TAMANO_VIDA * enEste / Jugador.PARTES_POR_CORAZON;
                float desde = haciaLaIzquierda ? x + TAMANO_VIDA - ancho : x;
                ui.rectangulo(batch, rectAux.set(desde, y, ancho, TAMANO_VIDA), tinte);
            }
        }
    }

    /**
     * El arma en la mano de un duelista, en su esquina de arriba (el Jugador 1 a la izquierda, el 2 a la
     * derecha): el arma sobre un fondo con brillo y, debajo, las balas como rectangulos verticales, llenos
     * los que quedan y apagados los gastados, en el mismo estilo que las vidas. Mientras se ve el cartel de
     * controles (arriba a la izquierda), la del Jugador 1 va debajo del cartel y sube cuando se desvanece.
     */
    /**
     * Las mejoras que tiene un duelista, debajo de sus balas: el icono de cada una y una barra con el tiempo
     * que le queda. En la columna de la esquina del jugador, como el resto de su interfaz.
     */
    private void dibujarPowerUps(Jugador jugador, boolean aLaIzquierda, float xCuadro, float arriba) {
        int numero = 0;
        for (PowerUp mejora : PowerUp.values()) {
            float tiempo = jugador.getTiempoPowerUp(mejora);
            if (tiempo <= 0f) {
                continue;
            }
            float x = aLaIzquierda
                ? xCuadro + numero * (LADO_POWER_UP_HUD + 8f)
                : xCuadro + ANCHO_CUADRO_ARMA - LADO_POWER_UP_HUD - numero * (LADO_POWER_UP_HUD + 8f);
            float y = arriba - LADO_POWER_UP_HUD;
            ui.rectangulo(batch, rectAux.set(x - 2f, y - 8f, LADO_POWER_UP_HUD + 4f, LADO_POWER_UP_HUD + 10f), FONDO_CUADRO_ARMA);
            batch.draw(ArmasEnJuego.texturaDePowerUp(recursos, mejora), x, y, LADO_POWER_UP_HUD, LADO_POWER_UP_HUD);
            float parte = tiempo / PowerUp.DURACION;
            ui.rectangulo(batch, rectAux.set(x, y - 6f, LADO_POWER_UP_HUD * parte, 3f), Color.WHITE);
            numero++;
        }
    }

    private void dibujarArma(Jugador jugador, Color tinte, boolean aLaIzquierda) {
        if (jugador.isDesarmado()) {
            return; // el blanco del modo de prueba no usa armas
        }
        float x = aLaIzquierda ? MARGEN_HUD : ANCHO_UI - MARGEN_HUD - ANCHO_CUADRO_ARMA;
        float arriba = ALTO_UI - MARGEN_HUD;
        if (aLaIzquierda) {
            float altoCartel = 2f * RELLENO_AYUDA + SEPARACION_TITULO_AYUDA + LINEAS_AYUDA.length * INTERLINEA_AYUDA;
            arriba -= (altoCartel + 24f) * alfaAyuda();
        }
        float abajo = arriba - ALTO_CUADRO_ARMA;
        ui.rectangulo(batch, rectAux.set(x, abajo, ANCHO_CUADRO_ARMA, ALTO_CUADRO_ARMA), FONDO_CUADRO_ARMA);
        dibujarPowerUps(jugador, aLaIzquierda, x, abajo - 6f - ALTO_BALA_HUD - 10f);
        TipoArma arma = jugador.getArma();
        if (arma == null) {
            String vacia = "MANO VACIA";
            float ancho = ui.anchoTexto(vacia, 0.8f);
            ui.texto(batch, vacia, x + (ANCHO_CUADRO_ARMA - ancho) / 2f, abajo + ALTO_CUADRO_ARMA / 2f + 6f, 0.8f, Color.GRAY);
            return;
        }
        batch.setColor(1f, 1f, 1f, 0.85f);
        batch.draw(recursos.brillo, x + 4f, abajo + 4f, ANCHO_CUADRO_ARMA - 8f, ALTO_CUADRO_ARMA - 8f);
        batch.setColor(Color.WHITE);
        Texture textura;
        switch (arma) {
            case ESPADA:
            case ESPADA_LASER:
                textura = recursos.espada;
                break;
            case ESCOPETA:
                textura = recursos.escopeta;
                break;
            case RIFLE_LASER:
                textura = recursos.rifleLaser;
                break;
            case LANZACOHETES:
                textura = recursos.lanzacohetes;
                break;
            default:
                textura = recursos.pistola;
                break;
        }
        if (arma == TipoArma.ESPADA_LASER) {
            batch.setColor(Jugador.COLOR_ESPADA_LASER);
        }
        // el arma entra en el cuadro sin deformarse
        float maximoAncho = ANCHO_CUADRO_ARMA - 16f;
        float maximoAlto = ALTO_CUADRO_ARMA - 16f;
        float escala = Math.min(maximoAncho / textura.getWidth(), maximoAlto / textura.getHeight());
        float ancho = textura.getWidth() * escala;
        float alto = textura.getHeight() * escala;
        batch.draw(textura, x + (ANCHO_CUADRO_ARMA - ancho) / 2f, abajo + (ALTO_CUADRO_ARMA - alto) / 2f, ancho, alto);
        batch.setColor(Color.WHITE);
        int maximo = arma.getBalasIniciales();
        // con muchas balas (el rifle laser) se achican para entrar debajo del cuadro
        float separacion = maximo > 10 ? 2f : SEPARACION_BALA_HUD;
        float anchoBala = maximo == 0 ? ANCHO_BALA_HUD
            : Math.min(ANCHO_BALA_HUD, (ANCHO_CUADRO_ARMA - (maximo - 1) * separacion) / maximo);
        for (int i = 0; i < maximo; i++) {
            float bx = aLaIzquierda
                ? x + i * (anchoBala + separacion)
                : x + ANCHO_CUADRO_ARMA - anchoBala - i * (anchoBala + separacion);
            rectAux.set(bx, abajo - 6f - ALTO_BALA_HUD, anchoBala, ALTO_BALA_HUD);
            ui.rectangulo(batch, rectAux, i < jugador.getBalas() ? tinte : COLOR_VIDA_PERDIDA);
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

    /** Cartel de victoria: anuncia al ganador de la partida y ofrece revancha o salir. */
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
        ui.boton(batch, botonRevancha, botonRevancha.contains(mouse), "REVANCHA", 1.5f);
        ui.boton(batch, botonGanadorSeleccion, botonGanadorSeleccion.contains(mouse), "SELECCION DE MAPA", 1.3f);
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
        String titulo = controlaAlDos ? "MODO DE PRUEBA: JUGADOR 2" : "MODO DE PRUEBA: JUGADOR 1";
        dibujarPanelAyuda(titulo, LINEAS_AYUDA, controlaAlDos ? TINTE_DOS : TINTE_UNO, false, alfa);
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
        ui.texto(batch, titulo, x, 468f, 2.5f, Color.WHITE);
        batch.end();
    }

    /**
     * Si los cohetes rompieron bloques (o, al empezar la ronda, se vacio la lista), rearma el mapa original sin
     * ellos.
     */
    private void aplicarCeldasRotas() {
        List<int[]> rotas = armas.getCeldasRotas();
        if (rotas.size() == celdasRotasAplicadas) {
            return;
        }
        if (filasOriginales.length > 0) {
            arena = Arena.desdeGrilla(filasOriginales).sinCeldas(rotas);
        }
        celdasRotasAplicadas = rotas.size();
    }

    /** Lleva la camara hacia los duelistas (o al mapa entero mientras se edita) y la aplica. */
    private void actualizarCamara(float delta) {
        camara.seguir(Math.min(delta, DELTA_MAXIMO), jugadorUno.getCaja(), jugadorDos.getCaja(), false);
        camara.aplicar((OrthographicCamera) viewport.getCamera());
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
        detenerSonidoCorrer();
        batch.dispose();
        ui.dispose();
    }
}
