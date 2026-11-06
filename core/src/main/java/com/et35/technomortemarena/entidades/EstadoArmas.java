package com.et35.technomortemarena.entidades;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que hay que dibujar de las armas fuera de las manos: los proyectiles en vuelo y las armas tiradas en
 * el mapa. Es lo que el anfitrion le manda al cliente, como {@link EstadoJugador}: datos planos, sin red.
 */
public final class EstadoArmas {

    /** Tope de proyectiles que viajan por la red: los que sobran no se dibujan en el cliente. */
    public static final int MAX_PROYECTILES = 24;
    /** Tope de armas tiradas en el mapa a la vez. */
    public static final int MAX_EN_EL_SUELO = 6;

    /** Cada uno como {@code {tipo (ordinal de Proyectil.Tipo), x, y, angulo}}. */
    public final List<float[]> proyectiles = new ArrayList<>();
    /** Cada una como {@code {codigo de TipoArma, x, y}}. */
    public final List<float[]> enElSuelo = new ArrayList<>();
    /** Tope de mejoras en el mapa que viajan por la red. */
    public static final int MAX_POWER_UPS = 4;
    /** Cada una como {@code {indice de PowerUp, x, y}}. */
    public final List<float[]> powerUps = new ArrayList<>();
    /** Las celdas que rompieron los cohetes en la ronda, como {@code {columna, fila}} contando desde abajo. */
    public final List<int[]> celdasRotas = new ArrayList<>();
    /** Veces que una espada tirada toco el piso, para que el cliente dispare el sonido. */
    public int contadorEspadasAlCaer;
    /** Veces que un arma de fuego tirada toco el piso, para que el cliente dispare el sonido. */
    public int contadorArmasDeFuegoAlCaer;
    /** Cohetes que explotaron, para que el cliente dispare el sonido. */
    public int contadorExplosiones;

    public void copiarDe(EstadoArmas origen) {
        proyectiles.clear();
        for (float[] p : origen.proyectiles) {
            proyectiles.add(p.clone());
        }
        enElSuelo.clear();
        for (float[] a : origen.enElSuelo) {
            enElSuelo.add(a.clone());
        }
        powerUps.clear();
        for (float[] m : origen.powerUps) {
            powerUps.add(m.clone());
        }
        celdasRotas.clear();
        for (int[] c : origen.celdasRotas) {
            celdasRotas.add(c.clone());
        }
        contadorEspadasAlCaer = origen.contadorEspadasAlCaer;
        contadorArmasDeFuegoAlCaer = origen.contadorArmasDeFuegoAlCaer;
        contadorExplosiones = origen.contadorExplosiones;
    }
}
