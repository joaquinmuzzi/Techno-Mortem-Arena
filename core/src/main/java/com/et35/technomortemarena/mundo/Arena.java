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
    private final float agujeroDesde;
    private final float agujeroHasta;

    public Arena(float ancho, float alto, float alturaPiso) {
        this(ancho, alto, alturaPiso, 0f, 0f);
    }

    /**
     * @param agujeroDesde x donde empieza el agujero del piso
     * @param agujeroHasta x donde termina el agujero; si no es mayor que {@code agujeroDesde}, no hay agujero
     */
    public Arena(float ancho, float alto, float alturaPiso, float agujeroDesde, float agujeroHasta) {
        this.ancho = ancho;
        this.alto = alto;
        this.alturaPiso = alturaPiso;
        this.agujeroDesde = agujeroDesde;
        this.agujeroHasta = agujeroHasta;
    }

    /** Arena plana de 960x540 con el piso a 64 px del borde inferior. */
    public static Arena porDefecto() {
        return new Arena(960f, 540f, 64f);
    }

    /** Igual que {@link #porDefecto()} pero con un agujero de 192 px centrado en el piso. */
    public static Arena conAgujero() {
        return new Arena(960f, 540f, 64f, 384f, 576f);
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

    /** Verdadero si la coordenada horizontal cae dentro del agujero del piso (no hay piso debajo). */
    public boolean hayAgujeroEn(float x) {
        return x > agujeroDesde && x < agujeroHasta;
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
