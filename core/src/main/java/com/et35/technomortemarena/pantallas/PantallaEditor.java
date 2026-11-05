package com.et35.technomortemarena.pantallas;

import java.util.List;

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
import com.et35.technomortemarena.entidades.Jugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import com.et35.technomortemarena.niveles.MapaGuardado;
import com.et35.technomortemarena.niveles.RepositorioMapas;

/**
 * Editor de niveles: la grilla de la arena con una barra de herramientas arriba.
 *
 * <p>Se pinta con el boton izquierdo del mouse y se borra con el derecho. Cada cambio se refleja al
 * instante con el mismo dibujo que tiene la ronda. Los mapas se guardan con nombre en un unico archivo
 * (ver {@link RepositorioMapas}); CARGAR muestra la lista de los guardados.
 *
 * <p>La grilla solo se dibuja aca: la ronda y la pausa no la usan.
 */
public class PantallaEditor extends ScreenAdapter {

    private static final float ANCHO_UI = 960f;
    private static final float ALTO_UI = 540f;
    /** Una sola fila arriba: el boton de las herramientas y las acciones. */
    private static final float ALTO_BARRA = 54f;
    private static final float Y_BARRA = ALTO_UI - ALTO_BARRA;
    private static final float Y_FILA_ACCIONES = Y_BARRA + 9f;
    private static final float ANCHO_BOTON = 110f;
    private static final float PASO_BOTON = 118f;
    private static final float MARGEN = 10f;
    /** El boton "hamburguesa" que abre y cierra el panel de herramientas, y donde empiezan las acciones. */
    private static final float ANCHO_HAMBURGUESA = 44f;
    private static final float X_ACCIONES = MARGEN + ANCHO_HAMBURGUESA + 10f;

    /** El panel de herramientas de la izquierda: una lista vertical con barra de desplazamiento. */
    private static final float ANCHO_PANEL = 222f;
    private static final float ANCHO_HERRAMIENTA = 182f;
    private static final float ALTO_HERRAMIENTA = 44f;
    private static final float PASO_HERRAMIENTA = 52f;
    private static final float ANCHO_BARRA_PANEL = 12f;
    /** Cuantas herramientas entran a la vez en el panel: las demas se ven desplazandolo. */
    private static final int HERRAMIENTAS_VISIBLES = (int) ((Y_BARRA - 2f * MARGEN) / PASO_HERRAMIENTA);
    private static final float LADO_MUESTRA = 22f;

    private static final Rectangle PANEL_LISTA = new Rectangle(180f, 60f, 600f, 380f);
    private static final float ALTO_ITEM = 44f;
    private static final float PASO_ITEM = 50f;
    private static final int ITEMS_VISIBLES = 6;
    private static final int LARGO_MAXIMO_NOMBRE = 30;

    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color COLOR_BORRAR = new Color(1f, 0.3f, 0.3f, 0.35f);
    private static final Color COLOR_BARRA = new Color(0f, 0f, 0f, 0.75f);
    private static final Color COLOR_VELO = new Color(0f, 0f, 0f, 0.6f);

    /** Caracteres de la grilla que se pueden poner con la herramienta; '1' y '2' son los spawns. */
    private static final char[] HERRAMIENTAS = {'#', 'T', 'L', 'R', 'X', '1', '2', 'P', 'E', '.'};
    private static final String[] ETIQUETAS = {
        "1 BLOQUE", "2 TRAMPOLIN", "3 CINTA <", "4 CINTA >", "5 PINCHO", "6 SPAWN J1", "7 SPAWN J2",
        "8 PISTOLAS", "9 ESCOPETAS", "0 BORRAR"};
    /** Los spawners de armas se ponen siempre de 3x3, sin importar el pincel. */
    private static final int LADO_SPAWNER_ARMA = 3;
    private static final int TAMANO_PINCEL_MAXIMO = 4;

