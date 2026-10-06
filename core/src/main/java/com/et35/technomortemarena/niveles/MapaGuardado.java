package com.et35.technomortemarena.niveles;

/** Un mapa del editor: su nombre y sus filas de texto, la primera la de arriba. */
public final class MapaGuardado {

    private final String nombre;
    private final String[] filas;

    public MapaGuardado(String nombre, String[] filas) {
        this.nombre = nombre;
        this.filas = filas.clone();
    }

    public String getNombre() {
        return nombre;
    }

    /** Copia de las filas de texto, la primera la de arriba. */
    public String[] getFilas() {
        return filas.clone();
    }
}
