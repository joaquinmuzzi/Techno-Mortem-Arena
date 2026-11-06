package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;

public final class CamaraDuelo {
    public static final float ZOOM_MAXIMO = Arena.ANCHO_MUNDO / Arena.ANCHO_VISTA;
    static final float MARGEN_X = 700f;
    static final float MARGEN_Y = 500f;
    private static final float RAPIDEZ = 5f;

    private float x;
    private float y;
    private float zoom = 1f;
    private boolean iniciada;

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
        float medioAncho = Arena.ANCHO_VISTA * zoom / 2f;
        float medioAlto = Arena.ALTO_VISTA * zoom / 2f;
        x = Math.max(medioAncho, Math.min(Arena.ANCHO_MUNDO - medioAncho, x));
        y = Math.max(medioAlto, Math.min(Arena.ALTO_MUNDO - medioAlto, y));
    }

    public void reiniciar() {
        iniciada = false;
    }

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
