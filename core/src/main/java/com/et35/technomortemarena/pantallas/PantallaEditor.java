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
    private static final float ALTO_BARRA = 100f;
    private static final float Y_BARRA = ALTO_UI - ALTO_BARRA;
    private static final float Y_FILA_HERRAMIENTAS = 488f;
    private static final float Y_FILA_ACCIONES = 444f;
    private static final float ANCHO_BOTON = 110f;
    private static final float PASO_BOTON = 118f;
    private static final float MARGEN = 10f;

    private static final Rectangle PANEL_LISTA = new Rectangle(180f, 60f, 600f, 380f);
    private static final float ALTO_ITEM = 44f;
    private static final float PASO_ITEM = 50f;
    private static final int ITEMS_VISIBLES = 6;
    private static final int LARGO_MAXIMO_NOMBRE = 30;

    private static final Color FONDO = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color COLOR_GRILLA = new Color(1f, 1f, 1f, 0.22f);
    private static final Color COLOR_BORRAR = new Color(1f, 0.3f, 0.3f, 0.35f);
    private static final Color COLOR_BARRA = new Color(0f, 0f, 0f, 0.75f);
    private static final Color COLOR_VELO = new Color(0f, 0f, 0f, 0.6f);

    /** Caracteres de la grilla que se pueden poner con la herramienta; '1' y '2' son los spawns. */
    private static final char[] HERRAMIENTAS = {'#', 'T', 'L', 'R', 'X', '1', '2', '.'};
    private static final String[] ETIQUETAS = {
        "1 BLOQUE", "2 TRAMPOLIN", "3 CINTA <", "4 CINTA >", "5 PINCHO", "6 SPAWN J1", "7 SPAWN J2", "8 BORRAR"};
    private static final int TAMANO_PINCEL_MAXIMO = 4;

    private final RecursosGraficos recursos;
    private final SpriteBatch batch = new SpriteBatch();
    private final Ui ui = new Ui();
    private final Viewport viewportMundo = new FitViewport(Arena.ANCHO_MUNDO, Arena.ALTO_MUNDO,
        new OrthographicCamera());
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

    private final Rectangle[] botonesHerramienta = new Rectangle[HERRAMIENTAS.length];
    private final Rectangle botonGuardar = new Rectangle(MARGEN, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonCargar = new Rectangle(MARGEN + PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonProbar = new Rectangle(MARGEN + 2 * PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonVolver = new Rectangle(MARGEN + 3 * PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
    private final Rectangle botonPincel = new Rectangle(MARGEN + 4 * PASO_BOTON, Y_FILA_ACCIONES, ANCHO_BOTON, 36f);
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
            if (!listaAbierta) {
                return false;
            }
            desplazarLista(desplazamientoY > 0 ? 1 : -1);
            return true;
        }
    };

    /** Vuelve a edicion con un mapa ya armado, por ejemplo al salir de probarlo. */
    public PantallaEditor(RecursosGraficos recursos, String[] filas, String nombre) {
        this(recursos);
        cargarFilas(filas);
        nombreMapa = nombre;
    }

    public PantallaEditor(RecursosGraficos recursos) {
        this.recursos = recursos;
        Gdx.input.setInputProcessor(entrada);
        for (int i = 0; i < HERRAMIENTAS.length; i++) {
            botonesHerramienta[i] = new Rectangle(MARGEN + i * PASO_BOTON, Y_FILA_HERRAMIENTAS, ANCHO_BOTON, 40f);
        }
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
                cambiarA(new PantallaMenu(recursos));
                return;
            }
            for (int i = 0; i < HERRAMIENTAS.length; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                    herramienta = HERRAMIENTAS[i];
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                cambiarTamanoPincel();
            }
            boolean enBarra = mouseUi.y >= Y_BARRA;
            if (enBarra) {
                if (Gdx.input.justTouched() && atenderBarra(mouseUi)) {
                    return;
                }
            } else {
                pintarBajoElMouse();
            }
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
        int tamano = tamanoPincel;
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

    /** Devuelve verdadero si el clic cambio de pantalla y el fotograma tiene que cortarse. */
    private boolean atenderBarra(Vector2 mouseUi) {
        for (int i = 0; i < HERRAMIENTAS.length; i++) {
            if (botonesHerramienta[i].contains(mouseUi)) {
                herramienta = HERRAMIENTAS[i];
            }
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
            cambiarA(new PantallaArena(recursos, Arena.desdeGrilla(filas),
                () -> new PantallaEditor(recursos, filas, nombre)));
            return true;
        } else if (botonPincel.contains(mouseUi)) {
            cambiarTamanoPincel();
        } else if (botonVolver.contains(mouseUi)) {
            cambiarA(new PantallaMenu(recursos));
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
        if (!nombrando && !listaAbierta && mouseUi.y < Y_BARRA) {
            resaltarCeldaBajoElMouse();
        }
        batch.end();

        viewportUi.apply();
        batch.setProjectionMatrix(viewportUi.getCamera().combined);
        batch.begin();
        dibujarBarra(mouseUi);
        if (tiempoMensaje > 0f) {
            ui.texto(batch, mensaje, MARGEN, Y_BARRA - 8f, 1.1f, Color.WHITE);
        }
        String etiquetaMapa = "Mapa: " + (nombreMapa != null ? nombreMapa : "sin guardar");
        ui.texto(batch, etiquetaMapa, ANCHO_UI - 330f, Y_BARRA - 8f, 1.1f, Color.WHITE);
        if (listaAbierta) {
            dibujarLista(mouseUi);
        }
        if (nombrando) {
            dibujarNombre();
        }
        batch.end();
    }

    private void dibujarGrilla() {
        // Dos pixeles de pantalla, sin importar cuanto se achique la arena para entrar en la ventana.
        float grosor = 2f * viewportMundo.getWorldWidth() / viewportMundo.getScreenWidth();
        for (int columna = 0; columna <= Arena.COLUMNAS; columna++) {
            ui.rectangulo(batch, new Rectangle(columna * Arena.CELDA, 0f, grosor, Arena.FILAS * Arena.CELDA),
                COLOR_GRILLA);
        }
        for (int fila = 0; fila <= Arena.FILAS; fila++) {
            ui.rectangulo(batch, new Rectangle(0f, fila * Arena.CELDA, Arena.COLUMNAS * Arena.CELDA, grosor),
                COLOR_GRILLA);
        }
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
        int tamano = tamanoPincel;
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
        for (int i = 0; i < HERRAMIENTAS.length; i++) {
            boolean seleccionada = herramienta == HERRAMIENTAS[i];
            boolean resaltada = seleccionada || botonesHerramienta[i].contains(mouseUi);
            ui.boton(batch, botonesHerramienta[i], resaltada, ETIQUETAS[i], 1.0f);
        }
        ui.boton(batch, botonGuardar, botonGuardar.contains(mouseUi), "GUARDAR", 1.1f);
        ui.boton(batch, botonCargar, botonCargar.contains(mouseUi), "CARGAR", 1.1f);
        ui.boton(batch, botonProbar, botonProbar.contains(mouseUi), "PROBAR", 1.1f);
        ui.boton(batch, botonVolver, botonVolver.contains(mouseUi), "VOLVER", 1.1f);
        ui.boton(batch, botonPincel, botonPincel.contains(mouseUi), "PINCEL " + tamanoPincel + "x" + tamanoPincel, 1.0f);
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
        viewportMundo.update(width, height, true);
        // La grilla ocupa lo que queda debajo de la barra de herramientas.
        int alturaGrilla = (int) (height * (Y_BARRA / ALTO_UI));
        viewportMundo.setScreenBounds(0, 0, width, alturaGrilla);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        batch.dispose();
        ui.dispose();
    }
}
