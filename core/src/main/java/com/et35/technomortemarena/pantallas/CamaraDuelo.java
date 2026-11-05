package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;

/**
 * La camara del duelo: sigue a los dos duelistas y se aleja cuando se separan, para que los dos entren
 * siempre en pantalla. Con los duelistas cerca muestra {@link Arena#ANCHO_VISTA} x {@link Arena#ALTO_VISTA}
 * (zoom 1); como mucho se aleja hasta mostrar el mapa entero ({@link #ZOOM_MAXIMO}). Nunca muestra lo que
 * hay fuera del mapa: si los duelistas estan cerca de un borde, la camara se queda pegada a ese borde.
 *
 * <p>Se mueve con suavidad: cada fotograma recorre parte del camino hacia donde deberia estar. La primera
 * vez va directo, para no arrancar la ronda con un barrido.
 */
public final class CamaraDuelo {

    /** El zoom con el que se ve el mapa entero. */
    public static final float ZOOM_MAXIMO = Arena.ANCHO_MUNDO / Arena.ANCHO_VISTA;
    /** Lugar libre que se deja alrededor de los duelistas, para ver venir lo que hay cerca. */
    static final float MARGEN_X = 700f;
    static final float MARGEN_Y = 500f;
    /** Que tan rapido alcanza la camara a su objetivo: mas alto, mas nerviosa. */
    private static final float RAPIDEZ = 5f;

    private float x;
    private float y;
    private float zoom = 1f;
    private boolean iniciada;

    /**
     * Mueve la camara hacia donde tiene que estar este fotograma.
     *
     * @param uno, dos las cajas de los duelistas; {@code dos} puede ser {@code null} (el anfitrion solo)
     * @param todoElMapa si hay que mostrar el mapa entero, como en la edicion entre rondas
     */
    public void seguir(float delta, Rectangle uno, Rectangle dos, boolean todoElMapa) {
        float minimoX = uno.x;
        float maximoX = uno.x + uno.width;
        float minimoY = uno.y;
        float maximoY = uno.y + uno.height;
        if (dos != null) {
            minimoX = Math.min(minimoX, dos.x);
            maximoX = Math.max(maximoX, dos.x + dos.width);
            minimoY = Math.min(minimoY, dos.y);
            maximoY = Math.max(maximoY, dos.y + dos.height);
        }
        float zoomObjetivo;
        float xObjetivo;
        float yObjetivo;
        if (todoElMapa) {
            zoomObjetivo = ZOOM_MAXIMO;
            xObjetivo = Arena.ANCHO_MUNDO / 2f;
            yObjetivo = Arena.ALTO_MUNDO / 2f;
        } else {
            // el ancho que hace falta para que entren los dos con margen, mirando tambien el alto
            float anchoNecesario = Math.max(maximoX - minimoX + 2f * MARGEN_X,
                (maximoY - minimoY + 2f * MARGEN_Y) * Arena.ANCHO_VISTA / Arena.ALTO_VISTA);
            zoomObjetivo = Math.max(1f, Math.min(ZOOM_MAXIMO, anchoNecesario / Arena.ANCHO_VISTA));
            xObjetivo = (minimoX + maximoX) / 2f;
            yObjetivo = (minimoY + maximoY) / 2f;
        }
        float avance = iniciada ? 1f - (float) Math.exp(-RAPIDEZ * delta) : 1f;
        iniciada = true;
        zoom += (zoomObjetivo - zoom) * avance;
        x += (xObjetivo - x) * avance;
        y += (yObjetivo - y) * avance;
        // que no se vea afuera del mapa
        float medioAncho = Arena.ANCHO_VISTA * zoom / 2f;
        float medioAlto = Arena.ALTO_VISTA * zoom / 2f;
        x = Math.max(medioAncho, Math.min(Arena.ANCHO_MUNDO - medioAncho, x));
        y = Math.max(medioAlto, Math.min(Arena.ALTO_MUNDO - medioAlto, y));
    }

    /** La proxima vez va directo a su lugar, sin barrido: al empezar una partida nueva. */
    public void reiniciar() {
        iniciada = false;
    }

    /** Pone la camara de libGDX donde corresponde. */
    public void aplicar(OrthographicCamera camara) {
        camara.position.set(x, y, 0f);
        camara.zoom = zoom;
        camara.update();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZoom() {
        return zoom;
    }
}
