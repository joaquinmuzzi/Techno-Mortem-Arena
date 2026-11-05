package com.et35.technomortemarena.entidades;

/**
 * Lo que un duelista puede tener en la mano. La espada se tiene al empezar cada ronda; si se la tira,
 * rebota y queda en el mapa hasta que alguien la agarre. La pistola y la escopeta se agarran del mapa y
 * tienen municion limitada: al quedarse sin balas se vuelve a la espada.
 *
 * <p>El dano se mide en sextos de corazon: un corazon son {@link Jugador#PARTES_POR_CORAZON} partes.
 */
public enum TipoArma {

    /** Tirada saca un corazon entero, como un golpe con la hoja. No tiene balas. */
    ESPADA(6, 0),
    /** Tirada saca un tercio de corazon. Trae 6 balas. */
    PISTOLA(2, 6),
    /** Tirada saca un tercio de corazon. Trae 3 disparos de {@link #PERDIGONES} perdigones. */
    ESCOPETA(2, 3);

    /** Dano de una bala de pistola: un tercio de corazon. */
    public static final int DANIO_BALA = 2;
    /** Dano de cada perdigon de la escopeta: un sexto de corazon. */
    public static final int DANIO_PERDIGON = 1;
    /** Dano de un punetazo sin arma: un sexto, menos que una bala. */
    public static final int DANIO_PUNIO = 1;
    /** Perdigones por disparo de escopeta, abiertos en abanico. */
    public static final int PERDIGONES = 5;
    /** El mayor numero de balas que puede traer un arma: lo usa la red para validar. */
    public static final int MAX_BALAS = 6;

    private final int danioLanzada;
    private final int balasIniciales;

    TipoArma(int danioLanzada, int balasIniciales) {
        this.danioLanzada = danioLanzada;
        this.balasIniciales = balasIniciales;
    }

    /** Partes de corazon que saca si se la tira y toca al rival. */
    public int getDanioLanzada() {
        return danioLanzada;
    }

    /** Balas con las que viene recien agarrada; 0 para la espada. */
    public int getBalasIniciales() {
        return balasIniciales;
    }

    /** Verdadero si es un arma de fuego (dispara balas). */
    public boolean esDeFuego() {
        return balasIniciales > 0;
    }

    /** El arma con ese numero, o {@code null} si es 0 o no existe: es el numero que viaja por la red. */
    public static TipoArma porCodigo(int codigo) {
        TipoArma[] todas = values();
        return codigo >= 1 && codigo <= todas.length ? todas[codigo - 1] : null;
    }

    /** El numero que viaja por la red: 0 sin arma, 1 espada, 2 pistola, 3 escopeta. */
    public static int codigoDe(TipoArma arma) {
        return arma == null ? 0 : arma.ordinal() + 1;
    }
}