    private final RecursosGraficos recursos;
    private final RecursosAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    /**
     * La grilla tiene su propia camara: con el mapa grande no entra entera a un tamano comodo. La rueda
     * acerca y aleja (hacia el cursor), las flechas o WASD y el boton del medio la mueven, y F muestra el
     * mapa entero. Las celdas siempre se ven cuadradas: el ancho del mundo visible es fijo y el alto sale de
     * la proporcion de la zona de la grilla en la ventana.
     */
    private final OrthographicCamera camaraGrilla = new OrthographicCamera();
    private final Viewport viewportMundo = new FitViewport(Arena.ANCHO_VISTA, Arena.ALTO_VISTA, camaraGrilla);
    private static final float ZOOM_MINIMO = 0.35f;
    /** Alcanza para ver el mapa entero aun con la zona de la grilla mas ancha que alta (la del editor lo es). */
    private static final float ZOOM_MAXIMO = 3f;
    /** Cuanto acerca o aleja cada paso de la rueda. */
    private static final float PASO_ZOOM = 1.15f;
    /** Mundo por segundo que se mueve la camara con las teclas, con zoom 1. */
    private static final float VELOCIDAD_CAMARA = 2400f;
    /** Donde estaba el mouse al arrastrar con el boton del medio, en pixeles de pantalla; -1 si no se arrastra. */
    private float arrastreX = -1f;
    private float arrastreY;
    private static final Color COLOR_FUERA = new Color(0.06f, 0.07f, 0.10f, 1f);
    private static final Color COLOR_DENTRO = new Color(0.11f, 0.12f, 0.17f, 1f);
    private static final Color COLOR_LINEA = new Color(1f, 1f, 1f, 0.09f);
    private static final Color COLOR_LINEA_GUIA = new Color(1f, 1f, 1f, 0.22f);
    private static final Color COLOR_CENTRO = new Color(0.45f, 0.70f, 1f, 0.30f);
    private static final Color COLOR_BORDE = new Color(0.55f, 0.65f, 0.85f, 0.70f);
    /** Una linea mas marcada cada tantas celdas, para orientarse en el mapa grande. */
    private static final int CELDAS_POR_GUIA = 10;
    private final Viewport viewportUi = new FitViewport(ANCHO_UI, ALTO_UI, new OrthographicCamera());

    /** celdas[fila][columna], con la fila 0 en el piso. */
    private final char[][] celdas = new char[Arena.FILAS][Arena.COLUMNAS];
    private char herramienta = '#';
    private String nombreMapa;
    private boolean cambiado = true;
    private Arena arenaActual;
    private String mensaje = "";
    private float tiempoMensaje;

    private boolean nombrando;
    private final StringBuilder nombreEnEdicion = new StringBuilder();

    private boolean listaAbierta;
    private List<MapaGuardado> mapasGuardados;
    private int primerVisible;

