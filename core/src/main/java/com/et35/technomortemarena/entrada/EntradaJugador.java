package com.et35.technomortemarena.entrada;

public final class EntradaJugador {
    public boolean izquierda;
    public boolean derecha;

    public boolean saltar;
    public boolean estocada;
    public boolean rodar;
    public boolean lanzar;

    public boolean hayPuntero;
    public float apuntaX;
    public float apuntaY;

    public void limpiar() {
        izquierda = false;
        derecha = false;
        saltar = false;
        estocada = false;
        rodar = false;
        lanzar = false;
        hayPuntero = false;
        apuntaX = 0f;
        apuntaY = 0f;
    }
}
