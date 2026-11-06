package com.et35.technomortemarena.red;

import com.et35.technomortemarena.entrada.EntradaJugador;
import com.et35.technomortemarena.entrada.FuenteEntrada;

public final class EntradaRemota implements FuenteEntrada {
    private boolean izquierda;
    private boolean derecha;
    private boolean hayPuntero;
    private float apuntaX;
    private float apuntaY;

    private int ultimaSecuencia;
    private boolean huboPaquete;
    private byte ultimoSalto;
    private byte ultimaEstocada;
    private byte ultimoRodar;
    private byte ultimoLanzar;
    private boolean lanzarPendiente;
    private byte ultimoColocar;
    private int eleccionPieza = -1;
    private boolean colocarPendiente;

    private boolean saltoPendiente;
    private boolean estocadaPendiente;
    private boolean rodarPendiente;

    boolean aplicar(int secuencia, boolean izquierda, boolean derecha, boolean hayPuntero,
                    byte contadorSalto, byte contadorEstocada, byte contadorRodar, byte contadorLanzar, int eleccionPieza, byte contadorColocar, float apuntaX, float apuntaY) {
        if (huboPaquete && !Protocolo.esMasNueva(secuencia, ultimaSecuencia)) {
            return false;
        }
        huboPaquete = true;
        ultimaSecuencia = secuencia;
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.hayPuntero = hayPuntero;
        this.apuntaX = apuntaX;
        this.apuntaY = apuntaY;
        saltoPendiente |= avanzo(contadorSalto, ultimoSalto);
        estocadaPendiente |= avanzo(contadorEstocada, ultimaEstocada);
        this.eleccionPieza = eleccionPieza;
        colocarPendiente |= avanzo(contadorColocar, ultimoColocar);
        ultimoColocar = contadorColocar;
        lanzarPendiente |= avanzo(contadorLanzar, ultimoLanzar);
        ultimoLanzar = contadorLanzar;
        rodarPendiente |= avanzo(contadorRodar, ultimoRodar);
        ultimoRodar = contadorRodar;
        ultimoSalto = contadorSalto;
        ultimaEstocada = contadorEstocada;
        return true;
    }

    private static boolean avanzo(byte nuevo, byte viejo) {
        int diferencia = (nuevo - viejo) & 0xFF;
        return diferencia >= 1 && diferencia <= 127;
    }

    @Override
    public void leer(EntradaJugador destino) {
        destino.limpiar();
        destino.izquierda = izquierda;
        destino.derecha = derecha;
        destino.hayPuntero = hayPuntero;
        destino.apuntaX = apuntaX;
        destino.apuntaY = apuntaY;
        destino.saltar = saltoPendiente;
        destino.estocada = estocadaPendiente;
        destino.rodar = rodarPendiente;
        destino.lanzar = lanzarPendiente;
        destino.eleccionPieza = eleccionPieza;
        destino.colocarPieza = colocarPendiente;
        saltoPendiente = false;
        estocadaPendiente = false;
        rodarPendiente = false;
        lanzarPendiente = false;
        colocarPendiente = false;
    }
}
