package com.et35.technomortemarena.entrada;

public interface FuenteEntrada {
    FuenteEntrada NINGUNA = destino -> destino.limpiar();

    void leer(EntradaJugador destino);
}