    private final Rectangle botonHerramientas = new Rectangle(MARGEN, Y_FILA_ACCIONES, ANCHO_HAMBURGUESA, 36f);
    private final Rectangle botonGuardar = new Rectangle(X_ACCIONES, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonCargar = new Rectangle(X_ACCIONES + PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonProbar = new Rectangle(X_ACCIONES + 2 * PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonVolver = new Rectangle(X_ACCIONES + 3 * PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonPincel = new Rectangle(X_ACCIONES + 4 * PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    /** El panel de herramientas: se abre y se cierra con el boton hamburguesa (o la tecla TAB). */
    private boolean panelAbierto = true;
    private final Rectangle panel = new Rectangle(0f, 0f, ANCHO_PANEL, Y_BARRA);
    private final BarraDeDesplazamiento barraPanel = new BarraDeDesplazamiento(
        MARGEN + ANCHO_HERRAMIENTA + 8f, MARGEN, ANCHO_BARRA_PANEL, Y_BARRA - 2f * MARGEN);
    /** Rectangulo de trabajo para cada herramienta del panel, que se arma de cero en cada fotograma. */
    private final Rectangle filaHerramienta = new Rectangle();
    /** Filas que movio la rueda del mouse sobre el panel desde el ultimo fotograma. */
    private int ruedaPanel;
    private final Rectangle botonCerrarLista = new Rectangle(380f, 70f, 200f, 36f);
    private int tamanoPincel = 1;

    private final InputAdapter entrada = new InputAdapter() {
        @Override
        public boolean keyTyped(char caracter) {
            if (!nombrando || caracter < 32 || nombreEnEdicion.length() >= LARGO_MAXIMO_NOMBRE) {
                return false;
            }
            nombreEnEdicion.append(caracter);
            return true;
        }

        @Override
        public boolean keyDown(int tecla) {
            if (!nombrando) {
                return false;
            }
            if (tecla == Input.Keys.BACKSPACE && nombreEnEdicion.length() > 0) {
                nombreEnEdicion.deleteCharAt(nombreEnEdicion.length() - 1);
                return true;
            }
            if (tecla == Input.Keys.ENTER) {
                confirmarNombre();
                return true;
            }
            return false;
        }

        @Override
        public boolean scrolled(float desplazamientoX, float desplazamientoY) {
            if (listaAbierta) {
                desplazarLista(desplazamientoY > 0 ? 1 : -1);
                return true;
            }
            if (panelAbierto) {
                ruedaPanel += desplazamientoY > 0 ? 1 : -1;
                return true;
            }
            return false;
        }
    };

    /** Vuelve a edicion con un mapa ya armado, por ejemplo al salir de probarlo. */
    public PantallaEditor(RecursosGraficos recursos, RecursosAudio audio, String[] filas, String nombre) {
        this(recursos, audio);
        cargarFilas(filas);
        nombreMapa = nombre;
    }

    public PantallaEditor(RecursosGraficos recursos, RecursosAudio audio) {
        this.recursos = recursos;
        this.audio = audio;
        Gdx.input.setInputProcessor(entrada);
        barraPanel.setContenido(HERRAMIENTAS.length, HERRAMIENTAS_VISIBLES);
        // arranca al centro y pegada abajo (limitarCamara la sube lo justo), asi se ve el piso
        camaraGrilla.position.set(Arena.ANCHO_MUNDO / 2f, 0f, 0f);
        for (int fila = 0; fila < Arena.FILAS; fila++) {
            for (int columna = 0; columna < Arena.COLUMNAS; columna++) {
                celdas[fila][columna] = fila < Arena.FILAS_PISO ? '#' : '.';
            }
        }
    }

    @Override
    public void render(float delta) {
        tiempoMensaje -= delta;
        Vector2 mouseUi = viewportUi.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        if (nombrando) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                nombrando = false;
            }
        } else if (listaAbierta) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                listaAbierta = false;
            } else if (Gdx.input.justTouched()) {
                atenderLista(mouseUi);
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                cambiarA(new PantallaMenu(recursos, audio));
                return;
            }
            for (int i = 0; i < HERRAMIENTAS.length; i++) {
                // las teclas 1 a 9 y despues el 0, como en la barra
                int tecla = i < 9 ? Input.Keys.NUM_1 + i : Input.Keys.NUM_0;
                if (Gdx.input.isKeyJustPressed(tecla)) {
                    herramienta = HERRAMIENTAS[i];
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                cambiarTamanoPincel();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                panelAbierto = !panelAbierto;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                verMapaEntero();
            }
            boolean enBarra = mouseUi.y >= Y_BARRA;
            boolean enPanel = panelAbierto && panel.contains(mouseUi);
            if (enBarra) {
                if (Gdx.input.justTouched() && atenderBarra(mouseUi)) {
                    return;
                }
            } else if (enPanel || barraPanel.estaArrastrando()) {
                atenderPanel(mouseUi);
            } else {
                pintarBajoElMouse();
            }
            if (!enPanel && !enBarra && ruedaPanel != 0) {
                acercar(ruedaPanel > 0 ? PASO_ZOOM : 1f / PASO_ZOOM);
            }
            ruedaPanel = 0;
            moverCamara(delta);
        }

        if (cambiado) {
            arenaActual = construirArena();
            cambiado = false;
        }
        dibujar(mouseUi);
    }

    private void pintarBajoElMouse() {
        boolean izquierdo = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean derecho = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        if (!izquierdo && !derecho) {
            return;
        }
        int[] celda = celdaBajoElMouse();
        if (celda == null) {
            return;
        }
        char nuevo = izquierdo ? herramienta : '.';
        if (esSpawn(nuevo)) {
            ponerSpawn(celda[0], celda[1], nuevo);
            return;
        }
        int tamano = esSpawnerDeArma(nuevo) ? LADO_SPAWNER_ARMA : tamanoPincel;
        for (int fila = celda[1]; fila < celda[1] + tamano && fila < Arena.FILAS; fila++) {
            for (int columna = celda[0]; columna < celda[0] + tamano && columna < Arena.COLUMNAS; columna++) {
                if (celdas[fila][columna] != nuevo) {
                    celdas[fila][columna] = nuevo;
                    cambiado = true;
                }
            }
        }
    }

    /** Un jugador tiene un unico spawn: ponerlo en otra celda mueve el anterior. */
    private void ponerSpawn(int columna, int fila, char spawn) {
        for (int f = 0; f < Arena.FILAS; f++) {
            for (int c = 0; c < Arena.COLUMNAS; c++) {
                if (celdas[f][c] == spawn) {
                    celdas[f][c] = '.';
                }
            }
        }
        celdas[fila][columna] = spawn;
        cambiado = true;
    }

    private static boolean esSpawnerDeArma(char caracter) {
        return caracter == 'P' || caracter == 'E';
    }

    private static boolean esSpawn(char caracter) {
        return caracter == '1' || caracter == '2';
    }

    /** Columna y fila de la celda bajo el mouse, o {@code null} si el mouse esta fuera de la grilla. */
    private int[] celdaBajoElMouse() {
        Vector2 mundo = viewportMundo.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        int columna = (int) Math.floor(mundo.x / Arena.CELDA);
        int fila = (int) Math.floor(mundo.y / Arena.CELDA);
        if (columna < 0 || columna >= Arena.COLUMNAS || fila < 0 || fila >= Arena.FILAS) {
            return null;
        }
        return new int[] {columna, fila};
    }

    /** Acerca ({@code factor > 1} aleja) dejando quieto el punto del mapa que esta bajo el cursor. */
    private void acercar(float factor) {
        Vector2 antes = viewportMundo.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        camaraGrilla.zoom = Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, camaraGrilla.zoom * factor));
        camaraGrilla.update();
        Vector2 despues = viewportMundo.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        camaraGrilla.position.add(antes.x - despues.x, antes.y - despues.y, 0f);
        limitarCamara();
    }

