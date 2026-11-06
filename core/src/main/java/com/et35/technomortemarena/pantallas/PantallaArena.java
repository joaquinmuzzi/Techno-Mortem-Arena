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
import com.et35.technomortemarena.entidades.armas.Arma;
import com.et35.technomortemarena.entrada.Controles;
import com.et35.technomortemarena.entrada.EntradaLocal;
import com.et35.technomortemarena.entrada.FuenteEntrada;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import com.et35.technomortemarena.partida.RondasPartida;

public class PantallaArena extends ScreenAdapter {
    private static final float DELTA_MAXIMO = 1f / 30f;
    private static final float ANCHO_UI = 960f;
    private static final float ALTO_UI = 540f;
    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color TINTE_UNO = Jugador.TINTE_UNO;
    private static final Color TINTE_DOS = Jugador.TINTE_DOS;
    private static final Color VELO_PAUSA = new Color(0f, 0f, 0f, 0.6f);

    private static final float VOLUMEN_CORRER = 0.6f;
    private static final float VOLUMEN_GOLPE = 1.0f;
    private static final float VOLUMEN_ATERRIZAR = 0.8f;
    private static final float VOLUMEN_DISPARO_PISTOLA = 0.8f;
    private static final float VOLUMEN_DISPARO_ESCOPETA = 0.8f;
    private static final float VOLUMEN_EQUIPAR_ESPADA = 0.7f;
    private static final float VOLUMEN_EQUIPAR_ARMA = 0.7f;
    private static final float VOLUMEN_ESPADA_CAE = 0.8f;

    private static final float DURACION_AYUDA = 7f;
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

    private static final int VIDAS_INICIALES = 3;

    private static final float ESCALA_RONDA_HUD = 1.2f;
    private static final float ESCALA_MARCADOR_HUD = 1.8f;
    private static final float MARGEN_HUD = 18f;
    private static final float TAMANO_VIDA = 20f;
    private static final float SEPARACION_VIDA = 8f;
    private static final float SEPARACION_VIDAS_DEL_CENTRO = 70f;
    private static final Color COLOR_VIDA_PERDIDA = new Color(0.25f, 0.25f, 0.28f, 1f);
    private static final float ANCHO_CUADRO_ARMA = 120f;
    private static final float ALTO_CUADRO_ARMA = 44f;
    private static final Color FONDO_CUADRO_ARMA = new Color(0f, 0f, 0f, 0.45f);
    private static final float ANCHO_BALA_HUD = 8f;
    private static final float ALTO_BALA_HUD = 18f;
    private static final float SEPARACION_BALA_HUD = 5f;
    private static final float LADO_POWER_UP_HUD = 24f;
    private static final Color COLOR_TIMER = new Color(1f, 0.9f, 0.4f, 1f);
    private static final Color COLOR_MUERTE_SUBITA = new Color(1f, 0.3f, 0.2f, 1f);

    static final float DISTANCIA_SPAWN_A_LA_PARED = 15f;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private Arena arena;
    private final Supplier<Screen> alReiniciar;
    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Viewport viewportUi = new FitViewport(ANCHO_UI, ALTO_UI, new OrthographicCamera());
    private final Ui ui = new Ui();
    private Jugador jugadorUno;
    private Jugador jugadorDos;
    private boolean controlaAlDos;
    private EntradaLocal entradaLocal;
    private final Vector2 spawnUno;
    private final Vector2 spawnDos;
    private int golpesSonados;
    private int aterrizajesSonados;
    private int disparosPistolaSonados;
    private int disparosEscopetaSonados;
    private int espadasEquipadasSonadas;
    private int armasDeFuegoEquipadasSonadas;
    private int espadasCaidasSonadas;

    private final RondasPartida rondas = new RondasPartida();

    private final String[] filasOriginales;
    private final List<float[]> zonasProhibidas;
    private int celdasRotasAplicadas;
    private final CamaraDuelo camara = new CamaraDuelo();
    private ArmasEnJuego armas;

