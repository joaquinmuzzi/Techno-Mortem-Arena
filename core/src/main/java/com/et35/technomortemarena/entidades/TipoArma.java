package com.et35.technomortemarena.entidades;

public enum TipoArma {
    ESPADA(6, 6, 0),
    PISTOLA(2, 0, 6),
    ESCOPETA(2, 0, 3),
    RIFLE_LASER(2, 0, 20),
    LANZACOHETES(2, 0, 1),
    ESPADA_LASER(8, 8, 0);

    public static final int DANIO_BALA = 2;
    public static final int DANIO_PERDIGON = 1;
    public static final int DANIO_LASER = 1;
    public static final int DANIO_EXPLOSION = 6;
    public static final int DANIO_PUNIO = 1;
    public static final int PERDIGONES = 5;
    public static final int MAX_BALAS = 20;

    private final int danioLanzada;
    private final int danioHoja;
    private final int balasIniciales;

    TipoArma(int danioLanzada, int danioHoja, int balasIniciales) {
        this.danioLanzada = danioLanzada;
        this.danioHoja = danioHoja;
        this.balasIniciales = balasIniciales;
    }

    public int getDanioLanzada() {
        return danioLanzada;
    }

    public int getDanioHoja() {
        return danioHoja;
    }

    public int getBalasIniciales() {
        return balasIniciales;
    }

    public boolean esDeFuego() {
        return balasIniciales > 0;
    }

    public boolean esEspada() {
        return this == ESPADA || this == ESPADA_LASER;
    }

    public static TipoArma porCodigo(int codigo) {
        TipoArma[] todas = values();
        return codigo >= 1 && codigo <= todas.length ? todas[codigo - 1] : null;
    }

    public static int codigoDe(TipoArma arma) {
        return arma == null ? 0 : arma.ordinal() + 1;
    }
}
