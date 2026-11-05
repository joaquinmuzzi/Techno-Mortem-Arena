package com.et35.technomortemarena.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Barra de desplazamiento vertical para una lista de filas: la pista y una manija que se arrastra con el
 * mouse. Un clic en la pista, fuera de la manija, avanza o retrocede una pagina. Solo se ve (y solo
 * responde) cuando hay mas filas que las que entran.
 *
 * <p>Lleva cual es la primera fila visible; la pantalla que la usa dibuja las filas desde ahi. La rueda
 * del mouse la atiende la pantalla (es un evento de su {@code InputProcessor}) y la pasa con
 * {@link #desplazar}.
 */
final class BarraDeDesplazamiento {

    private static final float ALTO_MINIMO_MANIJA = 24f;
    private static final Color COLOR_PISTA = new Color(0f, 0f, 0f, 0.45f);

    private final Rectangle pista;
    private final Rectangle manija = new Rectangle();
    private int total;
    private int visibles = 1;
    private int primero;
    private boolean arrastrando;
    /** Donde se agarro la manija, medido desde su borde de abajo: asi no salta al empezar a arrastrar. */
    private float agarre;

    BarraDeDesplazamiento(float x, float y, float ancho, float alto) {
        pista = new Rectangle(x, y, ancho, alto);
    }

    /** Cuantas filas hay y cuantas entran a la vez. Acomoda la primera visible si ya no hace falta tanto. */
    void setContenido(int total, int visibles) {
        this.total = total;
        this.visibles = Math.max(1, visibles);
        setPrimero(primero);
    }

    int getPrimero() {
        return primero;
    }

    void setPrimero(int fila) {
        primero = Math.max(0, Math.min(maximo(), fila));
    }

    /** Mueve la lista {@code filas} filas (positivo hacia abajo de la lista). */
    void desplazar(int filas) {
        setPrimero(primero + filas);
    }

    boolean hayParaDesplazar() {
        return total > visibles;
    }

    boolean estaArrastrando() {
        return arrastrando;
    }

    private int maximo() {
        return Math.max(0, total - visibles);
    }

    private Rectangle calcularManija() {
        float alto = Math.max(ALTO_MINIMO_MANIJA, pista.height * visibles / Math.max(1, total));
        float recorrido = pista.height - alto;
        float fraccion = maximo() == 0 ? 0f : (float) primero / maximo();
        return manija.set(pista.x, pista.y + pista.height - alto - recorrido * fraccion, pista.width, alto);
    }

    /**
     * Atiende el mouse.
     *
     * @param presionado si el boton izquierdo esta apretado
     * @param recienPresionado si se apreto en este fotograma
     * @return {@code true} si la barra uso el clic: la pantalla no tiene que hacer nada mas con el
     */
    boolean actualizar(Vector2 mouse, boolean presionado, boolean recienPresionado) {
        if (!hayParaDesplazar()) {
            arrastrando = false;
            return false;
        }
        Rectangle actual = calcularManija();
        if (arrastrando) {
            if (!presionado) {
                arrastrando = false;
            } else {
                float recorrido = pista.height - actual.height;
                float abajoDeseado = mouse.y - agarre;
                float fraccion = recorrido <= 0f ? 0f : (pista.y + recorrido - abajoDeseado) / recorrido;
                setPrimero(Math.round(Math.max(0f, Math.min(1f, fraccion)) * maximo()));
            }
            return true;
        }
        if (recienPresionado && actual.contains(mouse)) {
            arrastrando = true;
            agarre = mouse.y - actual.y;
            return true;
        }
        if (recienPresionado && pista.contains(mouse)) {
            desplazar(mouse.y > actual.y ? -visibles : visibles);
            return true;
        }
        return false;
    }

    /** Dibuja la pista y la manija, con el batch ya abierto y la proyeccion de la interfaz. */
    void dibujar(SpriteBatch batch, Ui ui, Vector2 mouse) {
        if (!hayParaDesplazar()) {
            return;
        }
        ui.rectangulo(batch, pista, COLOR_PISTA);
        Rectangle actual = calcularManija();
        boolean resaltada = arrastrando || actual.contains(mouse);
        ui.rectangulo(batch, actual, resaltada ? Ui.COLOR_BOTON_RESALTADO : Ui.COLOR_BOTON);
    }
}
