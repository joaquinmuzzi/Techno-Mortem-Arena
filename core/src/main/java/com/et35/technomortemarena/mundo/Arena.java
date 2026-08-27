package com.et35.technomortemarena.mundo;

/**
 * Escenario cerrado donde se desarrolla el duelo.
 *
 * <p>Las medidas estan en unidades de mundo, que en este proyecto equivalen a pixeles: la arena por
 * defecto mide 960x540, la misma relacion 16:9 de la ventana. Trabajar en pixeles simplifica las
 * cuentas porque los tamanios de los sprites y las posiciones usan la misma escala.
 *
 * <p>La clase guarda sus medidas como campos privados e inmutables ({@code final}) y las expone por
 * getters. Asi ninguna otra clase puede modificar el tamanio de la arena una vez creada, y cuando
 * mas adelante hagan falta varios mapas alcanzara con construir instancias distintas.
 */
public class Arena {

    private final float ancho;
    private final float alto;
    private final float alturaPiso;

    public Arena(float ancho, float alto, float alturaPiso) {
        this.ancho = ancho;
        this.alto = alto;
        this.alturaPiso = alturaPiso;
    }

    /** Arena plana de 960x540 con el piso a 64 px del borde inferior. */
    public static Arena porDefecto() {
        return new Arena(960f, 540f, 64f);
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }

    /** Altura de la superficie sobre la que caminan los jugadores, medida desde y = 0. */
    public float getAlturaPiso() {
        return alturaPiso;
    }

    /**
     * Devuelve {@code x} corregido para que una entidad de ancho {@code anchoEntidad} no atraviese
     * las paredes laterales.
     *
     * <p>Es la implementacion del "espacio cerrado y delimitado" de la propuesta: en lugar de
     * permitir el avance territorial de Nidhogg, la arena encierra a los dos jugadores.
     */
    public float limitarX(float x, float anchoEntidad) {
        float maximo = ancho - anchoEntidad;
        if (x < 0f) {
            return 0f;
        }
        if (x > maximo) {
            return maximo;
        }
        return x;
    }
}
