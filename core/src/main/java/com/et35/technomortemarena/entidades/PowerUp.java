package com.et35.technomortemarena.entidades;

public enum PowerUp {
    VELOCIDAD,
    SALTO,
    PUNTERIA;

    public static final float DURACION = 10f;
    public static final float FACTOR_VELOCIDAD = 1.4f;
    public static final float FACTOR_SALTO = 1.35f;
    public static final float FACTOR_RECARGA = 0.6f;
    public static final float FACTOR_ABANICO = 0.3f;

    public static PowerUp porIndice(int indice) {
        PowerUp[] todas = values();
        return indice >= 0 && indice < todas.length ? todas[indice] : null;
    }
}
