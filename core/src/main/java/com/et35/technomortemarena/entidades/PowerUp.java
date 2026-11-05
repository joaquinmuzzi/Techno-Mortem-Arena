package com.et35.technomortemarena.entidades;

/**
 * Las mejoras que caen del cielo durante la ronda. Quien pasa por encima la agarra (con o sin arma en la
 * mano) y le dura {@link #DURACION} segundos; agarrar otra igual vuelve a llenar el tiempo.
 */
public enum PowerUp {

    /** Corre y rueda un 40 % mas rapido. */
    VELOCIDAD,
    /** Salta un 35 % mas alto. */
    SALTO,
    /** Las armas de fuego disparan mas seguido y la escopeta casi no abre los perdigones. */
    PUNTERIA;

    /** Segundos que dura una mejora. */
    public static final float DURACION = 10f;
    public static final float FACTOR_VELOCIDAD = 1.4f;
    public static final float FACTOR_SALTO = 1.35f;
    /** Cuanto del tiempo de recarga entre disparos queda con PUNTERIA. */
    public static final float FACTOR_RECARGA = 0.6f;
    /** Cuanto de la abertura del abanico de la escopeta queda con PUNTERIA. */
    public static final float FACTOR_ABANICO = 0.3f;

    /** La mejora con ese numero, o {@code null} si no existe. */
    public static PowerUp porIndice(int indice) {
        PowerUp[] todas = values();
        return indice >= 0 && indice < todas.length ? todas[indice] : null;
    }
}