    private boolean pausado;
    private boolean liberada;
    private boolean suenaCorrer;
    private float tiempoAyuda;
    private boolean muerteSubitaActivada;
    private final Color colorAux = new Color();
    private final Rectangle rectAux = new Rectangle();
    private final Rectangle botonSeguir = new Rectangle(330f, 390f, 300f, 50f);
    private final Rectangle botonReiniciarRonda = new Rectangle(330f, 320f, 300f, 50f);
    private final Rectangle botonMenu = new Rectangle(330f, 250f, 300f, 50f);
    private final Rectangle botonSeleccion = new Rectangle(330f, 180f, 300f, 50f);
    private final Rectangle botonSalir = new Rectangle(330f, 110f, 300f, 50f);
    private final Rectangle botonRevancha = new Rectangle(330f, 250f, 300f, 50f);
    private final Rectangle botonGanadorSeleccion = new Rectangle(330f, 180f, 300f, 50f);
    private final Rectangle botonGanadorMenu = new Rectangle(330f, 110f, 300f, 50f);

    public PantallaArena(RecursosGraficos recursos, RecursosAudio audio, Arena arena) {
        this(recursos, audio, arena, () -> new PantallaArena(recursos, audio, arena));
    }

    public PantallaArena(RecursosGraficos recursos, RecursosAudio audio, Arena arena, Supplier<Screen> alReiniciar) {
        this.recursos = recursos;
        this.audio = audio;
        audio.pedirMusica(RecursosAudio.Musica.COMBATE);
        this.arena = arena;
        this.filasOriginales = arena.getFilas();
        this.alReiniciar = alReiniciar;
        ui.setAudio(audio);
        this.viewport = new FitViewport(Arena.ANCHO_VISTA, Arena.ALTO_VISTA, new OrthographicCamera());
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
        espadasCaidasSonadas = armas.getContadorEspadasAlCaer();
        muerteSubitaActivada = false;
    }

    private void asignarControl() {
        Jugador manejado = controlaAlDos ? jugadorDos : jugadorUno;
        Jugador blanco = controlaAlDos ? jugadorUno : jugadorDos;
        manejado.setFuente(entradaLocal);
        blanco.setFuente(FuenteEntrada.NINGUNA);
    }

    private void cambiarDeJugador() {
        controlaAlDos = !controlaAlDos;
        asignarControl();
        tiempoAyuda = 0f;
    }

