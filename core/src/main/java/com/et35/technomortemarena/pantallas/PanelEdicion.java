package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.mundo.Arena;
import com.et35.technomortemarena.mundo.Bloque;
import com.et35.technomortemarena.partida.ColocadorPiezas;
import com.et35.technomortemarena.partida.EdicionNivel;
import com.et35.technomortemarena.partida.PiezaNivel;
import java.util.List;

/**
 * La interfaz de la edicion del nivel entre rondas: las tres tarjetas con los objetos para elegir, la
 * vista previa de la pieza sobre el mapa (verde si se puede colocar, roja si no) y las zonas prohibidas
 * alrededor de los spawns.
 *
 * <p>Del lado de quien edita lee el mouse y el teclado y lo vuelca en una {@link EntradaJugador}: el
 * punto apuntado, el objeto elegido (tarjeta con clic o teclas 1, 2 y 3) y el clic sobre el mapa para
 * colocar. {@code PantallaArena} decide que hacer con eso, segun sea anfitrion o cliente.
 */
final class PanelEdicion {

    private static final float ANCHO_UI = 960f;
    private static final float ANCHO_TARJETA = 270f;
    private static final float ALTO_TARJETA = 112f;
    private static final float SEPARACION = 14f;
    private static final Color FONDO_TARJETA = new Color(0.08f, 0.09f, 0.13f, 0.92f);
    private static final Color BORDE_ELEGIDA = new Color(1f, 0.85f, 0.3f, 1f);
    private static final Color VALIDA = new Color(0.3f, 1f, 0.4f, 0.55f);
    private static final Color INVALIDA = new Color(1f, 0.25f, 0.25f, 0.55f);
    private static final Color ZONA = new Color(1f, 0.2f, 0.2f, 0.14f);
    private static final Color COLOR_TIEMPO = new Color(1f, 0.9f, 0.4f, 1f);

    private final Rectangle[] tarjetas = new Rectangle[EdicionNivel.OPCIONES];
    private final Rectangle rectAux = new Rectangle();
    private final Vector2 punteroAux = new Vector2();
    /** El objeto que el jugador de esta instancia tiene elegido; -1 si todavia no eligio. */
    private int eleccionLocal = -1;

    PanelEdicion() {
        float total = EdicionNivel.OPCIONES * ANCHO_TARJETA + (EdicionNivel.OPCIONES - 1) * SEPARACION;
        float x0 = (ANCHO_UI - total) / 2f;
        for (int i = 0; i < tarjetas.length; i++) {
            tarjetas[i] = new Rectangle(x0 + i * (ANCHO_TARJETA + SEPARACION), 14f, ANCHO_TARJETA, ALTO_TARJETA);
        }
    }

    /** Vuelve a no tener nada elegido: se llama al empezar cada edicion. */
    void reiniciar() {
        eleccionLocal = -1;
    }

    int getEleccionLocal() {
        return eleccionLocal;
    }

