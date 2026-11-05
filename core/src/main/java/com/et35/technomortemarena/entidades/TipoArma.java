package com.et35.technomortemarena.entidades;

/**
 * Lo que un duelista puede tener en la mano. La espada se tiene al empezar cada ronda; si se la tira,
 * rebota y queda en el mapa hasta que alguien la agarre. Las armas de fuego se agarran del mapa y tienen
 * municion limitada: al quedarse sin balas se queda con las manos vacias. La espada laser es muy rara:
 * pega mas fuerte, no se puede desarmar y atraviesa a la espada comun.
 *
 * <p>El dano se mide en sextos de corazon: un corazon son {@link Jugador#PARTES_POR_CORAZON} partes.
 */
public enum TipoArma {

    /** Pega y, tirada, saca un corazon entero. No tiene balas. */
    ESPADA(6, 6, 0),
    /** Tirada saca un tercio de corazon. Trae 6 balas. */
    PISTOLA(2, 0, 6),
    /** Tirada saca un tercio de corazon. Trae 3 disparos de {@link #PERDIGONES} perdigones. */
    ESCOPETA(2, 0, 3),
    /** Rayos largos y rapidos, sin caida, de un sexto de corazon cada uno. Trae 20. */
    RIFLE_LASER(2, 0, 20),
    /** Un cohete que explota al chocar: saca un corazon en un area y rompe los bloques de alrededor. */
    LANZACOHETES(2, 0, 1),
    /** Espada roja: pega 1 corazon y 1/3, no se puede desarmar, atraviesa a la comun y refleja perfecto. */
    ESPADA_LASER(8, 8, 0);

    /** Dano de una bala de pistola: un tercio de corazon. */
    public static final int DANIO_BALA = 2;
    /** Dano de cada perdigon de la escopeta: un sexto de corazon. */
    public static final int DANIO_PERDIGON = 1;
    /** Dano de cada rayo del rifle laser: un sexto de corazon. */
    public static final int DANIO_LASER = 1;
    /** Dano de la explosion del cohete a quien quede dentro del radio: un corazon. */
    public static final int DANIO_EXPLOSION = 6;
    /** Dano de un punetazo sin arma: un sexto, menos que una bala. */
    public static final int DANIO_PUNIO = 1;
    /** Perdigones por disparo de escopeta, abiertos en abanico. */
    public static final int PERDIGONES = 5;
    /** El mayor numero de balas que puede traer un arma. */
    public static final int MAX_BALAS = 20;

    private final int danioLanzada;
    private final int danioHoja;
    private final int balasIniciales;

    TipoArma(int danioLanzada, int danioHoja, int balasIniciales) {
        this.danioLanzada = danioLanzada;
        this.danioHoja = danioHoja;
        this.balasIniciales = balasIniciales;
    }

    /** Partes de corazon que saca si se la tira y toca al rival. */
    public int getDanioLanzada() {
        return danioLanzada;
    }

    /** Partes de corazon que saca un golpe con la hoja; 0 si no es una espada. */
    public int getDanioHoja() {
        return danioHoja;
    }

    /** Balas con las que viene recien agarrada; 0 para las espadas. */
    public int getBalasIniciales() {
        return balasIniciales;
    }

    /** Verdadero si es un arma de fuego (dispara). */
    public boolean esDeFuego() {
        return balasIniciales > 0;
    }

    /** Verdadero si es una espada (comun o laser): tiene hoja, choca y refleja. */
    public boolean esEspada() {
        return this == ESPADA || this == ESPADA_LASER;
    }

    /** El arma con ese numero, o {@code null} si es 0 o no existe. */
    public static TipoArma porCodigo(int codigo) {
        TipoArma[] todas = values();
        return codigo >= 1 && codigo <= todas.length ? todas[codigo - 1] : null;
    }

    /** El numero de cada arma: 0 sin arma, 1 espada, 2 pistola, 3 escopeta, y asi. */
    public static int codigoDe(TipoArma arma) {
        return arma == null ? 0 : arma.ordinal() + 1;
    }
}
