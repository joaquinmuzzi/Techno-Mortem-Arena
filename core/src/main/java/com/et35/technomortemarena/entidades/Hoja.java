package com.et35.technomortemarena.entidades;

import com.badlogic.gdx.math.Rectangle;
import com.et35.technomortemarena.mundo.Arena;

public final class Hoja {
    public static final float LARGO = 56f * Arena.ESCALA;
    public static final float GROSOR = 10f * Arena.ESCALA;
    public static final float ALCANCE_ESTOCADA = 32f * Arena.ESCALA;

    private static final float PASO_BARRIDO = 0.2f;

    private final float[] vertices = new float[8];
    private float baseX;
    private float baseY;
    private float angulo;

    public void colocar(float pivoteX, float pivoteY, float angulo, float avance) {
        this.angulo = angulo;
        float dx = (float) Math.cos(angulo);
        float dy = (float) Math.sin(angulo);
        baseX = pivoteX + dx * avance;
        baseY = pivoteY + dy * avance;
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

    public float[] getVertices() {
        return vertices.clone();
    }

    public float getPuntaX() {
        return (vertices[4] + vertices[6]) / 2f;
    }

    public float getPuntaY() {
        return (vertices[5] + vertices[7]) / 2f;
    }

    public float[] puntoDeContacto(Hoja otra) {
        float d1x = (float) Math.cos(angulo) * LARGO;
        float d1y = (float) Math.sin(angulo) * LARGO;
        float d2x = (float) Math.cos(otra.angulo) * LARGO;
        float d2y = (float) Math.sin(otra.angulo) * LARGO;
        float rx = baseX - otra.baseX;
        float ry = baseY - otra.baseY;
        float a = d1x * d1x + d1y * d1y;
        float e = d2x * d2x + d2y * d2y;
        float f = d2x * rx + d2y * ry;
        float c = d1x * rx + d1y * ry;
        float b = d1x * d2x + d1y * d2y;
        float denominador = a * e - b * b;
        float s = denominador > 1e-6f ? limitar01((b * f - c * e) / denominador) : 0f;
        float t = (b * s + f) / e;
        if (t < 0f) {
            t = 0f;
            s = limitar01(-c / a);
        } else if (t > 1f) {
            t = 1f;
            s = limitar01((b - c) / a);
        }
        return new float[] {s, t};
    }

    private static float limitar01(float valor) {
        return Math.max(0f, Math.min(1f, valor));
    }

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

    public static int pasosDeBarrido(float desde, float hasta) {
        float diferencia = Math.abs(diferenciaAngular(desde, hasta));
        return Math.max(1, (int) Math.ceil(diferencia / PASO_BARRIDO));
    }

    public static float anguloDeBarrido(float desde, float hasta, int i, int pasos) {
        return desde + diferenciaAngular(desde, hasta) * i / pasos;
    }

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