    /**
     * Lee lo que hace el editor de esta instancia: elige con las teclas 1 a 3 o con un clic en una tarjeta,
     * y pide colocar con un clic en cualquier otro lado. Siempre apunta con el mouse.
     *
     * @param mundo viewport de la arena, para llevar el cursor a coordenadas del mundo
     * @param interfaz viewport de la interfaz, para saber si el clic fue sobre una tarjeta
     */
    void leer(Viewport mundo, Viewport interfaz, EntradaJugador destino) {
        destino.limpiar();
        for (int i = 0; i < EdicionNivel.OPCIONES; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                eleccionLocal = i;
            }
        }
        boolean clic = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean sobreTarjeta = false;
        punteroAux.set(Gdx.input.getX(), Gdx.input.getY());
        interfaz.unproject(punteroAux);
        for (int i = 0; i < tarjetas.length; i++) {
            if (tarjetas[i].contains(punteroAux)) {
                sobreTarjeta = true;
                if (clic) {
                    eleccionLocal = i;
                }
            }
        }
        punteroAux.set(Gdx.input.getX(), Gdx.input.getY());
        mundo.unproject(punteroAux);
        destino.hayPuntero = true;
        destino.apuntaX = punteroAux.x;
        destino.apuntaY = punteroAux.y;
        destino.eleccionPieza = eleccionLocal;
        destino.colocarPieza = clic && !sobreTarjeta;
    }

    /**
     * Dibuja sobre el mapa (con el batch ya abierto y la proyeccion de la arena): las zonas prohibidas y, si
     * hay una pieza elegida, su vista previa.
     */
    void dibujarEnElMundo(SpriteBatch batch, RecursosGraficos recursos, List<float[]> zonas,
                          PiezaNivel pieza, int cursorColumna, int cursorFila, boolean valida) {
        batch.setColor(ZONA);
        for (float[] zona : zonas) {
            float x0 = Math.max(0f, zona[0]) * Arena.CELDA;
            float y0 = Math.max(0f, zona[1]) * Arena.CELDA;
            float x1 = Math.min(Arena.COLUMNAS, zona[2]) * Arena.CELDA;
            float y1 = Math.min(Arena.FILAS, zona[3]) * Arena.CELDA;
            batch.draw(recursos.piso, x0, y0, x1 - x0, y1 - y0);
        }
        if (pieza != null) {
            int[] origen = ColocadorPiezas.origenPara(pieza, cursorColumna, cursorFila);
            batch.setColor(valida ? VALIDA : INVALIDA);
            for (int dy = 0; dy < pieza.getAlto(); dy++) {
                for (int dx = 0; dx < pieza.getAncho(); dx++) {
                    if (pieza.celda(dx, dy) != '.') {
                        batch.draw(recursos.piso, (origen[0] + dx) * Arena.CELDA, (origen[1] + dy) * Arena.CELDA,
                            Arena.CELDA, Arena.CELDA);
                    }
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * Dibuja el cartel y las tarjetas (con el batch ya abierto y la proyeccion de la interfaz).
     *
     * @param opciones los tres objetos ofrecidos (algunos pueden ser {@code null})
     * @param eleccion el objeto elegido que se resalta, o -1
     * @param soyEditor si esta instancia es la de quien edita: solo ahi las tarjetas responden al mouse
     * @param colorEditor el color del jugador que edita
     */
    void dibujarEnLaInterfaz(SpriteBatch batch, Ui ui, PiezaNivel[] opciones, int eleccion, boolean soyEditor,
                             int editor, Color colorEditor, float segundos, boolean valida) {
        String titulo = soyEditor ? "ELEGI UN OBJETO Y COLOCALO EN EL MAPA"
            : "EL JUGADOR " + (editor + 1) + " ESTA COLOCANDO UN OBJETO";
        float yTitulo = 478f;
        ui.texto(batch, titulo, (ANCHO_UI - ui.anchoTexto(titulo, 1.3f)) / 2f, yTitulo, 1.3f, colorEditor);
        String tiempo = ((int) Math.ceil(Math.max(0f, segundos))) + "s";
        ui.texto(batch, tiempo, (ANCHO_UI - ui.anchoTexto(tiempo, 1.6f)) / 2f, yTitulo - 26f, 1.6f, COLOR_TIEMPO);
        if (soyEditor) {
            String ayuda = "1 / 2 / 3 O CLIC EN LA TARJETA PARA ELEGIR - CLIC EN EL MAPA PARA COLOCAR";
            ui.texto(batch, ayuda, (ANCHO_UI - ui.anchoTexto(ayuda, 0.95f)) / 2f, yTitulo - 58f, 0.95f, Color.LIGHT_GRAY);
            if (eleccion >= 0 && !valida) {
                String aviso = "ACA NO SE PUEDE COLOCAR";
                ui.texto(batch, aviso, (ANCHO_UI - ui.anchoTexto(aviso, 1.1f)) / 2f, yTitulo - 80f, 1.1f, INVALIDA);
            }
        } else {
            String espera = "SE COLOCA SOLO SI SE ACABA EL TIEMPO";
            ui.texto(batch, espera, (ANCHO_UI - ui.anchoTexto(espera, 0.95f)) / 2f, yTitulo - 58f, 0.95f, Color.LIGHT_GRAY);
        }
        for (int i = 0; i < tarjetas.length; i++) {
            dibujarTarjeta(batch, ui, tarjetas[i], opciones[i], i, i == eleccion);
        }
    }

    /** Si el cursor esta sobre una tarjeta (para no mostrar la vista previa debajo). */
    boolean cursorSobreTarjeta(Viewport interfaz) {
        punteroAux.set(Gdx.input.getX(), Gdx.input.getY());
        interfaz.unproject(punteroAux);
        for (Rectangle tarjeta : tarjetas) {
            if (tarjeta.contains(punteroAux)) {
                return true;
            }
        }
        return false;
    }

    private void dibujarTarjeta(SpriteBatch batch, Ui ui, Rectangle area, PiezaNivel pieza, int indice, boolean elegida) {
        if (elegida) {
            ui.rectangulo(batch, rectAux.set(area.x - 3f, area.y - 3f, area.width + 6f, area.height + 6f), BORDE_ELEGIDA);
        }
        ui.rectangulo(batch, area, FONDO_TARJETA);
        if (pieza == null) {
            return;
        }
        ui.texto(batch, (indice + 1) + " - " + pieza.getNombre(), area.x + 10f, area.y + area.height - 8f, 1.0f, Color.WHITE);
        // la forma, a escala para que entre en el espacio de abajo
        float libreAncho = area.width - 20f;
        float libreAlto = area.height - 40f;
        float lado = Math.min(Math.min(libreAncho / pieza.getAncho(), libreAlto / pieza.getAlto()), 9f);
        float x0 = area.x + (area.width - lado * pieza.getAncho()) / 2f;
        float y0 = area.y + 8f + (libreAlto - lado * pieza.getAlto()) / 2f;
        for (int dy = 0; dy < pieza.getAlto(); dy++) {
            for (int dx = 0; dx < pieza.getAncho(); dx++) {
                char celda = pieza.celda(dx, dy);
                if (celda != '.') {
                    rectAux.set(x0 + dx * lado, y0 + dy * lado, lado, lado);
                    ui.rectangulo(batch, rectAux, colorDe(celda));
                }
            }
        }
    }

    /** El color con que se dibuja un caracter de la grilla: el mismo del bloque que representa. */
    static Color colorDe(char celda) {
        Bloque bloque = Arena.bloquePara(celda, new Rectangle(0f, 0f, 1f, 1f));
        return bloque != null ? bloque.getColor() : Color.WHITE;
    }
}
