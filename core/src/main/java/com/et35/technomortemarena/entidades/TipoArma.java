package com.et35.technomortemarena.entidades;

/**
 * Lo que un duelista puede tener en la mano. La espada es el arma de siempre: se tiene al empezar cada
 * ronda y reaparece en la mano un rato despues de tirar la que se tenia. La pistola se agarra del mapa y
 * tiene municion limitada: al quedarse sin balas se vuelve a la espada.
 *
 * <p>El dano se mide en tercios de corazon: un corazon son {@link Jugador#TERCIOS_POR_CORAZON} tercios.
 */
public enum TipoArma {

    /** Dano al tirarla: un corazon entero, como un golpe con la hoja. */
    ESPADA(3),
    /** Dano al tirarla: un tercio de corazon (es chica y liviana). */
    PISTOLA(1);

    /** Balas con las que viene una pistola recien agarrada. */
    public static final int BALAS_PISTOLA = 6;
    /** Dano de una bala, en tercios de corazon. */
    public static final int DANIO_BALA = 1;

    private final int danioLanzada;

    TipoArma(int danioLanzada) {
        this.danioLanzada = danioLanzada;
    }

    /** Tercios de corazon que saca si se la tira y toca al rival. */
    public int getDanioLanzada() {
        return danioLanzada;
    }

    /** El arma con ese numero, o {@code null} si es 0 o no existe: es el numero que viaja por la red. */
    public static TipoArma porCodigo(int codigo) {
        TipoArma[] todas = values();
        return codigo >= 1 && codigo <= todas.length ? todas[codigo - 1] : null;
    }

    /** El numero que viaja por la red: 0 sin arma, 1 espada, 2 pistola. */
    public static int codigoDe(TipoArma arma) {
        return arma == null ? 0 : arma.ordinal() + 1;
    }
}
