package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;

/**
 * La hoja de la espada como una caja rotada: es la forma que se dibuja y la que choca.
 *
 * <p>Nace en una base (el pivote del brazo mas lo que avanza la estocada) y se extiende
 * {@link #LARGO} hacia el angulo dado. Es un rectangulo de {@link #LARGO} por {@link #GROSOR} girado,
 * asi que el choque es exacto: no es una caja alineada con los ejes que engorda al girar.
 *
 * <p>Los choques usan el teorema del eje de separacion (SAT) sobre cuadrilateros convexos: dos
 * formas no se tocan si existe una recta que las separa, y para rectangulos alcanza con probar los
 * ejes de sus lados.
 */
public final class Hoja {

    public static final float LARGO = 56f * Arena.ESCALA;
    public static final float GROSOR = 10f * Arena.ESCALA;
    /** Cuanto avanza la base de la hoja en lo mas fuerte de una estocada. */
    public static final float ALCANCE_ESTOCADA = 24f * Arena.ESCALA;

    /** Angulo maximo entre dos posiciones de la hoja probadas al barrer un tajo, en radianes (unos 11 grados). */
    private static final float PASO_BARRIDO = 0.2f;

    /** Cuatro vertices (x, y) en orden: base, base, punta, punta. */
    private final float[] vertices = new float[8];
    private float baseX;
    private float baseY;
    private float angulo;

    /**
     * Coloca la hoja.
     *
     * @param pivoteX   hombro del jugador
     * @param pivoteY   hombro del jugador
     * @param angulo    hacia donde apunta, en radianes (0 es hacia la derecha, positivo hacia arriba)
     * @param avance    cuanto se adelanta la base a lo largo de la hoja, para la estocada
     */
    public void colocar(float pivoteX, float pivoteY, float angulo, float avance) {
        this.angulo = angulo;
        float dx = (float) Math.cos(angulo);
        float dy = (float) Math.sin(angulo);
        baseX = pivoteX + dx * avance;
        baseY = pivoteY + dy * avance;
        // Perpendicular a la direccion, para el grosor.
        float px = -dy * GROSOR / 2f;
        float py = dx * GROSOR / 2f;
        vertices[0] = baseX + px;
        vertices[1] = baseY + py;
        vertices[2] = baseX - px;
        vertices[3] = baseY - py;
        vertices[4] = baseX - px + dx * LARGO;
        vertices[5] = baseY - py + dy * LARGO;
        vertices[6] = baseX + px + dx * LARGO;
        vertices[7] = baseY + py + dy * LARGO;
    }

    public float getBaseX() {
        return baseX;
    }

    public float getBaseY() {
        return baseY;
    }

    public float getAngulo() {
        return angulo;
    }

    /** Copia de los ocho valores (x, y de cada uno de los cuatro vertices). */
    public float[] getVertices() {
        return vertices.clone();
    }

    /** Punta de la hoja (el centro del borde mas lejano). */
    public float getPuntaX() {
        return (vertices[4] + vertices[6]) / 2f;
    }

    public float getPuntaY() {
        return (vertices[5] + vertices[7]) / 2f;
    }

    /** Rectangulo alineado con los ejes que contiene a la hoja. Sirve para estimar cuanto se superponen. */
    public Rectangle limites(Rectangle destino) {
        float minX = vertices[0];
        float maxX = vertices[0];
        float minY = vertices[1];
        float maxY = vertices[1];
        for (int i = 2; i < 8; i += 2) {
            minX = Math.min(minX, vertices[i]);
            maxX = Math.max(maxX, vertices[i]);
            minY = Math.min(minY, vertices[i + 1]);
            maxY = Math.max(maxY, vertices[i + 1]);
        }
        return destino.set(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean solapa(Rectangle rectangulo) {
        float[] otro = {
            rectangulo.x, rectangulo.y,
            rectangulo.x + rectangulo.width, rectangulo.y,
            rectangulo.x + rectangulo.width, rectangulo.y + rectangulo.height,
            rectangulo.x, rectangulo.y + rectangulo.height,
        };
        return cuadrilateroSolapa(vertices, otro);
    }

    public boolean solapa(Hoja otra) {
        return cuadrilateroSolapa(vertices, otra.vertices);
    }

    /**
     * Verdadero si dos cuadrilateros convexos se superponen (tocarse en un borde no cuenta).
     * Para cada lado de cada uno se proyectan los dos sobre su normal; si en algun eje las
     * proyecciones no se solapan, hay una recta que los separa.
     */
    static boolean cuadrilateroSolapa(float[] a, float[] b) {
        return !hayEjeSeparador(a, a, b) && !hayEjeSeparador(b, a, b);
    }

    private static boolean hayEjeSeparador(float[] fuenteDeEjes, float[] a, float[] b) {
        for (int i = 0; i < 8; i += 2) {
            int siguiente = (i + 2) % 8;
            float ejeX = fuenteDeEjes[i + 1] - fuenteDeEjes[siguiente + 1];
            float ejeY = fuenteDeEjes[siguiente] - fuenteDeEjes[i];
            float minA = Float.POSITIVE_INFINITY;
            float maxA = Float.NEGATIVE_INFINITY;
            float minB = Float.POSITIVE_INFINITY;
            float maxB = Float.NEGATIVE_INFINITY;
            for (int k = 0; k < 8; k += 2) {
                float pa = a[k] * ejeX + a[k + 1] * ejeY;
                float pb = b[k] * ejeX + b[k + 1] * ejeY;
                minA = Math.min(minA, pa);
                maxA = Math.max(maxA, pa);
                minB = Math.min(minB, pb);
                maxB = Math.max(maxB, pb);
            }
            if (maxA <= minB || maxB <= minA) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cantidad de posiciones intermedias a probar para barrer el giro de {@code desde} a {@code hasta}
     * por el arco mas corto. Con una hoja de este largo, un giro grande de un fotograma al otro podria
     * saltar por encima de un cuerpo sin tocarlo: se prueba cada {@link #PASO_BARRIDO} radianes.
     */
    public static int pasosDeBarrido(float desde, float hasta) {
        float diferencia = Math.abs(diferenciaAngular(desde, hasta));
        return Math.max(1, (int) Math.ceil(diferencia / PASO_BARRIDO));
    }

    /** El angulo del paso {@code i} (de 1 a {@code pasos}) de un barrido: en {@code pasos} llega a {@code hasta}. */
    public static float anguloDeBarrido(float desde, float hasta, int i, int pasos) {
        return desde + diferenciaAngular(desde, hasta) * i / pasos;
    }

    /** Diferencia con signo de {@code hasta - desde} por el arco mas corto, en (-PI, PI]. */
    public static float diferenciaAngular(float desde, float hasta) {
        double d = (hasta - desde) % (2 * Math.PI);
        if (d > Math.PI) {
            d -= 2 * Math.PI;
        } else if (d <= -Math.PI) {
            d += 2 * Math.PI;
        }
        return (float) d;
    }
}
