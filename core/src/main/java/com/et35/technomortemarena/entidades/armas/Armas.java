package com.et35.technomortemarena.entidades.armas;

public final class Armas {
    public static final Espada ESPADA = new Espada();
    public static final Pistola PISTOLA = new Pistola();
    public static final Escopeta ESCOPETA = new Escopeta();
    public static final RifleLaser RIFLE_LASER = new RifleLaser();
    public static final Lanzacohetes LANZACOHETES = new Lanzacohetes();
    public static final EspadaLaser ESPADA_LASER = new EspadaLaser();

    private static final Arma[] TODAS = {ESPADA, PISTOLA, ESCOPETA, RIFLE_LASER, LANZACOHETES, ESPADA_LASER};
    private static final ArmaDeFuego[] QUE_APARECEN = {PISTOLA, ESCOPETA, RIFLE_LASER, LANZACOHETES};

    private Armas() {
    }

    public static ArmaDeFuego[] queAparecen() {
        return QUE_APARECEN.clone();
    }

    public static Arma porCodigo(int codigo) {
        return codigo >= 1 && codigo <= TODAS.length ? TODAS[codigo - 1] : null;
    }

    public static int codigoDe(Arma arma) {
        for (int i = 0; i < TODAS.length; i++) {
            if (TODAS[i] == arma) {
                return i + 1;
            }
        }
        return 0;
    }
}
