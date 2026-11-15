package com.et35.technomortemarena.entidades.armas;

import com.badlogic.gdx.graphics.Texture;
import com.et35.technomortemarena.RecursosGraficos;
import com.et35.technomortemarena.entidades.Hoja;

public class Espada extends Arma {
    private static final int DANIO_HOJA = 6;
    private static final float VELOCIDAD_REFLEJO = 0.8f;
    private static final float DESVIO_REFLEJO = 0.12f;

    public Espada() {
        this("Espada", 6);
    }

    protected Espada(String nombre, int danioLanzada) {
        super(nombre, danioLanzada);
    }

    @Override
    public boolean esEspada() {
        return true;
    }

    public int getDanioHoja() {
        return DANIO_HOJA;
    }

    public float getVelocidadReflejo() {
        return VELOCIDAD_REFLEJO;
    }

    public float getDesvioReflejo() {
        return DESVIO_REFLEJO;
    }

    protected boolean esDeLuz() {
        return false;
    }

    public boolean chocaCon(Espada otra) {
        return esDeLuz() == otra.esDeLuz();
    }

    @Override
    public boolean quedaEnElMapa(int balasQueLeQuedan) {
        return true;
    }

    @Override
    public Texture getTextura(RecursosGraficos recursos) {
        return recursos.espada;
    }

    @Override
    public float getAnchoEnLaMano() {
        return Hoja.LARGO;
    }

    @Override
    public float getAltoEnLaMano() {
        return Hoja.GROSOR;
    }

    @Override
    public float getAnchoDibujado() {
        return Hoja.LARGO;
    }

    @Override
    public float getAltoDibujado() {
        return Hoja.GROSOR;
    }

    @Override
    public float getAnchoEnElSuelo() {
        return Hoja.LARGO;
    }
}
