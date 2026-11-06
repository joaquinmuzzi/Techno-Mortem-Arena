package com.et35.technomortemarena;

/**
 * Si suenan la musica y los efectos. Por defecto suena todo. Se guarda en un {@link Almacen} para que se
 * mantenga entre una partida y la siguiente; en el juego, las preferencias de libGDX.
 */
public final class OpcionesAudio {

    /** Donde se guardan las opciones. Es una interfaz para poder probarlas sin abrir el juego. */
    public interface Almacen {
        boolean leer(String clave, boolean porDefecto);

        void guardar(String clave, boolean valor);
    }

    static final String CLAVE_MUSICA = "musica";
    static final String CLAVE_EFECTOS = "efectos";

    private final Almacen almacen;
    private boolean musica;
    private boolean efectos;

    public OpcionesAudio(Almacen almacen) {
        this.almacen = almacen;
        musica = almacen.leer(CLAVE_MUSICA, true);
        efectos = almacen.leer(CLAVE_EFECTOS, true);
    }

    public boolean isMusica() {
        return musica;
    }

    public boolean isEfectos() {
        return efectos;
    }

    public void setMusica(boolean musica) {
        this.musica = musica;
        almacen.guardar(CLAVE_MUSICA, musica);
    }

    public void setEfectos(boolean efectos) {
        this.efectos = efectos;
        almacen.guardar(CLAVE_EFECTOS, efectos);
    }
}
