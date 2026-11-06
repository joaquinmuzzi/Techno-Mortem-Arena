package com.et35.technomortemarena.niveles;

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

    public String[] getFilas() {
        return filas.clone();
    }
}