    /** Las flechas o WASD mueven la camara, y tambien arrastrar con el boton del medio. */
    private void moverCamara(float delta) {
        float paso = VELOCIDAD_CAMARA * camaraGrilla.zoom * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            camaraGrilla.position.x -= paso;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            camaraGrilla.position.x += paso;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            camaraGrilla.position.y += paso;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            camaraGrilla.position.y -= paso;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            if (arrastreX >= 0f) {
                float mundoPorPixel = viewportMundo.getWorldWidth() * camaraGrilla.zoom / viewportMundo.getScreenWidth();
                camaraGrilla.position.x -= (Gdx.input.getX() - arrastreX) * mundoPorPixel;
                camaraGrilla.position.y += (Gdx.input.getY() - arrastreY) * mundoPorPixel;
            }
            arrastreX = Gdx.input.getX();
            arrastreY = Gdx.input.getY();
        } else {
            arrastreX = -1f;
        }
        limitarCamara();
    }

    /** Aleja la camara hasta ver el mapa entero, centrado. */
    private void verMapaEntero() {
        float zoomAncho = Arena.ANCHO_MUNDO / viewportMundo.getWorldWidth();
        float zoomAlto = Arena.ALTO_MUNDO / viewportMundo.getWorldHeight();
        camaraGrilla.zoom = Math.min(ZOOM_MAXIMO, Math.max(zoomAncho, zoomAlto) * 1.02f);
        camaraGrilla.position.set(Arena.ANCHO_MUNDO / 2f, Arena.ALTO_MUNDO / 2f, 0f);
        limitarCamara();
    }

    /** Que la camara no se vaya lejos del mapa: a lo sumo deja ver un par de celdas de afuera. */
    private void limitarCamara() {
        float margen = 2f * Arena.CELDA;
        float medioAncho = viewportMundo.getWorldWidth() * camaraGrilla.zoom / 2f;
        float medioAlto = viewportMundo.getWorldHeight() * camaraGrilla.zoom / 2f;
        camaraGrilla.position.x = limitarEje(camaraGrilla.position.x, medioAncho, Arena.ANCHO_MUNDO, margen);
        camaraGrilla.position.y = limitarEje(camaraGrilla.position.y, medioAlto, Arena.ALTO_MUNDO, margen);
        camaraGrilla.update();
    }

    private static float limitarEje(float centro, float medio, float largo, float margen) {
        if (2f * medio >= largo + 2f * margen) {
            return largo / 2f; // se ve todo ese eje: centrado
        }
        return Math.max(medio - margen, Math.min(largo - medio + margen, centro));
    }

    /** Devuelve verdadero si el clic cambio de pantalla y el fotograma tiene que cortarse. */
    /** El rectangulo de la herramienta visible numero {@code numero} del panel (0 es la de arriba). */
    private Rectangle herramientaVisible(int numero) {
        float y = Y_BARRA - MARGEN - ALTO_HERRAMIENTA - numero * PASO_HERRAMIENTA;
        return filaHerramienta.set(MARGEN, y, ANCHO_HERRAMIENTA, ALTO_HERRAMIENTA);
    }

    /** El panel de herramientas: la rueda y la barra lo desplazan, y un clic en una herramienta la elige. */
    private void atenderPanel(Vector2 mouseUi) {
        if (ruedaPanel != 0) {
            barraPanel.desplazar(ruedaPanel);
        }
        boolean presionado = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (barraPanel.actualizar(mouseUi, presionado, Gdx.input.justTouched()) || !Gdx.input.justTouched()) {
            return;
        }
        for (int numero = 0; numero < HERRAMIENTAS_VISIBLES; numero++) {
            int indice = barraPanel.getPrimero() + numero;
            if (indice < HERRAMIENTAS.length && herramientaVisible(numero).contains(mouseUi)) {
                herramienta = HERRAMIENTAS[indice];
                ui.clic();
                return;
            }
        }
    }

    private boolean atenderBarra(Vector2 mouseUi) {
        if (botonHerramientas.contains(mouseUi)) {
            panelAbierto = !panelAbierto;
            return false;
        }
        if (botonGuardar.contains(mouseUi)) {
            iniciarNombre();
        } else if (botonCargar.contains(mouseUi)) {
            mapasGuardados = RepositorioMapas.cargarTodos();
            primerVisible = 0;
            listaAbierta = true;
        } else if (botonProbar.contains(mouseUi)) {
            String[] filas = filasDeCeldas();
            String nombre = nombreMapa;
            cambiarA(new PantallaArena(recursos, audio, Arena.desdeGrilla(filas),
                () -> new PantallaEditor(recursos, audio, filas, nombre)));
            return true;
        } else if (botonPincel.contains(mouseUi)) {
            cambiarTamanoPincel();
        } else if (botonVolver.contains(mouseUi)) {
            cambiarA(new PantallaMenu(recursos, audio));
            return true;
        }
        return false;
    }

    private void cambiarTamanoPincel() {
        tamanoPincel = tamanoPincel % TAMANO_PINCEL_MAXIMO + 1;
    }

    private void atenderLista(Vector2 mouseUi) {
        if (botonCerrarLista.contains(mouseUi)) {
            listaAbierta = false;
            return;
        }
        for (int i = 0; i < ITEMS_VISIBLES; i++) {
            int indice = primerVisible + i;
            if (indice >= mapasGuardados.size()) {
                break;
            }
            if (rectanguloItem(i).contains(mouseUi)) {
                cargarMapa(mapasGuardados.get(indice));
                listaAbierta = false;
                return;
            }
        }
    }

    private void desplazarLista(int pasos) {
        int maximo = Math.max(0, mapasGuardados.size() - ITEMS_VISIBLES);
        primerVisible = Math.max(0, Math.min(maximo, primerVisible - pasos));
    }

    private Rectangle rectanguloItem(int posicion) {
        float y = PANEL_LISTA.y + PANEL_LISTA.height - 60f - posicion * PASO_ITEM;
        return new Rectangle(PANEL_LISTA.x + 30f, y, PANEL_LISTA.width - 60f, ALTO_ITEM);
    }

    private void iniciarNombre() {
        nombreEnEdicion.setLength(0);
        nombreEnEdicion.append(nombreMapa != null ? nombreMapa : nombreNuevo());
        nombrando = true;
    }

    private void confirmarNombre() {
        String nombre = nombreEnEdicion.toString().trim();
        nombrando = false;
        if (nombre.isEmpty()) {
            mostrar("El nombre estaba vacio: no se guardo");
            return;
        }
        RepositorioMapas.guardar(new MapaGuardado(nombre, filasDeCeldas()));
        nombreMapa = nombre;
        mostrar("Guardado: " + nombre);
    }

    private String nombreNuevo() {
        List<MapaGuardado> existentes = RepositorioMapas.cargarTodos();
        int numero = existentes.size() + 1;
        while (existe(existentes, "Mapa " + numero)) {
            numero++;
        }
        return "Mapa " + numero;
    }

    private static boolean existe(List<MapaGuardado> mapas, String nombre) {
        for (MapaGuardado mapa : mapas) {
            if (mapa.getNombre().equals(nombre)) {
                return true;
            }
        }
        return false;
    }

    private void cargarMapa(MapaGuardado mapa) {
        cargarFilas(mapa.getFilas());
        nombreMapa = mapa.getNombre();
        mostrar("Cargado: " + nombreMapa);
    }

    private void cargarFilas(String[] filas) {
        for (int i = 0; i < Arena.FILAS; i++) {
            celdas[Arena.FILAS - 1 - i] = filas[i].toCharArray();
        }
        cambiado = true;
    }

    private void mostrar(String texto) {
        mensaje = texto;
        tiempoMensaje = 3f;
    }

    private String[] filasDeCeldas() {
        String[] filas = new String[Arena.FILAS];
        for (int i = 0; i < Arena.FILAS; i++) {
            filas[i] = new String(celdas[Arena.FILAS - 1 - i]);
        }
        return filas;
    }

    private Arena construirArena() {
        return Arena.desdeGrilla(filasDeCeldas());
    }

    private void dibujar(Vector2 mouseUi) {
        ScreenUtils.clear(FONDO);
        viewportMundo.apply();
        batch.setProjectionMatrix(viewportMundo.getCamera().combined);

        batch.begin();
        dibujarGrilla();
        for (Bloque bloque : arenaActual.getBloques()) {
            Rectangle caja = bloque.getArea();
            batch.setColor(bloque.getColor());
            batch.draw(recursos.piso, caja.x, caja.y, caja.width, caja.height);
        }
        batch.setColor(Color.WHITE);
        dibujarSpawns();
        if (!nombrando && !listaAbierta && mouseUi.y < Y_BARRA && !(panelAbierto && panel.contains(mouseUi))) {
            resaltarCeldaBajoElMouse();
        }
        batch.end();

        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        dibujarBarra(mouseUi);
        if (panelAbierto) {
            dibujarPanel(mouseUi);
        }
        if (tiempoMensaje > 0f) {
            float xMensaje = panelAbierto ? ANCHO_PANEL + MARGEN : MARGEN;
            ui.texto(batch, mensaje, xMensaje, Y_BARRA - 8f, 1.1f, Color.WHITE);
        }
        String etiquetaMapa = "Mapa: " + (nombreMapa != null ? nombreMapa : "sin guardar");
        ui.texto(batch, etiquetaMapa, ANCHO_UI - 330f, Y_BARRA - 8f, 1.1f, Color.WHITE);
        String ayuda = "RUEDA: ZOOM   FLECHAS / WASD O BOTON DEL MEDIO: MOVER   F: VER TODO";
        float xAyuda = panelAbierto ? ANCHO_PANEL + MARGEN : MARGEN;
        ui.texto(batch, ayuda, xAyuda, 22f, 0.85f, Color.LIGHT_GRAY);
        if (listaAbierta) {
            dibujarLista(mouseUi);
        }
        if (nombrando) {
            dibujarNombre();
        }
        batch.end();
    }

    /**
     * La grilla: el mapa con un fondo un poco mas claro que lo de afuera, lineas finas por celda (solo si
     * las celdas se ven lo bastante grandes como para que no sea ruido), una linea mas marcada cada
     * {@link #CELDAS_POR_GUIA} celdas, la del centro resaltada para armar mapas parejos, y el borde del mapa.
     */
    private void dibujarGrilla() {
        float ancho = Arena.COLUMNAS * Arena.CELDA;
        float alto = Arena.FILAS * Arena.CELDA;
        float mundoPorPixel = viewportMundo.getWorldWidth() * camaraGrilla.zoom / viewportMundo.getScreenWidth();
        float fuera = viewportMundo.getWorldWidth() * camaraGrilla.zoom;
        ui.rectangulo(batch, new Rectangle(-fuera, -fuera, ancho + 2f * fuera, alto + 2f * fuera), COLOR_FUERA);
        ui.rectangulo(batch, new Rectangle(0f, 0f, ancho, alto), COLOR_DENTRO);
        boolean celdasVisibles = Arena.CELDA / mundoPorPixel >= 6f;
        float fina = mundoPorPixel;
        float guia = 1.5f * mundoPorPixel;
        for (int columna = 0; columna <= Arena.COLUMNAS; columna++) {
            boolean esGuia = columna % CELDAS_POR_GUIA == 0;
            if (esGuia || celdasVisibles) {
                ui.rectangulo(batch, new Rectangle(columna * Arena.CELDA, 0f, esGuia ? guia : fina, alto),
                    esGuia ? COLOR_LINEA_GUIA : COLOR_LINEA);
            }
        }
        for (int fila = 0; fila <= Arena.FILAS; fila++) {
            boolean esGuia = fila % CELDAS_POR_GUIA == 0;
            if (esGuia || celdasVisibles) {
                ui.rectangulo(batch, new Rectangle(0f, fila * Arena.CELDA, ancho, esGuia ? guia : fina),
                    esGuia ? COLOR_LINEA_GUIA : COLOR_LINEA);
            }
        }
        ui.rectangulo(batch, new Rectangle(ancho / 2f - mundoPorPixel, 0f, 2f * mundoPorPixel, alto), COLOR_CENTRO);
        float borde = 2f * mundoPorPixel;
        ui.rectangulo(batch, new Rectangle(-borde, -borde, ancho + 2f * borde, borde), COLOR_BORDE);
        ui.rectangulo(batch, new Rectangle(-borde, alto, ancho + 2f * borde, borde), COLOR_BORDE);
        ui.rectangulo(batch, new Rectangle(-borde, 0f, borde, alto), COLOR_BORDE);
        ui.rectangulo(batch, new Rectangle(ancho, 0f, borde, alto), COLOR_BORDE);
    }

    /** Vista previa del objeto que se colocaria en la celda bajo el mouse, del tamaño exacto de la celda. */
    private void resaltarCeldaBajoElMouse() {
        int[] celda = celdaBajoElMouse();
        if (celda == null) {
            return;
        }
        if (esSpawn(herramienta)) {
            Color tinte = new Color(colorDeSpawn(herramienta));
            tinte.a = 0.4f;
            dibujarJugador(celda[0], celda[1], tinte);
            return;
        }
        int tamano = esSpawnerDeArma(herramienta) ? LADO_SPAWNER_ARMA : tamanoPincel;
        Rectangle area = new Rectangle(celda[0] * Arena.CELDA, celda[1] * Arena.CELDA,
            tamano * Arena.CELDA, tamano * Arena.CELDA);
        Bloque vista = Arena.bloquePara(herramienta, area);
        if (vista == null) {
            ui.rectangulo(batch, area, COLOR_BORRAR);
            return;
        }
        Color color = new Color(vista.getColor());
        color.a = 0.5f;
        batch.setColor(color);
        batch.draw(recursos.piso, area.x, area.y, area.width, area.height);
        batch.setColor(Color.WHITE);
    }

    /** Dibuja la silueta del jugador con los pies en la celda dada, del tamaño real que tiene en la ronda. */
    private void dibujarJugador(int columna, int fila, Color tinte) {
        batch.setColor(tinte);
        batch.draw(recursos.jugador, columna * Arena.CELDA, fila * Arena.CELDA, Jugador.ANCHO, Jugador.ALTO);
        batch.setColor(Color.WHITE);
    }

    private static Color colorDeSpawn(char spawn) {
        return spawn == '1' ? Jugador.TINTE_UNO : Jugador.TINTE_DOS;
    }

    private void dibujarSpawns() {
        for (int fila = 0; fila < Arena.FILAS; fila++) {
            for (int columna = 0; columna < Arena.COLUMNAS; columna++) {
                char celda = celdas[fila][columna];
                if (esSpawn(celda)) {
                    dibujarJugador(columna, fila, colorDeSpawn(celda));
                }
            }
        }
    }

    private void dibujarBarra(Vector2 mouseUi) {
        ui.rectangulo(batch, new Rectangle(0f, Y_BARRA, ANCHO_UI, ALTO_BARRA), COLOR_BARRA);
        // el boton hamburguesa: tres rayas
        ui.boton(batch, botonHerramientas, panelAbierto || botonHerramientas.contains(mouseUi), "", 1f);
        for (int i = 0; i < 3; i++) {
            ui.rectangulo(batch, new Rectangle(botonHerramientas.x + 11f, botonHerramientas.y + 9f + i * 8f, 22f, 3f),
                Color.WHITE);
        }
        String actual = "HERRAMIENTA: " + ETIQUETAS[indiceHerramienta()].substring(2);
        ui.texto(batch, actual, X_ACCIONES + 5 * PASO_BOTON + 6f, Y_FILA_ACCIONES + 26f, 1.0f, Color.LIGHT_GRAY);
        ui.boton(batch, botonGuardar, botonGuardar.contains(mouseUi), "GUARDAR", 1.1f);
        ui.boton(batch, botonCargar, botonCargar.contains(mouseUi), "CARGAR", 1.1f);
        ui.boton(batch, botonProbar, botonProbar.contains(mouseUi), "PROBAR", 1.1f);
        ui.boton(batch, botonVolver, botonVolver.contains(mouseUi), "VOLVER", 1.1f);
        ui.boton(batch, botonPincel, botonPincel.contains(mouseUi), "PINCEL " + tamanoPincel + "x" + tamanoPincel, 1.0f);
    }

    private int indiceHerramienta() {
        for (int i = 0; i < HERRAMIENTAS.length; i++) {
            if (HERRAMIENTAS[i] == herramienta) {
                return i;
            }
        }
        return 0;
    }

    /**
     * El panel de herramientas a la izquierda: cada una con una muestra de su color y su tecla, la elegida
     * resaltada, y la barra para ver las que no entran.
     */
    private void dibujarPanel(Vector2 mouseUi) {
        ui.rectangulo(batch, panel, COLOR_BARRA);
        for (int numero = 0; numero < HERRAMIENTAS_VISIBLES; numero++) {
            int indice = barraPanel.getPrimero() + numero;
            if (indice >= HERRAMIENTAS.length) {
                break;
            }
            Rectangle fila = herramientaVisible(numero);
            boolean resaltada = HERRAMIENTAS[indice] == herramienta || fila.contains(mouseUi);
            // la identidad del sonido es la herramienta: las filas comparten el mismo Rectangle
            ui.boton(batch, fila, resaltada, "", 1f, HERRAMIENTAS[indice]);
            Rectangle muestra = new Rectangle(fila.x + 10f, fila.y + (fila.height - LADO_MUESTRA) / 2f, LADO_MUESTRA, LADO_MUESTRA);
            ui.rectangulo(batch, muestra, colorDeHerramienta(HERRAMIENTAS[indice]));
            ui.texto(batch, ETIQUETAS[indice], muestra.x + LADO_MUESTRA + 10f, fila.y + fila.height / 2f + 7f, 1.0f, Color.WHITE);
        }
        barraPanel.dibujar(batch, ui, mouseUi);
    }

    /** El color con que se ve cada herramienta en el panel: el del bloque que pone, o el del spawn. */
    private Color colorDeHerramienta(char caracter) {
        if (esSpawn(caracter)) {
            return colorDeSpawn(caracter);
        }
        Bloque bloque = Arena.bloquePara(caracter, new Rectangle(0f, 0f, 1f, 1f));
        return bloque != null ? bloque.getColor() : COLOR_BORRAR;
    }

    private void dibujarLista(Vector2 mouseUi) {
        ui.rectangulo(batch, new Rectangle(0f, 0f, ANCHO_UI, ALTO_UI), COLOR_VELO);
        ui.rectangulo(batch, PANEL_LISTA, COLOR_BARRA);
        String titulo = mapasGuardados.isEmpty() ? "No hay mapas guardados" : "Elegi un mapa";
        float x = PANEL_LISTA.x + (PANEL_LISTA.width - ui.anchoTexto(titulo, 1.6f)) / 2f;
        ui.texto(batch, titulo, x, PANEL_LISTA.y + PANEL_LISTA.height - 14f, 1.6f, Color.WHITE);
        for (int i = 0; i < ITEMS_VISIBLES; i++) {
            int indice = primerVisible + i;
            if (indice >= mapasGuardados.size()) {
                break;
            }
            Rectangle item = rectanguloItem(i);
            ui.boton(batch, item, item.contains(mouseUi), mapasGuardados.get(indice).getNombre(), 1.3f);
        }
        ui.boton(batch, botonCerrarLista, botonCerrarLista.contains(mouseUi), "CERRAR (ESC)", 1.1f);
    }

    private void dibujarNombre() {
        ui.rectangulo(batch, new Rectangle(0f, 0f, ANCHO_UI, ALTO_UI), COLOR_VELO);
        Rectangle caja = new Rectangle(230f, 220f, 500f, 110f);
        ui.rectangulo(batch, caja, COLOR_BARRA);
        ui.texto(batch, "Nombre del mapa:", caja.x + 20f, caja.y + caja.height - 16f, 1.3f, Color.WHITE);
        ui.texto(batch, nombreEnEdicion + "_", caja.x + 20f, caja.y + caja.height - 52f, 1.5f, Color.WHITE);
        ui.texto(batch, "ENTER guarda   ESC cancela", caja.x + 20f, caja.y + 28f, 1.0f, Color.WHITE);
    }

    /** Cambio de pantalla: {@code setScreen} no libera esta pantalla, hay que disponerla a mano. */
    private void cambiarA(Screen siguiente) {
        Game juego = (Game) Gdx.app.getApplicationListener();
        juego.setScreen(siguiente);
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewportUi.update(width, height, true);
        // La grilla ocupa lo que queda debajo de la barra de herramientas. El ancho del mundo visible es
        // fijo y el alto sale de la proporcion de esa zona, asi las celdas se ven cuadradas.
        int alturaGrilla = Math.max(1, (int) (height * (Y_BARRA / ALTO_UI)));
        viewportMundo.setWorldSize(Arena.ANCHO_VISTA, Arena.ANCHO_VISTA * alturaGrilla / Math.max(1, width));
        viewportMundo.setScreenBounds(0, 0, width, alturaGrilla);
        viewportMundo.apply();
        limitarCamara();
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        batch.dispose();
        ui.dispose();
    }
}
