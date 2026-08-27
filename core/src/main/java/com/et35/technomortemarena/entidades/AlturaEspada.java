package com.et35.technomortemarena.entidades;

/**
 * Las tres alturas de guardia de la propuesta: cadera, pecho y cabeza.
 *
 * <p>Es la mecanica central del juego. Cuando se implemente el combate, una estocada a la misma
 * altura que la guardia del rival se bloquea, y a una altura distinta resulta letal. Modelarlo como
 * {@code enum} y no como un {@code int} tiene dos ventajas concretas:
 *
 * <ul>
 *   <li>Es imposible construir un estado invalido: no existe "altura 7".
 *   <li>El compilador puede avisar si un {@code switch} sobre esta enumeracion olvida un caso.
 * </ul>
 *
 * <p>Cada constante guarda el desplazamiento vertical en pixeles al que se dibuja la espada, medido
 * desde los pies del jugador.
 */
public enum AlturaEspada {

    CADERA(38f),
    PECHO(56f),
    CABEZA(74f);

    private final float desplazamientoY;

    AlturaEspada(float desplazamientoY) {
        this.desplazamientoY = desplazamientoY;
    }

    /** Altura en pixeles a la que se dibuja la hoja, relativa a los pies del jugador. */
    public float getDesplazamientoY() {
        return desplazamientoY;
    }

    /** Sube la guardia un nivel. Si ya esta en {@link #CABEZA}, se queda ahi. */
    public AlturaEspada subir() {
        if (this == CABEZA) {
            return this;
        }
        return values()[ordinal() + 1];
    }

    /** Baja la guardia un nivel. Si ya esta en {@link #CADERA}, se queda ahi. */
    public AlturaEspada bajar() {
        if (this == CADERA) {
            return this;
        }
        return values()[ordinal() - 1];
    }
}