    @Override
    public void render(float delta) {
        boolean enRonda = rondas.getFase() == RondasPartida.Fase.RONDA;
        if (!enRonda) {
            pausado = false;
        }
        if (enRonda && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pausado = !pausado;
            if (pausado) {
                detenerSonidoCorrer();
            }
        }
        if (pausado) {
            if (atenderPausa()) {
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
        float paso = Math.min(delta, DELTA_MAXIMO);
        tiempoAyuda += paso;
        boolean enRonda = rondas.getFase() == RondasPartida.Fase.RONDA
            || rondas.getFase() == RondasPartida.Fase.MUERTE_SUBITA;
        entradaLocal.setActiva(!pausado && enRonda);
        if (enRonda) {
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

    private void actualizarSonidos() {
        boolean corriendo = rondas.getFase() == RondasPartida.Fase.RONDA
            && (jugadorUno.estaCorriendo() || jugadorDos.estaCorriendo());
        if (corriendo && !suenaCorrer) {
            audio.repetir(audio.correr, VOLUMEN_CORRER);
            suenaCorrer = true;
        } else if (!corriendo) {
            detenerSonidoCorrer();
        }
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

    private void detenerSonidoCorrer() {
        if (suenaCorrer) {
            audio.correr.stop();
            suenaCorrer = false;
        }
    }

    private boolean atenderPausa() {
        if (!Gdx.input.justTouched()) {
            return false;
        }
        Vector2 mouse = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (botonSeguir.contains(mouse)) {
            ui.clic();
            pausado = false;
        } else if (botonReiniciarRonda.contains(mouse)) {
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

    private void reiniciarRondaActual() {
        jugadorUno.reiniciar(spawnUno.x, spawnUno.y, true);
        jugadorDos.reiniciar(spawnDos.x, spawnDos.y, false);
        muerteSubitaActivada = false;
        armas.limpiar();
    }

    private void forzarMuerteSubita() {
        float centroX = arena.getAncho() / 2f;
        float yPiso = Arena.FILAS_PISO * Arena.CELDA;
        jugadorUno.reiniciar(centroX - 80f, yPiso, true);
        jugadorDos.reiniciar(centroX + 80f - Jugador.ANCHO, yPiso, false);
    }

    private void reiniciarRondasYNivel() {
        rondas.reiniciar();
        armas.limpiar();
    }

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

    private List<float[]> crearZonasProhibidas() {
        List<float[]> zonas = new ArrayList<>();
        zonas.add(ArmasEnJuego.zonaDeSpawn(spawnUno.x, spawnUno.y, Jugador.ANCHO, Jugador.ALTO));
        zonas.add(ArmasEnJuego.zonaDeSpawn(spawnDos.x, spawnDos.y, Jugador.ANCHO, Jugador.ALTO));
        float yPiso = Arena.FILAS_PISO * Arena.CELDA;
        zonas.add(ArmasEnJuego.zonaDeSpawn(arena.getAncho() / 2f - 80f, yPiso, 160f, Jugador.ALTO));
        return zonas;
    }

    private void reiniciarPartida() {
        reiniciarRondasYNivel();
        armarJugadores();
    }

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

    private void dibujarVidas(Jugador jugador, Color tinte, float xAncla, boolean haciaLaIzquierda) {
        int partes = jugador.isVivo() ? jugador.getPartes() : 0;
        for (int i = 0; i < VIDAS_INICIALES; i++) {
            float x = haciaLaIzquierda
                ? xAncla - TAMANO_VIDA - i * (TAMANO_VIDA + SEPARACION_VIDA)
                : xAncla + i * (TAMANO_VIDA + SEPARACION_VIDA);
            float y = ALTO_UI - MARGEN_HUD - TAMANO_VIDA;
            ui.rectangulo(batch, rectAux.set(x, y, TAMANO_VIDA, TAMANO_VIDA), COLOR_VIDA_PERDIDA);
            int enEste = MathUtils.clamp(partes - i * Jugador.PARTES_POR_CORAZON, 0, Jugador.PARTES_POR_CORAZON);
            if (enEste > 0) {
                float ancho = TAMANO_VIDA * enEste / Jugador.PARTES_POR_CORAZON;
                float desde = haciaLaIzquierda ? x + TAMANO_VIDA - ancho : x;
                ui.rectangulo(batch, rectAux.set(desde, y, ancho, TAMANO_VIDA), tinte);
            }
        }
    }

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
            return;
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
        Arma arma = jugador.getArma();
        if (arma == null) {
            String vacia = "MANO VACIA";
            float ancho = ui.anchoTexto(vacia, 0.8f);
            ui.texto(batch, vacia, x + (ANCHO_CUADRO_ARMA - ancho) / 2f, abajo + ALTO_CUADRO_ARMA / 2f + 6f, 0.8f, Color.GRAY);
            return;
        }
        batch.setColor(1f, 1f, 1f, 0.85f);
        batch.draw(recursos.brillo, x + 4f, abajo + 4f, ANCHO_CUADRO_ARMA - 8f, ALTO_CUADRO_ARMA - 8f);
        batch.setColor(Color.WHITE);
        Texture textura = arma.getTextura(recursos);
        batch.setColor(arma.getColor());
        float maximoAncho = ANCHO_CUADRO_ARMA - 16f;
        float maximoAlto = ALTO_CUADRO_ARMA - 16f;
        float escala = Math.min(maximoAncho / textura.getWidth(), maximoAlto / textura.getHeight());
        float ancho = textura.getWidth() * escala;
        float alto = textura.getHeight() * escala;
        batch.draw(textura, x + (ANCHO_CUADRO_ARMA - ancho) / 2f, abajo + (ALTO_CUADRO_ARMA - alto) / 2f, ancho, alto);
        batch.setColor(Color.WHITE);
        int maximo = arma.getBalasIniciales();
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

    private void dibujarTextoCentrado(String texto, float escala, float yArriba, Color color) {
        ui.texto(batch, texto, (ANCHO_UI - ui.anchoTexto(texto, escala)) / 2f, yArriba, escala, color);
    }

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
