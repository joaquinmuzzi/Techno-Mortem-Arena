package com.et35.technomortemarena.entidades;

import java.util.ArrayList;
import java.util.List;

public final class EstadoArmas {
    public static final int MAX_PROYECTILES = 24;
    public static final int MAX_EN_EL_SUELO = 6;

    public final List<float[]> proyectiles = new ArrayList<>();
    public final List<float[]> enElSuelo = new ArrayList<>();
    public static final int MAX_POWER_UPS = 4;
    public final List<float[]> powerUps = new ArrayList<>();
    public final List<int[]> celdasRotas = new ArrayList<>();
    public int contadorEspadasAlCaer;
    public int contadorArmasDeFuegoAlCaer;
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
